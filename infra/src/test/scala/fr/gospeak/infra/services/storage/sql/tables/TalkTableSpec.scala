package fr.gospeak.infra.services.storage.sql.tables

import cats.data.NonEmptyList
import fr.gospeak.core.domain.Talk
import fr.gospeak.infra.services.storage.sql.tables.TalkTable._
import fr.gospeak.infra.services.storage.sql.tables.testingutils.TableSpec

class TalkTableSpec extends TableSpec {
  describe("TalkTable") {
    describe("insert") {
      it("should generate the query") {
        val q = insert(talk)
        q.sql shouldBe "INSERT INTO talks (id, slug, title, duration, status, description, speakers, slides, video, created, created_by, updated, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        check(q)
      }
    }
    describe("selectOne") {
      it("should generate the query") {
        val q = selectOne(user.id, talk.slug)
        q.sql shouldBe "SELECT id, slug, title, duration, status, description, speakers, slides, video, created, created_by, updated, updated_by FROM talks WHERE speakers LIKE ? AND slug=?"
        check(q)
      }
    }
    describe("selectPage") {
      it("should generate the query") {
        val (s, c) = selectPage(user.id, params)
        s.sql shouldBe "SELECT id, slug, title, duration, status, description, speakers, slides, video, created, created_by, updated, updated_by FROM talks WHERE speakers LIKE ? ORDER BY title OFFSET 0 LIMIT 20"
        c.sql shouldBe "SELECT count(*) FROM talks WHERE speakers LIKE ? "
        check(s)
        check(c)
      }
    }
    describe("selectAll") {
      it("should generate the query") {
        val q = selectAll(NonEmptyList.of(talk.id))
        q.sql shouldBe "SELECT id, slug, title, duration, status, description, speakers, slides, video, created, created_by, updated, updated_by FROM talks WHERE id IN (?) "
        check(q)
      }
    }
    describe("update") {
      it("should generate the query") {
        val q = update(user.id, talk.slug)(talk.data, now)
        q.sql shouldBe "UPDATE talks SET slug=?, title=?, duration=?, description=?, slides=?, video=?, updated=?, updated_by=? WHERE speakers LIKE ? AND slug=?"
        check(q)
      }
    }
    describe("updateStatus") {
      it("should generate the query") {
        val q = updateStatus(user.id, talk.slug)(Talk.Status.Public)
        q.sql shouldBe "UPDATE talks SET status=? WHERE speakers LIKE ? AND slug=?"
        check(q)
      }
    }
  }
}
