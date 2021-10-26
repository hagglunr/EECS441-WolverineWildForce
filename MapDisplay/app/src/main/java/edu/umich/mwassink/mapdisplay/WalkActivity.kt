package edu.umich.mwassink.mapdisplay

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts

class WalkActivity : AppCompatActivity(), SensorEventListener {

    lateinit var sensorManager: SensorManager
    var steps: Float = 0f
    var startSteps: Float = 0f
    var sensorOn = false
    var firstValue: Float = -1f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walk)

        val reqResult = ActivityResultContracts.RequestPermission()

        val reqLauncher = registerForActivityResult(reqResult) {
                granted ->
            if (!granted) {
                System.exit(1)
            }
        }

        reqLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager


    }

    override fun onResume() {
        super.onResume()

        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            sensorOn = true
        } else {
            System.exit(1)
        }

    }

    override fun onSensorChanged(event: SensorEvent) {

        var st = findViewById<TextView>(R.id.steps)
        if (sensorOn) {
            if (firstValue < 0f) {
                firstValue = event.values[0]
            }
            steps = event.values[0]
            st.text = (steps-firstValue).toString()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    fun clearSteps() {
        var st = findViewById<TextView>(R.id.steps)
        st.text = "0"
    }
}

