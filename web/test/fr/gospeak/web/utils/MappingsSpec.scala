package fr.gospeak.web.utils

import fr.gospeak.web.utils.Mappings._
import org.scalatest.{FunSpec, Matchers}
import play.api.data.FormError
import play.api.data.validation.{Invalid, Valid, ValidationError}

import scala.util.{Failure, Success, Try}

class MappingsSpec extends FunSpec with Matchers {

  case class Value(value: String)

  object Value {
    def from(in: String): Try[Value] = {
      if(in.nonEmpty && in.length < 4) Success(Value(in))
      else Failure(new Exception("Wrong format"))
    }
  }

  describe("Mappings") {
    describe("required") {
      val c = required[Value](_.value)
      it("should validate value") {
        c.name shouldBe Some("constraint.required")
        c(Value("aaa")) shouldBe Valid
        c(Value(" ")) shouldBe Invalid(List(ValidationError(requiredError)))
        c(Value("")) shouldBe Invalid(List(ValidationError(requiredError)))
      }
    }
    describe("pattern") {
      val regex = "a+".r
      val c = pattern[Value](regex)(_.value)
      it("should validate value") {
        c.name shouldBe Some("constraint.pattern")
        c(Value("aaa")) shouldBe Valid
        c(Value("bb")) shouldBe Invalid(List(ValidationError(patternError, regex)))
        c(Value(" ")) shouldBe Invalid(List(ValidationError(patternError, regex)))
        c(Value("")) shouldBe Invalid(List(ValidationError(patternError, regex)))
      }
    }
    describe("stringFormatter") {
      val f = stringFormatter[Value](Value(_), _.value)
      it("should bind & unbind value") {
        val value = Value("aaa")
        f.bind("key", Map("key" -> value.value)) shouldBe Right(value)
        f.unbind("key", value) shouldBe Map("key" -> value.value)
      }
      it("should return required error") {
        f.bind("key", Map()) shouldBe Left(List(FormError("key", List(requiredError))))
      }
    }
    describe("stringTryFormatter") {
      val f = stringTryFormatter[Value](Value.from, _.value)
      it("should bind & unbind value") {
        val value = Value("aaa")
        f.bind("key", Map("key" -> value.value)) shouldBe Right(value)
        f.unbind("key", value) shouldBe Map("key" -> value.value)
      }
      it("should return required error") {
        f.bind("key", Map()) shouldBe Left(List(FormError("key", List(requiredError))))
      }
      it("should return format error") {
        f.bind("key", Map("key" -> "")) shouldBe Left(List(FormError("key", List(formatError), "Wrong format")))
      }
    }
  }
}
