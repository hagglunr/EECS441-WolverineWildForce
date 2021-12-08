package edu.umich.mwassink.mapdisplay

import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList

class PathGenerator {
    // Use the A* algorithm
    // https://www.researchgate.net/figure/A-search-algorithm-Pseudocode-of-the-A-search-algorithm-operating-with-open-and-closed_fig8_232085273

    // Returns list of Nodes in order of their intended traversal
    fun getFastestPath(building: String, nodesList: ArrayList<Node>, entranceNode: Node, destinationNode: Node) : ArrayList<Node> {
        // Sort nodesList by id (ascending)
        nodesList.sortBy { it.id }
        nodesList.forEach { print(" " + it.id) }

        var openList = ArrayList<Node>()
        var closedList = ArrayList<Node>()
        var parentList = ArrayList<Int>()
        for (i in 0 until nodesList.size) {
            parentList.add(0)
        }

        openList.add(entranceNode)

        // Calculate f(Node) = h(Node) + g(Node) from hList and gList
        // h(Node) = estimated cost from Node to Goal based on Manhattan Heuristic
        var hList = ArrayList<Double>()
        for (node in nodesList) {
            hList.add(node.getManhattanHeuristicValue(destinationNode))
        }

        // g(Node) = cost so far to reach Node
        var gList = ArrayList<Double>()
        for (i in 0 until nodesList.size) {
            gList.add(Double.MAX_VALUE)
        } // Every node costs unknown (MAX) to get to
        gList[0] = 0.0 // Start node costs zero to get to

        print("Starting A*\n")

        // Perform A* algorithm
        while (!openList.isEmpty()) {

            // Consider node with lowest f score in open list
            var checkNode = openList[0]
            var minFscore = Double.MAX_VALUE
            for (i in 0 until openList.size) {
                val idx = openList[i].id as Int
                if (hList[idx] + gList[idx] < minFscore) {
                    checkNode = openList[i]
                    minFscore = hList[idx] + gList[idx]
                }
            }

            print("Found new checkNode: " + checkNode.id + "\n")

            // Move checkNode from open to closed list
            openList.remove(checkNode)
            closedList.add(checkNode)

            // If node is destination node, we are finished
            if (checkNode.isSameAs(destinationNode)) {
                print("Reached destinationNode\n")
                break
            }

            // Check its neighboring nodes
            for (neighborID in checkNode.neighbors!!) {

                val neighborNode = nodesList[neighborID]

                // Calculate new relative cost
                val cost = gList[checkNode.id as Int] + checkNode.distanceTo(neighborNode)

                // If neighbor in open list and cost < g(neighbor), remove from open as new path is better
                var isInOpenList = false
                for (node in openList) {
                    if (node.isSameAs(neighborNode)) {
                        isInOpenList = true
                    }
                }
                if (isInOpenList && cost < gList[neighborID]) {
                    openList.remove(neighborNode)
                    parentList[neighborID] = checkNode.id as Int
                    print("Neighbor " + neighborID + " removed from openList\n")
                }

                // If neighbor in closed list and cost < g(neighbor), remove from closed list
                var isInClosedList = false
                for (node in closedList) {
                    if (node.isSameAs(neighborNode)) {
                        isInClosedList = true
                    }
                }
                if (isInClosedList && cost < gList[neighborID]) {
                    closedList.remove(neighborNode)
                    print("Neighbor " + neighborID + " removed from closedList\n")
                }

                // If neighbor not in open or closed, then add to open and update g(neighbor)
                if (!isInClosedList && !isInOpenList) {
                    openList.add(neighborNode)
                    gList[neighborID] = cost
                    parentList[neighborID] = checkNode.id as Int
                    print("Neighbor " + neighborID + " added to openList\n")
                }
            }
        }

        print("Finished A*\n")

        print("Starting backtracking\n")

        // Traverse from destination to start by parent list
        var path = ArrayList<Node>()
        var currentNode = destinationNode
        while (!currentNode.isSameAs(entranceNode)) {
            path.add(currentNode)
            print("Added node " + currentNode.id + " to backtracking path\n")
            if (nodesList[parentList[currentNode.id as Int]] == null) {

            }
            currentNode = nodesList[parentList[currentNode.id as Int]]
        }
        path.add(entranceNode)
        //path.reverse()

        print("Length of fastest path: " + path.size + "\n")
        print("Order of Nodes:\n")
        for (i in 0 until path.size) {
            var next = path[i].id as Int
            print("$next ")
        }
        print("\n")
        for (i in 0 until path.size) {
            var next = path[i].name as String
            print("$next -> ")
        }
        print("\n")

        return path
    }
/*
    // Return all nodes SORTED BY ID ASCENDING of a given building as an ArrayList<Node>
    fun getNodes(building: String) : ArrayList<Node> {
        var nodes = ArrayList<Node>()
        // Get all node info from building via request to our server
        lateinit var completion: () -> Unit
        val url = "https://52.14.13.109/getnodes/?building=" + building as String
        val getRequest = JsonObjectRequest(url,
            { response ->
                print("response")
                val infoReceived = try {
                    print("try")
                    response.getJSONArray(building)
                } catch (e:JSONException) {
                    print("catch")
                    JSONArray()
                }
                completion()
            } , {
                error -> Log.e("getNodes", error.localizedMessage ?: "JsonObjectRequest error")
            }
        )
        print("done")
        /*
        val getRequest = JsonObjectRequest(url,
            { response ->
                print("response\n")
                val infoReceived = try {
                    print("try\n")
                    response.getJSONArray(building)
                } catch (e: JSONException) {
                    print("catch\n")
                    JSONArray()
                }
                for (i in 0 until infoReceived.length()) {
                    val nodeinfo = infoReceived[i] as JSONArray
                    val id = nodeinfo[1].toString().toInt()
                    val coords = JSONArray(nodeinfo[2])
                    val latitude = coords[0].toString().toDouble()
                    val longitude = coords[1].toString().toDouble()
                    val neighborsarray = JSONArray(nodeinfo[3])
                    var neighbors = ArrayList<Int>()
                    for (j in 0 until neighborsarray.length()) {
                        neighbors.add(neighborsarray[j].toString().toInt())
                    }
                    nodes.add(
                        Node(
                            id = i,
                            latitude = latitude,
                            longitude = longitude,
                            neighbors = neighbors
                        )
                    )
                }
                completion()
            }, {
                print("failed\n")
                completion()
            }
        )
        */
        return nodes
    }

 */
}