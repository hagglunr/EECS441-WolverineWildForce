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


class DebugActivity : AppCompatActivity() {
    var nodes =  arrayListOf<Double>()
    val serverUrl: String = "https://52.14.13.109/"
    lateinit var queue: RequestQueue
    lateinit var mostRecent: JSONArray
    var handledReq: Boolean = false
    fun getNodes(building: String, context: Context, completion: () -> Unit) {
        val getRequest = JsonObjectRequest(serverUrl + "getrooms/" + building,
            { response ->
                Toast.makeText(
                    context, "Handling Request",
                    Toast.LENGTH_SHORT
                ).show();
                nodes.clear()
                handledReq = true
                val nodesReceived = try {
                    response.getJSONArray("rooms")
                } catch (e: JSONException) {
                    JSONArray()
                }
                for (i in 0 until nodesReceived.length()) {
                    val chattEntry = nodesReceived[i] as JSONArray
                    mostRecent = nodesReceived[i] as JSONArray
                    if (chattEntry.length() == 3) {
                        nodes.add(((chattEntry[0]).toString()).toDouble()) // n
                        nodes.add(((chattEntry[1]).toString()).toDouble()) // w
                    } else {
                        Log.e(
                            "getChatts",
                            "Received unexpected number of fields: " + chattEntry.length()
                                .toString() + " instead of " + 3.toString()
                        )
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


    fun launchGL(v: View) {
        val th: Thread = Thread(Runnable() {
            var intent: Intent =Intent(this, DisplayActivity::class.java)


            var iStream = (URL("https://52.14.13.109/media/BBB_1_glrqG87.jpeg").content) as InputStream
            var img = BitmapFactory.decodeStream(iStream)
            intent.putExtra("buildingFile", "bbb.png")
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