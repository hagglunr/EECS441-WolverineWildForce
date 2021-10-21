package edu.umich.mwassink.mapdisplay

import android.app.Activity
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.app.AppCompatActivity


class DisplayActivity: AppCompatActivity() {

    lateinit var view: DisplayView


    init {

    }

    override fun onCreate(bundle: Bundle?): Unit{
        super.onCreate(bundle)
        view = DisplayView(this)
        setContentView(view)
    }







    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // Let the ScaleGestureDetector inspect all events.

        return true
    }


}