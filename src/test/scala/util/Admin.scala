package util

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.net.HttpHeaders.AUTHORIZATION
import okhttp3.HttpUrl
import org.interledger.connector.client.{ConnectorAdminClient, ConnectorRoutesClient}
import org.interledger.connector.jackson.ObjectMapperFactory
import org.interledger.spsp.client.SimpleSpspClient

object Admin {

  val objectMapper : ObjectMapper = ObjectMapperFactory.create();

  val accountClient = ConnectorAdminClient.construct(HttpUrl.parse(Config.javaConnectorUrl),
    template => template.header(AUTHORIZATION, Config.basicAuthHeader))

  val routeClient = ConnectorRoutesClient.construct(HttpUrl.parse(Config.javaConnectorUrl),
    template => template.header(AUTHORIZATION, Config.basicAuthHeader))

  val spspClient = new SimpleSpspClient()

}
