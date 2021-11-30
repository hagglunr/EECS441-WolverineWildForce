package edu.umich.mwassink.mapdisplay

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import java.io.InputStream
import java.net.URL
import android.R.string.no
import android.util.Log
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import kotlin.math.floor


class MapLauncher : AppCompatActivity() {
    @Volatile var nodes =  arrayListOf<Double>()
    @Volatile var connections = arrayListOf<Int>()
    val serverUrl: String = "https://52.14.13.109/"
    lateinit var queue: RequestQueue
    lateinit var mostRecent: JSONArray
    lateinit var fastestPath: ArrayList<Node>
    var handledReq: Boolean = false
    @Volatile var reqComplete: Int = 0
    var buildingName = ""
    var floorURL = ""
    fun getNodes(building: String, floorNum: Int,  context: Context, completion: () -> Unit) {
        val getRequest = JsonObjectRequest(serverUrl + "getnodes/?building=" + building,
            { response ->
                nodes.clear()
                System.out.println("***Nodes cleared***")
                connections.clear()
                handledReq = true
                val nodesReceived = try {
                    response.getJSONArray(buildingName)
                } catch (e: JSONException) {
                    Toast.makeText(context, "Bad JSON Array" ,
                        Toast.LENGTH_SHORT).show();
                    JSONArray()
                }
                var nCount = 0
                for (i in 0 until nodesReceived.length()) {

                    val chattEntry = nodesReceived[i] as JSONArray
                    mostRecent = nodesReceived[i] as JSONArray
                    val neighbors = if (chattEntry[7] == JSONObject.NULL) null else chattEntry[7] as JSONArray
                    if (chattEntry.length() == 8) {

                        if (chattEntry[4].toString().toInt() == floorNum) {
                            nodes.add(((chattEntry[5]).toString()).toDouble()) // long
                            nodes.add(((chattEntry[6]).toString()).toDouble()) // latitude
                            nCount++
                            if (neighbors != null) {
                                for (j in 0 until neighbors.length()) {
                                    val flanders = neighbors[j].toString().toInt()
                                    /*connections.add(i)
                                    connections.add(flanders)*/
                                }
                            }
                        }

                    } else {
                        Toast.makeText(context, "Wrong length expected 8 got " + chattEntry.length().toString(),
                            Toast.LENGTH_SHORT).show();
                    }

                    val nodeinfo = nodesReceived[i] as JSONArray
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
                    var neoneighbors = ArrayList<Int>()
                    for (j in 0 until neighborsarray.length()) {
                        /* I just changed the .getJSONObject(j) to the regular bracket index since
                        the value inside was already an int it seems. */

//                        neighbors.add(neighborsarray.getJSONObject(j).toString().toInt())
                        val testObject = neighborsarray[j]
                        val testInt = testObject.toString().toInt()
                        neoneighbors.add(testInt)
                    }
                    NodesStore.nodes.add(
                        Node(
                            name = name,
                            id = id,
                            floorNum = floorNum,
                            type = type,
                            latitude = latitude,
                            longitude = longitude,
                            neighbors = neoneighbors
                        )
                    )
                }
                Toast.makeText(context, "Got " + nCount + " nodes" ,
                    Toast.LENGTH_SHORT).show();
                completion()
            }, { completion() }
        )

        if (!this::queue.isInitialized) {
            queue = Volley.newRequestQueue(context)
        }
        queue.add(getRequest)
    }

    fun getMediaURL(building: String, floorNum: Int, context: Context, completion: () -> Unit) {
        val getRequest = JsonObjectRequest(serverUrl + "getfloorplans/?building=" + building,
            { response ->

                Toast.makeText(context, "Fetching floor plan",
                    Toast.LENGTH_SHORT).show();
                val nodesReceived = try {
                    response.getJSONArray(buildingName)
                } catch (e: JSONException) {
                    JSONArray()
                }
                for (i in 0 until nodesReceived.length()) {
                    val chattEntry = nodesReceived[i] as JSONArray
                    if (chattEntry.length() == 3) {
                        if (chattEntry[1].toString().toInt() == floorNum) {
                            floorURL = chattEntry[2].toString()
                        }

                    } else {
                        Toast.makeText(context, "Wrong length expected 3 got " + chattEntry.length().toString(),
                            Toast.LENGTH_SHORT).show();
                    }
                }
                completion()
            }, { completion() }
        )

        if (!this::queue.isInitialized) {
            queue = Volley.newRequestQueue(context)
        }
        queue.add(getRequest)
    }


    fun setComplete() {
        synchronized(this) {
            reqComplete += 1
        }
    }


    fun launchGL(s: String, ctx: Context, floorNum: Int, roomn: String) {

        buildingName = s
        reqComplete = 0
        //buildingName = s + 1.toString()
        val th: Thread = Thread(Runnable() {
            var intent: Intent =Intent(ctx, DisplayActivity::class.java)

            getNodes(buildingName, floorNum, context = ctx, {
                runOnUiThread {
                    setComplete()
                }
            })
            getMediaURL(buildingName, floorNum,  context =  ctx, {
                runOnUiThread {
                    setComplete()
                }
            })
            var complete = 0
            while (complete != 2) {
                synchronized(this) {
                    complete = reqComplete
                }
            }


            var buildingNodes = ArrayList(nodes)

            var pathGenerator = PathGenerator()
            var updateUserLocation = UpdateUserLocation()
            var entranceNode = NodesStore.nodes[0]//updateUserLocation.getClosestEntrance()
            var destinationNode = NodesStore.nodes[2]
            fastestPath = pathGenerator.getFastestPath(buildingName, entranceNode, destinationNode)
            for (i in 0 until fastestPath.size) {
                connections.add(fastestPath[i].id as Int)
            }

            var conns = ArrayList(connections)

            var iStream = (URL(floorURL).content) as InputStream
            var img = BitmapFactory.decodeStream(iStream)
            var extras: Bundle = Bundle()
            extras.putIntegerArrayList("connections", conns)
            extras.putString("buildingFile", "bbb.png")
            extras.putDoubleArray("nodes", buildingNodes.toDoubleArray() )
            extras.putString("buildingName", buildingName)
            extras.putInt("floorNum", floorNum)
            extras.putString("roomName", roomn)
            intent.putExtras(extras)
            val stream = ctx.openFileOutput("bbb.png", Context.MODE_PRIVATE)
            img.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            img.recycle()

            ctx.startActivity(intent)
        })
        th.start()
    }
}