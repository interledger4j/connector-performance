package util

import java.io.ByteArrayInputStream

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import org.interledger.codecs.ilp.InterledgerCodecContextFactory
import org.interledger.core.{InterledgerPreparePacket, InterledgerResponsePacket}

object ConnectorRequests {

  def ilp(accountName: String, bearer: String, prepare: InterledgerPreparePacket): ChainBuilder = {
    val requestName = s"""[Connector] Send ILP prepare for ${prepare.getAmount.toString()} from ${accountName} to ${prepare.getDestination.getValue}"""
    exec(
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
    )
  }

  def createChildAccount(accountName: String, bearer: String = "shh"): ChainBuilder = exec(
    http("[Connector] Creating child account: " + accountName)
      .post("/accounts")
      .header("accept", "application/json")
      .header("content-type", "application/json")
      .header("Authorization", Config.basicAuthHeader)
      .body(StringBody(
        s"""
          |{
          |  "accountId": "${accountName}",
          |  "accountRelationship": "CHILD",
          |  "linkType": "ILP_OVER_HTTP",
          |  "assetCode": "USD",
          |  "assetScale": "4",
          |  "customSettings": {
          |        "ilpOverHttp.incoming.auth_type": "SIMPLE",
          |        "ilpOverHttp.incoming.simple.auth_token": "${bearer}",
          |        "ilpOverHttp.outgoing.auth_type": "SIMPLE",
          |        "ilpOverHttp.outgoing.simple.auth_token": "${bearer}",
          |        "ilpOverHttp.outgoing.url": "${Config.spspUrl}/accounts/${accountName}/ilp"
          |   }
          |}
          |""".stripMargin)).asJson
      .check(status.in(201, 409))
  )

  def createLoopbackFulfillAccount(accountName: String): ChainBuilder = exec(
    http("[Connector] Creating loopback fulfill account: " + accountName)
      .post("/accounts")
      .header("accept", "application/json")
      .header("content-type", "application/json")
      .header("Authorization", Config.basicAuthHeader)
      .body(StringBody(
        s"""
           |{
           |  "accountId": "${accountName}",
           |  "accountRelationship": "CHILD",
           |  "linkType": "LOOPBACK",
           |  "assetCode": "XRP",
           |  "assetScale": "9"
           |}
           |""".stripMargin)).asJson
      .check(status.in(201, 409))
  )

  def createLoopbackRejectAccount(accountName: String): ChainBuilder = exec(
    http("[Connector] Creating loopback reject account: " + accountName)
      .post("/accounts")
      .header("accept", "application/json")
      .header("content-type", "application/json")
      .header("Authorization", Config.basicAuthHeader)
      .body(StringBody(
        s"""
           |{
           |  "accountId": "${accountName}",
           |  "accountRelationship": "CHILD",
           |  "linkType": "LOOPBACK",
           |  "assetCode": "XRP",
           |  "assetScale": "9",
           |  "customSettings": {
           |  	"simulatedRejectErrorCode":"T02"
           |  }
           |}
           |""".stripMargin)).asJson
      .check(status.in(201, 409))
  )

}
