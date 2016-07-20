package edu.oregonstate.mist.termsapi.resources

import com.codahale.metrics.annotation.Timed
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.api.AuthenticatedUser
import edu.oregonstate.mist.termsapi.dao.TermsDAO
import io.dropwizard.auth.Auth
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.ResponseBuilder
import javax.ws.rs.core.MediaType

@Path('/terms/')
class TermsResource extends Resource {
    Logger logger = LoggerFactory.getLogger(TermsResource.class)

    private TermsDAO termsDAO

    TermsResource(TermsDAO termsDAO) {
        this.termsDAO = termsDAO
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public Response getAllTerms(@Auth AuthenticatedUser _) {
        try {
            def response = termsDAO.getTerms(getPageNumber(), getPageSize())
            //@todo: these params are calculated twice :(
            ResultObject resultObject = new ResultObject(data: response.data)
            resultObject.links = setPaginationLinks(response.sourcePagination)

            ok(resultObject).build()
        } catch (Exception e) {
            logger.error("Exception while getting all terms", e)
            internalServerError("Woot you found a bug for us to fix!").build()
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path('{id: [0-9a-zA-Z]+}')
    @Timed
    public Response getTerm(@Auth AuthenticatedUser _, @PathParam('id') String term) {
        try {
            def response = termsDAO.getTerm(term)
            ResultObject resultObject = new ResultObject(data: response)
            ok(resultObject).build()
        } catch (Exception e) {
            logger.error("Exception while getting a term", e)
            internalServerError("Woot you found a bug for us to fix!").build()
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path('/open')
    @Timed
    public Response getOpenTerms(@Auth AuthenticatedUser _) {
        try {
            def response = termsDAO.getOpenTerms()
            ResultObject resultObject = new ResultObject(data: response)
            ok(resultObject).build()
        } catch (Exception e) {
            logger.error("Exception while getting open terms", e)
            internalServerError("Woot you found a bug for us to fix!").build()
        }
    }

    private HashMap setPaginationLinks(def sourcePagination) {
        def links = [:]
        // If no results were found, no need to add links
        if (!sourcePagination?.totalCount) {
            return
        }

        Integer pageNumber = getPageNumber()
        Integer pageSize = getPageSize()
        def urlParams = [
                "pageSize": pageSize,
                "pageNumber": pageNumber
        ]

        int lastPage = Math.ceil(sourcePagination.totalCount / pageSize)

        links["self"] = getPaginationUrl(urlParams)
        urlParams.pageNumber = 1
        links["first"] = getPaginationUrl(urlParams)
        urlParams.pageNumber = lastPage
        links["last"] = getPaginationUrl(urlParams)

        if (pageNumber > DEFAULT_PAGE_NUMBER) {
            urlParams.pageNumber = pageNumber - 1
            links["prev"] = getPaginationUrl(urlParams)
        } else {
            links["prev"] = null
        }

        if (sourcePagination?.totalCount > (pageNumber * pageSize)) {
            urlParams.pageNumber = pageNumber + 1
            links["next"] = getPaginationUrl(urlParams)
        } else {
            links["next"] = null
        }

        links
    }
}
