package edu.umich.mwassink.mapdisplay

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class DisplayActivity: Activity() {

    lateinit var view: DisplayView
    init {

    }

    override fun onCreate(bundle: Bundle?): Unit{
        super.onCreate(bundle)
        view = DisplayView(this)
        setContentView(view)
    }

}