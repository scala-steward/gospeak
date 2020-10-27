package gospeak.infra.services.storage.sql.database.tables

import java.time.{Instant, LocalDateTime}

import cats.data.NonEmptyList
import gospeak.core.domain._
import gospeak.core.domain.messages.Message
import gospeak.core.domain.utils.SocialAccounts.SocialAccount._
import gospeak.core.services.meetup.domain.{MeetupEvent, MeetupGroup, MeetupUser, MeetupVenue}
import gospeak.core.services.slack.domain.SlackToken
import gospeak.libs.scala.domain._
import gospeak.libs.sql.dsl.Table._
import gospeak.libs.sql.dsl._

import scala.concurrent.duration.FiniteDuration

/**
 * Generated file, do not update it!
 *
 * Regenerate it using Gospeak CLI (`gospeak.web.GsCLI` class) to keep it in sync with the database state.
 *
 * --
 *
 * Class generated by gospeak.libs.sql.generator.writer.ScalaWriter
 */
class GROUP_SETTINGS private(getAlias: Option[String] = Some("gs")) extends Table.SqlTable("PUBLIC", "group_settings", getAlias) {
  type Self = GROUP_SETTINGS

  val GROUP_ID: SqlFieldRef[Group.Id, GROUP_SETTINGS, GROUPS] = SqlField(this, "group_id", "CHAR(36) NOT NULL", JdbcType.Char, nullable = false, 1, GROUPS.table.ID)
  val MEETUP_ACCESS_TOKEN: SqlField[Crypted, GROUP_SETTINGS] = SqlField(this, "meetup_access_token", "VARCHAR(200)", JdbcType.VarChar, nullable = true, 2)
  val MEETUP_REFRESH_TOKEN: SqlField[Crypted, GROUP_SETTINGS] = SqlField(this, "meetup_refresh_token", "VARCHAR(200)", JdbcType.VarChar, nullable = true, 3)
  val MEETUP_GROUP_SLUG: SqlField[MeetupGroup.Slug, GROUP_SETTINGS] = SqlField(this, "meetup_group_slug", "VARCHAR(120)", JdbcType.VarChar, nullable = true, 4)
  val MEETUP_LOGGED_USER_ID: SqlField[MeetupUser.Id, GROUP_SETTINGS] = SqlField(this, "meetup_logged_user_id", "BIGINT", JdbcType.BigInt, nullable = true, 5)
  val MEETUP_LOGGED_USER_NAME: SqlField[String, GROUP_SETTINGS] = SqlField(this, "meetup_logged_user_name", "VARCHAR(120)", JdbcType.VarChar, nullable = true, 6)
  val SLACK_TOKEN: SqlField[SlackToken, GROUP_SETTINGS] = SqlField(this, "slack_token", "VARCHAR(200)", JdbcType.VarChar, nullable = true, 7)
  val SLACK_BOT_NAME: SqlField[String, GROUP_SETTINGS] = SqlField(this, "slack_bot_name", "VARCHAR(120)", JdbcType.VarChar, nullable = true, 8)
  val SLACK_BOT_AVATAR: SqlField[Avatar, GROUP_SETTINGS] = SqlField(this, "slack_bot_avatar", "VARCHAR(1024)", JdbcType.VarChar, nullable = true, 9)
  val EVENT_DESCRIPTION: SqlField[LiquidMarkdown[Message.EventInfo], GROUP_SETTINGS] = SqlField(this, "event_description", "VARCHAR NOT NULL", JdbcType.VarChar, nullable = false, 10)
  val EVENT_TEMPLATES: SqlField[Map[String, Liquid[Message.EventInfo]], GROUP_SETTINGS] = SqlField(this, "event_templates", "VARCHAR NOT NULL", JdbcType.VarChar, nullable = false, 11)
  val PROPOSAL_TWEET: SqlField[Liquid[Message.ProposalInfo], GROUP_SETTINGS] = SqlField(this, "proposal_tweet", "VARCHAR DEFAULT 'Presentation of \"{{proposal.title}}\" by{{#proposal.speakers}}{{^-first}} and{{/-first}} {{#links.twitter}}{{handle}}{{/links.twitter}}{{^links.twitter}}{{name}}{{/links.twitter}}{{/proposal.speakers}}' NOT NULL", JdbcType.VarChar, nullable = false, 15)
  val ACTIONS: SqlField[Map[Group.Settings.Action.Trigger, List[Group.Settings.Action]], GROUP_SETTINGS] = SqlField(this, "actions", "VARCHAR NOT NULL", JdbcType.VarChar, nullable = false, 12)
  val UPDATED_AT: SqlField[Instant, GROUP_SETTINGS] = SqlField(this, "updated_at", "TIMESTAMP NOT NULL", JdbcType.Timestamp, nullable = false, 13)
  val UPDATED_BY: SqlFieldRef[User.Id, GROUP_SETTINGS, USERS] = SqlField(this, "updated_by", "CHAR(36) NOT NULL", JdbcType.Char, nullable = false, 14, USERS.table.ID)

  override def getFields: List[SqlField[_, GROUP_SETTINGS]] = List(GROUP_ID, MEETUP_ACCESS_TOKEN, MEETUP_REFRESH_TOKEN, MEETUP_GROUP_SLUG, MEETUP_LOGGED_USER_ID, MEETUP_LOGGED_USER_NAME, SLACK_TOKEN, SLACK_BOT_NAME, SLACK_BOT_AVATAR, EVENT_DESCRIPTION, EVENT_TEMPLATES, PROPOSAL_TWEET, ACTIONS, UPDATED_AT, UPDATED_BY)

  override def getSorts: List[Sort] = List(Sort("group-id", "group_id", NonEmptyList.of(GROUP_ID.asc)))

  override def searchOn: List[SqlField[_, GROUP_SETTINGS]] = List(GROUP_ID, MEETUP_ACCESS_TOKEN, MEETUP_REFRESH_TOKEN, MEETUP_GROUP_SLUG, MEETUP_LOGGED_USER_ID, MEETUP_LOGGED_USER_NAME, SLACK_TOKEN, SLACK_BOT_NAME, SLACK_BOT_AVATAR, EVENT_DESCRIPTION, EVENT_TEMPLATES, PROPOSAL_TWEET, ACTIONS, UPDATED_AT, UPDATED_BY)

  override def getFilters: List[Filter] = List()

  def alias(alias: String): GROUP_SETTINGS = new GROUP_SETTINGS(Some(alias))
}

private[database] object GROUP_SETTINGS {
  val table = new GROUP_SETTINGS() // table instance, should be accessed through `gospeak.infra.services.storage.sql.database.Tables` object
}