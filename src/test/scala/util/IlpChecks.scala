package util

import io.gatling.core.Predef._

object IlpChecks {

  val CODE_T00 = jsonPath("$.code.code").is("T00")
}
