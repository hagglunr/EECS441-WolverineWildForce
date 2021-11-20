package edu.umich.mwassink.mapdisplay

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException

object BuildingInfoStore {
    val buildingRoomMap = mutableMapOf<String, ArrayList<String>>()
    private lateinit var queue: RequestQueue
    private const val serverUrl = "https://52.14.13.109/"

    fun getBuildings(context: Context, completion: () -> Unit) {
        val getRequest = JsonObjectRequest(
            serverUrl + "getbuildings/",
            { response ->
                val buildingsReceived = try {
                    response.getJSONArray("buildings")
                } catch (e: JSONException) {
                    JSONArray()
                }
                val buildingInfoArray = buildingsReceived as JSONArray
                for (i in 0 until buildingInfoArray.length()) {
                    val building = (buildingInfoArray[i] as JSONArray)[0].toString()
                    buildingRoomMap[building] = arrayListOf()
                }
                getRooms(context, completion)
            },
            {
                    error -> Log.e("getBuildings", error.localizedMessage ?: "JsonObjectRequest error")
            }
        )

        if (!this::queue.isInitialized) {
            queue = Volley.newRequestQueue(context)
        }
        queue.add(getRequest)
    }

    fun getRooms(context: Context, completion: () -> Unit) {
        for (building in buildingRoomMap.keys) {
            val getRequest = JsonObjectRequest(
                serverUrl + "getrooms/?building=" + building,
                { response ->
                    val roomsReceived = try {
                        response.getJSONArray(building + " Rooms")
                    } catch (e: JSONException) {
                        JSONArray()
                    }
                    val roomsArray = roomsReceived as JSONArray
                    val rooms = mutableSetOf<String>()
                    for (i in 0 until roomsArray.length()) {
                        val room = (roomsArray[i] as JSONArray)[0].toString()
                        rooms.add(room.split("Door")[0].trim())
                    }
                    buildingRoomMap[building] = rooms.toCollection(ArrayList<String>())
                    // Log.d("buildingRoomMap", rooms.toString())
                    completion()
                },
                { error ->
                    Log.e("getRooms", error.localizedMessage ?: "JsonObjectRequest error")
                }
            )
            if (!this::queue.isInitialized) {
                queue = Volley.newRequestQueue(context)
            }
            queue.add(getRequest)
        }
    }
}