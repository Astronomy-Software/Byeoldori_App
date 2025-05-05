package com.example.byeoldori.ui.screen.SkyMap.render

import android.opengl.GLES32
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*

data class Star(
    val position: FloatArray,   // x, y, z
    val color: FloatArray,      // r, g, b, a
    val size: Float             // gl_PointSize
)

class StarManager {
    private val stars = mutableListOf<Star>()

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var colorBuffer: FloatBuffer
    private lateinit var sizeBuffer: FloatBuffer

    private var bufferNeedsUpdate = true

    companion object {
        val DEFAULT_COLOR = floatArrayOf(1f, 1f, 1f, 1f)
        const val DEFAULT_SIZE = 2.5f
    }

    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        attribute vec4 aColor;
        attribute float aSize;
        varying vec4 vColor;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            gl_PointSize = aSize;
            vColor = aColor;
        }
    """

    private val fragmentShaderCode = """
        precision mediump float;
        varying vec4 vColor;
        void main() {
            gl_FragColor = vColor;
        }
    """

    private val program = GLES32.glCreateProgram().also {
        val vShader = loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode)
        val fShader = loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode)
        GLES32.glAttachShader(it, vShader)
        GLES32.glAttachShader(it, fShader)
        GLES32.glLinkProgram(it)
    }

    fun addStarByRaDec(
        raDeg: Float,
        decDeg: Float,
        color: FloatArray = DEFAULT_COLOR,
        size: Float = DEFAULT_SIZE,
        radius: Float = 1.0f
    ) {
        val position = raDecToXYZ(raDeg, decDeg, radius)
        stars.add(Star(position, color, size))
        bufferNeedsUpdate = true
    }

    fun draw(mvpMatrix: FloatArray) {
        if (stars.isEmpty()) return

        if (!::vertexBuffer.isInitialized || bufferNeedsUpdate) {
            updateBuffers()
            bufferNeedsUpdate = false
        }

        GLES32.glUseProgram(program)

        val posHandle = GLES32.glGetAttribLocation(program, "vPosition")
        val colorHandle = GLES32.glGetAttribLocation(program, "aColor")
        val sizeHandle = GLES32.glGetAttribLocation(program, "aSize")
        val mvpHandle = GLES32.glGetUniformLocation(program, "uMVPMatrix")

        GLES32.glEnableVertexAttribArray(posHandle)
        GLES32.glVertexAttribPointer(posHandle, 3, GLES32.GL_FLOAT, false, 0, vertexBuffer)

        GLES32.glEnableVertexAttribArray(colorHandle)
        GLES32.glVertexAttribPointer(colorHandle, 4, GLES32.GL_FLOAT, false, 0, colorBuffer)

        GLES32.glEnableVertexAttribArray(sizeHandle)
        GLES32.glVertexAttribPointer(sizeHandle, 1, GLES32.GL_FLOAT, false, 0, sizeBuffer)

        GLES32.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)

        GLES32.glDrawArrays(GLES32.GL_POINTS, 0, stars.size)

        GLES32.glDisableVertexAttribArray(posHandle)
        GLES32.glDisableVertexAttribArray(colorHandle)
        GLES32.glDisableVertexAttribArray(sizeHandle)
    }

    private fun updateBuffers() {
        val positions = stars.flatMap { it.position.asList() }.toFloatArray()
        val colors = stars.flatMap { it.color.asList() }.toFloatArray()
        val sizes = stars.map { it.size }.toFloatArray()

        vertexBuffer = ByteBuffer.allocateDirect(positions.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(positions)
                position(0)
            }

        colorBuffer = ByteBuffer.allocateDirect(colors.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(colors)
                position(0)
            }

        sizeBuffer = ByteBuffer.allocateDirect(sizes.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(sizes)
                position(0)
            }
    }

    private fun raDecToXYZ(raDeg: Float, decDeg: Float, radius: Float): FloatArray {
        val raRad = Math.toRadians(raDeg.toDouble())
        val decRad = Math.toRadians(decDeg.toDouble())
        val x = cos(decRad) * cos(raRad)
        val y = sin(decRad)
        val z = cos(decRad) * sin(raRad)
        return floatArrayOf((x * radius).toFloat(), (y * radius).toFloat(), (z * radius).toFloat())
    }

    private fun loadShader(type: Int, code: String): Int {
        val shader = GLES32.glCreateShader(type)
        GLES32.glShaderSource(shader, code)
        GLES32.glCompileShader(shader)
        return shader
    }
}
