package edu.umich.wwf

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import okhttp3.Request

class UpdateUserLocation : AppCompatActivity() {
    private val serverURL = ""

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    private var cancellationTokenSource = CancellationTokenSource()

    fun getLocationFromGPS() : Pair<Double, Double> {
        var latitude = 0.0
        var longitude = 0.0

        // Get current location from GPS data
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {

                val currentLocationTask: Task<Location> = fusedLocationClient.getCurrentLocation(
                    PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                )

            currentLocationTask.addOnCompleteListener { task: Task<Location> ->
                val result = if (task.isSuccessful) {
                    val result: Location = task.result
                    "Location (success): ${result.latitude}, ${result.longitude}"
                    longitude = result.longitude
                    latitude = result.latitude
                } else {
                    val exception = task.exception
                    "Location (failure): $exception"
                }

                Log.d(TAG, "getLocationFromGPS() result: $result")
            }
        }

        return Pair(latitude, longitude)
    }

    fun getEntrance(building: String) {
        // Make call to getLocationFromGPS
        getLocationFromGPS()

        // Gather set of entrances to building from database
        val request = Request.Builder()
            .url(serverURL+"getimages/")
            .build() // Refer to getChatts() from lab 1 to implement this AFTER backend is setup

        // Return the nearest neighbor entrance to current location

    }
}