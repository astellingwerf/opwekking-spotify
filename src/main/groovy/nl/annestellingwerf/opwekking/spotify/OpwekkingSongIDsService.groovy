package nl.annestellingwerf.opwekking.spotify

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import static groovy.json.JsonOutput.toJson
import static javax.ws.rs.core.Response.Status.NOT_FOUND

@Path("/opwekking/spotify")
class OpwekkingSongIDsService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/bundle/{bundle}/number/{number}")
    public Response getSong(@PathParam("bundle") String bundle, @PathParam("number") int number) {
        String trackId = OpwekkingSongIDs.fetch()?.getAt(bundle)?.getAt(number)

        if (trackId) {
            Response.ok(toJson(trackId: trackId)).build()
        } else {
            Response.status(NOT_FOUND).entity(toJson(error: "Song cannot be found")).build()
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/bundle/{bundle}")
    public Response getBundle(@PathParam("bundle") String bundle) {
        def trackIds = OpwekkingSongIDs.fetch()?.getAt(bundle)

        if (trackIds) {
            Response.ok(toJson(trackIds.sort())).build()
        } else {
            Response.status(NOT_FOUND).entity(toJson(error: "Bundle cannot be found")).build()
        }
    }
}
