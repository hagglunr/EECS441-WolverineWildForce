package edu.umich.mwassink.mapdisplay

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class DisplayRenderer(v: GLSurfaceView) : GLSurfaceView.Renderer {
    var view: GLSurfaceView;
    var width: Int
    var height: Int
    var ctr: Int
    var vertexShader: String
    var pixelShader: String
    var initialProgram: Int
    var orthoMatrixLocation: Int
    var geo: Geometry
    var r: Float
    var l: Float
    var t: Float
    var b: Float
    var bufferName: Int
    var numVertices: Int
    var vertices: FloatArray
    init {
        view = v
        width = 0
        height = 0
        ctr = 0
        vertexShader =  "uniform mat4 orthoProj;" +
                "attribute vec4 pos;" +
                "void main() {" +
                "    gl_Position = orthoProj*pos;" + //gl_Position is the opengl clipSpaceCoordinate of vertex - Note: opengl may not render GL_POINT if any part of the point is outside of clipSpace
                "    gl_PointSize = 40.;" +                      //gl_Point size is the size of GL_POINT vertices in pixels
                "}";
        pixelShader = "void main() {" +
                "gl_FragColor = vec4(0.0, 0.0 ,1.0, 1.0);" +
                "}"

        initialProgram = -1
        orthoMatrixLocation = -1
        geo = Geometry()
        r = 1f
        l = 0f
        b = 0f
        t = 1f
        bufferName = -1
        numVertices = 2
        vertices =  floatArrayOf(.5f, .5f, -5f, 1f, .25f ,.25f, -5f, 1f)


    }

    fun createShader(vs: String, ps: String): Int {
        val status: IntArray = intArrayOf(0)
        val programName = GLES20.glCreateProgram()
        val vShader = compileShader(false, vs)
        val pShader = compileShader(true, ps)
        GLES20.glAttachShader(programName, vShader)
        GLES20.glAttachShader(programName, pShader)
        GLES20.glLinkProgram(programName)
        var err = GLES20.glGetError()

        GLES20.glGetProgramiv(programName, GLES20.GL_LINK_STATUS, status, 0)
        err = GLES20.glGetError()
        if (status[0] == GLES20.GL_FALSE ) {
            System.err.println("Error compiling shader");
            System.exit(1)
        }


        GLES20.glDetachShader(programName, vShader)
        GLES20.glDetachShader(programName, pShader)
        err = GLES20.glGetError()
        GLES20.glDeleteShader(vShader)
        GLES20.glDeleteShader(pShader)


        return programName

    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        width = view.width
        height = view.height
        GLES20. glClearColor(1.0f, 1.0f, 1.0f, 1f)
        var err: Int = GLES20.glGetError()


        initialProgram = createShader(vertexShader, pixelShader)
        orthoMatrixLocation = GLES20.glGetUniformLocation(initialProgram, "orthoProj")
        val indexBuffer: IntBuffer = IntBuffer.allocate(1)
        GLES20.glGenBuffers(1, indexBuffer)

        bufferName = indexBuffer[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferName)

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 4*4*numVertices, arrToBuffer(vertices, 4*numVertices*4), GLES20.GL_STATIC_DRAW  )

        GLES20.glUseProgram(initialProgram)


    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {

        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(p0: GL10?) {


        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        var orthoProj: Geometry.Matrix = geo.orthoProj(r, l, t, b, 100f, .5f)
        var buff: FloatArray = orthoProj.copyToArray()
        var v = Geometry.Vector(.5f, .5f, -5f, 1.0f)
        var v2 = orthoProj * v
        GLES20.glUniformMatrix4fv(orthoMatrixLocation, 1, false, buff, 0)


        GLES20.glVertexAttribPointer(0, 4, GLES20.GL_FLOAT, false, 0, 0)

        GLES20.glEnableVertexAttribArray(0)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, numVertices)

    }



     fun compileShader(pixelShader: Boolean, shader:String ): Int {

        // request a name for the shader
         val shaderName: Int
         val status: IntArray = intArrayOf(0)
         if (pixelShader) {
             shaderName = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)

         }
         else {
             shaderName = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
         }
         GLES20.glShaderSource(shaderName, shader)
         GLES20.glCompileShader(shaderName)

         GLES20.glGetShaderiv(shaderName, GLES20.GL_COMPILE_STATUS, status, 0)

         if (status[0] == GLES20.GL_FALSE ) {
             System.err.println("Error compiling shader" + GLES20.glGetShaderInfoLog(shaderName));
             System.exit(1)
         }

         return shaderName



    }

    fun contiguousMatrix(f: FloatArray): FloatBuffer {
        val b: FloatBuffer = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer()
        b.put(f)
        b.rewind()
        return b
    }
    fun arrToBuffer(f: FloatArray, s: Int): FloatBuffer {
        val b: FloatBuffer = ByteBuffer.allocateDirect(s).order(ByteOrder.nativeOrder()).asFloatBuffer()
        b.put(f)
        b.rewind()
        return b
    }
}