package util

object Config {

  val javaConnectorUrl = "https://jc.ilpv4.dev"
//  val javaConnectorUrl = "http://localhost:8080"

  val spspUrl = "https://money.ilpv4.dev"

  val spspRoutePrefix = "test.jc.money"

  val ingressAccount = "lt-lb-ingress"

  val fulfillLoopbackAccount = "lt-lb-fulfiller"

  val fulfillLoopbackAddress = s"""${spspRoutePrefix}.${fulfillLoopbackAccount}"""

  val rejectLoopbackAddress = s"""${spspRoutePrefix}.${rejectLoopbackAccount}"""

  val rejectLoopbackAccount = "lt-lb-rejector"

  val basicAuthHeader = "Basic YWRtaW46cGFzc3dvcmQ="
}
