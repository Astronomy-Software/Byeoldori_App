package com.example.byeoldori.ui.screen.SkyMap.render

import android.graphics.Color
import android.opengl.GLES32
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*

class EquatorialGrid(private val textLabelManager: TextLabelManager) {
    companion object {
        // GLES32에 GL_MULTISAMPLE 상수가 없으면 직접 정의
        private const val GL_MULTISAMPLE = 0x809D
    }

    // 1. 격자 정점 데이터
    private val gridVertices = generateGridVertices()
    private val vertexBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(gridVertices.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer().apply {
            put(gridVertices)
            position(0)
        }

    // 2. 쉐이더 코드 (uColor 유니폼 추가)
    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
        }
    """

    private val fragmentShaderCode = """
        precision mediump float;
        uniform vec4 uColor;
        void main() {
            gl_FragColor = uColor;
        }
    """

    // 3. 프로그램 생성 및 핸들 위치 캐싱
    private val program: Int = GLES32.glCreateProgram().also { prog ->
        val vsh = loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode)
        val fsh = loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode)
        GLES32.glAttachShader(prog, vsh)
        GLES32.glAttachShader(prog, fsh)
        GLES32.glLinkProgram(prog)
    }
    private val posLoc   = GLES32.glGetAttribLocation(program, "vPosition")
    private val mvpLoc   = GLES32.glGetUniformLocation(program, "uMVPMatrix")
    private val colorLoc = GLES32.glGetUniformLocation(program, "uColor")

    init {
        // 북극·남극 라벨
        textLabelManager.addLabelByRaDec("북극",  0f,  90f, Color.CYAN, 128f)
        textLabelManager.addLabelByRaDec("남극",  0f, -90f, Color.CYAN, 128f)

        // 모든 적위선마다 디클리네이션 값 라벨 추가 (textSize = 16f)
        for (dec in -80..80 step 10) {
            val label = "${dec}°"
            textLabelManager.addLabelByRaDec(label, -1f, dec.toFloat()+1f, Color.WHITE, 64f)
        }

        // 모든 적경선마다 라이트 어센션 값 라벨 추가 (textSize = 16f)
        // 여기서는 RA를 시간(h) 단위로 변환하여 'h' 표기
        for (raDeg in 0 until 360 step 30) {
            val hours = raDeg / 15
            val label = "${hours}h"
            // 위도 0도(적도) 근처에 위치시키기 위해 dec=0
            textLabelManager.addLabelByRaDec(label, raDeg.toFloat()+1f, 1f, Color.WHITE, 64f)
        }

        // 블렌딩 및 멀티샘플링 활성화
        GLES32.glEnable(GLES32.GL_BLEND)
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA)
        GLES32.glEnable(GL_MULTISAMPLE)
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES32.glUseProgram(program)
        GLES32.glUniformMatrix4fv(mvpLoc, 1, false, mvpMatrix, 0)

        // 1) 외곽 글로우 패스 (두껍고 반투명)
        GLES32.glLineWidth(6f)
        GLES32.glUniform4f(colorLoc, 0.5f, 0.7f, 1.0f, 0.2f)
        GLES32.glEnableVertexAttribArray(posLoc)
        GLES32.glVertexAttribPointer(posLoc, 3, GLES32.GL_FLOAT, false, 0, vertexBuffer)
        GLES32.glDrawArrays(GLES32.GL_LINES, 0, gridVertices.size / 3)

        // 2) 핵심 라인 패스 (중간 두께, 불투명)
        GLES32.glLineWidth(3f)
        GLES32.glUniform4f(colorLoc, 0.5f, 0.7f, 1.0f, 1.0f)
        GLES32.glDrawArrays(GLES32.GL_LINES, 0, gridVertices.size / 3)

        GLES32.glDisableVertexAttribArray(posLoc)
    }

    private fun loadShader(type: Int, src: String): Int =
        GLES32.glCreateShader(type).also { sh ->
            GLES32.glShaderSource(sh, src)
            GLES32.glCompileShader(sh)
        }

    private fun generateGridVertices(): FloatArray {
        val verts = mutableListOf<Float>()
        // 적위선: -80°~80°, 10° 간격, 5° 세그먼트
        for (dec in -80..80 step 10) {
            val dRad = Math.toRadians(dec.toDouble())
            for (ra in 0 until 360 step 5) {
                val r1 = Math.toRadians(ra.toDouble())
                val r2 = Math.toRadians(ra + 5.0)
                val x1 = (cos(dRad) * cos(r1)).toFloat()
                val y1 = (sin(dRad)).toFloat()
                val z1 = (cos(dRad) * sin(r1)).toFloat()
                val x2 = (cos(dRad) * cos(r2)).toFloat()
                val z2 = (cos(dRad) * sin(r2)).toFloat()
                verts += listOf(x1, y1, z1, x2, y1, z2)
            }
        }
        // 적경선: 0°~350°, 10° 간격, 5° 세그먼트
        for (ra in 0 until 360 step 10) {
            val raRad = Math.toRadians(ra.toDouble())
            for (dec in -90 until 90 step 5) {
                val d1 = Math.toRadians(dec.toDouble())
                val d2 = Math.toRadians(dec + 5.0)
                val x1 = (cos(d1) * cos(raRad)).toFloat()
                val y1 = (sin(d1)).toFloat()
                val z1 = (cos(d1) * sin(raRad)).toFloat()
                val x2 = (cos(d2) * cos(raRad)).toFloat()
                val y2 = (sin(d2)).toFloat()
                val z2 = (cos(d2) * sin(raRad)).toFloat()
                verts += listOf(x1, y1, z1, x2, y2, z2)
            }
        }
        return verts.toFloatArray()
    }
}
