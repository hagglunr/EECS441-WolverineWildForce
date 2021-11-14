package edu.umich.mwassink.mapdisplay

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.Volley.newRequestQueue
import edu.umich.mwassink.mapdisplay.databinding.ActivityDisplayBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.FileInputStream


class DisplayActivity: AppCompatActivity(), SensorEventListener {

    lateinit var view: DisplayView
    lateinit var buttonView: ActivityDisplayBinding
    lateinit var sensorManager: SensorManager
    var steps: Float = 0f
    var sensorOn = false
    var firstValue: Float = -1f
    val stepLength: Float = 10f
    var sensor: Sensor? = null


    init {

    }

    override fun onCreate(bundle: Bundle?): Unit{

        super.onCreate(bundle)
        val fileName: String? = intent.getStringExtra("buildingFile")
        val inStream: FileInputStream = this.openFileInput(fileName)
        var bmp: Bitmap = BitmapFactory.decodeStream(inStream)
        inStream.close()
        view = DisplayView(this, DefaultConnections(bmp))
        setContentView(view)

        buttonView = ActivityDisplayBinding.inflate(layoutInflater)
        addContentView(buttonView.root,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))



        val reqResult = ActivityResultContracts.RequestPermission()

        val reqLauncher = registerForActivityResult(reqResult) {
                granted ->
            if (!granted) {
                System.exit(1)
            }
        }

        reqLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        buttonView.LinesMode.setOnClickListener {
            view.setLineMode(true)
            view.setPointMode(false)
            view.setMoveMode(false)
            view.setDragMode(false)
        }
        buttonView.PointMode.setOnClickListener {
            view.setLineMode(false)
            view.setPointMode(true)
            view.setMoveMode(false)
            view.setDragMode(false)
        }

        buttonView.MoveMode.setOnClickListener {
            view.setLineMode(false)
            view.setPointMode(false)
            view.setMoveMode(true)
            view.setDragMode(false)
        }

        buttonView.Reposition.setOnClickListener {
            view.setLineMode(false)
            view.setPointMode(false)
            view.setMoveMode(false)
            view.setDragMode(true)
        }
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





}