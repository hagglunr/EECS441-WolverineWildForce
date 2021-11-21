package edu.umich.mwassink.mapdisplay

import android.content.Context
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import java.util.ArrayList

object NodesStore {
    private lateinit var queue: RequestQueue


    fun getNodes(context: Context, building: String, completion: () -> Unit) {

        val url = "https://52.14.13.109/getnodes/?building=" + building as String
        val getRequest = JsonObjectRequest(url,
            { response ->
                val infoReceived = try {
                    response.getJSONArray(building)
                } catch (e: JSONException) {
                    JSONArray()
                }

                Log.d("getNodes", "Size of nodes: ${infoReceived.length()}")

                completion()
            }, {
                    error -> Log.e("getNodes", error.localizedMessage ?: "JsonObjectRequest error")
            }
        )

        if (!this::queue.isInitialized) {
            queue = Volley.newRequestQueue(context)
        }
        queue.add(getRequest)
    }
}