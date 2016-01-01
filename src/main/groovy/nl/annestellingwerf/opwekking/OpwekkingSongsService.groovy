package nl.annestellingwerf.opwekking

import nl.annestellingwerf.Service
import nl.annestellingwerf.opwekking.spotify.OpwekkingSongs

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

@Path("/opwekking")
class OpwekkingSongsService extends Service {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path('/bundle/{bundle}/number/{number}')
    public Response getSong(@PathParam('bundle') String bundle, @PathParam('number') int number) {
        Map<String, ?> song = OpwekkingSongs.fetch()?.getAt(bundle)?.getAt(number)

        if (song) {
            build ok(toJson(song))
        } else {
            build status(NOT_FOUND).entity(toJson(error: 'Song cannot be found.'))
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path('/bundle/{bundle}/{property}s')
    public Response getBundle(@PathParam('bundle') String bundle, @PathParam('property') String property) {
        Map<Integer, Map<String, ?>> trackIds = OpwekkingSongs.fetch()?.getAt(bundle)

        if (trackIds) {
            build ok(toJson(trackIds.collectEntries { k, v -> [(k): v[property]] }.sort()))
        } else {
            build status(NOT_FOUND).entity(toJson(error: 'Bundle cannot be found.'))
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path('/bundle/')
    public Response getBundles() {
        Set<String> bundles = OpwekkingSongs.fetch().keySet()

        if (bundles) {
            build ok(toJson(bundles.sort()))
        } else {
            build status(NOT_FOUND).entity(toJson(error: 'Bundles cannot be found.'))
        }
    }

}
