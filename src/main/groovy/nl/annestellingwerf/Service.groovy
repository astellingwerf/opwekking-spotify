package nl.annestellingwerf

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response

class Service {

    @Context
    protected HttpServletRequest currentRequest;

    protected Response build(Response.ResponseBuilder responseBuilder) {
        responseBuilder.build()
    }

    protected HttpSession getSession() {
        currentRequest.getSession(true)
    }
}
