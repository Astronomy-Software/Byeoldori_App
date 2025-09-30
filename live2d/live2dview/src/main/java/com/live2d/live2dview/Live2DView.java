package com.live2d.live2dview;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

public class Live2DView extends GLSurfaceView {

    private GLRenderer glRenderer;

    public Live2DView(Context context) {
        super(context);
        init(context);
    }

    public Live2DView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        // OpenGL ES 2.0 사용
        setEGLContextClientVersion(2);

        // 배경 투명 설정
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(android.graphics.PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true); // 필요에 따라 setZOrderMediaOverlay(true)

        // Renderer 연결
        glRenderer = new GLRenderer();
        setRenderer(glRenderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);

        // Live2D 초기화
        if (context instanceof Activity) {
            LAppDelegate.getInstance().onStart((Activity) context);
        } else {
            LAppDelegate.getInstance().onStart(null);
        }

        // 시스템 UI 숨기기
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
                activity.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
            } else {
                WindowInsetsController controller = activity.getWindow().getInsetsController();
                if (controller != null) {
                    controller.hide(WindowInsets.Type.navigationBars() | WindowInsets.Type.statusBars());
                    controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LAppDelegate.getInstance().onPause();
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        final float pointX = event.getX();
        final float pointY = event.getY();

        // GLSurfaceView의 이벤트 큐에 전달
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
    // 외부에서 안전하게 호출할 수 있는 제어 API
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

}
