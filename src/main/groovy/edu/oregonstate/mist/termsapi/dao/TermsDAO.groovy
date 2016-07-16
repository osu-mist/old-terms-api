package edu.oregonstate.mist.termsapi.dao

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.termsapi.core.Attributes
import org.apache.http.HttpEntity
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.util.EntityUtils

class TermsDAO {
    private UtilHttp utilHttp
    private HttpClient httpClient
    private ObjectMapper mapper = new ObjectMapper()

    TermsDAO(UtilHttp utilHttp, HttpClient httpClient) {
        this.httpClient = httpClient
        this.utilHttp = utilHttp
    }

    /**
     * Performs class search and returns results in jsonapi format
     *
     * @param term
     * @param subject
     * @param courseNumber
     * @param q
     * @param pageNumber
     * @param pageSize
     * @return
     */
    public def getData(Integer pageNumber, Integer pageSize) {
        CloseableHttpResponse response
        def data = []
        def sourcePagination = [totalCount: 0, pageOffset: 0, pageMaxSize: 0]

        try {
            def query = getQueryMap(pageNumber, pageSize)
            response = utilHttp.sendGet(query, httpClient)

            HttpEntity entity = response.getEntity()
            def entityString = EntityUtils.toString(entity)

            data = this.mapper.readValue(entityString,
                    new TypeReference<List<HashMap>>() {
                })

            sourcePagination = getSourcePagination(response.getAllHeaders())
            EntityUtils.consume(entity)
        } finally {
            response?.close()
        }

        [data: getFormattedData(data), sourcePagination: sourcePagination]
    }

    private static getSourcePagination(headers) {
        def headerMap = [:]
        headers.each {
            headerMap[it.name] = it.value
        }

        [
            totalCount: headerMap['X-hedtech-totalCount']?.toInteger(),
            pageOffset: headerMap['X-hedtech-pageOffset']?.toInteger(),
            pageMaxSize: headerMap['X-hedtech-pageMaxSize']?.toInteger()
        ]
    }

    /**
     * Takes the data from the backend and formats it based on the swagger spec.
     *
     * @param data
     * @return
     */
    private static List<ResourceObject> getFormattedData(def data) {
        List<ResourceObject> result = new ArrayList<ResourceObject>()

        data.each {
            Attributes attributes = new Attributes(
                    code:               it.code,
                    description:        it.description,
                    startDate:          it.startDate,
                    endDate:            it.endDate,
                    financialAidYear:   it.financialAidProcessingYear,
                    housingStartDate:   it.housingStartDate,
                    housingEndDate:     it.housingEndDate
            )

            result << new ResourceObject(id: it.code, type: 'term',
                    attributes: attributes)
        }

        result
    }

    /**
     * Parses out the parameters and adds them to a map if they are not empty
     *
     * @param term
     * @param subject
     * @param courseNumber
     * @param q
     * @param pageNumber
     * @param pageSize
     * @return
     */
    private LinkedHashMap getQueryMap(Integer pageNumber, Integer pageSize) {
        def query = [offset:0]

        if (pageNumber && pageNumber > 1) {
            query['offset'] = pageSize * pageNumber
        }
        if (pageSize) {
            query['max'] = pageSize
        }
        query
    }
}
