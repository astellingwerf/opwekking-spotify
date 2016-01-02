package nl.annestellingwerf

import javax.ws.rs.core.Response

class PublicAPI extends Service {

    @Override
    protected Response build(Response.ResponseBuilder responseBuilder) {
        super.build(enhanceResponse(responseBuilder))
    }

    protected Response.ResponseBuilder enhanceResponse(Response.ResponseBuilder responseBuilder) {
        addExtraDefaultHeaders(responseBuilder)
    }

    protected Response.ResponseBuilder addExtraDefaultHeaders(Response.ResponseBuilder responseBuilder) {
        responseBuilder.header('Access-Control-Allow-Methods', '*').header('Access-Control-Allow-Origin', '*')
    }
}
