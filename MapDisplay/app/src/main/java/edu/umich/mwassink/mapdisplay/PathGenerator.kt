package edu.umich.mwassink.mapdisplay

import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class PathGenerator {
    // Use the A* algorithm
    // https://www.researchgate.net/figure/A-search-algorithm-Pseudocode-of-the-A-search-algorithm-operating-with-open-and-closed_fig8_232085273

    // Returns list of Nodes in order of their intended traversal
    fun getFastestPath(building: String, entranceNode: Node, destinationNode: Node) : ArrayList<Node> {
        // Init nodesList from database pull based on "building"
        var nodesList = getNodes(building)

        var openList = ArrayList<Node>()
        var closedList = ArrayList<Node>()
        var parentList = ArrayList<Int>(nodesList.size)

        openList.add(entranceNode)

        // Calculate f(Node) = h(Node) + g(Node) from hList and gList
        // h(Node) = estimated cost from Node to Goal based on Manhattan Heuristic
        var hList = ArrayList<Double>()
        for (node in nodesList) {
            hList.add(node.getManhattanHeuristicValue(destinationNode))
        }

        // g(Node) = cost so far to reach Node
        var gList = ArrayList<Double>(nodesList.size)
        gList.fill(Double.MAX_VALUE) // Every node costs unknown (MAX) to get to
        gList[0] = 0.0 // Start node costs zero to get to

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

            // Move checkNode from open to closed list
            openList.remove(checkNode)
            closedList.add(checkNode)

            // If node is destination node, we are finished
            if (checkNode.isSameAs(destinationNode)) {
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
                }

                // If neighbor not in open or closed, then add to open and update g(neighbor)
                if (!isInClosedList && !isInOpenList) {
                    openList.add(neighborNode)
                    gList[neighborID] = cost
                }
            }
        }

        // Traverse from destination to start by parent list
        var path = ArrayList<Node>()
        var currentNode = destinationNode
        while (!currentNode.isSameAs(entranceNode)) {
            path.add(currentNode)
            currentNode = nodesList[parentList[currentNode.id as Int]]
        }
        path.add(entranceNode)
        path.reverse()

        return path
    }

    // Return all nodes SORTED BY ID ASCENDING of a given building as an ArrayList<Node>
    fun getNodes(building: String) : ArrayList<Node> {
        var nodes = ArrayList<Node>()
        // Get all node info from building via request to our server
        val completion: () -> Unit = null
        val url = "https://"+serverURL+"/getnodes/?building="+building as String
        val getRequest = JsonObjectRequest(url,
            { response ->
                val infoReceived = try {
                    response.getJSONArray(building)
                } catch (e: JSONException) {
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
                    for (i in 0 until neighborsarray.length()) {
                        neighbors.add(neighborsarray[i].toString().toInt())
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
            }, { completion() }
        )
        return nodes
    }
}