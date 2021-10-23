package edu.umich.wwf

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.Menu.FIRST
import android.view.Menu.NONE
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import edu.umich.jadb.kotlinChatter.ChattStore.postChatt
import java.util.*

class UpdateUserLocation : AppCompatActivity() {
    private val serverURL = ""

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    private var cancellationTokenSource = CancellationTokenSource()

    fun updateGeoData() {
        LocationServices.getFusedLocationProviderClient(applicationContext)
            .getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    lat = it.result.latitude
                    lon = it.result.longitude
                    speed = it.result.speed
                    if (!enableSend) {
                        submitChatt()
                    }
                } else {
                    Log.e("PostActivity getFusedLocation", it.exception.toString())
                }
            }

        // read sensors to determine bearing
        sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
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