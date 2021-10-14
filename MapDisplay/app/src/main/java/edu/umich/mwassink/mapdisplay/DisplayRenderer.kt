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

    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        width = view.width
        height = view.height
        GLES20. glClearColor(1.0f, 1.0f, 1.0f, 1f)

    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {

        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        if (ctr % 2 == 1) {
            GLES20.glClearColor(1.0f, 0f, 1.0f, 1.0f)
        }
        else {
            GLES20.glClearColor(1f, 1f, 1f, 1f)
        }
        val err = GLES20.glGetError()
        ctr++
    }
}