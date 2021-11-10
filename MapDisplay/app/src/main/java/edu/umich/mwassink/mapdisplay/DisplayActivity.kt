package edu.umich.mwassink.mapdisplay

import android.Manifest
import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.Volley.newRequestQueue
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class DisplayActivity: AppCompatActivity(), SensorEventListener {

    lateinit var view: DisplayView
    lateinit var sensorManager: SensorManager
    lateinit var queue: RequestQueue
    val serverUrl: String = "https://52.14.13.109/"
    var steps: Float = 0f
    var sensorOn = false
    var firstValue: Float = -1f
    val stepLength: Float = 10f
    var sensor: Sensor? = null
    var nodes =  arrayListOf<Double>()

    init {

    }

    override fun onCreate(bundle: Bundle?): Unit{
        super.onCreate(bundle)
        view = DisplayView(this)
        setContentView(view)

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

        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            sensorOn = true
        } else {
            //System.exit(1)
        }

    }

    override fun onSensorChanged(event: SensorEvent) {

        var prevSteps = steps
        if (sensorOn && sensor != null) {
            if (firstValue < 0f) {
                firstValue = event.values[0]
                prevSteps = firstValue
            }
            steps = event.values[0]
            val newSteps = steps - prevSteps
            view.changePos(newSteps * stepLength, 0f) // for now

        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }




    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // Let the ScaleGestureDetector inspect all events.

        return true
    }

    // Pull the nodes down from the server
    fun GetNodes(building: String, context: Context, completion: () -> Unit) {
        val getRequest = JsonObjectRequest(serverUrl+"getnodes/?building="+building,
            { response ->
                nodes.clear()
                val nodesReceived = try { response.getJSONArray("nodes") } catch (e: JSONException) { JSONArray() }
                for (i in 0 until nodesReceived.length()) {
                    val chattEntry = nodesReceived[i] as JSONArray
                    if (chattEntry.length() == 3) {
                        nodes.add(((chattEntry[0]).toString()).toDouble()) // n
                        nodes.add(((chattEntry[1]).toString()).toDouble()) // w
                    } else {
                        Log.e("getChatts", "Received unexpected number of fields: " + chattEntry.length().toString() + " instead of " + 3.toString())
                    }
                }
                completion()
            }, { completion() }
        )

        if (!this::queue.isInitialized) {
            queue = newRequestQueue(context)
        }
        queue.add(getRequest)
    }




}