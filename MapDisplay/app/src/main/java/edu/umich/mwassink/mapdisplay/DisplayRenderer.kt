package edu.umich.mwassink.mapdisplay

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
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
    init {
        view = v
        width = 0
        height = 0
        ctr = 0
        vertexShader =  "uniform mat4 orthoProj;" +
                "attribute vec4 pos;" +
                "void main() {" +
                "    gl_Position = orthoProj*pos;" + //gl_Position is the opengl clipSpaceCoordinate of vertex - Note: opengl may not render GL_POINT if any part of the point is outside of clipSpace
                "    gl_PointSize = 20.;" +                      //gl_Point size is the size of GL_POINT vertices in pixels
                "}";
        pixelShader = "out vec4 color;" +
                "void main() {" +
                "gl_Color = vec4(1.0f, 1.0f ,1.0f, 1.0f);" +
                "}"

        initialProgram = -1
        orthoMatrixLocation = -1

    }

    fun createShader(vs: String, ps: String): Int {
        val status: IntArray = intArrayOf(0)
        val programName = GLES20.glCreateProgram()
        val vShader = compileShader(false, vertexShader)
        val pShader = compileShader(true, vertexShader)
        GLES20.glAttachShader(programName, vShader)
        GLES20.glAttachShader(programName, pShader)
        GLES20.glLinkProgram(programName)

        GLES20.glGetShaderiv(programName, GLES20.GL_LINK_STATUS, status, 0)

        if (status[0] == GLES20.GL_FALSE ) {
            System.err.println("Error compiling shader");
            System.exit(1)
        }

        GLES20.glDetachShader(programName, vShader)
        GLES20.glDetachShader(programName, pShader)
        GLES20.glDeleteShader(vShader)
        GLES20.glDeleteShader(pShader)

    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        width = view.width
        height = view.height
        GLES20. glClearColor(1.0f, 1.0f, 1.0f, 1f)

        initialProgram = createShader(vertexShader, pixelShader)
        orthoMatrixLocation = GLES20.glGetUniformLocation(initialProgram, "orthoProj")


    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {

        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(p0: GL10?) {

        GLES20.glUniformMatrix4fv()
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
             System.err.println("Error compiling shader");
             System.exit(1)
         }

         return shaderName



    }
}