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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.Semaphore
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import kotlin.coroutines.CoroutineContext
import kotlin.math.floor


class MapLauncher : AppCompatActivity(), CoroutineScope {
    protected lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main
    @Volatile var nodes =  arrayListOf<Double>()
    @Volatile var connections = arrayListOf<Int>()
    var ids = arrayListOf<Int>()
    val serverUrl: String = "https://52.14.13.109/"
    lateinit var queue: RequestQueue
    lateinit var mostRecent: JSONArray
    lateinit var fastestPath: ArrayList<Node>
    var handledReq: Boolean = false
    @Volatile var reqComplete: Int = 0
    var buildingName = ""
    var floorURL = ""
    var roomMap: MutableMap<String, Int> = mutableMapOf<String, Int>()
    fun getNodes(building: String, floorNum: Int,  context: Context, completion: () -> Unit) {
        val getRequest = JsonObjectRequest(serverUrl + "getnodes/?building=" + building,
            { response ->
                //nodes.clear()
                System.out.println("***Nodes cleared***")
                //connections.clear()
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

                        
                            nodes.add(((chattEntry[5]).toString()).toDouble()) // long
                            nodes.add(((chattEntry[6]).toString()).toDouble()) // latitude
                            nodes.add(-10000 * chattEntry[4].toString().toDouble())
                            roomMap[chattEntry[1].toString()] = chattEntry[2].toString().toInt()
                            if (chattEntry[2].toString().toInt() == -1) {
                                roomMap[chattEntry[1].toString()] = nCount
                            }
                            nCount++
                            ids.add(chattEntry[2].toString().toInt())
                            /*if (neighbors != null) {
                                for (j in 0 until neighbors.length()) {
                                    val flanders = neighbors[j].toString().toInt()
                                    connections.add(i)
                                    connections.add(flanders)
                                }
                            }*/


                    } else {
                        Toast.makeText(context, "Wrong length expected 8 got " + chattEntry.length().toString(),
                            Toast.LENGTH_SHORT).show();
                    }
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


    fun setComplete(sem: Semaphore) {
        sem.release()
    }


    fun launchGL(s: String, ctx: Context, floorNum: Int, roomn: String) {

        val semPath = Semaphore(0)
        val semMap = Semaphore(0)
        val semNodes = Semaphore(0)
        val semNodes2 = Semaphore(0)
        nodes.clear()
        connections.clear()
        job = Job()
        val thpath: Thread = Thread(Runnable(){
            semNodes.acquire()
            var pathGenerator = PathGenerator()
            var updateUserLocation = UpdateUserLocation()
            var allNodes = NodesStore.getNodes(ctx, s, semNodes2)

            semNodes2.acquire()
            System.out.println(roomMap.toString())
            val user = User()
            var entranceNode = allNodes[updateUserLocation.getClosestEntrance(allNodes, this)]
            var destinationID = roomMap[roomn]
            var destNode = allNodes[0]
            for (i in 0 until allNodes.size) {
                if (allNodes[i].id == destinationID) {
                    destNode = allNodes[i]
                }
            }
            fastestPath = pathGenerator.getFastestPath(s, allNodes, entranceNode as Node, destNode)
            for (i in 1 until fastestPath.size) {
                connections.add(fastestPath[i].id as Int)
                connections.add(fastestPath[i-1].id as Int)
            }
            semPath.release()
        })
        thpath.start()


        buildingName = s
        //buildingName = s + 1.toString()
        val th: Thread = Thread(Runnable() {
            var intent: Intent =Intent(ctx, DisplayActivity::class.java)

            getNodes(buildingName, floorNum, context = ctx, {
                runOnUiThread {
                    setComplete(semNodes)
                }
            })
            getMediaURL(buildingName, floorNum,  context =  ctx, {
                runOnUiThread {
                    setComplete(semMap)
                }
            })



            semPath.acquire() // downs the semaphore
            semMap.acquire()


            var buildingNodes = ArrayList(nodes)

            var conns = ArrayList(connections)
            var copyconns = ArrayList(buildingNodes)

            for (i in 0 until ids.size) {
                cpy(buildingNodes, copyconns, ids[i], i)
            }

            var iStream = (URL(floorURL).content) as InputStream
            var img = BitmapFactory.decodeStream(iStream)
            var extras: Bundle = Bundle()
            extras.putIntegerArrayList("connections", conns)
            extras.putString("buildingFile", "bbb.png")
            extras.putDoubleArray("nodes", copyconns.toDoubleArray() )
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

    fun cpy(src: ArrayList<Double>, dst: ArrayList<Double>, ind: Int, srcIndex: Int ) {
        dst[ind*3+0] = src[srcIndex*3+0]
        dst[ind*3+1] = src[srcIndex*3+1]
        dst[ind*3+2] = src[srcIndex*3+2]
    }

}