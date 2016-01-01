package nl.annestellingwerf

import javax.ws.rs.core.Response

/**
 * Created by astellingwerf on 31-12-15.
 */
class Service {
    protected final Response build(Response.ResponseBuilder responseBuilder) {
        enhanceResponse(responseBuilder).build()
    }

    protected Response.ResponseBuilder enhanceResponse(Response.ResponseBuilder responseBuilder) {
        addExtraDefaultHeaders(responseBuilder)
    }

    protected Response.ResponseBuilder addExtraDefaultHeaders(Response.ResponseBuilder responseBuilder) {
        responseBuilder.header('Access-Control-Allow-Methods', '*').header('Access-Control-Allow-Origin', '*')
    }
}
