package ilpv4

import com.google.common.primitives.UnsignedLong
import feign.FeignException
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.slf4j.LoggerFactory
import util.IlpChecks._
import util._

class LoopbackFulfillSimulation extends Simulation {

  val t_concurrency = Integer.getInteger("concurrency", 10).toInt
  val t_rampUp = Integer.getInteger("ramp-up", 1).toInt
  val t_holdFor = Integer.getInteger("hold-for", 60).toInt
  val t_throughput = Integer.getInteger("throughput", 100).toInt

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

  val sendPayments = scenario("send payments to fulfill loopback")
    .forever {
      exec(
        ConnectorRequests.ilp(Config.ingressAccount, "shh", Prepare.create(UnsignedLong.ONE, Config.fulfillLoopbackAddress))
          .check(FULFILLED)
      )
    }

  val execution = sendPayments
//    .inject(rampUsers(t_concurrency) over t_rampUp)
    .inject(atOnceUsers(t_concurrency))
    .protocols(httpConf)

  setUp(execution).
    throttle(jumpToRps(t_throughput), holdFor(t_holdFor)).
    maxDuration(t_rampUp + t_holdFor)

//  setUp(
//    sendPayments.inject(rampUsers(threads) during (rampup seconds))
////    sendPayments.inject(atOnceUsers(1))
//  ).protocols(httpConf)
}
