package gospeak.core.services.storage

import java.time.Instant

import cats.data.NonEmptyList
import cats.effect.IO
import gospeak.core.domain._
import gospeak.core.domain.utils.{OrgaCtx, UserAwareCtx, UserCtx}
import gospeak.libs.scala.domain._

trait ProposalRepo extends OrgaProposalRepo with SpeakerProposalRepo with UserProposalRepo with AuthProposalRepo with SuggestProposalRepo with PublicProposalRepo

trait OrgaProposalRepo {
  val fields: ProposalFields.type = ProposalFields

  def edit(cfp: Cfp.Slug, proposal: Proposal.Id, data: Proposal.DataOrga)(implicit ctx: OrgaCtx): IO[Unit]

  def accept(cfp: Cfp.Slug, id: Proposal.Id, event: Event.Id)(implicit ctx: OrgaCtx): IO[Unit]

  def cancel(cfp: Cfp.Slug, id: Proposal.Id, event: Event.Id)(implicit ctx: OrgaCtx): IO[Unit]

  def reject(cfp: Cfp.Slug, id: Proposal.Id, by: User.Id, now: Instant): IO[Unit]

  def reject(cfp: Cfp.Slug, id: Proposal.Id)(implicit ctx: OrgaCtx): IO[Unit]

  def cancelReject(cfp: Cfp.Slug, id: Proposal.Id)(implicit ctx: OrgaCtx): IO[Unit]

  def rate(cfp: Cfp.Slug, id: Proposal.Id, grade: Proposal.Rating.Grade)(implicit ctx: OrgaCtx): IO[Unit]

  def editSlides(cfp: Cfp.Slug, id: Proposal.Id, slides: Url.Slides)(implicit ctx: OrgaCtx): IO[Unit]

  def editVideo(cfp: Cfp.Slug, id: Proposal.Id, video: Url.Video)(implicit ctx: OrgaCtx): IO[Unit]

  def editOrgaTags(cfp: Cfp.Slug, id: Proposal.Id, orgaTags: List[Tag])(implicit ctx: OrgaCtx): IO[Unit]

  def removeSpeaker(cfp: Cfp.Slug, id: Proposal.Id, speaker: User.Id)(implicit ctx: OrgaCtx): IO[Unit]

  def listFull(params: Page.Params)(implicit ctx: OrgaCtx): IO[Page[Proposal.Full]]

  def listFull(cfp: Cfp.Slug, params: Page.Params)(implicit ctx: OrgaCtx): IO[Page[Proposal.Full]]

  def listFull(cfp: Cfp.Slug, status: Proposal.Status, params: Page.Params)(implicit ctx: OrgaCtx): IO[Page[Proposal.Full]]

  def listFull(speaker: User.Id, params: Page.Params)(implicit ctx: OrgaCtx): IO[Page[Proposal.Full]]

  def list(ids: List[Proposal.Id]): IO[List[Proposal]]

  def listFull(ids: List[Proposal.Id])(implicit ctx: UserAwareCtx): IO[List[Proposal.Full]]

  def find(cfp: Cfp.Slug, id: Proposal.Id): IO[Option[Proposal]]

  def findFull(cfp: Cfp.Slug, id: Proposal.Id)(implicit ctx: OrgaCtx): IO[Option[Proposal.Full]]

  def listRatings(id: Proposal.Id): IO[List[Proposal.Rating.Full]]

  def listRatings(cfp: Cfp.Slug)(implicit ctx: OrgaCtx): IO[List[Proposal.Rating]]

  def listRatings(proposals: List[Proposal.Id])(implicit ctx: OrgaCtx): IO[List[Proposal.Rating]]
}

trait SpeakerProposalRepo {
  def create(talk: Talk.Id, cfp: Cfp.Id, data: Proposal.Data, speakers: NonEmptyList[User.Id])(implicit ctx: UserCtx): IO[Proposal]

  def edit(talk: Talk.Slug, cfp: Cfp.Slug, data: Proposal.Data)(implicit ctx: UserCtx): IO[Unit]

  def editSlides(talk: Talk.Slug, cfp: Cfp.Slug, slides: Url.Slides)(implicit ctx: UserCtx): IO[Unit]

  def editVideo(talk: Talk.Slug, cfp: Cfp.Slug, video: Url.Video)(implicit ctx: UserCtx): IO[Unit]

  def removeSpeaker(talk: Talk.Slug, cfp: Cfp.Slug, speaker: User.Id)(implicit ctx: UserCtx): IO[Unit]

  def find(proposal: Proposal.Id): IO[Option[Proposal]]

  def findFull(proposal: Proposal.Id)(implicit ctx: UserCtx): IO[Option[Proposal.Full]]

  def findFull(talk: Talk.Slug, cfp: Cfp.Slug)(implicit ctx: UserCtx): IO[Option[Proposal.Full]]

  def listFull(params: Page.Params)(implicit ctx: UserCtx): IO[Page[Proposal.Full]]

  def listFull(talk: Talk.Id, params: Page.Params)(implicit ctx: UserCtx): IO[Page[Proposal.Full]]

  def find(talk: Talk.Slug, cfp: Cfp.Slug)(implicit ctx: UserCtx): IO[Option[Proposal]]
}

trait UserProposalRepo {
  def addSpeaker(proposal: Proposal.Id)(speaker: User.Id, by: User.Id, now: Instant): IO[Unit]

  def listFull(params: Page.Params)(implicit ctx: UserCtx): IO[Page[Proposal.Full]]
}

trait AuthProposalRepo

trait PublicProposalRepo {
  def listAllPublicIds()(implicit ctx: UserAwareCtx): IO[List[(Group.Id, Proposal.Id)]]

  def listAllPublicFull(speaker: User.Id)(implicit ctx: UserAwareCtx): IO[List[Proposal.Full]]

  def listPublicFull(group: Group.Id, params: Page.Params)(implicit ctx: UserAwareCtx): IO[Page[Proposal.Full]]

  def listPublic(ids: List[Proposal.Id]): IO[List[Proposal]]

  def listPublicFull(ids: List[Proposal.Id])(implicit ctx: UserAwareCtx): IO[List[Proposal.Full]]

  def findPublicFull(group: Group.Id, proposal: Proposal.Id)(implicit ctx: UserAwareCtx): IO[Option[Proposal.Full]]
}

trait SuggestProposalRepo {
  def listTags(): IO[List[Tag]]

  def listOrgaTags()(implicit ctx: OrgaCtx): IO[List[Tag]]

  def listFull(params: Page.Params)(implicit ctx: OrgaCtx): IO[Page[Proposal.Full]]
}

object ProposalFields {
  val title = "title"
  val created = "created"
}
