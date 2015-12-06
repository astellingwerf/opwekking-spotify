package nl.annestellingwerf.opwekking.spotify

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import static groovy.json.JsonOutput.toJson
import static javax.ws.rs.core.Response.Status.NOT_FOUND
import static javax.ws.rs.core.Response.ok
import static javax.ws.rs.core.Response.status

@Path("/opwekking/spotify")
class OpwekkingSongIDsService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/bundle/{bundle}/number/{number}")
    public Response getSong(@PathParam("bundle") String bundle, @PathParam("number") int number) {
        String trackId = OpwekkingSongIDs.fetch()?.getAt(bundle)?.getAt(number)

        if (trackId) {
            ok(toJson(trackId: trackId)).build()
        } else {
            status(NOT_FOUND).entity(toJson(error: 'Song cannot be found.')).build()
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/bundle/{bundle}")
    public Response getBundle(@PathParam("bundle") String bundle) {
        Map<Integer, String> trackIds = OpwekkingSongIDs.fetch()?.getAt(bundle)

        if (trackIds) {
            ok(toJson(trackIds.sort())).build()
        } else {
            status(NOT_FOUND).entity(toJson(error: 'Bundle cannot be found.')).build()
        }
    }
}
