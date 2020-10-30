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
class PARTNERS private(getAlias: Option[String] = Some("pa")) extends Table.SqlTable("PUBLIC", "partners", getAlias) {
  type Self = PARTNERS

  val ID: SqlField[Partner.Id, PARTNERS] = SqlField(this, "id", "CHAR(36) NOT NULL", JdbcType.Char, nullable = false, 1)
  val GROUP_ID: SqlFieldRef[Group.Id, PARTNERS, GROUPS] = SqlField(this, "group_id", "CHAR(36) NOT NULL", JdbcType.Char, nullable = false, 2, GROUPS.table.ID)
  val SLUG: SqlField[Partner.Slug, PARTNERS] = SqlField(this, "slug", "VARCHAR(120) NOT NULL", JdbcType.VarChar, nullable = false, 3)
  val NAME: SqlField[Partner.Name, PARTNERS] = SqlField(this, "name", "VARCHAR(120) NOT NULL", JdbcType.VarChar, nullable = false, 4)
  val NOTES: SqlField[Markdown, PARTNERS] = SqlField(this, "notes", "VARCHAR(4096) NOT NULL", JdbcType.VarChar, nullable = false, 5)
  val DESCRIPTION: SqlField[Markdown, PARTNERS] = SqlField(this, "description", "VARCHAR(4096)", JdbcType.VarChar, nullable = true, 6)
  val LOGO: SqlField[Logo, PARTNERS] = SqlField(this, "logo", "VARCHAR(1024) NOT NULL", JdbcType.VarChar, nullable = false, 7)
  val SOCIAL_FACEBOOK: SqlField[FacebookAccount, PARTNERS] = SqlField(this, "social_facebook", "VARCHAR(1024)", JdbcType.VarChar, nullable = true, 8)
  val SOCIAL_INSTAGRAM: SqlField[InstagramAccount, PARTNERS] = SqlField(this, "social_instagram", "VARCHAR(1024)", JdbcType.VarChar, nullable = true, 9)
  val SOCIAL_TWITTER: SqlField[TwitterAccount, PARTNERS] = SqlField(this, "social_twitter", "VARCHAR(1024)", JdbcType.VarChar, nullable = true, 10)
  val SOCIAL_LINKEDIN: SqlField[LinkedInAccount, PARTNERS] = SqlField(this, "social_linkedIn", "VARCHAR(1024)", JdbcType.VarChar, nullable = true, 11)
  val SOCIAL_YOUTUBE: SqlField[YoutubeAccount, PARTNERS] = SqlField(this, "social_youtube", "VARCHAR(1024)", JdbcType.VarChar, nullable = true, 12)
  val SOCIAL_MEETUP: SqlField[MeetupAccount, PARTNERS] = SqlField(this, "social_meetup", "VARCHAR(1024)", JdbcType.VarChar, nullable = true, 13)
  val SOCIAL_EVENTBRITE: SqlField[EventbriteAccount, PARTNERS] = SqlField(this, "social_eventbrite", "VARCHAR(1024)", JdbcType.VarChar, nullable = true, 14)
  val SOCIAL_SLACK: SqlField[SlackAccount, PARTNERS] = SqlField(this, "social_slack", "VARCHAR(1024)", JdbcType.VarChar, nullable = true, 15)
  val SOCIAL_DISCORD: SqlField[DiscordAccount, PARTNERS] = SqlField(this, "social_discord", "VARCHAR(1024)", JdbcType.VarChar, nullable = true, 16)
  val SOCIAL_GITHUB: SqlField[GithubAccount, PARTNERS] = SqlField(this, "social_github", "VARCHAR(1024)", JdbcType.VarChar, nullable = true, 17)
  val CREATED_AT: SqlField[Instant, PARTNERS] = SqlField(this, "created_at", "TIMESTAMP NOT NULL", JdbcType.Timestamp, nullable = false, 18)
  val CREATED_BY: SqlFieldRef[User.Id, PARTNERS, USERS] = SqlField(this, "created_by", "CHAR(36) NOT NULL", JdbcType.Char, nullable = false, 19, USERS.table.ID)
  val UPDATED_AT: SqlField[Instant, PARTNERS] = SqlField(this, "updated_at", "TIMESTAMP NOT NULL", JdbcType.Timestamp, nullable = false, 20)
  val UPDATED_BY: SqlFieldRef[User.Id, PARTNERS, USERS] = SqlField(this, "updated_by", "CHAR(36) NOT NULL", JdbcType.Char, nullable = false, 21, USERS.table.ID)

  override def getFields: List[SqlField[_, PARTNERS]] = List(ID, GROUP_ID, SLUG, NAME, NOTES, DESCRIPTION, LOGO, SOCIAL_FACEBOOK, SOCIAL_INSTAGRAM, SOCIAL_TWITTER, SOCIAL_LINKEDIN, SOCIAL_YOUTUBE, SOCIAL_MEETUP, SOCIAL_EVENTBRITE, SOCIAL_SLACK, SOCIAL_DISCORD, SOCIAL_GITHUB, CREATED_AT, CREATED_BY, UPDATED_AT, UPDATED_BY)

  override def getSorts: List[Sort] = List(Sort("name", "name", NonEmptyList.of(NAME.asc)))

  override def searchOn: List[SqlField[_, PARTNERS]] = List(ID, SLUG, NAME, NOTES, DESCRIPTION)

  override def getFilters: List[Filter] = List()

  def alias(alias: String): PARTNERS = new PARTNERS(Some(alias))
}

private[database] object PARTNERS {
  val table = new PARTNERS() // table instance, should be accessed through `gospeak.infra.services.storage.sql.database.Tables` object
}
