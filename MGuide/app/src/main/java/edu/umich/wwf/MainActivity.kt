package edu.umich.wwf

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.Volley.newRequestQueue
import edu.umich.wwf.BuildingInfoStore.buildingRoomMap
import edu.umich.wwf.BuildingInfoStore.getBuildings
import edu.umich.wwf.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException

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
                val selectedBuilding = resources.getStringArray(R.array.buildings)[p2]
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
            val room = view.roomsSearchableSpinner.getSelectedItem().toString()
            Toast.makeText(applicationContext, "Searching $building $room ...", Toast.LENGTH_SHORT).show()
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
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}