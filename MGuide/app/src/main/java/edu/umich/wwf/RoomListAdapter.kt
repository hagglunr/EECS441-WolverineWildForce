package edu.umich.wwf

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

class RoomListAdapter(context: Context, users: ArrayList<Room?>) :
    ArrayAdapter<Room?>(context, 0, users) {
}