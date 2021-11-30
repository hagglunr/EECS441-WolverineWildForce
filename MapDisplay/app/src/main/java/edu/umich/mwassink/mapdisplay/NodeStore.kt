package edu.umich.mwassink.mapdisplay

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

object NodesStore {
    private lateinit var queue: RequestQueue
    val nodes = arrayListOf<Node>()

    fun getNodes(context: Context, building: String, completion: () -> Unit) {
        nodes.clear()
        val url = "https://52.14.13.109/getnodes/?building=" + building as String
        var getRequest = JsonObjectRequest(url,
            { response ->
                val infoReceived = try {
                    response.getJSONArray(building)
                } catch (e: JSONException) {
                    JSONArray()
                }
                for (i in 0 until infoReceived.length()) {
                    val nodeinfo = infoReceived[i] as JSONArray
                    val name = nodeinfo[1].toString()
                    val id = nodeinfo[2].toString().toInt()
                    val typeStr = nodeinfo[3].toString()
                    var type: NodeType? = null
                    var floorNum: Int? = null
                    if (typeStr == "Entrance") {
                        type = NodeType.ENTRANCE
                    }
                    else if (typeStr == "Staircase") {
                        type = NodeType.STAIRCASE
                    }
                    else if (typeStr == "Path") {
                        type = NodeType.PATH
                    }
                    else if (typeStr == "Restroom") {
                        type = NodeType.RESTROOM
                    }
                    else if (typeStr == "Room") {
                        type = NodeType.ROOM
                    }
                    floorNum = nodeinfo[4].toString().toInt()
//                    val coords = JSONArray(nodeinfo[3])
                    val longitude = nodeinfo[5].toString().toDouble()
                    val latitude = nodeinfo[6].toString().toDouble()
                    val neighborsarray = nodeinfo[7] as JSONArray
                    var neighbors = ArrayList<Int>()
                    for (j in 0 until neighborsarray.length()) {
                        /* I just changed the .getJSONObject(j) to the regular bracket index since
                        the value inside was already an int it seems. */

//                        neighbors.add(neighborsarray.getJSONObject(j).toString().toInt())
                        val testObject = neighborsarray[j]
                        val testInt = testObject.toString().toInt()
                        neighbors.add(testInt)
                    }
                    nodes.add(
                        Node(
                            name = name,
                            id = id,
                            floorNum = floorNum,
                            type = type,
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
//
//                Log.d("getNodes", "Size of nodes: ${infoReceived.length()}")
//
//                completion()
//            }, {
//                    error -> Log.e("getNodes", error.localizedMessage ?: "JsonObjectRequest error")
//            }
//        )

        if (!this::queue.isInitialized) {
            queue = Volley.newRequestQueue(context)
        }
        queue.add(getRequest)
    }
}
