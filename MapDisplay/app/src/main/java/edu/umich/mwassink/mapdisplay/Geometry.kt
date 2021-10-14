package edu.umich.mwassink.mapdisplay

 // m * v
// 4
// 3
// 2
// w = 1
// 2 0 0 1
// 1 0 0 3
// 3 0 0 1
// 0 0 0 2
class Geometry {

     fun orthoProj(r: Float, l: Float, t: Float, b: Float, f: Float,
                   n: Float ): Matrix {
         var far = f
         var near = n
         val c1 = Vector(2/ (r-l), 0f, 0f, 0f)
         val c2 = Vector(0f, 2/(t-b), 0f, 0f)
         val c3 = Vector(0f, 0f, -2/(far-near), 0f)
         val c4 = Vector(-(r+l)/(r-l), -(t+b)/(t-b), -(far+near)/(far -near), 1f)

         return Matrix(c1, c2, c3, c4)
     }


    class Matrix (val c1In: Vector, val c2In: Vector, val c3In: Vector, val c4In: Vector){
        val c1: Vector
        val c2: Vector
        val c3: Vector
        val c4: Vector

        init {
            c1 = c1In
            c2 = c2In
            c3 = c3In
            c4 = c4In
        }

        operator fun times(v: Vector): Vector {

            val x  = c1.x * v.x + c2.x * v.y + c3.x * v.z + c4.x * v.w
            val y = c1.y * v.x + c2.y * v.y + c3.y * v.z + c4.y * v.w
            val z = c1.z * v.x + c2.z * v.y + c3.z * v.z + c4.z * v.w
            val w = c1.w * v.x + c2.w * v.y + c3.w * v.z + c4.w * v.w
            return Vector(x, y, z, w)
        }
        // Map xmin to -1, xmax to 1 (w will be kept as 1)


        fun copyToArray(): FloatArray {
            val f = FloatArray(16)
            c1.copyToArray(0, f)
            c2.copyToArray(4, f)
            c3.copyToArray(8, f)
            c4.copyToArray(12, f)
            return f
        }
    }

     class Vector (val xIn: Float, var yIn: Float, var zIn: Float, var wIn: Float ) {
         val x: Float
         val y: Float
         val z: Float
         val w: Float
        init {
            x = xIn
            y = yIn
            z = zIn
            w = wIn
        }

         fun copyToArray(start: Int, arr: FloatArray) {
             arr[start + 0] = x
             arr[start + 1] = y
             arr[start + 2] = z
             arr[start + 3] = w

         }

     }



}