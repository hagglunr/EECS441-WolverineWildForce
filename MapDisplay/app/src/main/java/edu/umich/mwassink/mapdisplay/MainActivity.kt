package edu.umich.mwassink.mapdisplay

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Path
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import edu.umich.mwassink.mapdisplay.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var view: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        view = ActivityMainBinding.inflate(layoutInflater)
//        view.root.setBackgroundColor(
//            Color.parseColor("#3399FF"))
        setContentView(view.root)

        view.buildingSpinnerView.setAdapter(ArrayAdapter<String>(this,
                                            android.R.layout.simple_spinner_item,
                                            resources.getStringArray(R.array.buildings)))

        view.roomSpinnerView.setAdapter(ArrayAdapter(this,
                                        android.R.layout.simple_spinner_item,
                                        arrayOf<String>()))

        view.buildingSpinnerView.onItemSelectedListener = object :  AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val building = resources.getStringArray(R.array.buildings)[p2]
                val building_id = resources.getIdentifier(building, "array", this@MainActivity.getPackageName())
                // Toast.makeText(applicationContext, "Building $building selected", Toast.LENGTH_SHORT).show()
                view.roomSpinnerView.setAdapter(ArrayAdapter(this@MainActivity,
                                                android.R.layout.simple_spinner_item,
                                                resources.getStringArray(building_id)))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        view.searchButton.setOnClickListener {
            val building = view.buildingSpinnerView.getSelectedItem().toString()
            val room = view.roomSpinnerView.getSelectedItem().toString()
            // Toast.makeText(applicationContext, "Searching $building $room ...", Toast.LENGTH_SHORT).show()
        }

        view.arriveButton.setOnClickListener {
            val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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

        view.searchButton.setOnClickListener {
            startActivity(Intent(this, DebugActivity::class.java))
        }

        view.nodeTester.setOnClickListener {

            // Below is for testing the implementation of A* algorithm
            NodesStore.getNodes(applicationContext, "BBB") {
                Log.d("getNodes", "getNodes completed!")
            }
            val allNodes = NodesStore.nodes
            print("Number of nodes: " + allNodes.size + "\n")

            val pathGen = PathGenerator()
            val fastestPath = pathGen.getFastestPath("BBB", allNodes[0] as Node, allNodes[allNodes.size-1] as Node)
            print("Order of Nodes:\n")
            for (i in 0 until fastestPath.size) {
                print(fastestPath[i].id as String + ", ")
            }

        }
    }
}