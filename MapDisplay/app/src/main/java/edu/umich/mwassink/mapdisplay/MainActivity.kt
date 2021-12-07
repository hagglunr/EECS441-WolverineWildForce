package edu.umich.mwassink.mapdisplay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import edu.umich.mwassink.mapdisplay.BuildingInfoStore.buildingRoomMap
import edu.umich.mwassink.mapdisplay.BuildingInfoStore.getBuildings
import edu.umich.mwassink.mapdisplay.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var view: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        view = ActivityMainBinding.inflate(layoutInflater)
        setContentView(view.root)

        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                toast("Fine location access denied", false)
                finish()
            }
        }.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        createStartButton()
    }



    private fun createStartButton() {


        view.arriveButton.setOnClickListener {
            val la = MapLauncher()
            la.launchGL("Michigan League", this, 1, roomn = "")
        }
    }
}