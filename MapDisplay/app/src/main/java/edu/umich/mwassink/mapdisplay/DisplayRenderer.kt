package edu.umich.mwassink.mapdisplay

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL10
import android.util.DisplayMetrics
import java.util.ArrayList
import android.view.WindowManager
import android.content.res.TypedArray
import android.text.method.MovementMethod
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.ViewConfiguration
import kotlin.math.sqrt


class DisplayRenderer(v: GLSurfaceView, building: Building)  : GLSurfaceView.Renderer {
    var view: GLSurfaceView;

    var ctr: Int
    var vertexShader: String
    var pixelShader: String
    var vertexTexture: String
    var pixelTexture: String
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
    var scaleFactor: Float
    var transLeft: Float
    var transUp: Float
    var mapTextureProgram: Int
    var pointBuffer = -1
    var heightPicture: Float
    var widthPicture: Float
    var defaultDepth = -5f
    var texturePointsHandle = -1
    var pictureVertices: FloatArray
    var mapTextureHandle = -1
    var ratio: Float
    var userPos: FloatArray
    var initializedFully = false
    var map: Bitmap
    var customPoints: ArrayList<Float>
    var customLines: ArrayList<Int>
    var realHeight: Float = 0f
    var realWidth: Float = 0f
    var PointMode: Boolean = false
    var MoveMode: Boolean = true
    var LineMode: Boolean = false
    init {
        view = v
        ctr = 0
        vertexShader =  "uniform mat4 orthoProj;" +
                "attribute vec4 pos;" +
                "void main() {" +
                "    gl_Position = orthoProj*pos;" + //gl_Position is the opengl clipSpaceCoordinate of vertex - Note: opengl may not render GL_POINT if any part of the point is outside of clipSpace
                "    gl_PointSize = 40.;" +                      //gl_Point size is the size of GL_POINT vertices in pixels
                "}";
        pixelShader = "precision mediump float;" +
                "uniform float red;" +
                "void main() {" +
                "gl_FragColor = vec4(0.0, 0.0 ,1.0, 1.0);" +
                "if (red > 0.0) { gl_FragColor = vec4(1.0, 0.0 ,0.0, 1.0); }" +
                "if (red > 5.0) { gl_FragColor = vec4(0.0, 1.0 ,0.0, 1.0); }" +
                "}"

        vertexTexture = "uniform mat4 orthoProj;" +
                "uniform float height;" +
                "uniform float width;" +
                "attribute vec4 pos;" +
                "varying vec2 uvOut;" +
                "void main() {" +
                " gl_Position = orthoProj * pos; " +
                        "vec2 v = pos.xy;" +
                        "uvOut = vec2(v.x / width, -(v.y / height));" +
                " }";

        pixelTexture = "precision mediump float;" +
            "uniform sampler2D tex;" +
                "varying vec2 uvOut;" +
                "void main() {" +
                        "gl_FragColor = texture2D(tex, uvOut);" +
                "}"



        initialProgram = -1
        orthoMatrixLocation = -1
        mapTextureProgram = -1
        geo = Geometry()
        r = 1000f
        l = 0f
        b = 0f
        t = 1000f
        ratio = 1f
        bufferName = -1



        scaleFactor = 1f
        transLeft = 0f
        transUp = 0f
        heightPicture = (building.Texture.height).toFloat()
        widthPicture =  (building.Texture.width).toFloat()
        map = building.Texture
        userPos = floatArrayOf(200f, 200f, -5f, 1f) // replace
        mapTextureHandle = -1
        vertices =  building.Connections.Nodes
        numVertices = building.Connections.Nodes.size / 4 //4 per point
        pictureVertices = floatArrayOf(0f, heightPicture, defaultDepth, 1f, widthPicture, 0f, defaultDepth, 1f,
            0f, 0f, defaultDepth, 1f,
            0f, heightPicture, defaultDepth, 1f,
            widthPicture, 0f, defaultDepth, 1f,
            widthPicture, heightPicture, defaultDepth, 1f)
        customPoints = arrayListOf(200f, 200f, -5f, 1f)
        customLines = arrayListOf()


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

        mapTextureHandle = loadMapTexture(map)
        val metrics: DisplayMetrics = view.context.getResources().getDisplayMetrics()


        realWidth = view.width.toFloat()
        realHeight = view.height.toFloat()

        ratio = metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()
        r = 1000f
        l = 0f
        b = 0f
        t = 1000f  * ratio
        //t = 1000f
        GLES20. glClearColor(1.0f, 1.0f, 1.0f, 1f)
        var err: Int = GLES20.glGetError()


        initialProgram = createShader(vertexShader, pixelShader)
        mapTextureProgram = createShader(vertexTexture, pixelTexture)
        orthoMatrixLocation = GLES20.glGetUniformLocation(initialProgram, "orthoProj")

        val indexBuffer: IntBuffer = IntBuffer.allocate(1)
        GLES20.glGenBuffers(1, indexBuffer)
        pointBuffer = indexBuffer[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, pointBuffer)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 4*4*numVertices, arrToBuffer(vertices, 4*numVertices*4), GLES20.GL_STATIC_DRAW  )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        val texturePointBuffer: IntBuffer = IntBuffer.allocate(1)
        GLES20.glGenBuffers(1, texturePointBuffer)
        texturePointsHandle  = texturePointBuffer[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texturePointsHandle)
        val numPoints = 6
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, numPoints * 4 * 4, arrToBuffer(pictureVertices, 4*numPoints*4),
        GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        GLES20.glUseProgram(initialProgram)
        GLES20.glLineWidth(30f)


    }

    fun completeInit(building: Building) {

        synchronized(this) {
            initializedFully = true
        }

    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {

        GLES20.glViewport(0, 0, view.width, view.height)
    }

    fun drawPoints(orthoProj: Geometry.Matrix) {
        var err = GLES20.glGetError()
        GLES20.glUseProgram(initialProgram)
        err = GLES20.glGetError()
        var buff: FloatArray = orthoProj.copyToArray()
        var v = Geometry.Vector(.5f, .5f, -5f, 1.0f)
        var v2 = orthoProj * v
        var mLoc = GLES20.glGetUniformLocation(initialProgram, "orthoProj")
        var redLoc = GLES20.glGetUniformLocation(initialProgram, "red")
        GLES20.glUniform1f(redLoc, 0f)
        GLES20.glUniformMatrix4fv(orthoMatrixLocation, 1, false, buff, 0)


        GLES20.glDisableVertexAttribArray(0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

        var buffCPUMemory = arrToBuffer(customPoints.toFloatArray(), 4 * customPoints.size)
        var indexBuffer = arrToBufferInt(customLines.toIntArray(), 4 * customLines.size)


        // user
        synchronized(this) {
            redLoc = GLES20.glGetUniformLocation(initialProgram, "red")
            GLES20.glUniform1f(redLoc, 10f)
            GLES20.glVertexAttribPointer(0, 4, GLES20.GL_FLOAT, false, 0, buffCPUMemory)
            GLES20.glEnableVertexAttribArray(0)
            GLES20.glDrawArrays(GLES20.GL_POINTS, 0, customPoints.size / 4)


            GLES20.glDrawElements(GLES20.GL_LINES, (customLines.size/2) * 2, GLES20.GL_UNSIGNED_INT, indexBuffer )
        }
        err = GLES20.glGetError()

    }

    fun drawMap(orthoProj: Geometry.Matrix) {
        GLES20.glUseProgram(mapTextureProgram)
        var err = GLES20.glGetError()
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mapTextureHandle)
        err = GLES20.glGetError()
        var buff: FloatArray = orthoProj.copyToArray()
        var mLoc = GLES20.glGetUniformLocation(mapTextureProgram, "orthoProj")
        GLES20.glUniformMatrix4fv(mLoc, 1, false, buff, 0)
        var widthLocation = GLES20.glGetUniformLocation(mapTextureProgram, "width")
        var heightLocation = GLES20.glGetUniformLocation(mapTextureProgram, "height")
        GLES20.glUniform1f(widthLocation, widthPicture)
        GLES20.glUniform1f(heightLocation, heightPicture)
        err = GLES20.glGetError()

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texturePointsHandle)
        GLES20.glVertexAttribPointer(0, 4, GLES20.GL_FLOAT, false, 0, 0)

        GLES20.glEnableVertexAttribArray(0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
        err = GLES20.glGetError()




        
    }

    override fun onDrawFrame(p0: GL10?) {


        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)


        var orthoProj: Geometry.Matrix
        synchronized(this) {
            orthoProj = geo.orthoProj(r, l, t, b, 100f, .5f)
        }

        drawMap(orthoProj)
        drawPoints(orthoProj)

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

    fun lerp(v1: Float, v2: Float, t: Float): Float {
        return (v1*(1f-t) + v2*t)
    }



    fun changeScale(scaleNew: Float) {
        var prevScale = scaleFactor
        scaleFactor = scaleNew
        var ratio = prevScale / scaleFactor
        var dx = (r - l) / 2
        var dy = (t - b) / 2
        var ymid = lerp(b,t, .5f)
        var xmid = lerp(l,r,.5f)
        synchronized(this) {
            if (!MoveMode) {
                return
            }

            l = xmid - dx * ratio
            r = xmid + dx * ratio
            t = ymid + dy * ratio
            b = ymid - dy * ratio
        }

    }

    fun changeTrans(dx: Float, dy: Float) {
         synchronized(this) {
             if (!MoveMode) {
                 return
             }
             l += dx
             r += dx
             t -= dy
             b -= dy
         }
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

    fun arrToBufferInt(f: IntArray, s: Int): IntBuffer {
        val b: IntBuffer = ByteBuffer.allocateDirect(s).order(ByteOrder.nativeOrder()).asIntBuffer()
        b.put(f)
        b.rewind()
        return b
    }

    fun loadBBBMapTexture(ctx: Context) {
        var bmp: Bitmap = BitmapFactory.decodeResource(ctx.resources, R.drawable.ic_action_bbb, BitmapFactory.Options() )
        mapTextureHandle = loadMapTexture(bmp)
        // unbind the texture here?

    }

    fun loadMapTexture(bmp: Bitmap): Int {
        var textureProgramBuff: IntArray= IntArray(1)
        GLES20.glGenTextures(1, textureProgramBuff, 0)
        var textureHandle = textureProgramBuff[0]



        // Make active and set linear filtering
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D , textureHandle)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0)
        bmp.recycle()
        return textureHandle
    }

    fun changePos(dx: Float, dy: Float) {
        synchronized(this) {
            if (!MoveMode) {
                return
            }
            userPos[0] += dx
            userPos[1] += dy
        }
    }

    // returns the t between them e.g [3, 6] w/ 4.5 has t = .5
    fun invLerp(v1: Float, v2: Float, v: Float): Float {
        return (v - v1) / (v2 - v1)
    }

    fun addPoint(x: Float, y: Float) {

        synchronized(this) {
            if (!PointMode) {
                return
            }
            val metrics: DisplayMetrics = view.context.getResources().getDisplayMetrics()
            val tx = invLerp(0f, realWidth, x)
            val ty = invLerp(0f, realHeight, y)

            customPoints.add(lerp(l, r, tx))
            customPoints.add(lerp(t, b, ty))
            customPoints.add(defaultDepth)
            customPoints.add(1f)
        }
    }

    fun SetPointMode(what: Boolean) {
        PointMode = what
    }

    fun SetLineMode(what: Boolean) {
        LineMode = what
    }

    fun SetMoveMode(what: Boolean) {
        MoveMode = what
    }

    fun ClosestPoint(XIn: Float, YIn: Float): Int {
        synchronized(this) {
            var bestDist = 1000000f
            var bestIndex = -1
            val tx = invLerp(0f, realWidth, XIn)
            val ty = invLerp(0f, realHeight, YIn)
            val X = lerp(l, r, tx)
            val Y = lerp(t, b, ty)
            var i = 0
            while (i < customPoints.size / 4) {
                val x = customPoints[i*4 + 0]
                val y = customPoints[i*4 + 1]
                val dx = X - x
                val dy = Y - y
                // I know I do not need the sqrt...
                if (sqrt(dx * dx + dy*dy) < bestDist) {
                    bestDist = sqrt(dx * dx + dy * dy)
                    bestIndex = i
                }
                i++
            }

            return bestIndex
        }

    }

    // Indices into the float list
    fun addLine(p1: Int, p2:Int) {
        synchronized(this) {
            customLines.add(p1)
            customLines.add(p2)
        }

    }




}