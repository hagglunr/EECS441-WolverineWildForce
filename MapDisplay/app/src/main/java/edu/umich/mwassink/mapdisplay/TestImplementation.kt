package edu.umich.mwassink.mapdisplay

fun main() {
    var pathgen = PathGenerator()
    var allNodes = pathgen.getNodes("BBB")
    var fastestPath = pathgen.getFastestPath("BBB", allNodes[0], allNodes[50])
    print("Order of Nodes:\n")
    for (i in 0 until fastestPath.size) {
        print(fastestPath[i].id as String + ", ")
    }
}