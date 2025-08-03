package com.example.byeoldori.ui.screen.SkyMap.render

import android.graphics.*
import android.opengl.GLES32
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.*

data class TextLabel(
    val text: String,
    val ra: Float,
    val dec: Float,
    val color: Int = Color.WHITE,
    val textSize: Float = 128f
)

class TextLabelManager {
    private data class LabelData(
        val textureId: Int,
        val position: FloatArray,
        val vertexBuffer: FloatBuffer,
        val texCoordBuffer: FloatBuffer
    )

    private val labels = mutableListOf<Pair<TextLabel, LabelData>>()
    private val program: Int

    companion object {
        // 기준 텍스트 크기 (비트맵 픽셀)과 기본 쿼드 스케일
        private const val BASE_TEXT_SIZE = 128f
        private const val BASE_QUAD_SIZE = 0.05f
    }

    init {
        val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            uniform mat4 uViewMatrix;
            uniform float uFov;
            attribute vec4 vPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            uniform vec3 uLabelPos;
            void main() {
                mat4 bill = mat4(
                  uViewMatrix[0][0], uViewMatrix[1][0], uViewMatrix[2][0], 0.0,
                  uViewMatrix[0][1], uViewMatrix[1][1], uViewMatrix[2][1], 0.0,
                  uViewMatrix[0][2], uViewMatrix[1][2], uViewMatrix[2][2], 0.0,
                  0.0,               0.0,               0.0,               1.0
                );
                float zoomScale = tan(radians(60.0 * 0.5)) / tan(radians(uFov * 0.5));
                vec4 scaled = vPosition / zoomScale;
                vec4 worldPos = vec4(uLabelPos, 1.0) + bill * scaled;
                gl_Position = uMVPMatrix * worldPos;
                vTexCoord = aTexCoord;
            }
        """

        val fragmentShaderCode = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D uTexture;
            void main() {
                gl_FragColor = texture2D(uTexture, vTexCoord);
            }
        """

        val vsh = loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode)
        val fsh = loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES32.glCreateProgram().also {
            GLES32.glAttachShader(it, vsh)
            GLES32.glAttachShader(it, fsh)
            GLES32.glLinkProgram(it)
        }
    }

    /** 적경·적위 기준 라벨 추가 */
    fun addLabelByRaDec(
        text: String,
        ra: Float,
        dec: Float,
        textColor: Int = Color.WHITE,
        textSize: Float = BASE_TEXT_SIZE
    ) {
        val pos = raDecToXYZ(ra, dec)
        val bmp = createTextBitmap(text, textSize, textColor)
        val tex = createTextureFromBitmap(bmp)

        // 텍스트 크기에 따른 쿼드 스케일 조정
        val scaleFactor = textSize / BASE_TEXT_SIZE
        val s = BASE_QUAD_SIZE * scaleFactor

        val verts = floatArrayOf(
            -s,  s, 0f,
            -s, -s, 0f,
            s,  s, 0f,
            s, -s, 0f
        )
        val uvs = floatArrayOf(0f, 0f, 0f, 1f, 1f, 0f, 1f, 1f)

        val vb = ByteBuffer.allocateDirect(verts.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(verts); position(0)
            }
        val tb = ByteBuffer.allocateDirect(uvs.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(uvs); position(0)
            }

        labels += TextLabel(text, ra, dec, textColor, textSize) to
                LabelData(tex, pos, vb, tb)
    }

    fun drawAll(mvp: FloatArray, view: FloatArray, fov: Float) {
        GLES32.glUseProgram(program)
        val pMVP = GLES32.glGetUniformLocation(program, "uMVPMatrix")
        val pView = GLES32.glGetUniformLocation(program, "uViewMatrix")
        val pFov  = GLES32.glGetUniformLocation(program, "uFov")
        val pPos  = GLES32.glGetUniformLocation(program, "uLabelPos")
        val aPos  = GLES32.glGetAttribLocation(program, "vPosition")
        val aUV   = GLES32.glGetAttribLocation(program, "aTexCoord")
        val smp   = GLES32.glGetUniformLocation(program, "uTexture")

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0)
        GLES32.glUniform1i(smp, 0)
        GLES32.glUniformMatrix4fv(pMVP, 1, false, mvp, 0)
        GLES32.glUniformMatrix4fv(pView, 1, false, view, 0)
        GLES32.glUniform1f(pFov, fov)

        labels.forEach { (_, data) ->
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, data.textureId)
            GLES32.glUniform3fv(pPos, 1, data.position, 0)

            GLES32.glEnableVertexAttribArray(aPos)
            GLES32.glVertexAttribPointer(aPos, 3, GLES32.GL_FLOAT, false, 0, data.vertexBuffer)
            GLES32.glEnableVertexAttribArray(aUV)
            GLES32.glVertexAttribPointer(aUV, 2, GLES32.GL_FLOAT, false, 0, data.texCoordBuffer)

            GLES32.glDrawArrays(GLES32.GL_TRIANGLE_STRIP, 0, 4)

            GLES32.glDisableVertexAttribArray(aPos)
            GLES32.glDisableVertexAttribArray(aUV)
        }
    }

    private fun raDecToXYZ(ra: Float, dec: Float): FloatArray {
        val r = Math.toRadians(ra.toDouble())
        val d = Math.toRadians(dec.toDouble())
        return floatArrayOf(
            (cos(d) * cos(r)).toFloat(),
            sin(d).toFloat(),
            (cos(d) * sin(r)).toFloat()
        )
    }

    private fun createTextBitmap(text: String, size: Float, color: Int): Bitmap {
        val p = Paint().apply {
            isAntiAlias = true
            textSize = size
            this.color = color
            textAlign = Paint.Align.LEFT
        }
        val w = (p.measureText(text) + 10).toInt()
        val h = (p.fontMetrics.bottom - p.fontMetrics.top).toInt()
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            val c = Canvas(this)
            c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            c.drawText(text, 0f, -p.fontMetrics.ascent, p)
        }
    }

    private fun createTextureFromBitmap(bmp: Bitmap): Int {
        val tid = IntArray(1)
        GLES32.glGenTextures(1, tid, 0)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, tid[0])
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR)
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR)
        GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bmp, 0)
        bmp.recycle()
        return tid[0]
    }

    private fun loadShader(type: Int, src: String): Int {
        val s = GLES32.glCreateShader(type)
        GLES32.glShaderSource(s, src)
        GLES32.glCompileShader(s)
        return s
    }
}
