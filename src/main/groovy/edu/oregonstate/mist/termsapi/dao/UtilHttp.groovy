package edu.oregonstate.mist.termsapi.dao

import org.apache.http.HttpHeaders
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.BasicCredentialsProvider

class UtilHttp {
    private final Map<String, String> apiConfiguration

    private String backendPath

    UtilHttp(Map<String, String> apiConfiguration) {
        this.apiConfiguration = apiConfiguration
    }

    public CloseableHttpResponse sendGet(LinkedHashMap<String, Object> query,
                                         HttpClient httpClient) {
        BasicCredentialsProvider credsProvider = getCredentialsProvider()
        HttpClientContext context = getClientContext(credsProvider)
        URI uri = getBackendURI(query)

        HttpGet httpGet = new HttpGet(uri)
        setCommonHeaders(httpGet)

        httpClient.execute(httpGet, context)
    }

    private static void setCommonHeaders(HttpGet httpGet) {
        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        httpGet.setHeader(HttpHeaders.ACCEPT, "application/json")
    }

    private static HttpClientContext getClientContext(BasicCredentialsProvider credsProvider) {
        HttpClientContext context
        context = HttpClientContext.create()
        context.setCredentialsProvider(credsProvider)
        context
    }

    private BasicCredentialsProvider getCredentialsProvider() {
        CredentialsProvider credsProvider = new BasicCredentialsProvider()
        credsProvider.setCredentials(
                new AuthScope(backendHost, backendPort),
                new UsernamePasswordCredentials(backendUsername, backendPassword))
        credsProvider
    }

    private URI getBackendURI(LinkedHashMap<String, Integer> query) {
        URIBuilder uriBuilder = new URIBuilder()
                .setScheme(backendScheme)
                .setHost(backendHost)
                .setPort(backendPort)
                .setPath(getBackendPath())

        query.each { k, v ->
            uriBuilder.setParameter(k, v.toString())
        }

        //@todo: don't like this approach, but have to clear out backendpath for future requests
        this.backendPath = null

        URI uri = uriBuilder.build()
        uri
    }

    private String getBackendHost() {
        apiConfiguration.get("backendHost")
    }

    private String getBackendScheme() {
        apiConfiguration.get("backendScheme")
    }

    private Integer getBackendPort() {
        apiConfiguration.get("backendPort").toInteger()
    }

    public String getBackendPath() {
        if (this.backendPath) {
            return this.backendPath
        }

        apiConfiguration.get("backendPath")
    }

    void setBackendPath(String backendPath) {
        this.backendPath = backendPath
    }

    private String getBackendUsername() {
        apiConfiguration.get("backendUsername")
    }

    private String getBackendPassword() {
        apiConfiguration.get("backendPassword")
    }
}
