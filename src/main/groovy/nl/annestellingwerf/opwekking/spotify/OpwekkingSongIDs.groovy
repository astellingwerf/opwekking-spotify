package nl.annestellingwerf.opwekking.spotify

import com.wrapper.spotify.models.Album
import com.wrapper.spotify.models.Artist
import com.wrapper.spotify.models.Page
import com.wrapper.spotify.models.SimpleAlbum
import groovy.transform.Memoized
import nl.annestellingwerf.spotify.SpotifyExtensions

import java.util.regex.Matcher
import java.util.regex.Pattern

import static com.wrapper.spotify.Api.DEFAULT_API
import static nl.annestellingwerf.opwekking.spotify.SpotifyClientConstants.MARKET

class OpwekkingSongIDs {
    private static final Pattern SONG_NUMBER_PATTERN = ~/^.*\((\d+)\)( - Live)?$/
    private static final String OPWEKKING_ARTIST_NAME = 'Stichting Opwekking'

    private static String getOpwekkingArtistId() {
        use(SpotifyExtensions) {
            final Page<Artist> artistSearchResult = DEFAULT_API
                    .searchArtists(OPWEKKING_ARTIST_NAME)
                    .market(MARKET)
                    .limit(2)
                    .<Page<Artist>> invoke()
            switch (artistSearchResult.total) {
                case 1:
                    return artistSearchResult.items.first().id
                case 0:
                    throw new IllegalStateException("No matches for '$OPWEKKING_ARTIST_NAME'")
                default:
                    throw new IllegalStateException("Too many matches for '$OPWEKKING_ARTIST_NAME'")
            }
        }
    }

    @Memoized
    public static Map<String, Map<Integer, String>> fetch() {
        Map<String, Map<Integer, String>> allSongs = [:].withDefault { key -> [:] }

        use(SpotifyExtensions) {
            DEFAULT_API.iterateAlbums(MARKET, opwekkingArtistId) { Page<SimpleAlbum> page ->
                page.items.each { SimpleAlbum album ->
                    DEFAULT_API.getAlbum(album.id).<Album> invoke().tracks.items.each {
                        Matcher m = SONG_NUMBER_PATTERN.matcher(it.name)
                        if (m.matches()) {
                            allSongs[normalizeAlbumName(album)][m.group(1) as int] = it.uri
                        }
                    }
                }
            }
        }

        return allSongs
    }

    private static String normalizeAlbumName(SimpleAlbum album) {
        switch (album.name.takeWhile { !Character.isDigit(it) }
                .trim()
                .toLowerCase()
                .replaceAll(~/ kerst$/, '')) {
            case 'opwekkingsliederen': return 'Opwekking'
            case 'life@opwekking': return 'Tieners'
            case 'opwekking kids': return 'Kinderen'
        }
    }
}
