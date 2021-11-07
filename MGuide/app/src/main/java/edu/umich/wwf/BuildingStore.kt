package edu.umich.wwf

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException

object BuildingStore {
    val buildings = arrayListOf<String>()
    private lateinit var queue: RequestQueue
    private const val serverUrl = "https://52.14.13.109/"

    fun getBuildings(context: Context) {
        val getRequest = JsonObjectRequest(serverUrl + "getbuildings/",
            { response ->
                buildings.clear()
                val buildingsReceived = try {
                    response.getJSONArray("buildings")
                } catch (e: JSONException) {
                    JSONArray()
                }
                val first_building = buildingsReceived[0] as JSONArray
                Toast.makeText(context, "Received getBuildings response: " + first_building[0], Toast.LENGTH_SHORT).show()
            },
            { error -> Log.e("getBuildings", error.localizedMessage ?: "JsonObjectRequest error") }
        )

        if (!this::queue.isInitialized) {
            queue = Volley.newRequestQueue(context)
        }
        queue.add(getRequest)
    }
}