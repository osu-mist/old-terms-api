package edu.oregonstate.mist.termsapi

import edu.oregonstate.mist.api.Configuration
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.InfoResource
import edu.oregonstate.mist.api.AuthenticatedUser
import edu.oregonstate.mist.api.BasicAuthenticator
import edu.oregonstate.mist.termsapi.dao.TermsDAO
import edu.oregonstate.mist.termsapi.dao.UtilHttp
import edu.oregonstate.mist.termsapi.health.BackendHealth
import edu.oregonstate.mist.termsapi.resources.TermsResource
import io.dropwizard.Application
import io.dropwizard.client.HttpClientBuilder
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.auth.AuthFactory
import io.dropwizard.auth.basic.BasicAuthFactory
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
        Resource.loadProperties()
        environment.jersey().register(new InfoResource())

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

        environment.jersey().register(
                AuthFactory.binder(
                        new BasicAuthFactory<AuthenticatedUser>(
                                new BasicAuthenticator(configuration.getCredentialsList()),
                                'TermsApplication',
                                AuthenticatedUser.class)))

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
