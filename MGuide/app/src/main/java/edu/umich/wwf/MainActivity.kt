package edu.umich.wwf

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import java.util.*

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity(), SensorEventListener {
    private var user = User()
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contract = ActivityResultContracts.RequestPermission()
        val launcher = registerForActivityResult(contract) { granted ->
            if (!granted) {
                toast("Fine location access denied", false)
                finish()
            }
        }
        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        launcher.launch(Manifest.permission.HIGH_SAMPLING_RATE_SENSORS)

        LocationServices.getFusedLocationProviderClient(applicationContext)
            .getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    user.lat = it.result.latitude
                    user.lon = it.result.longitude
                    user.speed = it.result.speed
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

        var updateUserLocation = UpdateUserLocation()
        val building = ""
        // TODO: change building to var later
//        building = getBuilding()
//        updateUserLocation.getEntrances(user, building, context = )
        // TODO: Fix context to call function

        setContentView(R.layout.activity_main)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    var gravity: FloatArray = emptyArray<Float>().toFloatArray()
    var geomagnetic: FloatArray = emptyArray<Float>().toFloatArray()

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD)
            geomagnetic = event.values
    }

    override fun onDestroy() {
        super.onDestroy()
        accelerometer?.let {
            sensorManager.unregisterListener(this, it)
        }
        magnetometer?.let {
            sensorManager.unregisterListener(this, it)
        }
    }

    fun convertBearing(): String {
        if (gravity.isNotEmpty() && geomagnetic.isNotEmpty()) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                // the 3 elements of orientation: azimuth, pitch, and roll,
                // bearing is azimuth = orientation[0], in rad
                val bearingdeg = (Math.toDegrees(orientation[0].toDouble()) + 360).rem(360)
                val compass = arrayOf("North", "NE", "East", "SE", "South", "SW", "West", "NW", "North")
                val index = (bearingdeg / 45).toInt()
                return compass[index]
            }
        }
        return "unknown"
    }
}
