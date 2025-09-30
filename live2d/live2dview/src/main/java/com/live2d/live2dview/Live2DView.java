package com.live2d.live2dview;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;


// Live2D 관리 파일
// 여기서 대부분을 관리함.
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
            // Activity context가 아닌 경우에는 나중에 Activity 주입 필요
            LAppDelegate.getInstance().onStart(null);
        }

        // 시스템 UI 숨기기 설정 (원본 샘플 코드 반영)
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
    public void onResume() {
        super.onResume();
        // GLSurfaceView 자체 resume 호출
        this.onResume();
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
}
