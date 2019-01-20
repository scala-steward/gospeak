package fr.gospeak.infra.services.storage.sql.tables

import cats.data.NonEmptyList
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.update.Update
import fr.gospeak.core.domain.User
import fr.gospeak.core.domain.utils.Email
import fr.gospeak.infra.utils.DoobieUtils.Fragments._
import fr.gospeak.infra.utils.DoobieUtils.Mappings._

object UserTable {
  private val _ = userIdMeta // for intellij not remove DoobieUtils.Mappings import
  private val table = "users"
  private val fields = Seq("id", "first_name", "last_name", "email", "created", "updated")
  private val tableFr: Fragment = Fragment.const0(table)
  private val fieldsFr: Fragment = Fragment.const0(fields.mkString(", "))

  private def values(e: User): Fragment =
    fr0"${e.id}, ${e.firstName}, ${e.lastName}, ${e.email}, ${e.created}, ${e.updated}"

  private[tables] def selectOneQuery(email: Email): doobie.Query0[User] =
    buildSelect(tableFr, fieldsFr, fr0"WHERE email=$email").query[User]

  def insert(elt: User): doobie.Update0 = buildInsert(tableFr, fieldsFr, values(elt)).update

  def insertMany(elts: NonEmptyList[User]): doobie.ConnectionIO[Int] = Update[User](insert(elts.head).sql).updateMany(elts)

  def selectOne(email: Email): doobie.ConnectionIO[Option[User]] = selectOneQuery(email).option
}
