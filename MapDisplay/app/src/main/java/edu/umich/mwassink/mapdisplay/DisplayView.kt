package edu.umich.mwassink.mapdisplay

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.text.InputType
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
import android.widget.EditText
import android.content.DialogInterface








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
    var roomMap: MutableMap<Int, String> = mutableMapOf<Int, String>()


    var trackLines: Boolean = false
    var drag: Boolean = false
    var annotate: Boolean = false
    var l1: Int = -1
    var l2: Int = -1

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


        if (trackLines) {
            if (l1 == -1) {
                l1 = renderer.ClosestPoint(x, y)
            } else {
                l2 = renderer.ClosestPoint(x, y)
                if (l2 == l1) {

                }
                else {


                    renderer.addLine(l1, l2)
                    l1 = -1
                    l2 = -1
                }
            }
        } else if (drag) {
            val cp = renderer.ClosestPoint(x, y)
            renderer.SetPoint(cp, x, y)
        } else if (renderer.PointMode) {


            var txt = ""
            //https://stackoverflow.com/questions/10903754/input-text-dialog-android
            var bob = AlertDialog.Builder(context)
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT
            bob.setView(input)
            bob.setPositiveButton("OK",
                DialogInterface.OnClickListener {
                        dialog,
                        which ->
                    txt = input.text.toString()
                synchronized(this) {

                    renderer.addPoint(x, y)
                    val cp = renderer.ClosestPoint(x,y)
                    roomMap[cp] = txt

                }})
            bob.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

            bob.show()

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
        if (!trackLines) {
            l1 = -1
            l2 = -1
        }

    }

    fun setDragMode(what: Boolean) {
        drag = what
    }




}