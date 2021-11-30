package edu.umich.mwassink.mapdisplay

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

enum class NodeType (val type: String) {
    ENTRANCE ("Entrance"),
    PATH ("Path"),
    RESTROOM ("Restroom"),
    STAIRCASE ("Staircase"),
    ROOM ("Room")
}

open class Node (
//    val entranceNum: Int? = null,
    val name: String? = null,
    val id: Int? = null,
    val type: NodeType? = null,
    val floorNum: Int? = null,
    val longitude: Double? = null,
    val latitude: Double? = null,
    val neighbors: ArrayList<Int>? = null,
) {
    fun isSameAs(node: Node): Boolean {
        if (this.latitude == node.latitude && this.longitude == node.longitude) {
            return true
        }
        return false
    }

    fun distanceTo(node: Node): Double {
        if (this.latitude == null || this.longitude == null || node.latitude == null || node.longitude == null) {
            return Double.MAX_VALUE
        }
        return sqrt((this.latitude - node.latitude).pow(2) + (this.longitude - node.longitude).pow(2))
    }

    fun getManhattanHeuristicValue(endNode: Node): Double {
        if (this.latitude == null || this.longitude == null || endNode.latitude == null || endNode.longitude == null) {
            return 0.0
        }
        return abs(this.latitude - endNode.latitude) + abs(this.longitude - endNode.longitude)
    }

    fun print() {
        print("Node $name:\n")
        print("ID: $id\n")
        print("Type: $type\n")
        print("Coords: ($latitude, $longitude)\n")
        print("Floor: $floorNum\n")
        print("Neighbors: ")
        printNeighbors()
    }

    private fun printNeighbors() {
        if (this.neighbors == null) {
            print("None\n")
        }
        print("[")
        for (i in 0 until this.neighbors!!.size) {
            print("$i ")
        }
        print("]\n")
    }
}
