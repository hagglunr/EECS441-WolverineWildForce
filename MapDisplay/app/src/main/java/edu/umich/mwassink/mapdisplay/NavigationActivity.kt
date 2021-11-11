package edu.umich.mwassink.mapdisplay

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.umich.mwassink.mapdisplay.databinding.ActivityMainBinding
import edu.umich.mwassink.mapdisplay.databinding.ActivityNavigationBinding

class NavigationActivity : AppCompatActivity() {

    private lateinit var view: ActivityNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(view.root)

        val building = getIntent().getExtras()?.getString("building")
        val room = getIntent().getExtras()?.getString("room")

        Toast.makeText(applicationContext, "navigationActivity received: ${building + room}" , Toast.LENGTH_SHORT).show()
        view.destination.text = "$building $room"
    }

}