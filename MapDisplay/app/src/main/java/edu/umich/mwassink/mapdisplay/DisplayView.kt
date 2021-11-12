package edu.umich.mwassink.mapdisplay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import kotlin.math.sqrt


class DisplayView (ctx: Context, building: Building) : GLSurfaceView(ctx) {
    val renderer: DisplayRenderer
    var scale: Float
    var mScaleDetector: ScaleGestureDetector
    var scaleListener: ScaleGestureDetector.SimpleOnScaleGestureListener
    var gestureListener: GestureDetector.SimpleOnGestureListener
    var gestureDetector: GestureDetector
    var transRight: Float
    var transUp: Float
    var scaleFactor: Float
    var textureHandle = -1
    var nodes =  arrayListOf<Double>()
    val serverUrl: String = "https://52.14.13.109/"
    lateinit var queue: RequestQueue
    lateinit var mostRecent: JSONArray
    var handledReq: Boolean = false
    var trackLines: Boolean = false
    var l1: Int = -1
    var l2: Int = -1
    var urlMe: String = "https://18.219.253.107/getmaps/"
    var urlBBB: String = "https://52.14.13.109/getrooms/"
    var lastX: Float = -1f
    var lastY: Float = -1f

    init {
        super.setEGLConfigChooser(8 , 8, 8, 8, 16, 0);
        setEGLContextClientVersion(2)
        setPreserveEGLContextOnPause(true)

        scale = 1f
        scaleFactor = 1f
        transRight = 0f
        transUp = 0f



        scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

            override fun onScale(detector: ScaleGestureDetector): Boolean {

                print("scale changed")
                scaleFactor *= detector.scaleFactor

                // Don't let the object get too small or too large.
                scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 15.0f))

                return true
            }
        }
        mScaleDetector = ScaleGestureDetector(context, scaleListener)
        gestureListener = object : GestureDetector.SimpleOnGestureListener() {

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                transRight = distanceX / 1.5f / scaleFactor
                transUp = distanceY / 1.5f / scaleFactor
                return true
            }
        }

        gestureDetector = GestureDetector(context, gestureListener)

        renderer = DisplayRenderer(this, building)
        setRenderer(renderer)
        getNodes("BBB", context) {

        }

    }




    override fun onTouchEvent(ev: MotionEvent): Boolean{
        println("touch changed")


        var x: Float = ev.getX()
        var y: Float = ev.getY()
        var dx: Float = lastX - x
        var dy: Float = lastY - y

        if (sqrt((dx)*(dx) + dy*dy) > 25f)
        lastX = x
        lastY = y
        renderer.addPoint(x, y)

        if (trackLines) {
            if (l1 == -1) {
                l1 = renderer.ClosestPoint(x, y)
            } else {
                l2 = renderer.ClosestPoint(x, y)
                if (l2 == l1) {
                    l2 = -1
                    l1 = -1
                }
                else {


                    renderer.addLine(l1, l2)
                    l1 = -1
                    l2 = -1
                }
            }
        }
        println(java.lang.String.format("Event at (%f, %f)", x, y))

        mScaleDetector.onTouchEvent(ev)
        gestureDetector.onTouchEvent(ev)
        setZoom(scaleFactor)
        renderer.changeTrans(transRight, transUp)
        return true
    }

    fun setZoom(scaleIn: Float) {
        scale = scaleIn
        renderer.changeScale(scale)
    }

    fun changePos(dx: Float, dy: Float) {
        renderer.changePos(dx, dy)
    }

    fun getNodes(building: String, context: Context, completion: () -> Unit) {
        val getRequest = JsonObjectRequest( urlBBB,
            { response ->
                Toast.makeText(context, "Handling Request",
                    Toast.LENGTH_SHORT).show();
                nodes.clear()
                handledReq = true
                val nodesReceived = try { response.getJSONArray("rooms") } catch (e: JSONException) { JSONArray() }
                for (i in 0 until nodesReceived.length()) {
                    val chattEntry = nodesReceived[i] as JSONArray
                    mostRecent  = nodesReceived[i] as JSONArray
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
            queue = Volley.newRequestQueue(context)
        }
        queue.add(getRequest)
    }

    // If I have a bunch of 2D points, then I can encode them in a different form with an s and a t value
    // based off of some min and max points and interpolation
    // e.g min (25, 25), max (50, 75)... 45, 50 -> .8, .5
    fun interpArray(minX: Double, maxX: Double, minY: Double, maxY: Double, interleavedPoints: DoubleArray): FloatArray {
        System.exit(1)
        var f: FloatArray = FloatArray(interleavedPoints.size)
        return f


    }

    fun setMoveMode(what: Boolean) {
        renderer.SetMoveMode(what)

    }

    fun setPointMode(what: Boolean) {
        renderer.SetPointMode(what)

    }

    fun setLineMode(what: Boolean) {
        renderer.SetLineMode(what)
        trackLines = what

    }


}