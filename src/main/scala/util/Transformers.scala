package util

import java.io.ByteArrayInputStream

import io.gatling.core.Predef._
import io.gatling.http.ResponseTransformer
import io.gatling.http.response.StringResponseBody
import org.interledger.codecs.ilp.InterledgerCodecContextFactory
import org.interledger.core.InterledgerResponsePacket
import util.ConnectorRequests.{logger, objectMapper}

object Transformers {

  val convertIlpResponseToJson: ResponseTransformer = (session, response) => {
    val context = InterledgerCodecContextFactory.oer()
    val bas = new ByteArrayInputStream(response.body.bytes)
    val responsePacket: InterledgerResponsePacket = context.read(classOf[InterledgerResponsePacket], bas)
    val ilpJson = objectMapper.writeValueAsString(responsePacket)
    logger.trace("[Transformers] Deserialized ILP response to {}", ilpJson)
    response.copy(body = new StringResponseBody(ilpJson, response.charset))
  }

}
