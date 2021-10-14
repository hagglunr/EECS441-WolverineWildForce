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
        var tempBuilding = Building
        BuildingDirectoryMap[tempBuilding.name] = tempBuilding
    }


    fun launchGL(v: View) {
        startActivity(Intent(this, DisplayActivity::class.java))
    }
}