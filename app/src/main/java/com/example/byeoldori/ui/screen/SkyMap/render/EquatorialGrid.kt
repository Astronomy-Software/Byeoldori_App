package com.example.byeoldori.ui.screen.SkyMap.render

import android.graphics.Color
import android.opengl.GLES32
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*

class EquatorialGrid(private val textLabelManager: TextLabelManager) {
    private val gridVertices = generateGridVertices()
    private val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(gridVertices.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer().apply {
            put(gridVertices)
            position(0)
        }

    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
        }
    """

    private val fragmentShaderCode = """
        precision mediump float;
        void main() {
            gl_FragColor = vec4(0.5, 0.7, 1.0, 1.0);  // 옅은 파란색 격자선
        }
    """

    private val program: Int = GLES32.glCreateProgram().also {
        val vertexShader = loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode)
        GLES32.glAttachShader(it, vertexShader)
        GLES32.glAttachShader(it, fragmentShader)
        GLES32.glLinkProgram(it)
    }

    init {
        // TextLabel 객체 대신, 파라미터 형태로 넘깁니다.
        textLabelManager.addLabelByRaDec(
            "북극",
            0f,
            90f,
            Color.CYAN,
            96f
        )
        textLabelManager.addLabelByRaDec(
            "남극",
            0f,
            -90f,
            Color.CYAN,
            96f
        )
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES32.glUseProgram(program)
        val positionHandle = GLES32.glGetAttribLocation(program, "vPosition")
        val mvpHandle = GLES32.glGetUniformLocation(program, "uMVPMatrix")

        GLES32.glEnableVertexAttribArray(positionHandle)
        GLES32.glVertexAttribPointer(positionHandle, 3, GLES32.GL_FLOAT, false, 0, vertexBuffer)
        GLES32.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)

        GLES32.glDrawArrays(GLES32.GL_LINES, 0, gridVertices.size / 3)
        GLES32.glDisableVertexAttribArray(positionHandle)
    }

    private fun loadShader(type: Int, code: String): Int {
        return GLES32.glCreateShader(type).also { shader ->
            GLES32.glShaderSource(shader, code)
            GLES32.glCompileShader(shader)
        }
    }

    private fun generateGridVertices(): FloatArray {
        val vertices = mutableListOf<Float>()

        // 적위선: -80° ~ 80° (10도 간격)
        for (dec in -80..80 step 10) {
            for (ra in 0 until 360 step 5) {
                val ra1 = Math.toRadians(ra.toDouble())
                val ra2 = Math.toRadians((ra + 5).toDouble())
                val decRad = Math.toRadians(dec.toDouble())
                val r = 1.0

                val x1 = r * cos(decRad) * cos(ra1)
                val y1 = r * sin(decRad)
                val z1 = r * cos(decRad) * sin(ra1)

                val x2 = r * cos(decRad) * cos(ra2)
                val y2 = y1
                val z2 = r * cos(decRad) * sin(ra2)

                vertices.addAll(listOf(x1.toFloat(), y1.toFloat(), z1.toFloat(), x2.toFloat(), y2.toFloat(), z2.toFloat()))
            }
        }

        // 적경선: 0° ~ 350° (10도 간격)
        for (ra in 0 until 360 step 10) {
            for (dec in -90 until 90 step 5) {
                val dec1 = Math.toRadians(dec.toDouble())
                val dec2 = Math.toRadians((dec + 5).toDouble())
                val raRad = Math.toRadians(ra.toDouble())
                val r = 1.0

                val x1 = r * cos(dec1) * cos(raRad)
                val y1 = r * sin(dec1)
                val z1 = r * cos(dec1) * sin(raRad)

                val x2 = r * cos(dec2) * cos(raRad)
                val y2 = r * sin(dec2)
                val z2 = r * cos(dec2) * sin(raRad)

                vertices.addAll(listOf(x1.toFloat(), y1.toFloat(), z1.toFloat(), x2.toFloat(), y2.toFloat(), z2.toFloat()))
            }
        }

        return vertices.toFloatArray()
    }
}
