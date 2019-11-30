package fr.gospeak.libs.scalautils.domain

import fr.gospeak.libs.scalautils.Crypto
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class CryptedSpec extends AnyFunSpec with Matchers {
  describe("Crypted") {
    it("should not print its value in toString") {
      Crypted("test").toString shouldBe "*****"
    }
    it("should return its value with decode") {
      val key = Crypto.aesGenerateKey().get
      val crypted = Crypted.from("test", key).get
      crypted.decode(key).get shouldBe "test"
    }
  }
}
