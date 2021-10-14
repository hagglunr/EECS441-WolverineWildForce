package edu.umich.mwassink.mapdisplay

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent

class DisplayView (ctx: Context) : GLSurfaceView(ctx) {
    val renderer: DisplayRenderer
    init {
        super.setEGLConfigChooser(8 , 8, 8, 8, 16, 0);
        setEGLContextClientVersion(2)
        setPreserveEGLContextOnPause(true)
        renderer = DisplayRenderer(this)

        setRenderer(renderer)


    }

    override fun onTouchEvent(ev: MotionEvent): Boolean{
        return true
    }
}