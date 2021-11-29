package edu.umich.mwassink.mapdisplay

import android.content.Context
import android.graphics.Bitmap
import android.view.SurfaceControl
import android.widget.Toast
import java.io.*
import java.nio.FloatBuffer
import java.nio.IntBuffer
import kotlin.math.floor

class Building {
    var Connections: Connections
    lateinit var Texture: Bitmap
    var floorNum: Int = -1
    //var userX: Float
    //var userY: Float


    init {
        Connections = Connections(floatArrayOf(), intArrayOf())

    }

    constructor(conns: Connections, texture: Bitmap, fn: Int)  {
        Connections = conns
        Texture = texture
        floorNum = fn
    }

    constructor(nodeFileName: String, textureFileName: String, context: Context, fn: Int ){
        ReadLocalFile(nodeFileName, textureFileName, context)
        floorNum = fn
    }

    fun ReadLocalFile(nodeFileName: String, textureFileName: String, context: Context) {
        // get files dir
        try {

            var nodeFile: File = File(context.filesDir, nodeFileName)
            var textureFile: File = File(context.filesDir, textureFileName)
            Connections = ObjectInputStream(FileInputStream(nodeFile)).readObject() as Connections
            var oin: ObjectInputStream = ObjectInputStream(FileInputStream(textureFile))
            Texture = readBitmap(oin)

        } catch (ex: Exception) {
            Toast.makeText(context, "Failure reading in building file",
                Toast.LENGTH_LONG).show();

        }
    }

    fun WriteLocalFile(nodeFileName: String, textureFileName: String, context: Context) {
        try {
            var nodeFile: File = File(context.filesDir, nodeFileName)
            var textureFile: File = File(context.filesDir, textureFileName)
            ObjectOutputStream(FileOutputStream(nodeFile)).writeObject(Connections)
            var oout = ObjectOutputStream(FileOutputStream(textureFile))
            writeBitmap(oout, Texture)
        } catch (e: Exception) {
            Toast.makeText(context, "Failure writing out building file",
                Toast.LENGTH_LONG).show();
        }
    }


}