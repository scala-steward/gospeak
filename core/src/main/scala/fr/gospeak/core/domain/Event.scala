package fr.gospeak.core.domain

import fr.gospeak.core.domain.utils.{DataClass, Info, SlugBuilder, UuidIdBuilder}

final case class Event(group: Group.Id,
                       id: Event.Id,
                       slug: Event.Slug,
                       name: Event.Name,
                       // start: datetime
                       // duration: Option[Duration]
                       description: Option[String],
                       venue: Option[String],
                       talks: Seq[Proposal.Id],
                       info: Info)

object Event {

  final class Id private(value: String) extends DataClass(value)

  object Id extends UuidIdBuilder[Id]("Event.Id", new Id(_))

  final class Slug private(value: String) extends DataClass(value)

  object Slug extends SlugBuilder[Slug]("Event.Slug", new Slug(_))

  final case class Name(value: String) extends AnyVal

}
