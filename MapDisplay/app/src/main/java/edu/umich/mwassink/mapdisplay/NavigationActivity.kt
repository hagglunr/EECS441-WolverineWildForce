package edu.umich.mwassink.mapdisplay

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.umich.mwassink.mapdisplay.NavigationActivity.NavigationState.*
import edu.umich.mwassink.mapdisplay.databinding.ActivityMainBinding
import edu.umich.mwassink.mapdisplay.databinding.ActivityNavigationBinding

class NavigationActivity : AppCompatActivity() {

    enum class NavigationState {
        BROWSING, NAVIGATING, REPONSITIONING_B, REPONSITIONING_N, ARRIVE
    }

    private lateinit var view: ActivityNavigationBinding
    private var currState: NavigationState = BROWSING
    private lateinit var building: String
    private lateinit var room: String

    private fun showStart() {
        view.startButton.visibility = Button.VISIBLE
    }
    private fun hideStart() {
        view.startButton.visibility = Button.INVISIBLE
    }
    private fun showReposition() {
        view.repositionButton.text = "Reposition"
        view.repositionButton.setBackgroundColor(Color.parseColor("#FBC520"))
    }
    private fun showRepositionDone() {
        view.repositionButton.text = "Done"
        view.repositionButton.setBackgroundColor(Color.parseColor("#6EE14F"))
    }
    private fun transition(clickedButton: View) {
        with (clickedButton as Button) {
            Log.d("Transition", "CurrState: $currState, Action: $text, $id")
        }
        when (clickedButton.id) {
            view.startButton.id -> {
                when (currState) {
                    BROWSING -> { currState = NAVIGATING; hideStart() }
                }
            }
            view.repositionButton.id -> {
                with (clickedButton as Button) {
                    if (text == "Reposition") {
                        when (currState) {
                            BROWSING -> { currState = REPONSITIONING_B }
                            NAVIGATING -> { currState = REPONSITIONING_N }
                        }
                        hideStart()
                        showRepositionDone()
                    }
                    else {
                        when (currState) {
                            REPONSITIONING_B -> { currState = BROWSING; showStart() }
                            REPONSITIONING_N -> { currState = NAVIGATING }
                        }
                        showReposition()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(view.root)

        building = getIntent().getExtras()?.getString("building").toString()
        room = getIntent().getExtras()?.getString("room").toString()

        view.destination.text = "$building $room"

        view.startButton.setOnClickListener{
            transition(it)
        }

        view.repositionButton.setOnClickListener{
            transition(it)
        }

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
                intent.putExtra("building", building)
                intent.putExtra("room", room)
                startActivity(intent)
            }
        }
    }


}