package fr.gospeak.web.services.openapi.models

import fr.gospeak.web.services.openapi.error.OpenApiError.ErrorMessage
import fr.gospeak.web.services.openapi.models.utils.Url
import org.scalatest.{FunSpec, Matchers}

class ServerSpec extends FunSpec with Matchers {
  describe("Server") {
    it("should extract variables from an Url") {
      Server.extractVariables(Url("https://gospeak.io/api")) shouldBe Seq()
      Server.extractVariables(Url("https://{user}:{pass}@gospeak.io:{port}/api")) shouldBe Seq("user", "pass", "port")
    }
    it("should check if all variables are referenced") {
      Server(Url("https://gospeak.io/api"), None, None, None).validate shouldBe None
      Server(Url("https://gospeak.io/api"), None, Some(Map("port" -> Server.Variable("9000", None, None))), None).validate shouldBe None
      Server(Url("https://gospeak.io:{port}/api"), None, Some(Map("port" -> Server.Variable("9000", None, None))), None).validate shouldBe None
      Server(Url("https://gospeak.io:{port}/api"), None, None, None).validate shouldBe Some(Seq(ErrorMessage.missingVariable("port")))
    }
  }
}