package edu.umich.mwassink.mapdisplay

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.Volley.newRequestQueue
import edu.umich.mwassink.mapdisplay.databinding.ActivityDisplayBinding
import edu.umich.mwassink.mapdisplay.databinding.NavigateBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.FileInputStream
import java.util.*
import com.travijuu.numberpicker.library.NumberPicker
import java.util.concurrent.Semaphore
import kotlin.collections.ArrayList
import kotlin.math.*

// Uses some Code from Paul Lawitzki, https://www.codeproject.com/Articles/729759/Android-Sensor-Fusion-Tutorial
// In order to fuse sensors and get more accurate readings from them

class DisplayActivity: AppCompatActivity(), SensorEventListener {

    lateinit var view: DisplayView
    lateinit var buttonView: ActivityDisplayBinding
    var steps: Float = 0f
    var sensorOn = false
    var firstValue: Float = -1f
    val serverUrl: String = "https://52.14.13.109/"
    private lateinit var queue: RequestQueue
  
    // TODO need to experiment with this
    val stepLength: Float = 10f
    var sensor: Sensor? = null
    var initialTime: Float = -1F
    var gyroBegun: Boolean = false
    var stepCounterBegun: Boolean = false

    // Code taken from Paul Lawitzki, https://www.codeproject.com/Articles/729759/Android-Sensor-Fusion-Tutorial
    private var mSensorManager: SensorManager?  = null

    // angular speeds from gyro
    private var gyro = FloatArray(3)

    // rotation matrix from gyro data
    private var gyroMatrix = FloatArray(9)

    // orientation angles from gyro matrix
    private var gyroOrientation = FloatArray(3)

    // magnetic field vector
    private var magnet = FloatArray(3)

    // accelerometer vector
    private var accel = FloatArray(3)

    // orientation angles from accel and magnet
    private var accMagOrientation = FloatArray(3)

    // Variables used to average the original accMagOrientation data
    private var accMagOrientationSum = FloatArray(3)
    private var accMagOrientationAverage = FloatArray(3)
    private var accMagOrientationCount: Int = 0

    // Array to hold our current world acceleration totals
    private var worldAccelerationSumArray = FloatArray(3) {0F}

    // final orientation angles from sensor fusion
    private var fusedOrientation = FloatArray(3)

    // accelerometer and magnetometer based rotation matrix
    private var rotationMatrix = FloatArray(9)

    // TODO: need to experiment with time_constant and filter_co
    // How often to run our filter
    val TIME_CONSTANT: Long = 30
    // How heavy of a filter to apply, closer to 1 means more gyro and less accMag
    val FILTER_COEFFICIENT = 1.0f
    private val fuseTimer = Timer()
    // END Code from Paul Lawitzki
    var floorNum = -1
    var numPoints: Int = 0
    lateinit var navView: NavigateBinding
    private var currState: NavigationState = NavigationState.BROWSING
    var room: String = ""
    var buildingName = ""

    enum class NavigationState {
        BROWSING, NAVIGATING, REPONSITIONING_B, REPONSITIONING_N, ARRIVE
    }

    init {

    }

    override fun onCreate(bundle: Bundle?): Unit{

        super.onCreate(bundle)
        val extras: Bundle? = intent.extras
        val fileName: String? = extras?.getString("buildingFile")
        val intarr = extras?.getIntegerArrayList("connections")
        val nodes: DoubleArray?= extras?.getDoubleArray("nodes")
        buildingName = extras?.getString("buildingName") as String
        room = extras?.getString("roomName") as String
        floorNum = extras?.getInt("floorNum")
        val inStream: FileInputStream = this.openFileInput(fileName)
        var bmp: Bitmap = BitmapFactory.decodeStream(inStream)
        inStream.close()
        if (nodes == null || intarr == null) finish()
        val intArr = intarr as ArrayList<Int>
        numPoints = nodes?.size as Int
        numPoints /= 2
        var flNodes: FloatArray
        if (buildingName != "BBB" && buildingName != "GOOB") {
            flNodes = scaleDoubles(nodes as DoubleArray )
        }
        else {
            flNodes = scaleDoubles(nodes as DoubleArray, -83.716537, 42.2926866,  -83.716103, 42.29279955,
                29.318f, 549.536f, 37.166f, 204.9336f)
        }

        negify(flNodes, intarr )





        val conns: Connections = Connections(flNodes, intArr.toIntArray())
        val building: Building = Building(conns, bmp, floorNum)

        view = DisplayView(this, building)
        setContentView(view)

        buttonView = ActivityDisplayBinding.inflate(layoutInflater)
        //addContentView(buttonView.root,
        //    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))


        navView = NavigateBinding.inflate(layoutInflater)
        addContentView(navView.root,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))


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
            postPoints(points, lines, buildingName, view.roomMap)

        }

        buttonView.Clear.setOnClickListener {
            view.renderer.clear()
        }

        buttonView.UpButton.setOnClickListener {
            val launcher: MapLauncher =  MapLauncher()
            launcher.launchGL(buildingName, this, floorNum+1, room)

        }

        navView.exitButton.setOnClickListener {
            onBackPressed()
        }

        navView.startButton.setOnClickListener{
            transition(it)
        }
        navView.repositionButton.setOnClickListener{
            transition(it)
        }

        navView.destination.text = buildingName + " " + room
        hideFloorSelector()
        
        
        // Code taken from Paul Lawitzki, https://www.codeproject.com/Articles/729759/Android-Sensor-Fusion-Tutorial
        gyroOrientation[0] = 0.0f
        gyroOrientation[1] = 0.0f
        gyroOrientation[2] = 0.0f

        // initialise gyroMatrix with identity matrix
        gyroMatrix[0] = 1.0f
        gyroMatrix[1] = 0.0f
        gyroMatrix[2] = 0.0f
        gyroMatrix[3] = 0.0f
        gyroMatrix[4] = 1.0f
        gyroMatrix[5] = 0.0f
        gyroMatrix[6] = 0.0f
        gyroMatrix[7] = 0.0f
        gyroMatrix[8] = 1.0f

        val reqResult = ActivityResultContracts.RequestPermission()

        val reqLauncher = registerForActivityResult(reqResult) {
                granted ->
            if (!granted) {
                System.exit(1)
            }
        }

        reqLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)

        // get sensorManager and initialise sensor listeners
        mSensorManager = this.getSystemService(SENSOR_SERVICE) as SensorManager
        initListeners()


        // wait for one second until gyroscope and magnetometer/accelerometer
        // data is initialised then schedule the complementary filter task
        fuseTimer.scheduleAtFixedRate(
            CalculateFusedOrientationTask(),
            1000, TIME_CONSTANT
        )
        // END Code from Paul Lawitzki
    }

    private fun showStart() {
        navView.startButton.visibility = Button.VISIBLE
    }
    private fun hideStart() {
        navView.startButton.visibility = Button.INVISIBLE
    }
    private fun showReposition() {
        navView.repositionButton.text = "Reposition"
        navView.repositionButton.setBackgroundColor(Color.parseColor("#FBC520"))
    }
    private fun showRepositionDone() {
        navView.repositionButton.text = "Done"
        navView.repositionButton.setBackgroundColor(Color.parseColor("#6EE14F"))
    }
    private fun showFloorSelector() {
        navView.currentFloorHeader.visibility = TextView.VISIBLE
        navView.floorPicker.visibility = NumberPicker.VISIBLE
    }
    private fun hideFloorSelector() {
        navView.currentFloorHeader.visibility = TextView.INVISIBLE
        navView.floorPicker.visibility = NumberPicker.INVISIBLE
    }
    private fun transition(clickedButton: View) {
        with (clickedButton as Button) {
            Log.d("Transition", "CurrState: $currState, Action: $text, $id")
        }
        when (clickedButton.id) {
            navView.startButton.id -> {
                when (currState) {
                    NavigationState.BROWSING -> { currState = NavigationState.NAVIGATING; hideStart() }
                }
            }
            navView.repositionButton.id -> {
                with (clickedButton as Button) {
                    if (text == "Reposition") {
                        when (currState) {
                            NavigationState.BROWSING -> { currState =
                                NavigationState.REPONSITIONING_B
                            }
                            NavigationState.NAVIGATING -> { currState =
                                NavigationState.REPONSITIONING_N
                            }
                        }
                        createRepositionPopupWindow()
                        hideStart()
                        showRepositionDone()
                        showFloorSelector()

                        // same to turning on repos mode
                        view.setLineMode(false)
                        view.setPointMode(false)
                        view.setMoveMode(false)
                        view.setDragMode(true)

                    }
                    else {
                        when (currState) {
                            NavigationState.REPONSITIONING_B -> { currState =
                                NavigationState.BROWSING; showStart() }
                            NavigationState.REPONSITIONING_N -> { currState =
                                NavigationState.NAVIGATING
                            }
                        }
                        showReposition()
                        hideFloorSelector()
                        // same as turning on move mdode
                        view.setLineMode(false)
                        view.setPointMode(false)
                        view.setMoveMode(true)
                        view.setDragMode(false)
                        view.renderer.walkPast()
                        checkQuit()

                        if (navView.floorPicker.value != floorNum) {
                            val launcher = MapLauncher()


                            launcher.launchGL(buildingName, context, navView.floorPicker.value, room )
                        }
                    }
                }
            }
        }
    }
    private fun createRepositionPopupWindow() {
        val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.resposition_tutorial, null)

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        popupWindow.showAtLocation(
            navView.repositionButton, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )

        val reposTutorialOKbutton = popupView.findViewById<Button>(R.id.reposTutorialOKbutton)
        reposTutorialOKbutton.setOnClickListener{
            popupWindow.dismiss()
        }
    }

    // Code taken from Paul Lawitzki, https://www.codeproject.com/Articles/729759/Android-Sensor-Fusion-Tutorial
    fun initListeners() {
        mSensorManager!!.registerListener(
            this,
            mSensorManager!!.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
            SensorManager.SENSOR_DELAY_FASTEST
        )
        mSensorManager!!.registerListener(
            this,
            mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_FASTEST
        )
        mSensorManager!!.registerListener(
            this,
            mSensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
            SensorManager.SENSOR_DELAY_FASTEST
        )
        mSensorManager!!.registerListener(
            this,
            mSensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_FASTEST
        )
        mSensorManager!!.registerListener(
            this,
            mSensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
            SensorManager.SENSOR_DELAY_UI
        ) // Could possibly change delay to "fastest"
        sensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    // Code taken from Paul Lawitzki, https://www.codeproject.com/Articles/729759/Android-Sensor-Fusion-Tutorial
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                // no longer need to worry about averaging the accMag orientation average if the gyro has begun
                if (gyroBegun) return

                // copy new accelerometer data into accel array
                // then calculate new orientation
                System.arraycopy(event.values, 0, accel, 0, 3)

                calculateAccMagOrientation()
            }
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                if (!gyroBegun) {
                    return
                }

                calculateWorldMovement(event)
            }
            Sensor.TYPE_GYROSCOPE -> {     // process gyro data
                gyroFunction(event)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {     // copy new magnetometer data into magnet array
                // no longer need to worry about averaging the accMag orientation average if the gyro has begun
                if (gyroBegun) return

                System.arraycopy(event.values, 0, magnet, 0, 3)
            }
            Sensor.TYPE_STEP_COUNTER -> {
                if (!gyroBegun) return
                stepCounterEvent(event)
            }
        }
    }

    // END Code from Paul Lawitzki

    fun stepCounterEvent(event: SensorEvent) {
        if (!stepCounterBegun) {
            stepCounterBegun = true
            steps = event.values[0]
            return
        }

        var prevSteps = steps
        if (sensor != null) {
            steps = event.values[0]
            val newSteps = steps - prevSteps

            var worldDistanceXratio: Float = 0F
            var worldDistanceYratio: Float = 0F

            if (newSteps > 0) {
                // normalize the direction that the user moved in the time that it took them to make a step
                val worldDistanceX = worldAccelerationSumArray[0]
                val worldDistanceY = worldAccelerationSumArray[1]
                val totalDistance = abs(worldDistanceX) + abs(worldDistanceY)
                worldDistanceXratio = worldDistanceX / totalDistance
                worldDistanceYratio = worldDistanceY / totalDistance

                // reset the world acceleration array each time that steps are taken
                worldAccelerationSumArray = FloatArray(3) {0F}
            }

            // Multiply the distance by the direction and send this to the display
            val xDistance = newSteps * stepLength * worldDistanceXratio
            val yDistance = newSteps * stepLength * worldDistanceYratio
            view.changePos(xDistance, yDistance) // for now
            checkQuit()
            view.renderer.walkPast()

        }
    }

    // Code taken from Paul Lawitzki, https://www.codeproject.com/Articles/729759/Android-Sensor-Fusion-Tutorial
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    private fun calculateAccMagOrientation() {
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation)
        }

        accMagOrientationCount++
        accMagOrientationSum[0] += accMagOrientation[0]
        accMagOrientationSum[1] += accMagOrientation[1]
        accMagOrientationSum[2] += accMagOrientation[2]
    }



    private val NS2S = 1.0f / 1000000000.0f
    private var gyroTimestamp = 0f
    private var initState = true

    private fun gyroFunction(event: SensorEvent) {
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (initialTime == -1F) {
            initialTime = event.timestamp.toFloat()
            return
        }

        // Wait 10 seconds at the beginning for the phone to realize its position in the world
        // Ideally this will be while the user is outside a building and away from artificial electromagnetic fields
        val timeSinceBegan: Float = (event.timestamp.toFloat() - initialTime) * 0.000000001F
        // This time could be increased to get a better idea for how the phone is oriented in the world
        // For this to happen though, we would have to add a calibration function at the beginning to ensure that the user was not moving the phone in order to get a good average of where the phone is
        // This calibration would be a good opportunity to calibrate the linear acceleration offset as mentioned at https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-linear
        if (timeSinceBegan < 1) return
        gyroBegun = true

        // Average the accMagOrientation over the first 10 seconds to be used in the gyro initialization
        accMagOrientationAverage[0] = accMagOrientationSum[0] / accMagOrientationCount
        accMagOrientationAverage[1] = accMagOrientationSum[1] / accMagOrientationCount
        accMagOrientationAverage[2] = accMagOrientationSum[2] / accMagOrientationCount


        // initialisation of the gyroscope based rotation matrix
        if (initState) {
            var initMatrix: FloatArray? = FloatArray(9)
            initMatrix = getRotationMatrixFromOrientation(accMagOrientationAverage)
            val test = FloatArray(3)
            SensorManager.getOrientation(initMatrix, test)
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix)
            initState = false
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        val deltaVector = FloatArray(4)
        if (gyroTimestamp != 0f) {
            val dT = (event.timestamp - gyroTimestamp) * NS2S
            System.arraycopy(event.values, 0, gyro, 0, 3)
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f)
        }

        // measurement done, save current time for next interval
        gyroTimestamp = event.timestamp.toFloat()

        // convert rotation vector into rotation matrix
        val deltaMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector)

        // apply the new rotation interval on the gyroscope based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix)

        // get the gyroscope based orientation from the rotation matrix
        SensorManager.getOrientation(gyroMatrix, gyroOrientation)
    }


    // Value used to check if gyroscope rotation vector is big enough
    // TODO: experiment with this value
    private val EPSILON = 0.000000001f

    private fun getRotationVectorFromGyro(
        gyroValues: FloatArray,
        deltaRotationVector: FloatArray,
        timeFactor: Float
    ) {
        val normValues = FloatArray(3)

        // Calculate the angular speed of the sample
        val omegaMagnitude =
            sqrt((gyroValues[0] * gyroValues[0] + gyroValues[1] * gyroValues[1] + gyroValues[2] * gyroValues[2]))

        // Normalize the rotation vector if it's big enough to get the axis
        if (omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude
            normValues[1] = gyroValues[1] / omegaMagnitude
            normValues[2] = gyroValues[2] / omegaMagnitude
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        val thetaOverTwo = omegaMagnitude * timeFactor
        val sinThetaOverTwo = sin(thetaOverTwo)
        val cosThetaOverTwo = cos(thetaOverTwo)
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0]
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1]
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2]
        deltaRotationVector[3] = cosThetaOverTwo
    }

    // This takes orientation angles and converts them to a rotation matrix
    // TODO potentially optimize this function
    private fun getRotationMatrixFromOrientation(o: FloatArray): FloatArray {
        val xM = FloatArray(9)
        val yM = FloatArray(9)
        val zM = FloatArray(9)
        val sinX = sin(o[1])
        val cosX = cos(o[1])
        val sinY = sin(o[2])
        val cosY = cos(o[2])
        val sinZ = sin(o[0])
        val cosZ = cos(o[0])

        // rotation about x-axis (pitch)
        xM[0] = 1.0f
        xM[1] = 0.0f
        xM[2] = 0.0f
        xM[3] = 0.0f
        xM[4] = cosX
        xM[5] = sinX
        xM[6] = 0.0f
        xM[7] = -sinX
        xM[8] = cosX

        // rotation about y-axis (roll)
        yM[0] = cosY
        yM[1] = 0.0f
        yM[2] = sinY
        yM[3] = 0.0f
        yM[4] = 1.0f
        yM[5] = 0.0f
        yM[6] = -sinY
        yM[7] = 0.0f
        yM[8] = cosY

        // rotation about z-axis (azimuth)
        zM[0] = cosZ
        zM[1] = sinZ
        zM[2] = 0.0f
        zM[3] = -sinZ
        zM[4] = cosZ
        zM[5] = 0.0f
        zM[6] = 0.0f
        zM[7] = 0.0f
        zM[8] = 1.0f

        // rotation order is y, x, z (roll, pitch, azimuth)
        var resultMatrix: FloatArray? = matrixMultiplication(xM, yM)
        resultMatrix = matrixMultiplication(zM, resultMatrix!!)
        return resultMatrix
    }

    private fun matrixMultiplication(A: FloatArray, B: FloatArray): FloatArray {
        val result = FloatArray(9)
        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6]
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7]
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8]
        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6]
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7]
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8]
        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6]
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7]
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8]
        return result
    }

    inner class CalculateFusedOrientationTask : TimerTask() {
        override fun run() {
            val oneMinusCoeff: Float = 1.0f - FILTER_COEFFICIENT
            fusedOrientation[0] = (FILTER_COEFFICIENT * gyroOrientation[0]
                    + oneMinusCoeff * accMagOrientation[0])
            fusedOrientation[1] = (FILTER_COEFFICIENT * gyroOrientation[1]
                    + oneMinusCoeff * accMagOrientation[1])
            fusedOrientation[2] = (FILTER_COEFFICIENT * gyroOrientation[2]
                    + oneMinusCoeff * accMagOrientation[2])

            // overwrite gyro matrix and orientation with fused orientation
            // to compensate gyro drift
            gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation)
            System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3)
        }
    }
    // END Code from Paul Lawitzki

    // used to find hardcoded linear acceleration offset
    var xAccelerationSum = 0.0
    var yAccelerationSum = 0.0
    var zAccelerationSum = 0.0
    var linAccelCount = 0

    // values found by laying the phone flat and finding the average linear acceleration, this is the offset that will be subtracted below
    val xAccelerationOffset = -0.03
    val yAccelerationOffset = -0.0224
    val zAccelerationOffset = -0.00062
    
    private fun calculateWorldMovement(event: SensorEvent) {
        // phone linear accelerations, need to be corrected for orientation
        val xAcceleration = event.values[0] - xAccelerationOffset
        val yAcceleration = event.values[1] - yAccelerationOffset
        val zAcceleration = event.values[2] - zAccelerationOffset

        // update linear acceleration sums and count, used to find offsets
        xAccelerationSum += xAcceleration
        yAccelerationSum += yAcceleration
        zAccelerationSum += zAcceleration
        linAccelCount++

        // Calculate average thus far and print to log
        // As long as we hold the phone flat, we can use these values as the offset to be subtracted
        val xAccelerationAverage: Double = xAccelerationSum / linAccelCount
        val yAccelerationAverage: Double = yAccelerationSum / linAccelCount
        val zAccelerationAverage: Double = zAccelerationSum / linAccelCount
//        Log.d("offsets: ", "x: " + xAccelerationAverage + "\ny: " + yAccelerationAverage + "\nz: " + zAccelerationAverage)

        // current phone orientation angles, used to correct the above accelerations
        val cosX = cos(fusedOrientation[1])
        val cosY = cos(fusedOrientation[2])
        val cosZ = cos(fusedOrientation[0])
        val sinX = sin(fusedOrientation[1])
        val sinY = sin(fusedOrientation[2])
        val sinZ = sin(fusedOrientation[0])

        // calculating the acceleration of the phone compared to the world axis
        // The z World Acc can be disregarded for now as we are only moving along a given floor, but may be needed later
        val xWorldAcceleration = xAcceleration * (cosZ + cosY) + yAcceleration * sinZ + zAcceleration * sinY
        val yWorldAcceleration = yAcceleration * (cosZ + cosX) + zAcceleration * sinX - xAcceleration * sinZ
        val zWorldAcceleration = zAcceleration * (cosX + cosY) - xAcceleration * sinY - yAcceleration * sinX

        // add new accelerations to the running sum to be averaged later and increase the count of total measurements
        worldAccelerationSumArray[0] += xWorldAcceleration.toFloat()
        worldAccelerationSumArray[1] += yWorldAcceleration.toFloat()
        worldAccelerationSumArray[2] += zWorldAcceleration.toFloat()
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
        for (i in 0 until f.size/3) {
            val tdx = invLerp(x1, x2, f[i*3]) //long first
            val tdy = invLerp(y1, y2, f[i*3+1])
            val txf = tdx.toFloat()
            val tyf = tdy.toFloat()
            val xf = lerp(x1f, x2f, txf)
            val yf = lerp(y1f, y2f, tyf)
            fArr.add(xf)
            fArr.add(yf)
            fArr.add(f[i*3 + 2].toFloat() * -1f)
            fArr.add(1f)

        }
        return fArr.toFloatArray()


    }

    fun scaleDoubles(f: DoubleArray): FloatArray {
        var fArr: ArrayList<Float> = ArrayList()
        for (i in 0 until f.size/3) {
            fArr.add(f[i*3].toFloat())
            fArr.add(f[i*3 + 1].toFloat())
            fArr.add(f[i*3 + 2].toFloat() * -1f)
            fArr.add(1f)
        }
        return fArr.toFloatArray()
    }

    fun makeNeg(f: Float): Float {
        if ( f > 0) {
            return f * -1
        }
        return f

    }

    fun negify(fArr: FloatArray, conns: ArrayList<Int>){
        for (i in 0 until conns.size) {
            val validNode = conns[i]
            fArr[validNode * 4 + 2] = makeNeg(fArr[validNode * 4 + 2])
        }
    }

    fun posify(fArr: FloatArray, conns: ArrayList<Int>){
        for (i in 0 until conns.size) {
            val validNode = conns[i]
            fArr[validNode * 4 + 2] = abs(fArr[validNode * 4 + 2])
        }
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
    fun postPoints(customPoints: FloatArray, connections: IntArray, buildingName: String, ptMap: MutableMap<Int, String>) {
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
            if (i >= numPoints) {
                postPoint(i, buildingName, applicationContext, customPoints, flanders.toIntArray(), ptMap )
            }

        }
    }

    fun postPoint(index: Int, buildingName: String, ctx:Context, customPoints: FloatArray, neighbors: IntArray, ptMap: MutableMap<Int, String> ) {
        val connectionsObj = JSONArray(listOf(0))
        val jsonObj = mapOf(
            "building_name" to buildingName,
            "name" to ptMap[index],
            "id" to -1,
            "type" to "Room",
            "floor" to floorNum,
            "coordinates" to JSONArray(listOf(customPoints[index*4],  customPoints[index*4+1])),
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



    fun checkQuit() {
        if (view.renderer.isFinished()) {
            val s = Semaphore(0)
            val inflater: LayoutInflater = getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.arrival, null)

            val popupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            popupWindow.showAtLocation(
                navView.floorPicker, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
            )

            val returnButton = popupView.findViewById<Button>(R.id.returnButton)
            returnButton.setOnClickListener{
                onBackPressed()
            }

        }
    }

}