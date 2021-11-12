package edu.umich.mwassink.mapdisplay

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class DebugActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        /*
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
        */
    }


    fun launchGL(v: View) {
        var intent: Intent =Intent(this, DisplayActivity::class.java)
        var img = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.ic_action_bbb, BitmapFactory.Options() )
        intent.putExtra("buildingFile", "bbb.png")
        val stream = this.openFileOutput("bbb.png", Context.MODE_PRIVATE)
        img.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
        img.recycle()

        startActivity(intent)
    }

    fun launchWalk(v: View) {

        startActivity(Intent(this, WalkActivity::class.java))
    }
    
    fun launchGPS(v: View) {
        startActivity(Intent(this, GPSActivity::class.java))
    }

    fun launchSearch(v: View) {
        //startActivity(Intent(this, MainActivity::class.java))
    }
}