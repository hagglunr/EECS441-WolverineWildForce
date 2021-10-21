package edu.umich.mwassink.mapdisplay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        BuildingDirectoryMap = LinkedHashMap<String, Building>()
        val tempBuilding = Building
        val tempRoom = Room
        tempBuilding.rooms[tempRoom.number] = tempRoom
        val tempEntry = Entry
        tempBuilding.entries[tempEntry.id] = tempEntry
        BuildingDirectoryMap[tempBuilding.name] = tempBuilding

        // Data to be passed in is tempBuilding. Then we can access all the information we need from there
        // tempBuilding.rooms["1670]  .x or .y
        // tembBuilding.entries["1"]  .x or .y

    }


    fun launchGL(v: View) {
        startActivity(Intent(this, DisplayActivity::class.java))
    }
}