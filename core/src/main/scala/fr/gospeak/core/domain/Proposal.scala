package fr.gospeak.core.domain

import cats.data.NonEmptyList
import fr.gospeak.core.domain.utils.Info
import fr.gospeak.libs.scalautils.domain._

import scala.concurrent.duration.FiniteDuration

final case class Proposal(id: Proposal.Id,
                          talk: Talk.Id,
                          cfp: Cfp.Id,
                          event: Option[Event.Id],
                          title: Talk.Title,
                          duration: FiniteDuration,
                          status: Proposal.Status,
                          description: Markdown,
                          speakers: NonEmptyList[User.Id],
                          slides: Option[Slides],
                          video: Option[Video],
                          info: Info) {
  def data: Proposal.Data = Proposal.Data(this)
}

object Proposal {

  final class Id private(value: String) extends DataClass(value)

  object Id extends UuidIdBuilder[Id]("Proposal.Id", new Id(_))

  sealed trait Status extends Product with Serializable

  object Status extends EnumBuilder[Status]("Proposal.Status") {

    case object Pending extends Status

    case object Accepted extends Status

    case object Rejected extends Status

    val all: Seq[Status] = Seq(Pending, Accepted, Rejected)
  }

  final case class Data(title: Talk.Title,
                        duration: FiniteDuration,
                        description: Markdown,
                        slides: Option[Slides],
                        video: Option[Video])

  object Data {
    def apply(talk: Talk): Data = Data(talk.title, talk.duration, talk.description, talk.slides, talk.video)

    def apply(proposal: Proposal): Data = Data(proposal.title, proposal.duration, proposal.description, proposal.slides, proposal.video)
  }

}
