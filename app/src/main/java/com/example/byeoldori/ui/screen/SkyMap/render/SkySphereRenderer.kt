package com.example.byeoldori.ui.screen.SkyMap.render

import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import kotlin.math.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SkySphereRenderer : GLSurfaceView.Renderer {
    private var yaw    = 0f
    private var pitch  = 0f
    private var fov    = 60f

    private val viewMatrix = FloatArray(16)
    private val projMatrix = FloatArray(16)
    private val mvpMatrix  = FloatArray(16)

    private var aspectRatio = 1f

    private lateinit var starManager: StarManager
    private lateinit var textLabelManager: TextLabelManager
    private lateinit var equatorialGrid: EquatorialGrid

    companion object {
        private const val GL_PROGRAM_POINT_SIZE = 0x8642
    }

    fun updateCamera(yaw: Float, pitch: Float, fov: Float) {
        this.yaw   = yaw
        this.pitch = pitch
        this.fov   = fov
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // 기본 GL 설정
        GLES32.glClearColor(0f, 0f, 0.1f, 1f)
        GLES32.glEnable(GLES32.GL_DEPTH_TEST)
        GLES32.glEnable(GL_PROGRAM_POINT_SIZE)
        GLES32.glEnable(GLES32.GL_BLEND)
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA)

        // 매니저 초기화
        starManager       = StarManager().also { StarCatalog(it) }
        textLabelManager  = TextLabelManager()
        equatorialGrid    = EquatorialGrid(textLabelManager)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES32.glViewport(0, 0, width, height)
        aspectRatio = width.toFloat() / height
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT or GLES32.GL_DEPTH_BUFFER_BIT)

        val forward = rotateCameraDirection(yaw, pitch)
        val eye      = floatArrayOf(0f, 0f, 0f)
        val center   = floatArrayOf(forward[0], forward[1], forward[2])
        val up       = getSafeUpVector(forward)

        Matrix.setLookAtM(viewMatrix, 0,
            eye[0], eye[1], eye[2],
            center[0], center[1], center[2],
            up[0], up[1], up[2]
        )
        Matrix.perspectiveM(projMatrix, 0, fov, aspectRatio, 0.1f, 100f)
        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, viewMatrix, 0)

        // 별과 격자 그리기 (깊이테스트)
        GLES32.glEnable(GLES32.GL_DEPTH_TEST)
        starManager.draw(mvpMatrix)
        equatorialGrid.draw(mvpMatrix)

        // 텍스트는 항상 앞에
        GLES32.glDisable(GLES32.GL_DEPTH_TEST)
        textLabelManager.drawAll(mvpMatrix, viewMatrix, fov)
        GLES32.glEnable(GLES32.GL_DEPTH_TEST)
    }

    private fun rotateCameraDirection(yaw: Float, pitch: Float): FloatArray {
        val ry = Math.toRadians(yaw.toDouble()).toFloat()
        val rp = Math.toRadians(pitch.toDouble()).toFloat()
        val x  = cos(rp) * sin(ry)
        val y  = sin(rp)
        val z  = cos(rp) * cos(ry)
        return floatArrayOf(x, y, z)
    }

    private fun getSafeUpVector(forward: FloatArray): FloatArray {
        val worldUp = floatArrayOf(0f, 1f, 0f)
        val dot     = forward[0]*worldUp[0] + forward[1]*worldUp[1] + forward[2]*worldUp[2]
        return if (abs(dot) > 0.99f) floatArrayOf(0f,0f,1f) else worldUp
    }
}
