package edu.umich.wwf

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException

object BuildingInfoStore {
    val buildings = arrayListOf<String>()
    private lateinit var queue: RequestQueue
    private const val serverUrl = "https://52.14.13.109/"

    fun getBuildings(context: Context, completion: () -> Unit) {
        val getRequest = JsonObjectRequest(
            serverUrl + "getbuildings/",
            { response ->
                BuildingInfoStore.buildings.clear()
                val buildingsReceived = try {
                    response.getJSONArray("buildings")
                } catch (e: JSONException) {
                    JSONArray()
                }

                val buildingInfoArray = buildingsReceived as JSONArray                  // [[BBB],[PIER]]
                for (i in 0 until buildingInfoArray.length()) {                         // iterate from 0 to (length_of_array - 1)
                    val building = (buildingInfoArray[i] as JSONArray)[0].toString()    // get the building name from the inner array
                    buildings.add(building)
                    Toast.makeText(context, "Add Building: $building", Toast.LENGTH_SHORT).show()
                }
                completion()
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
}