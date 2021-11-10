package edu.umich.mwassink.mapdisplay

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

open class Node (
    val entranceNum: Int? = null,
    val id: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val neighbors: ArrayList<Int>? = null
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

    fun getManhattanHeuristicValue(startNode: Node, endNode: Node): Double {
        if (startNode.latitude == null || startNode.longitude == null || endNode.latitude == null || endNode.longitude == null) {
            return 0.0
        }
        return abs(startNode.latitude - endNode.latitude) + abs(startNode.longitude - endNode.longitude)
    }
}