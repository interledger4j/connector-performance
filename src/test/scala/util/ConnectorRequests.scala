package util

import java.io.ByteArrayInputStream

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import org.interledger.codecs.ilp.InterledgerCodecContextFactory
import org.interledger.core.{InterledgerPreparePacket, InterledgerResponsePacket}

object ConnectorRequests {

  def ilp(accountName: String, bearer: String, prepare: InterledgerPreparePacket): HttpRequestBuilder = {
    val requestName = s"""[Connector] Send ILP prepare for ${prepare.getAmount.toString()} from ${accountName} to ${prepare.getDestination.getValue}"""
    http(requestName)
      .post("/accounts/" + accountName + "/ilp")
      .header("accept", "application/octet-stream")
      .header("content-type", "application/octet-stream")
      .header("Authorization", "Bearer " + bearer)
      .body(ByteArrayBody(Prepare.serialize(prepare)))
      .check(status.is(200))
      .check(bodyBytes.exists)
      .check(
        bodyBytes.transform((byteArray, session) => {
          val context = InterledgerCodecContextFactory.oer()
          val bas = new ByteArrayInputStream(byteArray)
          val response: InterledgerResponsePacket = context.read(classOf[InterledgerResponsePacket], bas)
          response
        })
        .saveAs("ilpResponse")
    )
  }
}
