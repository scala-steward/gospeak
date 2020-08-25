/*
 * This file is generated by jOOQ.
 */
package gospeak.libs.sql.jooqdb.tables


import gospeak.libs.sql.jooqdb.Public
import gospeak.libs.sql.jooqdb.tables.records.KindsRecord

import java.lang.Boolean
import java.lang.Class
import java.lang.Double
import java.lang.Integer
import java.lang.Long
import java.lang.String
import java.time.LocalDate
import java.time.LocalDateTime

import org.jooq.Field
import org.jooq.ForeignKey
import org.jooq.Name
import org.jooq.Record
import org.jooq.Row9
import org.jooq.Schema
import org.jooq.Table
import org.jooq.TableField
import org.jooq.TableOptions
import org.jooq.impl.DSL
import org.jooq.impl.TableImpl

import scala.Array


object Kinds {

  /**
   * The reference instance of <code>PUBLIC.kinds</code>
   */
  val KINDS = new Kinds
}

/**
 * This class is generated by jOOQ.
 */
class Kinds(
  alias : Name,
  child : Table[_ <: Record],
  path : ForeignKey[_ <: Record, KindsRecord],
  aliased : Table[KindsRecord],
  parameters : Array[ Field[_] ]
)
extends TableImpl[KindsRecord](
  alias,
  Public.PUBLIC,
  child,
  path,
  aliased,
  parameters,
  DSL.comment(""),
  TableOptions.table
)
{

  /**
   * The class holding records for this type
   */
  override def getRecordType : Class[KindsRecord] = {
    classOf[KindsRecord]
  }

  /**
   * The column <code>PUBLIC.kinds.char</code>.
   */
  val CHAR : TableField[KindsRecord, String] = createField(DSL.name("char"), org.jooq.impl.SQLDataType.CHAR(4), "")

  /**
   * The column <code>PUBLIC.kinds.varchar</code>.
   */
  val VARCHAR : TableField[KindsRecord, String] = createField(DSL.name("varchar"), org.jooq.impl.SQLDataType.VARCHAR(50), "")

  /**
   * The column <code>PUBLIC.kinds.timestamp</code>.
   */
  val TIMESTAMP : TableField[KindsRecord, LocalDateTime] = createField(DSL.name("timestamp"), org.jooq.impl.SQLDataType.LOCALDATETIME, "")

  /**
   * The column <code>PUBLIC.kinds.date</code>.
   */
  val DATE : TableField[KindsRecord, LocalDate] = createField(DSL.name("date"), org.jooq.impl.SQLDataType.LOCALDATE, "")

  /**
   * The column <code>PUBLIC.kinds.boolean</code>.
   */
  val BOOLEAN : TableField[KindsRecord, Boolean] = createField(DSL.name("boolean"), org.jooq.impl.SQLDataType.BOOLEAN, "")

  /**
   * The column <code>PUBLIC.kinds.int</code>.
   */
  val INT : TableField[KindsRecord, Integer] = createField(DSL.name("int"), org.jooq.impl.SQLDataType.INTEGER, "")

  /**
   * The column <code>PUBLIC.kinds.bigint</code>.
   */
  val BIGINT : TableField[KindsRecord, Long] = createField(DSL.name("bigint"), org.jooq.impl.SQLDataType.BIGINT, "")

  /**
   * The column <code>PUBLIC.kinds.double</code>.
   */
  val DOUBLE : TableField[KindsRecord, Double] = createField(DSL.name("double"), org.jooq.impl.SQLDataType.DOUBLE, "")

  /**
   * The column <code>PUBLIC.kinds.a_long_name</code>.
   */
  val A_LONG_NAME : TableField[KindsRecord, Integer] = createField(DSL.name("a_long_name"), org.jooq.impl.SQLDataType.INTEGER, "")

  /**
   * Create a <code>PUBLIC.kinds</code> table reference
   */
  def this() = {
    this(DSL.name("kinds"), null, null, null, null)
  }

  /**
   * Create an aliased <code>PUBLIC.kinds</code> table reference
   */
  def this(alias : String) = {
    this(DSL.name(alias), null, null, gospeak.libs.sql.jooqdb.tables.Kinds.KINDS, null)
  }

  /**
   * Create an aliased <code>PUBLIC.kinds</code> table reference
   */
  def this(alias : Name) = {
    this(alias, null, null, gospeak.libs.sql.jooqdb.tables.Kinds.KINDS, null)
  }

  private def this(alias : Name, aliased : Table[KindsRecord]) = {
    this(alias, null, null, aliased, null)
  }

  override def getSchema : Schema = Public.PUBLIC

  override def as(alias : String) : Kinds = {
    new Kinds(DSL.name(alias), this)
  }

  override def as(alias : Name) : Kinds = {
    new Kinds(alias, this)
  }

  /**
   * Rename this table
   */
  override def rename(name : String) : Kinds = {
    new Kinds(DSL.name(name), null)
  }

  /**
   * Rename this table
   */
  override def rename(name : Name) : Kinds = {
    new Kinds(name, null)
  }

  // -------------------------------------------------------------------------
  // Row9 type methods
  // -------------------------------------------------------------------------

  override def fieldsRow : Row9[String, String, LocalDateTime, LocalDate, Boolean, Integer, Long, Double, Integer] = {
    super.fieldsRow.asInstanceOf[ Row9[String, String, LocalDateTime, LocalDate, Boolean, Integer, Long, Double, Integer] ]
  }
}
