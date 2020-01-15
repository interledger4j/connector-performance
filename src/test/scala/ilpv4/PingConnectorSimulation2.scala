package ilpv4

import com.google.common.primitives.UnsignedLong
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.interledger.core.InterledgerResponsePacket
import org.slf4j.LoggerFactory
import util.{Config, ConnectorRequests, Prepare}

import scala.concurrent.duration._

/**
 * This test pings a Connector using an ingress account, asserting a fulfill response.
 */
class PingConnectorSimulation2 extends Simulation {

  val logger = LoggerFactory.getLogger(classOf[PingConnectorSimulation2])

  // The account that all incoming packets ingress into the Connector on.
  val ingressAccount = "lt-ingress"

  val httpConf = http.baseUrl(Config.javaConnectorUrl)
  val ping = scenario("Ping Connector").exec(
      ConnectorRequests.ilp(
        "lt-ingress",
        "shh",
        Prepare.create(UnsignedLong.ONE, "test.money.ilpv4.dev._ping")
      )
    )
    .exec(session=>{
  //      val packet:InterledgerFulfillPacket = session("lastResponse")
      val packet:InterledgerResponsePacket = session("ilpResponse").as[InterledgerResponsePacket]

      //Analyse theResponse...

  //      check(packet.getFulfillment().equals(null)).isTrue()

      //... and make sure to return the session
      session
    })

  logger.info("Body: " + bodyBytes);

  setUp(ping.inject(constantUsersPerSec(1) during (1 seconds))
    .protocols(httpConf))

  //    .assertions(
  //      global.successfulRequests.percent.is(100),
  //      global.responseTime.max.lt(1000),
  //      global.responseTime.mean.lt(750),
  //      global.requestsPerSec.lt(10)
  //    )
}