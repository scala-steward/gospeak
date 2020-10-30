package gospeak.infra.services.storage.sql.database.tables

import java.time.{Instant, LocalDateTime}

import cats.data.NonEmptyList
import fr.loicknuchel.safeql.Table._
import fr.loicknuchel.safeql._
import gospeak.core.domain._
import gospeak.core.domain.messages.Message
import gospeak.core.domain.utils.SocialAccounts.SocialAccount._
import gospeak.core.services.meetup.domain.{MeetupEvent, MeetupGroup, MeetupUser, MeetupVenue}
import gospeak.core.services.slack.domain.SlackToken
import gospeak.libs.scala.domain._

import scala.concurrent.duration.FiniteDuration

/**
 * Generated file, do not update it!
 *
 * Regenerate it using Gospeak CLI (`gospeak.web.GsCLI` class) to keep it in sync with the database state.
 *
 * --
 *
 * Class generated by fr.loicknuchel.safeql.gen.writer.ScalaWriter
 */
class EVENT_RSVPS private(getAlias: Option[String] = Some("er")) extends Table.SqlTable("PUBLIC", "event_rsvps", getAlias) {
  type Self = EVENT_RSVPS

  val EVENT_ID: SqlFieldRef[Event.Id, EVENT_RSVPS, EVENTS] = SqlField(this, "event_id", "CHAR(36) NOT NULL", JdbcType.Char, nullable = false, 1, EVENTS.table.ID)
  val USER_ID: SqlFieldRef[User.Id, EVENT_RSVPS, USERS] = SqlField(this, "user_id", "CHAR(36) NOT NULL", JdbcType.Char, nullable = false, 2, USERS.table.ID)
  val ANSWER: SqlField[Event.Rsvp.Answer, EVENT_RSVPS] = SqlField(this, "answer", "VARCHAR(10) NOT NULL", JdbcType.VarChar, nullable = false, 3)
  val ANSWERED_AT: SqlField[Instant, EVENT_RSVPS] = SqlField(this, "answered_at", "TIMESTAMP NOT NULL", JdbcType.Timestamp, nullable = false, 4)

  override def getFields: List[SqlField[_, EVENT_RSVPS]] = List(EVENT_ID, USER_ID, ANSWER, ANSWERED_AT)

  override def getSorts: List[Sort] = List(Sort("answered", "answer date", NonEmptyList.of(ANSWERED_AT.asc)))

  override def searchOn: List[SqlField[_, EVENT_RSVPS]] = List(ANSWER)

  override def getFilters: List[Filter] = List()

  def alias(alias: String): EVENT_RSVPS = new EVENT_RSVPS(Some(alias))
}

private[database] object EVENT_RSVPS {
  val table = new EVENT_RSVPS() // table instance, should be accessed through `gospeak.infra.services.storage.sql.database.Tables` object
}
