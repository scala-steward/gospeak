package gospeak.web.pages.orga.settings

import cats.data.OptionT
import cats.effect.IO
import com.mohiva.play.silhouette.api.Silhouette
import gospeak.core.domain.messages.Message
import gospeak.core.domain.utils.OrgaCtx
import gospeak.core.domain.{Group, User, UserRequest}
import gospeak.core.services.email.EmailSrv
import gospeak.core.services.meetup.MeetupSrv
import gospeak.core.services.meetup.domain.{MeetupCredentials, MeetupException, MeetupGroup}
import gospeak.core.services.slack.SlackSrv
import gospeak.core.services.slack.domain.SlackCredentials
import gospeak.core.services.storage.{OrgaGroupRepo, OrgaGroupSettingsRepo, OrgaUserRepo, OrgaUserRequestRepo}
import gospeak.libs.scala.Extensions._
import gospeak.libs.scala.domain.{CustomException, Liquid}
import gospeak.web.AppConf
import gospeak.web.auth.domain.CookieEnv
import gospeak.web.domain.Breadcrumb
import gospeak.web.emails.Emails
import gospeak.web.pages.orga.GroupCtrl
import gospeak.web.pages.orga.settings.SettingsCtrl._
import gospeak.web.pages.user.routes.{UserCtrl => UserRoutes}
import gospeak.web.utils._
import play.api.data.Form
import play.api.mvc._
import play.twirl.api.HtmlFormat

import scala.util.control.NonFatal

class SettingsCtrl(cc: ControllerComponents,
                   silhouette: Silhouette[CookieEnv],
                   conf: AppConf,
                   val groupRepo: OrgaGroupRepo,
                   groupSettingsRepo: OrgaGroupSettingsRepo,
                   userRepo: OrgaUserRepo,
                   userRequestRepo: OrgaUserRequestRepo,
                   emailSrv: EmailSrv,
                   meetupSrvEth: Either[String, MeetupSrv],
                   slackSrv: SlackSrv) extends UICtrl(cc, silhouette, conf) with UICtrl.OrgaAction {
  def settings(group: Group.Slug): Action[AnyContent] = OrgaAction(group) { implicit req =>
    groupSettingsRepo.find.flatMap(settingsView(_))
  }

  def createAction(group: Group.Slug): Action[AnyContent] = OrgaAction(group) { implicit req =>
    IO.pure(Ok(createActionView(GsForms.groupAction)))
  }

  def doCreateAction(group: Group.Slug): Action[AnyContent] = OrgaAction(group) { implicit req =>
    for {
      settings <- groupSettingsRepo.find
      res <- GsForms.groupAction.bindFromRequest.fold(
        formWithErrors => IO.pure(BadRequest(createActionView(formWithErrors))),
        data => groupSettingsRepo.set(settings.addAction(data.trigger, data.action))
          .map(_ => Redirect(routes.SettingsCtrl.settings(group)).flashing("success" -> "Action created"))
      )
    } yield res
  }

  private def createActionView(actionForm: Form[GsForms.GroupAction])(implicit req: OrgaReq[AnyContent]): HtmlFormat.Appendable = {
    val b = breadcrumb("Create action" -> routes.SettingsCtrl.createAction(req.group.slug))
    html.actionCreate(actionForm)(b)
  }

  def updateAction(group: Group.Slug, trigger: Group.Settings.Action.Trigger, index: Int): Action[AnyContent] = OrgaAction(group) { implicit req =>
    (for {
      settings <- OptionT.liftF(groupSettingsRepo.find)
      action <- OptionT(IO.pure(settings.actions.get(trigger).flatMap(_.lift(index))))
      res = Ok(updateActionView(trigger, index, GsForms.groupAction.fill(GsForms.GroupAction(trigger, action))))
    } yield res).value.map(_.getOrElse(groupNotFound(group)))
  }

  def doUpdateAction(group: Group.Slug, trigger: Group.Settings.Action.Trigger, index: Int): Action[AnyContent] = OrgaAction(group) { implicit req =>
    for {
      settings <- groupSettingsRepo.find
      res <- GsForms.groupAction.bindFromRequest.fold(
        formWithErrors => IO.pure(BadRequest(updateActionView(trigger, index, formWithErrors))),
        data => groupSettingsRepo.set(settings.updateAction(trigger, index)(data.trigger, data.action))
          .map(_ => Redirect(routes.SettingsCtrl.settings(group)).flashing("success" -> "Action updated"))
      )
    } yield res
  }

  private def updateActionView(trigger: Group.Settings.Action.Trigger, index: Int, actionForm: Form[GsForms.GroupAction])(implicit req: OrgaReq[AnyContent]): HtmlFormat.Appendable = {
    val b = breadcrumb("Update action" -> routes.SettingsCtrl.createAction(req.group.slug))
    html.actionUpdate(trigger, index, actionForm)(b)
  }

  def doRemoveAction(group: Group.Slug, trigger: Group.Settings.Action.Trigger, index: Int): Action[AnyContent] = OrgaAction(group) { implicit req =>
    for {
      settings <- groupSettingsRepo.find
      _ <- groupSettingsRepo.set(settings.removeAction(trigger, index))
    } yield Redirect(routes.SettingsCtrl.settings(group)).flashing("success" -> "Action removed")
  }

  def meetupAuthorize(group: Group.Slug): Action[AnyContent] = OrgaAction(group) { implicit req =>
    GsForms.groupAccountMeetup.bindFromRequest.fold(
      formWithErrors => groupSettingsRepo.find.flatMap(settingsView(_, meetup = Some(formWithErrors))),
      data => meetupSrvEth.fold(err => IO.pure(Redirect(routes.SettingsCtrl.settings(group)).flashing("error" -> err)), meetupSrv => {
        val redirectUri = routes.SettingsCtrl.meetupCallback(group, data.group).absoluteURL(meetupSrv.hasSecureCallback)
        meetupSrv.buildAuthorizationUrl(redirectUri).map(url => Redirect(url.value)).toIO
      })
    )
  }

  def meetupCallback(group: Group.Slug, meetupGroup: MeetupGroup.Slug): Action[AnyContent] = OrgaAction(group) { implicit req =>
    req.getQueryString("code").map { code =>
      (for {
        settings <- groupSettingsRepo.find
        meetupSrv <- meetupSrvEth.toIO(CustomException(_))
        redirectUri = routes.SettingsCtrl.meetupCallback(group, meetupGroup).absoluteURL(meetupSrv.hasSecureCallback)
        token <- meetupSrv.requestAccessToken(redirectUri, code, conf.app.aesKey)
        loggedUser <- meetupSrv.getLoggedUser(conf.app.aesKey)(token)
        meetupGroupElt <- meetupSrv.getGroup(meetupGroup, conf.app.aesKey)(token)
        creds = MeetupCredentials(token, loggedUser, meetupGroupElt)
        _ <- groupSettingsRepo.set(settings.set(creds))
        next = Redirect(routes.SettingsCtrl.settings(group)).flashing("success" -> s"Connected to <b>${loggedUser.name}</b> meetup account")
      } yield next).recoverWith {
        case e: MeetupException => IO.pure(Redirect(routes.SettingsCtrl.settings(group)).flashing("error" -> e.getMessage))
      }
    }.getOrElse {
      val state = req.getQueryString("state")
      val error = req.getQueryString("error")
      val msg = s"Failed to authenticate with meetup${error.map(e => s", reason: $e").getOrElse("")}${state.map(s => s" (state: $s)").getOrElse("")}"
      IO.pure(Redirect(routes.SettingsCtrl.settings(group)).flashing("error" -> msg))
    }
  }

  def updateSlackAccount(group: Group.Slug): Action[AnyContent] = OrgaAction(group) { implicit req =>
    for {
      settings <- groupSettingsRepo.find
      res <- GsForms.groupAccountSlack(conf.app.aesKey).bindFromRequest.fold(
        formWithErrors => settingsView(settings, slack = Some(formWithErrors)),
        creds => slackSrv.getInfos(creds.token, conf.app.aesKey)
          .flatMap(_ => groupSettingsRepo.set(settings.set(creds)))
          .map(_ => Redirect(routes.SettingsCtrl.settings(group)).flashing("success" -> "Slack account updated"))
          .recover { case NonFatal(e) => Redirect(routes.SettingsCtrl.settings(group)).flashing("error" -> s"Invalid Slack token: ${e.getMessage}") }
      )
    } yield res
  }

  def removeAccount(group: Group.Slug, kind: String): Action[AnyContent] = OrgaAction(group) { implicit req =>
    for {
      settings <- groupSettingsRepo.find
      updated <- settings.removeAccount(kind).toIO
      _ <- groupSettingsRepo.set(updated)
    } yield Redirect(routes.SettingsCtrl.settings(group)).flashing("success" -> s"${kind.capitalize} account removed")
  }

  def updateEventTemplate(group: Group.Slug, templateName: Option[String]): Action[AnyContent] = OrgaAction(group) { implicit req =>
    (for {
      settings <- OptionT.liftF(groupSettingsRepo.find)
      template <- templateName.map(name => OptionT.fromOption[IO](settings.event.getTemplate(name)).map(t => Some(GsForms.GroupEventTemplateItem(name, t))))
        .getOrElse(OptionT.pure[IO](None))
      form = template.map(GsForms.groupEventTemplateItem.fill).getOrElse(GsForms.groupEventTemplateItem)
    } yield updateEventTemplateView(templateName, settings, form))
      .value.map(_.getOrElse(groupNotFound(group)))
  }

  def doUpdateEventTemplate(group: Group.Slug, templateName: Option[String]): Action[AnyContent] = OrgaAction(group) { implicit req =>
    for {
      settings <- groupSettingsRepo.find
      res <- GsForms.groupEventTemplateItem.bindFromRequest.fold(
        formWithErrors => IO.pure(updateEventTemplateView(templateName, settings, formWithErrors)),
        data => templateName.map(name => settings.updateEventTemplate(name, data.id, data.template)).getOrElse(settings.addEventTemplate(data.id, data.template)).fold(
          e => IO.pure(updateEventTemplateView(templateName, settings, GsForms.groupEventTemplateItem.bindFromRequest.withGlobalError(e.getMessage))),
          updated => groupSettingsRepo.set(updated)
            .map(_ => Redirect(routes.SettingsCtrl.settings(group)).flashing("success" -> s"Template '${data.id}' updated for events"))
        )
      )
    } yield res
  }

  def doRemoveEventTemplate(group: Group.Slug, templateName: String): Action[AnyContent] = OrgaAction(group) { implicit req =>
    for {
      settings <- groupSettingsRepo.find
      updated <- settings.removeEventTemplate(templateName).toIO
      _ <- groupSettingsRepo.set(updated)
    } yield Redirect(routes.SettingsCtrl.settings(group)).flashing("success" -> s"Template '$templateName' removed for events")
  }

  def updateProposalTweet(group: Group.Slug): Action[AnyContent] = OrgaAction(group) { implicit req =>
    updateEventTemplateView(group, GsForms.templateForm)
  }

  def doUpdateProposalTweet(group: Group.Slug): Action[AnyContent] = OrgaAction(group) { implicit req =>
    GsForms.templateForm.bindFromRequest.fold(
      formWithErrors => updateEventTemplateView(group, formWithErrors),
      data => for {
        settings <- groupSettingsRepo.find
        updated = settings.copy(proposal = settings.proposal.copy(tweet = data.as[Message.ProposalInfo]))
        _ <- groupSettingsRepo.set(updated)
      } yield Redirect(routes.SettingsCtrl.settings(group)).flashing("success" -> s"Proposal tweet template updated")
    )
  }

  def inviteOrga(group: Group.Slug): Action[AnyContent] = OrgaAction(group) { implicit req =>
    val next = Redirect(routes.SettingsCtrl.settings(group))
    GsForms.invite.bindFromRequest.fold(
      formWithErrors => IO.pure(next.flashing(formWithErrors.flash)),
      data => for {
        invite <- userRequestRepo.invite(data.email)
        _ <- emailSrv.send(Emails.inviteOrgaToGroup(invite, data.message))
      } yield next.flashing("success" -> s"<b>${invite.email.value}</b> is invited as orga")
    )
  }

  def cancelInviteOrga(group: Group.Slug, request: UserRequest.Id): Action[AnyContent] = OrgaAction(group) { implicit req =>

    for {
      invite <- userRequestRepo.cancelGroupInvite(request)
      _ <- emailSrv.send(Emails.inviteOrgaToGroupCanceled(invite))
      next = redirectToPreviousPageOr(routes.SettingsCtrl.settings(group))
    } yield next.flashing("success" -> s"Invitation to <b>${invite.email.value}</b> has been canceled")
  }

  def doRemoveOrga(group: Group.Slug, orga: User.Slug): Action[AnyContent] = OrgaAction(group) { implicit req =>
    (for {
      orgaElt <- OptionT(userRepo.find(orga))
      _ <- OptionT.liftF(groupRepo.removeOwner(orgaElt.id))
      _ <- OptionT.liftF(emailSrv.send(Emails.orgaRemovedFromGroup(orgaElt)))
      next = if (req.user.slug == orga) Redirect(UserRoutes.index()).flashing("success" -> s"You removed yourself from <b>${req.group.name.value}</b> group")
      else Redirect(routes.SettingsCtrl.settings(group)).flashing("success" -> s"You removed <b>${orgaElt.name.value}</b> from  <b>${req.group.name.value}</b> group")
    } yield next).value.map(_.getOrElse(groupNotFound(group)))
      .recover { case NonFatal(e) => Redirect(routes.SettingsCtrl.settings(group)).flashing("error" -> s"Error: ${e.getMessage}") }
  }

  private def settingsView(settings: Group.Settings,
                           meetup: Option[Form[GsForms.GroupAccount.Meetup]] = None,
                           slack: Option[Form[SlackCredentials]] = None)
                          (implicit req: OrgaReq[AnyContent], ctx: OrgaCtx): IO[Result] = {
    for {
      orgas <- userRepo.list(req.group.owners.toList)
      invites <- userRequestRepo.listPendingInvites
    } yield Ok(html.settings(
      settings,
      orgas,
      invites,
      meetup.getOrElse(settings.accounts.meetup.map(s => GsForms.groupAccountMeetup.fill(GsForms.GroupAccount.Meetup(s.group))).getOrElse(GsForms.groupAccountMeetup)),
      slack.getOrElse(settings.accounts.slack.map(s => GsForms.groupAccountSlack(conf.app.aesKey).fill(s)).getOrElse(GsForms.groupAccountSlack(conf.app.aesKey)))
    )(listBreadcrumb))
  }

  private def updateEventTemplateView(templateName: Option[String],
                                      settings: Group.Settings,
                                      form: Form[GsForms.GroupEventTemplateItem])
                                     (implicit req: OrgaReq[AnyContent]): Result = {
    val b = listBreadcrumb.add(
      "Event" -> routes.SettingsCtrl.settings(req.group.slug),
      "Templates" -> routes.SettingsCtrl.settings(req.group.slug),
      templateName.getOrElse("New") -> routes.SettingsCtrl.updateEventTemplate(req.group.slug, templateName))
    Ok(html.updateEventTemplate(templateName, settings, form)(b))
  }

  private def updateEventTemplateView(group: Group.Slug, form: Form[Liquid[Nothing]])(implicit req: OrgaReq[AnyContent]): IO[Result] = {
    for {
      tweet <- groupSettingsRepo.findProposalTweet(req.group.id)
      filledForm = if (form.hasErrors) form else form.fill(tweet.as[Nothing])
      b = listBreadcrumb.add(
        "Proposal" -> routes.SettingsCtrl.settings(req.group.slug),
        "Tweet" -> routes.SettingsCtrl.updateProposalTweet(req.group.slug))
      res = Ok(html.updateTemplate(
        "Proposal tweet",
        filledForm,
        Message.Ref.proposalInfo,
        routes.SettingsCtrl.doUpdateProposalTweet(group),
        markdown = false)(b))
    } yield res
  }
}

object SettingsCtrl {
  def listBreadcrumb(implicit req: OrgaReq[AnyContent]): Breadcrumb =
    GroupCtrl.breadcrumb.add("Settings" -> routes.SettingsCtrl.settings(req.group.slug))

  def breadcrumb(setting: (String, Call))(implicit req: OrgaReq[AnyContent]): Breadcrumb =
    listBreadcrumb.add(setting._1 -> setting._2)
}
