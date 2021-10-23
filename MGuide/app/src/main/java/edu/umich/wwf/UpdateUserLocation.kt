package edu.umich.wwf

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley.newRequestQueue
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.reflect.full.declaredMemberProperties
import java.util.*

class UpdateUserLocation : AppCompatActivity() {
    private val serverURL = ""
    private val buildingName = ""
    private lateinit var queue: RequestQueue

    fun getClosestEntrance(building: String) {
        val entrances = arrayListOf<Node?>()
        val getRequest = JsonObjectRequest(serverURL+"?buildingName="+buildingName,
            { response ->
                val infoReceived = try { response.getJSONArray("maps") } catch (e: JSONException) { JSONArray() }
                val nFields = 3

                for (i in 0 until infoReceived.length()) {
                    val entrance = infoReceived[i] as JSONArray
                    if (entrance.length() == nFields) {
                        val geoArr = if (entrance[3] == JSONObject.NULL) null else JSONArray(entrance[3] as String)
                        entrances.add(Node(username = chattEntry[0].toString(),
                            message = chattEntry[1].toString(),
                            timestamp = chattEntry[2].toString(),
                            geodata = geoArr?.let { GeoData(
                                lat = it[0].toString().toDouble(),
                                lon = it[1].toString().toDouble(),
                                loc = it[2].toString(),
                                facing = it[3].toString(),
                                speed = it[4].toString()
                            )}
                        ))
                    } else {
                        Log.e("getChatts", "Received unexpected number of fields: " + chattEntry.length().toString() + " instead of " + nFields.toString())
                    }
                }
                completion()
            }, { completion() }
        )
        // Make call to getLocationFromGPS

        // Gather set of entrances to building from database
        val request = Request.Builder()
            .url(serverURL+"getimages/")
            .build() // Refer to getChatts() from lab 1 to implement this AFTER backend is setup

        // Return the nearest neighbor entrance to current location

    }
}