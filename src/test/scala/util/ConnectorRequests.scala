package util

import java.io.ByteArrayInputStream

import com.fasterxml.jackson.databind.ObjectMapper
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import io.gatling.http.response.StringResponseBody
import org.interledger.codecs.ilp.InterledgerCodecContextFactory
import org.interledger.connector.jackson.ObjectMapperFactory
import org.interledger.core.{InterledgerPreparePacket, InterledgerResponsePacket}
import org.slf4j.LoggerFactory

object ConnectorRequests {

  val logger = LoggerFactory.getLogger(getClass)

  val objectMapper : ObjectMapper = ObjectMapperFactory.create();

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
      .transformResponse((session, response) => {
        val context = InterledgerCodecContextFactory.oer()
        val bas = new ByteArrayInputStream(response.body.bytes)
        val responsePacket: InterledgerResponsePacket = context.read(classOf[InterledgerResponsePacket], bas)
        val ilpJson = objectMapper.writeValueAsString(responsePacket)
        logger.trace("[Connector Requests] Deserialized ILP response to {}", ilpJson)
        response.copy(body = new StringResponseBody(ilpJson, response.charset))
      })
  }
}
