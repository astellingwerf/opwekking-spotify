package nl.annestellingwerf.opwekking.playlist

import groovy.transform.TupleConstructor

import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = 'item')
@TupleConstructor
class PlaylistItem {
    String bundle
    int number
}
