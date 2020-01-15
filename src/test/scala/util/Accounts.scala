package util

import org.interledger.connector.accounts.{AccountId, AccountRelationship, AccountSettings}
import org.interledger.link.LinkType

import scala.collection.JavaConverters._

object Accounts {

  val ingress = AccountSettings.builder()
    .accountId(AccountId.of(Config.ingressAccount))
    .accountRelationship(AccountRelationship.CHILD)
    .linkType(LinkType.of("ILP_OVER_HTTP"))
    .assetCode("USD")
    .assetScale(4)
    .customSettings(Map(
      "ilpOverHttp.incoming.auth_type"-> "SIMPLE",
      "ilpOverHttp.incoming.simple.auth_token"-> "shh",
      "ilpOverHttp.outgoing.auth_type"-> "SIMPLE",
      "ilpOverHttp.outgoing.simple.auth_token"-> "shh",
      "ilpOverHttp.outgoing.url"-> s"""${Config.spspUrl}/accounts/${Config.ingressAccount}/ilp""",
    ).asJava)
    .build()

  val fulfillLoopback = AccountSettings.builder()
    .accountId(AccountId.of(Config.fulfillLoopbackAccount))
    .accountRelationship(AccountRelationship.CHILD)
    .linkType(LinkType.of("LOOPBACK"))
    .assetCode("XRP")
    .assetScale(9)
    .build()

  val rejectLoopback = AccountSettings.builder()
    .accountId(AccountId.of(Config.rejectLoopbackAccount))
    .accountRelationship(AccountRelationship.CHILD)
    .linkType(LinkType.of("LOOPBACK"))
    .assetCode("XRP")
    .assetScale(9)
    .customSettings(Map(
      "simulatedRejectErrorCode"->"T02"
    ).asJava)
    .build()
}
