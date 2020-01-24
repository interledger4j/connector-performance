package util

import io.gatling.core.Predef._

object IlpChecks {

  val REJECTED_T00 = jsonPath("$.code.code").is("T00")
  val REJECTED_T02 = jsonPath("$.code.code").is("T02")
  val FULFILLED = jsonPath("$.fulfillment").exists

}
