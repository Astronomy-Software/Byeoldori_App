package com.example.byeoldori.character

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.live2d.live2dview.GLRenderer
import com.live2d.live2dview.LAppDelegate


/**
 * Live2D 모델을 렌더링하기 위한 GLSurfaceView를 캡슐화한 커스텀 뷰입니다.
 * 기존 MainActivity.java의 GLSurfaceView 설정 및 터치 처리 로직을 담당합니다.
 */
class Live2DGLSurfaceView(context: Context) : GLSurfaceView(context) {

    // GLRenderer 인스턴스를 생성하고 GLSurfaceView에 연결합니다.
    private val live2DRenderer: GLRenderer = GLRenderer()

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        // Live2D 렌더링을 담당하는 GLRenderer 연결
        setRenderer(live2DRenderer)
        // 지속적인 렌더링 모드 설정 (Live2D 애니메이션을 위해 필수)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    // 터치 이벤트 처리 (기존 MainActivity.onTouchEvent 로직 이관)
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // GLSurfaceView의 이벤트 처리 큐에 터치 이벤트를 추가하여 Renderer 스레드에서 실행되도록 합니다.
        val pointX = event.x
        val pointY = event.y

        queueEvent {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> LAppDelegate.getInstance().onTouchBegan(pointX, pointY)
                MotionEvent.ACTION_UP -> LAppDelegate.getInstance().onTouchEnd(pointX, pointY)
                MotionEvent.ACTION_MOVE -> LAppDelegate.getInstance().onTouchMoved(pointX, pointY)
            }
        }
        return true // 이벤트를 소비합니다.
    }
}
