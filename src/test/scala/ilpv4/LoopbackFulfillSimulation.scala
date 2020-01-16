package ilpv4

import com.google.common.primitives.UnsignedLong
import feign.FeignException
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.slf4j.LoggerFactory
import util.IlpChecks._
import util._

class LoopbackFulfillSimulation extends Simulation {

  val logger = LoggerFactory.getLogger(classOf[LoopbackFulfillSimulation])
  val httpConf = http.baseUrl(Config.javaConnectorUrl)

  before {
    Admin.client.createAccountAsResponse(Accounts.ingress)
    Admin.client.createAccountAsResponse(Accounts.fulfillLoopback)
    try {
      Admin.client.createStaticRoute(Config.fulfillLoopbackAddress, Routes.fulfillLoopbackRoute)
    }
    catch {
      case e: FeignException => {
        if (e.status() != 409) {
          throw e
        }
      }
    }
  }

  val prepare = Prepare.create(UnsignedLong.ONE, Config.fulfillLoopbackAddress)

  val sendPayments = scenario("send payments to fulfill loopback")
    .exec(
      ConnectorRequests.ilp(Config.ingressAccount, "shh", prepare)
        .check(FULFILLED)
    )

  setUp(
//    sendPayments.inject(constantUsersPerSec(1) during(5))
    sendPayments.inject(atOnceUsers(1))
  ).protocols(httpConf)
}
