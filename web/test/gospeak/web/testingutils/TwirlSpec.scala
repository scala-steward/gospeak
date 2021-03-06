package gospeak.web.testingutils

import gospeak.web.domain.Breadcrumb
import gospeak.web.utils.{UserAwareReq, UserReq}
import play.api.mvc.AnyContent

trait TwirlSpec extends BaseSpec {
  protected implicit val userReq: UserReq[AnyContent] = Values.userReq
  protected implicit val userAwareReq: UserAwareReq[AnyContent] = Values.userAwareReq
  protected val b: Breadcrumb = Values.b
}
