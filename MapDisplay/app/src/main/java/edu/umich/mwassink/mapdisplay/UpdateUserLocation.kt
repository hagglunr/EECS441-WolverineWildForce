package edu.umich.mwassink.mapdisplay

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.concurrent.Semaphore
import kotlin.math.pow
import kotlin.math.sqrt

class UpdateUserLocation {
//    private val serverURL = ""
//    private val buildingName = ""
//    private lateinit var queue: RequestQueue

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
    fun getClosestEntrance(nodes: ArrayList<Node>, applicationContext : Context): Int {
        var shortestDistance = Double.MAX_VALUE
        var shortestIndex = Int.MAX_VALUE
        var entrances = ArrayList<Node>()
        var entranceIndices = ArrayList<Int>()
        var i = 0
        for (node in nodes) {
            if (node.type == NodeType.ENTRANCE && node.id!! < 0) {
                entrances.add(node)
                entranceIndices.add(i)
            }
            i++
        }
        if (entrances.isEmpty()) {
            return -1
        }

        var userLat = 0.0
        var userLon = 0.0
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return 0
        }
        val complete = Semaphore(0)
        val listener = LocationServices.getFusedLocationProviderClient(applicationContext)
            .getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    userLat = it.result.latitude
                    userLon = it.result.longitude
                    complete.release()
                } else {
                    Log.e("MainActivity getFusedLocation", it.exception.toString())
                }
            }


        complete.acquire() // wait on the semaphore until the other thing finishes
        for (i in 0 until entrances.size) {

            val entranceLat = entrances[i].latitude
            val entranceLon = entrances[i].longitude
            val distance =
                sqrt((userLat - entranceLat!!).pow(2) + (userLon - entranceLon!!).pow(2))
            if (distance < shortestDistance) {
                shortestDistance = distance
                shortestIndex = entrances[i].id!!
            }
        }
        if (shortestIndex == -100) {
            shortestIndex = 0
        } else {
            shortestIndex *= -1
        }
        var index = -1
        for (i in 0 until nodes.size) {
            if (nodes[i].id == shortestIndex) {
                index = i;
                break;
            }
        }
        return index
    }
}