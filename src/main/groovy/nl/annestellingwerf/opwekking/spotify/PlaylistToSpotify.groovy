package nl.annestellingwerf.opwekking.spotify

import com.wrapper.spotify.Api
import com.wrapper.spotify.models.Playlist as SpotifyPlaylist
import com.wrapper.spotify.models.User
import nl.annestellingwerf.Service
import nl.annestellingwerf.opwekking.playlist.Playlist
import nl.annestellingwerf.spotify.SpotifyExtensions

import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.core.Response

import static groovy.json.JsonOutput.toJson
import static javax.ws.rs.core.Response.Status.NOT_FOUND
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED
import static javax.ws.rs.core.Response.ok
import static javax.ws.rs.core.Response.status
import static nl.annestellingwerf.opwekking.playlist.PlaylistService.PLAYLIST
import static nl.annestellingwerf.spotify.AuthenticateService.SESSION_ATTR_SPOTIFY_API_INSTANCE

@Path('/opwekking/playlist/to-spotify')
class PlaylistToSpotify extends Service {

    @POST
    public Response create() {
        Playlist p = session.getAttribute(PLAYLIST);
        if (!p) {
            return build(status(NOT_FOUND).entity(toJson(error: 'No playlist specified.')))
        }

        Api api = session.getAttribute(SESSION_ATTR_SPOTIFY_API_INSTANCE)
        if (!api) {
            return build(status(UNAUTHORIZED).entity(toJson(error: 'Not authenticated yet.')))
        }

        use(SpotifyExtensions) {
            User user = api.me.invoke()
            SpotifyPlaylist spotifyPlaylist = api.createPlaylist(user.id, p.name).publicAccess(false).invoke()
            api.addTracksToPlaylist(
                    user.id,
                    spotifyPlaylist.id,
                    p.items.collect { pli -> OpwekkingSongs.fetch()[pli.bundle][pli.number]['spotifyTrackId'] }
            ).invoke()
        }

        build ok()
    }
}
