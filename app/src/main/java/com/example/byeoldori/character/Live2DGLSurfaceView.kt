package com.example.byeoldori.character

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.live2d.live2dview.GLRenderer
import com.live2d.live2dview.LAppDefine
import com.live2d.live2dview.LAppDelegate
import com.live2d.live2dview.LAppLive2DManager

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

    // =======================================================================
    // Live2D Controller와 연결하기 위한 공용 메서드
    // =======================================================================

    /**
     * GL 스레드에서 Live2D SDK 관련 코드를 실행하기 위한 헬퍼 함수입니다.
     */
    fun queueLive2DEvent(action: () -> Unit) {
        this.queueEvent(action)
    }

    /**
     * 다음 Live2D 모델로 전환합니다. (LAppLive2DManager.nextScene 호출)
     */
    fun nextCharacter() {
        queueLive2DEvent {
            LAppLive2DManager.getInstance().nextScene()
        }
    }

    /**
     * 특정 인덱스의 Live2D 모델로 전환합니다. (LAppLive2DManager.changeScene 호출)
     */
    fun changeCharacter(index: Int) {
        queueLive2DEvent {
            LAppLive2DManager.getInstance().changeScene(index)
        }
    }

    /**
     * 특정 모션을 재생합니다.
     * @param group 모션 그룹 이름 (예: "Tap", "Idle")
     * @param index 모션 그룹 내의 인덱스
     */
    fun playMotion(group: String, index: Int) {
        // Live2D 샘플에서 모션 재생 시 사용하는 우선순위(NORMAL)를 가져옵니다.
//        val priority = LAppDefine.Priority.NORMAL.getPriority()
        val priority = LAppDefine.Priority.FORCE.getPriority()

        queueLive2DEvent {
            // 현재 활성화된 모델(0번 인덱스)을 가져와서 모션을 재생합니다.
            // LAppLive2DManager.getModelNum()이 1일 때만 안전합니다.
            val model = LAppLive2DManager.getInstance().getModel(0)

            // LAppModel 클래스에 startMotion 메서드가 있다고 가정하고 호출합니다.
            model?.startMotion(group, index, priority, null, null)
        }
    }

    /**
     * 특정 표정(Expression)을 설정합니다.
     * @param exp 표정 파일의 이름 (예: "F01.exp3.json"에서 "F01")
     */
    fun setExpression(exp: String) {
        queueLive2DEvent {
            // 현재 활성화된 모델(0번 인덱스)을 가져와서 표정을 설정합니다.
            val model = LAppLive2DManager.getInstance().getModel(0)

            // LAppModel 클래스에 setExpression 메서드가 있다고 가정하고 호출합니다.
            model?.setExpression(exp)
        }
    }

    /**
     * 현재 로드된 모델의 모션 그룹 목록을 가져옵니다.
     * (주의: Live2D Manager가 초기화된 후 메인 스레드에서 호출되어야 합니다.)
     */
    fun getAvailableMotions(): List<String> {
        return LAppLive2DManager.getInstance().getAvailableMotionGroups()
    }

    // =======================================================================
    // GLSurfaceView 오버라이드 메서드
    // =======================================================================

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