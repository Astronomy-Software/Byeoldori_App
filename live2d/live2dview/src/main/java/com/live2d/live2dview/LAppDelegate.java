/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */

package com.live2d.live2dview;

import static android.opengl.GLES20.*;

import android.app.Activity;
import android.opengl.GLES20;

import com.live2d.sdk.cubism.framework.CubismFramework;

/**
 * Live2D 전체 수명주기를 관리하는 싱글톤 Delegate 클래스
 * (앱 프로세스 내에서 유일하게 존재)
 */
public class LAppDelegate {

    // ─────────────────────────────
    // 🔸 Singleton 관리
    // ─────────────────────────────
    private static LAppDelegate s_instance;

    public static LAppDelegate getInstance() {
        if (s_instance == null) {
            s_instance = new LAppDelegate();
        }
        return s_instance;
    }

    public static void releaseInstance() {
        if (s_instance != null) {
            s_instance = null;
        }
    }

    // ─────────────────────────────
    // 🔸 필드 선언
    // ─────────────────────────────
    private Activity activity;
    private final CubismFramework.Option cubismOption = new CubismFramework.Option();
    private LAppTextureManager textureManager;
    private LAppView view;
    private int windowWidth;
    private int windowHeight;
    private boolean isActive = true;
    private int currentModel;
    private boolean isCaptured;
    private float mouseX;
    private float mouseY;

    // ─────────────────────────────
    // 🔸 생성자 (단순 옵션 초기화만)
    // ─────────────────────────────
    private LAppDelegate() {
        // Activity는 아직 없음 → CubismFramework 초기화 금지
        currentModel = 0;
        cubismOption.logFunction = new LAppPal.PrintLogFunction();
        cubismOption.loggingLevel = LAppDefine.cubismLoggingLevel;
    }

    // ─────────────────────────────
    // 🔸 Lifecycle Hooks
    // ─────────────────────────────

    /** 앱 시작 시점 (Activity 연결 + CubismFramework 초기화) */
    public void onStart(Activity activity) {
        this.activity = activity;

        // Framework 초기화 (최초 1회만)
        if (!CubismFramework.isStarted()) {
            CubismFramework.startUp(cubismOption);
        }
        if (!CubismFramework.isInitialized()) {
            CubismFramework.initialize();
        }

        textureManager = new LAppTextureManager();
        view = new LAppView();
        LAppPal.updateTime();
    }

    /** 앱 일시정지 */
    public void onPause() {
        currentModel = LAppLive2DManager.getInstance().getCurrentModel();
    }

    /** 앱이 화면에서 완전히 사라질 때 (홈 이동 포함) */
    public void onStop() {
        if (view != null) view.close();
        textureManager = null;
        // 홈 이동 시 Framework 유지, 완전 종료는 onDestroy()에서 처리
    }

    /** 앱 완전 종료 (Activity destroy) */
    public void onDestroy() {
        LAppLive2DManager.releaseInstance();
        CubismFramework.dispose();
        releaseInstance();
    }

    // ─────────────────────────────
    // 🔸 OpenGL 이벤트 처리
    // ─────────────────────────────

    /** Surface가 처음 생성되거나 GLContext가 복원될 때 */
    public void onSurfaceCreated() {
        // 🔸 OpenGL 기본 설정
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        // 🔸 GLContext가 완전히 새로 생겼으므로 Cubism 초기화 확인
        if (!CubismFramework.isStarted()) {
            CubismFramework.startUp(cubismOption);
        }
        if (!CubismFramework.isInitialized()) {
            CubismFramework.initialize();
        }

        // 🔸 TextureManager 재생성 (GLContext 기반)
        if (textureManager == null) {
            textureManager = new LAppTextureManager();
        } else {
            textureManager.clear(); // 이전 텍스처 ID 무효화
        }

        // 🔸 View 재초기화 (Projection, Shader 등)
        if (view == null) {
            view = new LAppView();
        }
        view.initialize();

        // 🔸 모델을 완전히 다시 로드
        LAppLive2DManager live2D = LAppLive2DManager.getInstance();
        live2D.releaseAllModel();          // 기존 모델 완전 해제
        live2D.changeScene(currentModel);  // 현재 인덱스 모델 다시 로드

        isActive = true;
    }

    /** Surface 크기 변경 시 (회전 등) */
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        windowWidth = width;
        windowHeight = height;

        if (view != null) {
            view.initialize();
        }

        if (CubismFramework.isInitialized()) {
            int current = LAppLive2DManager.getInstance().getCurrentModel();
            LAppLive2DManager.getInstance().changeScene(current);
        }

        isActive = true;
    }

    /** GLContext 완전 복원 시 */
    public void onGLContextRestored() {
        if (view != null) view.initialize();
        if (textureManager != null) textureManager.clear();
        LAppLive2DManager.getInstance().reloadCurrentModel();
    }

    // ─────────────────────────────
    // 🔸 Render Loop
    // ─────────────────────────────
    public void run() {
        LAppPal.updateTime();
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearDepthf(1.0f);

        if (view != null) {
            view.render();
        }

        if (!isActive && activity != null) {
            activity.finishAndRemoveTask();
        }
    }

    // ─────────────────────────────
    // 🔸 터치 이벤트
    // ─────────────────────────────
    public void onTouchBegan(float x, float y) {
        mouseX = x;
        mouseY = y;
        if (view != null) {
            isCaptured = true;
            view.onTouchesBegan(mouseX, mouseY);
        }
    }

    public void onTouchEnd(float x, float y) {
        mouseX = x;
        mouseY = y;
        if (view != null) {
            isCaptured = false;
            view.onTouchesEnded(mouseX, mouseY);
        }
    }

    public void onTouchMoved(float x, float y) {
        mouseX = x;
        mouseY = y;
        if (isCaptured && view != null) {
            view.onTouchesMoved(mouseX, mouseY);
        }
    }

    // ─────────────────────────────
    // 🔸 Getter
    // ─────────────────────────────
    public Activity getActivity() { return activity; }
    public LAppTextureManager getTextureManager() { return textureManager; }
    public LAppView getView() { return view; }
    public int getWindowWidth() { return windowWidth; }
    public int getWindowHeight() { return windowHeight; }
}
