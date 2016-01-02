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

class OpwekkingSongs {
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
    public synchronized static Map<String, Map<Integer, String>> fetch() {
        Map<String, Map<Integer, String>> allSongs = [:].withDefault { key -> [:] }

        use(SpotifyExtensions) {
            List<String> albumIds = []
            DEFAULT_API.iterateAlbums(MARKET, opwekkingArtistId) { Page<SimpleAlbum> page ->
                albumIds += page.items*.id
            }

            // getAlbums accepts at most 20 IDs at a time
            albumIds.collate(20).each { twentyAlbumIds ->
                DEFAULT_API.getAlbums(twentyAlbumIds).<List<Album>> invoke().each { Album album ->
                    album.tracks.items.each {
                        Matcher m = SONG_NUMBER_PATTERN.matcher(it.name)
                        if (m.matches()) {
                            allSongs[normalizeAlbumName(album)][m.group(1) as int] = [
                                    spotifyTrackId: it.uri,
                                    spotifyTrackPreviewUrl: it.getPreviewUrl(),
                                    title: it.name,
                                    albumCovers: album.images]
                        }
                    }
                }
            }
        }

        return allSongs
    }

    private static String normalizeAlbumName(Album album) {
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
