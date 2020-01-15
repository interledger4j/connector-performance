package util

object Config {

  val javaConnectorUrl = "https://jc.ilpv4.dev"

  val spspUrl = "https://money.ilpv4.dev"

  val ingressAccount = "lt-lb-ingress"

  val fulfillLoopbackAccount = "lt-lb-fulfiller"

  val fulfillLoopbackAddress = "test.jc.money.lt-lb-fulfiller"

  val rejectLoopbackAccount = "lt-lb-rejector"

  val basicAuthHeader = "Basic YWRtaW46cGFzc3dvcmQ="
}
