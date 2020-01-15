package util

import org.interledger.connector.accounts.AccountId
import org.interledger.connector.routing.StaticRoute
import org.interledger.core.InterledgerAddressPrefix

object Routes {

  /**
   * InterledgerAddressPrefix routePrefix = spspAddressPrefix.with(returnedAccountSettings.accountId().value());
   *       connectorRoutesClient.createStaticRoute(
   *         routePrefix.getValue(),
   *         StaticRoute.builder()
   * .routePrefix(routePrefix)
   * .nextHopAccountId(returnedAccountSettings.accountId())
   * .build()
   * );
   */

  val fulfillLoopbackRoute = StaticRoute.builder()
    .routePrefix(InterledgerAddressPrefix.of(Config.fulfillLoopbackAddress))
    .nextHopAccountId(AccountId.of(Config.fulfillLoopbackAccount))
    .build()

}
