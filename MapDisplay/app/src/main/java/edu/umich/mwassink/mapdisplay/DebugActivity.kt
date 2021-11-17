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


class DebugActivity : AppCompatActivity() {
    var nodes =  arrayListOf<Double>()
    var connections = arrayListOf<Int>()
    val serverUrl: String = "https://52.14.13.109/"
    lateinit var queue: RequestQueue
    lateinit var mostRecent: JSONArray
    @Volatile var handledReq: Boolean = false
    var reqComplete: Boolean = false
    fun getNodes(building: String, context: Context, completion: () -> Unit) {
        val getRequest = JsonObjectRequest(serverUrl + "getnodes/?building=" + building,
            { response ->
                nodes.clear()
                Toast.makeText(context, "Handling request",
                    Toast.LENGTH_SHORT).show();
                handledReq = true
                val nodesReceived = try {
                    response.getJSONArray("BBB")
                } catch (e: JSONException) {

                    JSONArray()
                }
                for (i in 0 until nodesReceived.length()) {
                    val chattEntry = nodesReceived[i] as JSONArray
                    mostRecent = nodesReceived[i] as JSONArray
                    val neighbors = if (chattEntry[7] == JSONObject.NULL) null else chattEntry[7] as JSONArray
                    if (chattEntry.length() == 8) {

                        nodes.add(((chattEntry[6]).toString()).toDouble()) // long
                        nodes.add(((chattEntry[5]).toString()).toDouble()) // latitude
                        if (neighbors != null) {
                            for (j in 0 until neighbors.length()) {
                                val flanders = neighbors[j].toString().toInt()
                                connections.add(i)
                                connections.add(flanders)
                            }
                        }
                    } else {
                        Toast.makeText(context, "Wrong length expected 8 got " + chattEntry.length().toString(),
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




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        /*
        BuildingDirectoryMap = LinkedHashMap<String, Building>()
        val tempBuilding = Building
        val tempRoom = Room
        tempBuilding.rooms[tempRoom.number] = tempRoom
        val tempEntry = Entry
        tempBuilding.entries[tempEntry.id] = tempEntry
        BuildingDirectoryMap[tempBuilding.name] = tempBuilding

        // Data to be passed in is tempBuilding. Then we can access all the information we need from there
        // tempBuilding.rooms["1670]  .x or .y
        // tembBuilding.entries["1"]  .x or .y
        */
    }

    fun setComplete() {
        synchronized(this) {
            reqComplete = true
        }
    }


    fun launchGL(v: View) {


        val th: Thread = Thread(Runnable() {
            var intent: Intent =Intent(this, DisplayActivity::class.java)

            getNodes("BBB", context = applicationContext, {
                runOnUiThread {
                    setComplete()
                }
            })
            var complete = false
            while (!complete) {
                synchronized(this) {
                    complete = reqComplete
                }
            }
            var buildingNodes = nodes
            var conns = connections
            var iStream = (URL("https://52.14.13.109/media/BBB_1_glrqG87.jpeg").content) as InputStream
            var img = BitmapFactory.decodeStream(iStream)
            var extras: Bundle = Bundle()
            extras.putIntegerArrayList("connections", conns)
            extras.putString("buildingFile", "bbb.png")
            extras.putDoubleArray("nodes", buildingNodes.toDoubleArray() )
            intent.putExtras(extras)
            val stream = this.openFileOutput("bbb.png", Context.MODE_PRIVATE)
            img.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            img.recycle()

            startActivity(intent)
        })
        th.start()

    }

    fun launchWalk(v: View) {

        startActivity(Intent(this, WalkActivity::class.java))
    }
    
    fun launchGPS(v: View) {
        startActivity(Intent(this, GPSActivity::class.java))
    }

    fun launchSearch(v: View) {
        //startActivity(Intent(this, MainActivity::class.java))
    }
}