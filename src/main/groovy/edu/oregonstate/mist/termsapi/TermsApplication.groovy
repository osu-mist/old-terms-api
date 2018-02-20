package edu.oregonstate.mist.termsapi

import edu.oregonstate.mist.api.Application
import edu.oregonstate.mist.termsapi.dao.TermsDAO
import edu.oregonstate.mist.termsapi.dao.UtilHttp
import edu.oregonstate.mist.termsapi.health.BackendHealth
import edu.oregonstate.mist.termsapi.resources.TermsResource
import io.dropwizard.client.HttpClientBuilder
import io.dropwizard.setup.Environment
import org.apache.http.client.HttpClient

/**
 * Main application class.
 */
class TermsApplication extends Application<TermsConfiguration> {
    /**
     * Parses command-line arguments and runs the application.
     *
     * @param configuration
     * @param environment
     */
    @Override
    public void run(TermsConfiguration configuration, Environment environment) {
        this.setup(configuration, environment)

        // the httpclient from DW provides with many metrics and config options
        HttpClient httpClient = new HttpClientBuilder(environment)
                .using(configuration.getHttpClientConfiguration())
                .build("backend-http-client")

        // reusable UtilHttp instance for both DAO and healthcheck
        UtilHttp utilHttp = new UtilHttp(configuration.terms)

        // setup dao
        TermsDAO termsDAO = new TermsDAO(utilHttp, httpClient)

        def termResource = new TermsResource(termsDAO)
        termResource.setEndpointUri(configuration.api.endpointUri)
        environment.jersey().register(termResource)

        // healthchecks
        environment.healthChecks().register("backend",
                new BackendHealth(utilHttp, httpClient))
    }

    /**
     * Instantiates the application class with command-line arguments.
     *
     * @param arguments
     * @throws Exception
     */
    public static void main(String[] arguments) throws Exception {
        new TermsApplication().run(arguments)
    }
}
