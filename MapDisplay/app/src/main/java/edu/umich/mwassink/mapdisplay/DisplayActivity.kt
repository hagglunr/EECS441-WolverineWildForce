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
import kotlin.math.sqrt


class DisplayActivity: AppCompatActivity(), SensorEventListener {

    lateinit var view: DisplayView
    lateinit var buttonView: ActivityDisplayBinding
    lateinit var sensorManager: SensorManager
    var steps: Float = 0f
    var sensorOn = false
    var firstValue: Float = -1f
    val stepLength: Float = 10f
    var sensor: Sensor? = null
    val serverUrl: String = "https://52.14.13.109/"
    private lateinit var queue: RequestQueue


    init {

    }

    override fun onCreate(bundle: Bundle?): Unit{

        super.onCreate(bundle)
        val extras: Bundle? = intent.extras
        val fileName: String? = extras?.getString("buildingFile")
        val intarr = extras?.getIntegerArrayList("connections")
        val nodes: DoubleArray?= extras?.getDoubleArray("nodes")
        val buildingName: String = extras?.getString("buildingName") as String
        val inStream: FileInputStream = this.openFileInput(fileName)
        var bmp: Bitmap = BitmapFactory.decodeStream(inStream)
        inStream.close()
        if (nodes == null || intarr == null) finish()
        val intArr = intarr as ArrayList<Int>
        var flNodes: FloatArray
        if (buildingName[0] == 'f') {
            flNodes = scaleDoubles(nodes as DoubleArray )
        }
        else {
            flNodes = scaleDoubles(nodes as DoubleArray, -83.716537, 42.2926866,  -83.716103, 42.29279955,
                29.318f, 549.536f, 37.166f, 204.9336f)
        }





        val conns: Connections = Connections(flNodes, intArr.toIntArray())
        val building: Building = Building(conns, bmp )

        view = DisplayView(this, building)
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
        buttonView.Post.setOnClickListener {
            val points = view.renderer.getPoints()
            val lines = view.renderer.getConnections()
            postPoints(points, lines, buildingName  )
        }

        buttonView.Clear.setOnClickListener {
            view.renderer.clear()
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

    fun invLerp(v1: Double, v2: Double, v: Double): Double {
        return (v - v1) / (v2 - v1)
    }

    fun lerp(v1: Float, v2: Float, t: Float): Float {
        return (v1*(1f-t) + v2*t)
    }

    // lower left (x1, y1) -> upper right (x2 ,y2)
    // NOTE for some reason the latitude comes first in the coords
    fun scaleDoubles(f: DoubleArray, x1: Double, y1: Double, x2: Double, y2: Double,
                     x1f: Float, x2f: Float, y1f: Float, y2f: Float): FloatArray {

        var fArr: ArrayList<Float> = ArrayList()
        for (i in 0 until f.size/2) {
            val tdx = invLerp(x1, x2, f[i*2]) //long first
            val tdy = invLerp(y1, y2, f[i*2+1])
            val txf = tdx.toFloat()
            val tyf = tdy.toFloat()
            val xf = lerp(x1f, x2f, txf)
            val yf = lerp(y1f, y2f, tyf)
            fArr.add(xf)
            fArr.add(yf)
            fArr.add(-5f)
            fArr.add(1f)

        }
        return fArr.toFloatArray()


    }

    fun scaleDoubles(f: DoubleArray): FloatArray {
        var fArr: ArrayList<Float> = ArrayList()
        for (i in 0 until f.size/2) {
            fArr.add(f[i*2].toFloat())
            fArr.add(f[i*2 + 1].toFloat())
            fArr.add(-5f)
            fArr.add(1f)
        }
        return fArr.toFloatArray()
    }

    fun dist(points: FloatArray, i1: Int, i2: Int) : Float {
        val f1x = points[i1*4]
        val f1y = points[i1*4 + 1]
        val f2x = points[i2*4]
        val f2y = points[i2*4 + 1]
        return sqrt((f2x - f1x)*(f2x - f1x) + (f2y - f1y)*(f2y - f1y))
    }

    fun adjacencyMatrix(customPoints: FloatArray, connections: IntArray): Array<FloatArray>{
        val adjacencyMatrix: Array<FloatArray> = (Array<FloatArray>(customPoints.size) {FloatArray(customPoints.size/4){ 10000000f}})
        for (i in 0 until connections.size/2) {
            adjacencyMatrix[connections[i*2]][connections[i*2 + 1]] = dist(customPoints, connections[i*2], connections[i*2+1])
            adjacencyMatrix[connections[i*2 + 1]][connections[i*2]] = dist(customPoints, connections[i*2], connections[i*2+1])
        }
        return adjacencyMatrix
    }

    // Make the connections into an adjacency matrix
    fun postPoints(customPoints: FloatArray, connections: IntArray, buildingName: String) {
        val adjMatrix = adjacencyMatrix(customPoints, connections)
        for (i in 0 until customPoints.size/4) {
            val flanders: ArrayList<Int> = ArrayList<Int>()
            adjMatrix[i][i] = 0f
            for (j in 0 until adjMatrix[i].size) {
                if (adjMatrix[i][j] < 1000000f) {
                    flanders.add(j)
                    System.out.print(j)
                    System.out.print(" ")
                }
            }
            System.out.print("\n")
            postPoint(i, buildingName, applicationContext, customPoints, flanders.toIntArray() )
        }
    }

    fun postPoint(index: Int, buildingName: String, ctx:Context, customPoints: FloatArray, neighbors: IntArray ) {
        val connectionsObj = JSONArray(listOf(0))
        val jsonObj = mapOf(
            "building_name" to buildingName,
            "name" to "t",
            "id" to 1,
            "type" to "t",
            "floor" to 2,
            "coordinates" to JSONArray(listOf(customPoints[index*4+1],  customPoints[index*4])),
            "neighbors" to JSONArray(neighbors)
        )
        val postReq = JsonObjectRequest(Request.Method.POST, serverUrl + "postnodes/",
            JSONObject( jsonObj),   { Log.d("postmaps", "chatt posted!") },
            { error -> Log.e("postmaps", error.localizedMessage ?: "JsonObjectRequest error") }
        )
        if (!this::queue.isInitialized) {
            queue = Volley.newRequestQueue(ctx)
        }
        queue.add(postReq)
    }


    // Pull the nodes down from the server





}