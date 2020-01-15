package ilpv4

import com.google.common.primitives.UnsignedLong
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
  }

  val prepare = Prepare.create(UnsignedLong.ONE, Config.fulfillLoopbackAddress)

  val sendPayments = scenario("send payments to fulfill loopback")
    .exec(
      ConnectorRequests.ilp(Config.ingressAccount, "shh", prepare)
        .check(CODE_T00)
    )

  setUp(
//    sendPayments.inject(constantUsersPerSec(1) during(5))
    sendPayments.inject(atOnceUsers(1))
  ).protocols(httpConf)
}
