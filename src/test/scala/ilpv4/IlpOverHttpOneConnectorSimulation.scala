package ilpv4

import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}

import com.google.common.primitives.UnsignedLong
import feign.FeignException
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.interledger.spsp.{PaymentPointer, StreamConnectionDetails}
import org.slf4j.LoggerFactory
import util._

class IlpOverHttpOneConnectorSimulation extends Simulation {

  val logger = LoggerFactory.getLogger(classOf[IlpOverHttpOneConnectorSimulation])
  val httpConf = http.baseUrl(Config.javaConnectorUrl)

  Admin.client.createAccountAsResponse(Accounts.ingress)
  Admin.client.createAccountAsResponse(Accounts.javaSpsp)
  try {
    Admin.client.createStaticRoute(Config.javaSpspAddress, Routes.javaSpspRoute)
  }
  catch {
    case e: FeignException => {
      if (e.status() != 409) {
        throw e
      }
    }
  }

  val details = new AtomicReference[StreamConnectionDetails]()
  val streamDetails = Admin.spspClient.getStreamConnectionDetails(PaymentPointer.of(Config.javaSpspPaymentPointer))
  details.set(streamDetails)
  println(details.get())
//  before {
//
//  }

  val totalAmount = new AtomicInteger(1000);

  val prepare = Prepare.create(UnsignedLong.ONE, Config.javaSpspAddress)
  val sendPayments = scenario("send payments to spsp receiver")
      .exec(
        Stream.preflightCheck(Config.javaSpspAccount, "shh", details.get().sharedSecret(),
          details.get().destinationAddress(), Accounts.javaSpspDenomination)
      )
      .exec(
        Stream.sendStreamPacket(Config.javaSpspAccount, "shh", details.get().sharedSecret(), UnsignedLong.ONE,
          details.get().destinationAddress())
      )

  setUp(
    sendPayments.inject(constantUsersPerSec(10) during(15))
//    sendPayments.inject(atOnceUsers(1))
  ).protocols(httpConf)
}
