package util

import com.google.common.net.HttpHeaders.AUTHORIZATION
import okhttp3.HttpUrl
import org.interledger.connector.client.ConnectorAdminClient

object Admin {

  val client = ConnectorAdminClient.construct(HttpUrl.parse(Config.javaConnectorUrl),
    template => template.header(AUTHORIZATION, Config.basicAuthHeader))

}
