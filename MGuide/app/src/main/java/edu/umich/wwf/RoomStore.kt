package edu.umich.wwf

import android.content.Context

object RoomStore {
    val rooms = arrayListOf<Room?>()

    fun getRooms(context: Context, completion: () -> Unit) {
    }
}