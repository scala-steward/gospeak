package fr.gospeak.web.auth

import java.time.Instant

import cats.data.OptionT
import cats.effect.IO
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.exceptions.{IdentityNotFoundException, InvalidPasswordException}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import fr.gospeak.core.domain.UserRequest
import fr.gospeak.core.domain.UserRequest.EmailValidationRequest
import fr.gospeak.core.services.GospeakDb
import fr.gospeak.infra.services.EmailSrv
import fr.gospeak.web.HomeCtrl
import fr.gospeak.web.auth.AuthCtrl._
import fr.gospeak.web.auth.domain.{AuthUser, CookieEnv}
import fr.gospeak.web.auth.exceptions.{DuplicateIdentityException, DuplicateSlugException}
import fr.gospeak.web.auth.services.{AuthRepo, AuthSrv}
import fr.gospeak.web.domain.{AuthCookieConf, HeaderInfo}
import fr.gospeak.web.utils.UICtrl
import play.api.i18n.Messages
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

// TODO Add rememberMe feature
class AuthCtrl(cc: ControllerComponents,
               silhouette: Silhouette[CookieEnv],
               authCookieConf: AuthCookieConf,
               credentialsProvider: CredentialsProvider,
               passwordHasherRegistry: PasswordHasherRegistry,
               authRepo: AuthRepo,
               authSrv: AuthSrv,
               clock: Clock,
               db: GospeakDb,
               emailSrv: EmailSrv) extends UICtrl(cc) {
  private val loginRedirect = Redirect(fr.gospeak.web.user.routes.UserCtrl.index())
  private val logoutRedirect = Redirect(fr.gospeak.web.routes.HomeCtrl.index())

  def signup(): Action[AnyContent] = Action { implicit req: Request[AnyContent] =>
    authRepo.userAware() match {
      case Some(_) => loginRedirect
      case None => Ok(html.signup(AuthForms.signup)(header))
    }
  }

  def doSignup(): Action[AnyContent] = Action.async { implicit req: Request[AnyContent] =>
    println("doSignup")
    val now = Instant.now()
    AuthForms.signup.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(html.signup(formWithErrors)(header))),
      data => {
        val loginInfo = new LoginInfo(CredentialsProvider.ID, data.email.value)
        (for {
          user <- authSrv.createIdentity(loginInfo, data, now).unsafeToFuture()
          emailValidation <- db.createEmailValidationRequest(user.user.email, user.user.id, now).unsafeToFuture()
          _ <- sendSignupEmail(user, emailValidation).unsafeToFuture()
          result <- authSrv.login(user, loginRedirect)
          _ <- authRepo.login(user.user).unsafeToFuture() // TODO remove
        } yield result).recover {
          case _: DuplicateIdentityException => Ok(html.signup(AuthForms.signup.fill(data))(header)(req, implicitly[Messages], Flash(Map("error" -> s"User already exists"))))
          case e: DuplicateSlugException => Ok(html.signup(AuthForms.signup.fill(data))(header)(req, implicitly[Messages], Flash(Map("error" -> s"Username ${e.slug.value} is already taken"))))
          case NonFatal(e) => Ok(html.signup(AuthForms.signup.fill(data))(header)(req, implicitly[Messages], Flash(Map("error" -> s"${e.getClass.getSimpleName}: ${e.getMessage}"))))
        }
      }
    )
  }

  private def sendSignupEmail(user: AuthUser, emailValidation: EmailValidationRequest)(implicit req: Request[AnyContent]): IO[Unit] = {
    import EmailSrv._
    val email = Email(
      from = Contact("noreply@gospeak.fr", None),
      to = Seq(Contact(user.user.email.value, Some(user.user.name.value))),
      subject = "Welcome to gospeak!",
      content = HtmlContent(emails.html.signup(user, emailValidation).body)
    )
    emailSrv.send(email)
  }

  def login(): Action[AnyContent] = Action { implicit req: Request[AnyContent] =>
    authRepo.userAware() match {
      case Some(_) => loginRedirect
      case None => Ok(html.login(AuthForms.login)(header))
    }
  }

  def doLogin(): Action[AnyContent] = Action.async { implicit req: Request[AnyContent] =>
    println("doLogin")
    AuthForms.login.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(html.login(formWithErrors)(header))),
      data => (for {
        user <- authSrv.getIdentity(data)
        result <- authSrv.login(user, loginRedirect)
        _ <- authRepo.login(user.user).unsafeToFuture() // TODO remove
      } yield result).recover {
        case _: IdentityNotFoundException => Ok(html.login(AuthForms.login.fill(data))(header)(req, implicitly[Messages], Flash(Map("error" -> "Wrong login or password"))))
        case _: InvalidPasswordException => Ok(html.login(AuthForms.login.fill(data))(header)(req, implicitly[Messages], Flash(Map("error" -> "Wrong login or password"))))
        case NonFatal(e) => Ok(html.login(AuthForms.login.fill(data))(header)(req, implicitly[Messages], Flash(Map("error" -> s"${e.getClass.getSimpleName}: ${e.getMessage}"))))
      }
    )
  }

  def doLogout(): Action[AnyContent] = Action.async { implicit req =>
    authRepo.logout().unsafeToFuture()
      .map { _ => logoutRedirect }
  }

  /* def doLogout(): Action[AnyContent] = silhouette.SecuredAction.async { implicit req =>
    authSrv.logout().unsafeToFuture().flatMap { _ =>
      silhouette.env.eventBus.publish(LogoutEvent(req.identity, req))
      silhouette.env.authenticatorService.discard(req.authenticator, logoutRedirect)
    }
  } */

  // TODO add a message in every logged page to validate email if not already done
  // TODO create a containerSecured & containerUserAware
  def doValidateEmail(id: UserRequest.Id): Action[AnyContent] = Action.async { implicit req =>
    val now = Instant.now()
    (for {
      validation <- OptionT(db.getPendingEmailValidationRequest(id, now))
      _ <- OptionT.liftF(db.validateEmail(id, validation.user, now))
    } yield Redirect(routes.AuthCtrl.login())).value.map(_.getOrElse(notFound())).unsafeToFuture()
  }

  def passwordReset(): Action[AnyContent] = Action { implicit req: Request[AnyContent] =>
    authRepo.userAware() match {
      case Some(_) => loginRedirect
      case None => Ok(html.passwordReset(AuthForms.passwordReset)(header))
    }
  }

  def doPasswordReset(): Action[AnyContent] = Action { implicit req: Request[AnyContent] =>
    Redirect(routes.AuthCtrl.login())
  }
}

object AuthCtrl {
  val header: HeaderInfo =
    HomeCtrl.header.activeFor(routes.AuthCtrl.login())
}
