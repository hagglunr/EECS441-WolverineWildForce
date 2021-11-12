package edu.umich.mwassink.mapdisplay

import java.io.Serializable

class Connections (nodes: FloatArray, conns: IntArray): Serializable{

    var Nodes: FloatArray
    var Connections: IntArray

    init {
        Nodes = nodes
        Connections = conns
    }
}