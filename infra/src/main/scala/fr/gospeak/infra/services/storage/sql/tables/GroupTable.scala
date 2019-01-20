package fr.gospeak.infra.services.storage.sql.tables

import cats.data.NonEmptyList
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.update.Update
import fr.gospeak.core.domain.utils.Page
import fr.gospeak.core.domain.{Group, User}
import fr.gospeak.infra.utils.DoobieUtils.Fragments.{buildInsert, buildSelect, paginate}
import fr.gospeak.infra.utils.DoobieUtils.Mappings._

object GroupTable {
  private val _ = groupIdMeta // for intellij not remove DoobieUtils.Mappings import
  private[tables] val table = "groups"
  private[tables] val fields = Seq("id", "slug", "name", "description", "owners", "created", "created_by", "updated", "updated_by")
  private val tableFr: Fragment = Fragment.const0(table)
  private val fieldsFr: Fragment = Fragment.const0(fields.mkString(", "))
  private val searchFields = Seq("id", "slug", "name", "description")
  private val defaultSort = Page.OrderBy("name")

  private def values(e: Group): Fragment =
    fr0"${e.id}, ${e.slug}, ${e.name}, ${e.description}, ${e.owners}, ${e.info.created}, ${e.info.createdBy}, ${e.info.updated}, ${e.info.updatedBy}"

  private[tables] def slugToIdQuery(slug: Group.Slug): doobie.Query0[Group.Id] =
    buildSelect(tableFr, fr0"id", fr0"WHERE slug=$slug").query[Group.Id]

  private[tables] def selectOneQuery(id: Group.Id, user: User.Id): doobie.Query0[Group] =
    buildSelect(tableFr, fieldsFr, fr0"WHERE id=$id AND owners LIKE ${"%" + user.value + "%"}").query[Group]

  private[tables] def selectPageQuery(user: User.Id, params: Page.Params): doobie.Query0[Group] =
    buildSelect(tableFr, fieldsFr, paginate(params, searchFields, defaultSort, Some(fr0"WHERE owners LIKE ${"%" + user.value + "%"}"))).query[Group]

  private[tables] def countPageQuery(user: User.Id, params: Page.Params): doobie.Query0[Long] =
    buildSelect(tableFr, fr0"count(*)", fr0"WHERE owners LIKE ${"%" + user.value + "%"}").query[Long]

  def insert(elt: Group): doobie.Update0 = buildInsert(tableFr, fieldsFr, values(elt)).update

  def insertMany(elts: NonEmptyList[Group]): doobie.ConnectionIO[Int] = Update[Group](insert(elts.head).sql).updateMany(elts)

  // slug should be unique on the platform
  def slugToId(slug: Group.Slug): doobie.ConnectionIO[Option[Group.Id]] = slugToIdQuery(slug).option

  def selectOne(id: Group.Id, user: User.Id): doobie.ConnectionIO[Option[Group]] = selectOneQuery(id, user).option

  def selectPage(user: User.Id, params: Page.Params): doobie.ConnectionIO[Page[Group]] = for {
    elts <- selectPageQuery(user, params).to[List]
    total <- countPageQuery(user, params).unique
  } yield Page(elts, params, Page.Total(total.toInt))
}
