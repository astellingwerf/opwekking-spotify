package nl.annestellingwerf.spotify

import com.wrapper.spotify.Api
import com.wrapper.spotify.methods.Request
import com.wrapper.spotify.models.Page
import com.wrapper.spotify.models.SimpleAlbum

import java.math.RoundingMode

class SpotifyExtensions {

    static final int itemsPerPage = 10

    public static <T> void iteratePages(Closure<Page<T>> getter, Closure action) {
        int pageCount = (getter(0).total / itemsPerPage).setScale(0, RoundingMode.CEILING) as int
        (0..pageCount)
                .collect { getter(it) }
                .each action
    }

    public static Page<SimpleAlbum> getAlbums(Api api, String market, String artistId, int i) {
        invoke(api.getAlbumsForArtist(artistId)
                .market(market)
                .limit(itemsPerPage)
                .offset(i * itemsPerPage))
    }

    public static void iterateAlbums(Api api, String market, String artistId, Closure action) {
        iteratePages(SpotifyExtensions.&getAlbums.curry(api, market, artistId), action)
    }

    public static <T> T invoke(Request.Builder builder) {
        builder.build().get()
    }
}
