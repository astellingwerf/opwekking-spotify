package nl.annestellingwerf.opwekking.playlist

import groovy.transform.TupleConstructor

import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = 'playlist')
@TupleConstructor
class Playlist {
    String name
    List<PlaylistItem> items = []
}
