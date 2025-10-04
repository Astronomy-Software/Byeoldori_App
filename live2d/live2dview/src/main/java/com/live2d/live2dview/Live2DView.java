package com.live2d.live2dview;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Jetpack Compose(AndroidView)에서 감쌀 수 있는 Live2D GLSurfaceView.
 * - OpenGL 컨텍스트 관리
 * - Live2D 모델 터치/모션 이벤트 전달
 * - 외부에서 안전하게 호출 가능한 API 제공
 */
public class Live2DView extends GLSurfaceView {

    // -------------------------------
    // 필드
    // -------------------------------
    private GLRenderer glRenderer;

    // -------------------------------
    // 생성자 & 초기화
    // -------------------------------
    public Live2DView(Context context) {
        super(context);
        init();
    }

    public Live2DView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // OpenGL ES 2.0 사용
        setEGLContextClientVersion(2);

        // 배경 투명 설정
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(android.graphics.PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);

        // Renderer 연결
        glRenderer = new GLRenderer();
        setRenderer(glRenderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    // -------------------------------
    // GLSurfaceView 생명주기 콜백
    // -------------------------------
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);

        queueEvent(() -> {
            // GLContext가 새로 생겼으므로 Delegate 초기화
            LAppDelegate.getInstance().onSurfaceCreated();

            // Framework 살아있으면 모델 재로드
            if (com.live2d.sdk.cubism.framework.CubismFramework.isInitialized()) {
                LAppLive2DManager.getInstance().reloadCurrentModel();
            }
        });
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        super.surfaceChanged(holder, format, width, height);

        if (width == 0 || height == 0) {
            Log.w("Live2DView", "⚠️ surfaceChanged called with 0 size → skip init");
            return;
        }

        queueEvent(() -> {
            LAppDelegate.getInstance().onSurfaceChanged(width, height);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // 필요시 resume 로직 (현재는 GLThread resume만)
    }

    @Override
    public void onPause() {
        super.onPause();
        LAppDelegate.getInstance().onPause();
    }

    // -------------------------------
    // 터치 이벤트
    // -------------------------------
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        final float pointX = event.getX();
        final float pointY = event.getY();

        // 현재 표시 중인 모델 가져오기
        LAppModel model = LAppLive2DManager.getInstance().getModel(0);

        boolean hit = false;
        if (model != null) {
            // 좌표 변환: 화면 좌표 → Live2D 좌표(-1 ~ +1)
            float logicalX = (pointX / getWidth()) * 2f - 1f;
            float logicalY = (pointY / getHeight()) * -2f + 1f;

            if (model.hitTest("Head", logicalX, logicalY) ||
                    model.hitTest("Body", logicalX, logicalY)) {
                hit = true;
            }
        }

        if (!hit) return false;

        // ✅ 캐릭터 내부 터치 이벤트만 Live2D에 전달
        queueEvent(() -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    LAppDelegate.getInstance().onTouchBegan(pointX, pointY);
                    break;
                case MotionEvent.ACTION_UP:
                    LAppDelegate.getInstance().onTouchEnd(pointX, pointY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    LAppDelegate.getInstance().onTouchMoved(pointX, pointY);
                    break;
            }
        });
        return true;
    }

    // -------------------------------
    // 외부에서 호출 가능한 제어 API
    // -------------------------------

    /** 다음 캐릭터로 변경 */
    public void nextCharacter() {
        queueEvent(() -> LAppLive2DManager.getInstance().nextScene());
    }

    /** 특정 인덱스 캐릭터로 변경 */
    public void changeCharacter(int index) {
        queueEvent(() -> LAppLive2DManager.getInstance().changeScene(index));
    }

    /** 특정 모션 실행 */
    public void playMotion(String group, int no) {
        queueEvent(() -> {
            LAppModel model = LAppLive2DManager.getInstance().getModel(0);
            if (model != null) {
                model.startMotion(group, no, LAppDefine.Priority.FORCE.getPriority());
            }
        });
    }

    /** 표정 변경 */
    public void setExpression(String name) {
        queueEvent(() -> {
            LAppModel model = LAppLive2DManager.getInstance().getModel(0);
            if (model != null) {
                model.setExpression(name);
            }
        });
    }

    /** 드래그 전달 */
    public void onDrag(float x, float y) {
        queueEvent(() -> LAppLive2DManager.getInstance().onDrag(x, y));
    }

    /** 탭 이벤트 전달 */
    public void onTap(float x, float y) {
        queueEvent(() -> LAppLive2DManager.getInstance().onTap(x, y));
    }

    /** 현재 모델에서 사용 가능한 모션 이름들 */
    public List<String> getAvailableMotions() {
        List<String> motions = new ArrayList<>();
        LAppModel model = LAppLive2DManager.getInstance().getModel(0);
        if (model != null) {
            motions.addAll(model.getMotionList());
        }
        return motions;
    }

    // -------------------------------
    // 리소스 관리 유틸
    // -------------------------------

    /** 모델만 해제 후 다시 로드 */
    public void resetLive2D() {
        queueEvent(() -> {
            LAppLive2DManager.getInstance().releaseAllModel();
            LAppLive2DManager.getInstance().reloadCurrentModel();
        });
    }

    /** 모델만 다시 세팅 (Framework dispose는 하지 않음) */
    public void reInit() {
        queueEvent(() -> {
            LAppLive2DManager.getInstance().releaseAllModel();
            LAppLive2DManager.getInstance().reloadCurrentModel();
        });
    }
}
