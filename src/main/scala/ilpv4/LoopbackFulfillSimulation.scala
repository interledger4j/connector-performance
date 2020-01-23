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

  Admin.client.createAccountAsResponse(Accounts.ingress)
  Admin.client.createAccountAsResponse(Accounts.fulfillLoopback)
  Admin.safeCreateStaticRoute(Config.fulfillLoopbackAddress, Routes.fulfillLoopbackRoute)

  val sendPayments = scenario("send payments to fulfill loopback")
    .forever {
      exec(
        ConnectorRequests.ilp(Config.ingressAccount, "shh", Prepare.create(UnsignedLong.ONE, Config.fulfillLoopbackAddress))
          .check(FULFILLED)
      )
    }

  val execution = sendPayments
//    .inject(rampUsers(t_concurrency) over t_rampUp)
    .inject(atOnceUsers(Config.concurrency))
    .protocols(httpConf)

  setUp(execution).
    throttle(jumpToRps(Config.throughput), holdFor(Config.holdFor)).
    maxDuration(Config.rampUp + Config.holdFor)

//  setUp(
//    sendPayments.inject(rampUsers(threads) during (rampup seconds))
////    sendPayments.inject(atOnceUsers(1))
//  ).protocols(httpConf)
}
