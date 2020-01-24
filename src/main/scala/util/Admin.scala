package util

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.net.HttpHeaders.AUTHORIZATION
import feign.FeignException
import okhttp3.HttpUrl
import org.interledger.connector.client.ConnectorAdminClient
import org.interledger.connector.jackson.ObjectMapperFactory
import org.interledger.connector.routing.StaticRoute
import org.interledger.spsp.client.SimpleSpspClient

object Admin {

  val objectMapper : ObjectMapper = ObjectMapperFactory.create();

  val client = ConnectorAdminClient.construct(HttpUrl.parse(Config.javaConnectorUrl),
    template => template.header(AUTHORIZATION, Config.basicAuthHeader))

  val spspClient = new SimpleSpspClient()

  def safeCreateStaticRoute(address: String, route: StaticRoute) = {
    try {
      client.createStaticRoute(address, route)
    }
    catch {
      case e: FeignException => {
        if (e.status() != 409) {
          throw e
        }
      }
    }
  }

}
