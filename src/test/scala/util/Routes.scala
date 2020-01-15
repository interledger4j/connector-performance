package util

import org.interledger.connector.accounts.AccountId
import org.interledger.connector.routing.StaticRoute
import org.interledger.core.InterledgerAddressPrefix

object Routes {

  val fulfillLoopbackRoute = StaticRoute.builder()
    .routePrefix(InterledgerAddressPrefix.of(Config.fulfillLoopbackAddress))
    .nextHopAccountId(AccountId.of(Config.fulfillLoopbackAccount))
    .build()

  val rejectLoopbackRoute = StaticRoute.builder()
    .routePrefix(InterledgerAddressPrefix.of(Config.rejectLoopbackAddress))
    .nextHopAccountId(AccountId.of(Config.rejectLoopbackAccount))
    .build()
}
