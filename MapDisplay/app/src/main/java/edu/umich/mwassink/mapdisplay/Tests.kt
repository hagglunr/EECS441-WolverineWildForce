package edu.umich.mwassink.mapdisplay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.ConnectException

// Gets the nodes for the Bob and Betty Beyster Building
// 2260 Hayward St Ann Arbor, MI 48109
fun DefaultBuilding(ctx: Context): Building {
    var bmp: Bitmap = BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_action_bbb, BitmapFactory.Options() )
    var vs : FloatArray= floatArrayOf((bmp.width/2).toFloat(), (bmp.height/2).toFloat(), -5f, 1f, .25f ,.25f, -5f, 1f)
    var indices: IntArray = intArrayOf(0, 1)
    var conns: Connections = Connections(vs, indices)
    var building: Building = Building(conns, bmp)
    return building
}

fun DefaultConnections(bmp: Bitmap): Building {
    var vs : FloatArray= floatArrayOf((bmp.width/2).toFloat(), (bmp.height/2).toFloat(), -5f, 1f, .25f ,.25f, -5f, 1f)
    var indices: IntArray = intArrayOf(0, 1)
    var conns: Connections = Connections(vs, indices)
    return Building(conns, bmp)
}