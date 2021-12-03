package edu.umich.mwassink.mapdisplay

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.pow
import kotlin.math.sqrt

class UpdateUserLocation {
    private val serverURL = ""
    private val buildingName = ""
    private lateinit var queue: RequestQueue
    private val entrances = arrayListOf<Node?>()

//    fun getEntrances(user : User, building: String, context: Context, completion: () -> Unit) {
//        val getRequest = JsonObjectRequest(serverURL+"?buildingName="+buildingName,
//            { response ->
//                val infoReceived = try { response.getJSONArray("maps") } catch (e: JSONException) { JSONArray() }
//                val nFields = 3
//
//                for (i in 0 until infoReceived.length()) {
//                    val entrance = infoReceived[i] as JSONArray
//                    if (entrance.length() == nFields) {
//                        val geoArr =
//                            if (entrance[3] == JSONObject.NULL) null else JSONArray(entrance[3] as String)
//                        entrances.add(
//                            Node(
//                                entranceNum = infoReceived[0].toString().toInt(),
//                                latitude = infoReceived[1].toString().toDouble(),
//                                longitude = infoReceived[2].toString().toDouble(),
//                                neighbors = arrayListOf<Int>()
//                            )
//                        )
//                    } else {
//                        Log.e("getEntrances", "Received unexpected number of fields: " + infoReceived.length().toString() + " instead of " + nFields.toString())
//                    }
//                }
//                completion()
//            }, { completion() }
//        )
//        if (!this::queue.isInitialized) {
//            queue = Volley.newRequestQueue(context)
//        }
//        queue.add(getRequest)
//        getClosestEntrance(user)
//
//    }

    // TODO: Later consider having default entrance in case of null or bad server request
    fun getClosestEntrance(user : User): Node? {
        var shortestDistance = Double.MAX_VALUE
        var shortestIndex = Int.MAX_VALUE
        for (i in 0 until entrances.size) {
            val entranceLat = entrances[i]!!.latitude
            val entranceLon = entrances[i]!!.longitude
            val distance =
                sqrt((user.lat - entranceLat!!).pow(2) + (user.lon - entranceLon!!).pow(2))
            if (distance < shortestDistance) {
                shortestDistance = distance
                shortestIndex = i
            }
        }
        return entrances[shortestIndex]
    }
}