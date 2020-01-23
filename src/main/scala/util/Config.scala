package util

object Config {

  // simulation parameters
  val concurrency = Integer.getInteger("concurrency", 10).toInt
  val rampUp = Integer.getInteger("ramp-up", 1).toInt
  val holdFor = Integer.getInteger("hold-for", 5).toInt
  val throughput = Integer.getInteger("throughput", 100).toInt

  val javaConnectorUrl = "https://connector-load.ilpv4.dev"
//  val javaConnectorUrl = "http://localhost:8080"

  val spspUrl = "https://money-load.ilpv4.dev/"

  val spspRoutePrefix = "test.jc.money"

  val ingressAccount = "lt-lb-ingress"

  val javaSpspAccount = "lt-spsp-java"

  val javaSpspAddress = s"""${spspRoutePrefix}.${javaSpspAccount}"""

  val javaSpspPaymentPointer = s"""$$money-load.ilpv4.dev/${javaSpspAccount}"""

  val fulfillLoopbackAccount = "lt-lb-fulfiller"

  val rejectLoopbackAccount = "lt-lb-rejector"

  val fulfillLoopbackAddress = s"""${spspRoutePrefix}.${fulfillLoopbackAccount}"""

  val rejectLoopbackAddress = s"""${spspRoutePrefix}.${rejectLoopbackAccount}"""

  val basicAuthHeader = "Basic YWRtaW46cGFzc3dvcmQ="
}
