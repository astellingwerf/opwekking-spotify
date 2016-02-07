package nl.annestellingwerf.opwekking.playlist

import nl.annestellingwerf.Service

import javax.ws.rs.*
import javax.ws.rs.core.Response

import static groovy.json.JsonOutput.toJson
import static javax.ws.rs.core.MediaType.APPLICATION_JSON
import static javax.ws.rs.core.Response.ok

@Path('/opwekking/playlist')
class PlaylistService extends Service {

    public static final String PLAYLIST = 'playlist'

    Playlist getPlaylist() {
        return session.getAttribute(PLAYLIST) ?: (this.playlist = new Playlist(name: 'Komende zondag', items: [
        ]))
    }

    void setPlaylist(Playlist playlist) {
        session.setAttribute(PLAYLIST, playlist)
    }

    @GET
    @Produces(APPLICATION_JSON)
    public Response get() {
        build ok(toJson(playlist))
    }

    @PUT
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response put(Playlist playlist) {
        this.playlist = playlist
        return get()
    }
}
