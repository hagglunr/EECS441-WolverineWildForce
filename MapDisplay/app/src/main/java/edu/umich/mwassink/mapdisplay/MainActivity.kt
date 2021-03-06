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

        if (buildingRoomMap.isEmpty()) {
            fetchBuildingInfo()
        }
        else {
            createSearchView()
        }

        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                toast("Fine location access denied", false)
                finish()
            }
        }.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun fetchBuildingInfo() {
        getBuildings(applicationContext) {
            createSearchView()
        }
    }

    private fun createSearchView() {
        val buildingSpinnerViewAdapter = ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_item,
            (buildingRoomMap.keys).toCollection(ArrayList<String>()))
        view.buildingSearchableSpinner.adapter = buildingSpinnerViewAdapter
        view.buildingSearchableSpinner.setTitle("Select Building");
        view.buildingSearchableSpinner.setPositiveButton("OK");

        view.buildingSearchableSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedBuilding = view.buildingSearchableSpinner.getSelectedItem().toString()
                var rooms = arrayListOf<String>()
                if (buildingRoomMap[selectedBuilding] != null) {
                    rooms = buildingRoomMap[selectedBuilding]!!
                }
                view.roomsSearchableSpinner.adapter = ArrayAdapter<String>(this@MainActivity,
                    android.R.layout.simple_spinner_item,
                    rooms)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        view.roomsSearchableSpinner.setTitle("Select Rooms");
        view.roomsSearchableSpinner.setPositiveButton("OK");
        createArrivalButton()
    }

    private fun createArrivalButton() {
        view.searchButton.setOnClickListener {
            val building = view.buildingSearchableSpinner.getSelectedItem().toString()

            if (view.roomsSearchableSpinner.getSelectedItem() != null) {
                val launcher = MapLauncher()
                val room = view.roomsSearchableSpinner.getSelectedItem().toString()
                Toast.makeText(applicationContext, "Searching $building $room ...", Toast.LENGTH_SHORT).show()
                launcher.launchGL(building, this, 1, roomn = room)
            }
        }

        view.arriveButton.setOnClickListener {
            val inflater: LayoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.arrival, null)

            val popupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            popupWindow.showAtLocation(
                view.arriveButton, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
            )

            val returnButton = popupView.findViewById<Button>(R.id.returnButton)
            returnButton.setOnClickListener{
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }
}