package edu.umich.mwassink.mapdisplay

class PathGenerator {
    // Use the A* algorithm
    // https://brilliant.org/wiki/a-star-search/
    fun getFastestPath(building: String, entranceNode: Node, destinationNode: Node) : ArrayList<Node> {
        var nodesList = ArrayList<Node>()
        // Init nodesList from database pull based on "building"

        var fastestPath = arrayListOf<Node>(entranceNode)
        var checkedList = ArrayList<Node>()
        var hList = ArrayList<Double>()
        for (node in nodesList) {
            hList.add(getManhattanHeuristicValue(node, destinationNode))
        }
        while (!checkedList.contains(destinationNode)) {
            // Refer to link above when implementing this, shouldn't be hard
        }
    }
}