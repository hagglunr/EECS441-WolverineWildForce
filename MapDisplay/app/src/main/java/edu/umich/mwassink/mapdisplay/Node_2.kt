package edu.umich.wwf

open class Node(
    var entranceNum: Int? =null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var neighbors: ArrayList<Node>? = null
)