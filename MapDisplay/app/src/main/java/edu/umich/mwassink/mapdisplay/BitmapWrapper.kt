package edu.umich.mwassink.mapdisplay

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.*
import java.nio.ByteBuffer




    //https://stackoverflow.com/questions/5871482/serializing-and-de-serializing-android-graphics-bitmap-in-java
    fun writeBitmap(out: ObjectOutputStream, img: Bitmap){
        var stream: ByteArrayOutputStream = ByteArrayOutputStream()
        img.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val arr: ByteArray = stream.toByteArray()

        out.writeInt(arr.size)
        out.write(arr)
    }

    fun readBitmap(inStream: ObjectInputStream): Bitmap {
        var len: Int = inStream.readInt()
        var buff: ByteArray = ByteArray(len)

        var pos = 0
        do {
            val read = inStream.read(buff, pos, len - pos)

            if ( read != -1) {
                pos += read
            } else {
                break;
            }
        } while (pos < len)
        return BitmapFactory.decodeByteArray(buff, 0, len)

    }
