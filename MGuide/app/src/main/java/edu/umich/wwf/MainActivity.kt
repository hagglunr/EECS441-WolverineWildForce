package edu.umich.wwf

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*

class MainActivity : AppCompatActivity() {

    private lateinit var building_spinner_adapter: ArrayAdapter<String>
    private lateinit var room_spinner_adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val building_spinner_view = findViewById<Spinner>(R.id.building_spinner)
        building_spinner_adapter = ArrayAdapter<String>(this,
                                                        android.R.layout.simple_spinner_item,
                                                        resources.getStringArray(R.array.buildings))
        building_spinner_view.adapter = building_spinner_adapter

        val room_spinner_view = findViewById<Spinner>(R.id.room_spinner)
        room_spinner_adapter = ArrayAdapter(this,
                                            android.R.layout.simple_spinner_item,
                                            arrayOf<String>())
        room_spinner_view.adapter = room_spinner_adapter

        building_spinner_view.onItemSelectedListener = object :  AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val building = resources.getStringArray(R.array.buildings)[p2]
                val building_id = resources.getIdentifier(building, "array", this@MainActivity.getPackageName())
                Toast.makeText(applicationContext, "Building $building selected", Toast.LENGTH_SHORT).show()
                room_spinner_view.adapter = ArrayAdapter(this@MainActivity,
                                                     android.R.layout.simple_spinner_item,
                                                     resources.getStringArray(building_id))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }
}