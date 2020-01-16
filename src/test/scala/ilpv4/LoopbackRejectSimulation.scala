package ilpv4

import com.google.common.primitives.UnsignedLong
import feign.FeignException
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.slf4j.LoggerFactory
import util.IlpChecks._
import util._

class LoopbackRejectSimulation extends Simulation {

  val logger = LoggerFactory.getLogger(classOf[LoopbackRejectSimulation])
  val httpConf = http.baseUrl(Config.javaConnectorUrl)

  before {
    Admin.accountClient.createAccountAsResponse(Accounts.ingress)
    Admin.accountClient.createAccountAsResponse(Accounts.rejectLoopback)
    try {
      Admin.routeClient.createStaticRoute(Config.rejectLoopbackAddress, Routes.rejectLoopbackRoute)
    }
    catch {
      case e: FeignException => {
        if (e.status() != 409) {
          throw e
        }
      }
    }
  }

  val prepare = Prepare.create(UnsignedLong.ONE, Config.rejectLoopbackAddress)

  val sendPayments = scenario("send payments to reject loopback")
    .exec(
      ConnectorRequests.ilp(Config.ingressAccount, "shh", prepare)
        .check(REJECTED_T02)
    )

  setUp(
//    sendPayments.inject(constantUsersPerSec(1) during(5))
    sendPayments.inject(atOnceUsers(1))
  ).protocols(httpConf)
}