package com.live2d.live2dview;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.live2d.sdk.cubism.framework.CubismFramework;

import java.util.ArrayList;
import java.util.List;

/**
 * Jetpack Compose(AndroidView)에서 감쌀 수 있는 Live2D GLSurfaceView.
 * - OpenGL 컨텍스트 및 생명주기 관리 (Activity 수준으로 강화)
 * - Live2D 모델 터치/모션 이벤트 전달
 * - 외부에서 안전하게 호출 가능한 제어 API 제공
 */
public class Live2DView extends GLSurfaceView {

    // ─────────────────────────────
    // 🔹 필드
    // ─────────────────────────────
    private final GLRenderer glRenderer;

    // ─────────────────────────────
    // 🔹 생성자 & 초기화
    // ─────────────────────────────
    public Live2DView(Context context) {
        super(context);
        glRenderer = new GLRenderer();
        init();
    }

    public Live2DView(Context context, AttributeSet attrs) {
        super(context, attrs);
        glRenderer = new GLRenderer();
        init();
    }

    private void init() {
        // OpenGL ES 2.0 사용
        setEGLContextClientVersion(2);

        // 배경 투명 설정
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);

        // GLContext 보존 (pause → resume 시 컨텍스트 유지)
        setPreserveEGLContextOnPause(true);

        // 렌더러 연결
        setRenderer(glRenderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);

        Log.d("Live2DView", "✅ Live2DView initialized");
    }

    // ─────────────────────────────
    // 🔹 GLSurfaceView 생명주기 콜백
    // ─────────────────────────────

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);

        queueEvent(() -> {
            Log.d("Live2DView", "🟢 surfaceCreated → GL context recreated");

            Activity activity = getActivity();

            // CubismFramework 초기화
            if (!CubismFramework.isStarted()) {
                if (activity != null) {
                    LAppDelegate.getInstance().onStart(activity);
                } else {
                    Log.w("Live2DView", "⚠️ Activity context not found for CubismFramework");
                }
            }

            // GLContext 복원 또는 재초기화
            if (!CubismFramework.isInitialized()) {
                LAppDelegate.getInstance().onSurfaceCreated();
            } else {
                LAppDelegate.getInstance().onGLContextRestored();
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
            Log.d("Live2DView", "📐 surfaceChanged: " + width + "x" + height);
            LAppDelegate.getInstance().onSurfaceChanged(width, height);
        });
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        Log.d("Live2DView", "🟥 surfaceDestroyed → releasing GL resources");
        queueEvent(() -> {
            LAppDelegate.getInstance().onStop();
        });
    }

    // ─────────────────────────────
    // 🔹 수명주기 관리 (Activity 수준과 동일)
    // ─────────────────────────────

    public void onStart() {
        Activity activity = getActivity();
        if (activity != null) {
            queueEvent(() -> {
                Log.d("Live2DView", "▶ onStart");
                LAppDelegate.getInstance().onStart(activity);
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Live2DView", "▶ onResume → GLSurfaceView resume");
        queueEvent(() -> {
            LAppDelegate.getInstance().onSurfaceCreated();
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("Live2DView", "⏸️ onPause → stop rendering & save state");
        queueEvent(() -> {
            LAppDelegate.getInstance().onPause();
        });
    }

    public void onStop() {
        Log.d("Live2DView", "🟥 onStop called");
        queueEvent(() -> {
            LAppDelegate.getInstance().onStop();
        });
    }

    public void onDestroy() {
        Log.d("Live2DView", "💀 onDestroy → disposing Live2D resources");
        queueEvent(() -> {
            LAppDelegate.getInstance().onDestroy();
        });
    }

    // ─────────────────────────────
    // 🔹 터치 이벤트
    // ─────────────────────────────
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        final float pointX = event.getX();
        final float pointY = event.getY();

        LAppModel model = LAppLive2DManager.getInstance().getModel(0);
        boolean hit = false;

        if (model != null) {
            float logicalX = (pointX / getWidth()) * 2f - 1f;
            float logicalY = (pointY / getHeight()) * -2f + 1f;
            if (model.hitTest("Head", logicalX, logicalY) || model.hitTest("Body", logicalX, logicalY)) {
                hit = true;
            }
        }

        if (!hit) return false;

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

    // ─────────────────────────────
    // 🔹 외부 제어 API
    // ─────────────────────────────

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

    /** 현재 모델에서 사용 가능한 모션 목록 */
    public List<String> getAvailableMotions() {
        List<String> motions = new ArrayList<>();
        LAppModel model = LAppLive2DManager.getInstance().getModel(0);
        if (model != null) {
            motions.addAll(model.getMotionList());
        }
        return motions;
    }

    // ─────────────────────────────
    // 🔹 내부 유틸: Context → Activity 변환
    // ─────────────────────────────
    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
