package util

object Config {

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
