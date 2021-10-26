package edu.umich.mwassink.mapdisplay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector

class DisplayView (ctx: Context) : GLSurfaceView(ctx) {
    val renderer: DisplayRenderer
    var scale: Float
    lateinit var mScaleDetector: ScaleGestureDetector
    lateinit var scaleListener: ScaleGestureDetector.SimpleOnScaleGestureListener
    lateinit var gestureListener: GestureDetector.SimpleOnGestureListener
    lateinit var gestureDetector: GestureDetector
    var transRight: Float
    var transUp: Float
    var scaleFactor: Float
    var textureHandle = -1
    init {
        super.setEGLConfigChooser(8 , 8, 8, 8, 16, 0);
        setEGLContextClientVersion(2)
        setPreserveEGLContextOnPause(true)
        renderer = DisplayRenderer(this)
        setRenderer(renderer)
        scale = 1f
        scaleFactor = 1f
        transRight = 0f
        transUp = 0f


        scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

            override fun onScale(detector: ScaleGestureDetector): Boolean {

                print("scale changed")
                scaleFactor *= detector.scaleFactor

                // Don't let the object get too small or too large.
                scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f))

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

    }




    override fun onTouchEvent(ev: MotionEvent): Boolean{
        println("touch changed")

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



}