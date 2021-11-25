package edu.umich.mwassink.mapdisplay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
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

        view.exitButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        view.repositionButton.setOnClickListener {
            val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.reposition_tutorial, null)

            val popupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            popupWindow.showAtLocation(
                view.repositionButton, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
            )

            val reposTutorialOKbutton = popupView.findViewById<Button>(R.id.reposTutorialOKbutton)
            reposTutorialOKbutton.setOnClickListener{
                val intent = Intent(this, NavigationActivity::class.java)
                startActivity(intent)
            }
        }
    }

}