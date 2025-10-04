/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */

package com.live2d.live2dview;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_ONE;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glClearDepthf;
import android.app.Activity;
import android.opengl.GLES20;
import com.live2d.sdk.cubism.framework.CubismFramework;

public class LAppDelegate {
    public static LAppDelegate getInstance() {
        if (s_instance == null) {
            s_instance = new LAppDelegate();
        }
        return s_instance;
    }

    /**
     * クラスのインスタンス（シングルトン）を解放する。
     */
    public static void releaseInstance() {
        if (s_instance != null) {
            s_instance = null;
        }
    }

    /**
     * アプリケーションを非アクティブにする
     */
    public void deactivateApp() {
        isActive = false;
    }

    public void onStart(Activity activity) {
        textureManager = new LAppTextureManager();
        view = new LAppView();

        this.activity = activity;

        LAppPal.updateTime();
    }

    public void onPause() {
        currentModel = LAppLive2DManager.getInstance().getCurrentModel();
    }

    public void onStop() {
        if (view != null) {
            view.close();
        }
        textureManager = null;

        LAppLive2DManager.releaseInstance();
        CubismFramework.dispose();
    }

    public void onDestroy() {
        releaseInstance();
    }

//    public void onSurfaceCreated() {
//        // テクスチャサンプリング設定
//        GLES20.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//        GLES20.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//
//        // 透過設定
//        GLES20.glEnable(GLES20.GL_BLEND);
//        GLES20.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
//
//        // Initialize Cubism SDK framework
//        CubismFramework.initialize();
//
//        // ✅ GL context가 새로 생겼으니 캐시 비우고 다시 로드
//        if (textureManager != null) {
//            textureManager.clear();
//        }
//        LAppLive2DManager.getInstance().reloadCurrentModel();
//    }


public void onSurfaceCreated() {
    // 텍스처 관련 초기화
    GLES20.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    GLES20.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

    GLES20.glEnable(GLES20.GL_BLEND);
    GLES20.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

    // Framework 상태 확인
    if (!CubismFramework.isStarted() || !CubismFramework.isInitialized()) {
        CubismFramework.startUp(cubismOption);
        CubismFramework.initialize();
    }

    // ✅ GLContext가 새로 생겼으니 Shader도 재초기화 필요
    if (view != null) {
        view.initialize();       // 여기서 shader, sprite, 렌더링타겟 초기화
    }

    if (textureManager != null) {
        textureManager.clear();  // 텍스처 캐시 클리어
    }

    // 모델 재로드
    if (CubismFramework.isInitialized()) {
        LAppLive2DManager.getInstance().reloadCurrentModel();
    }
}



//    public void onSurfaceChanged(int width, int height) {
//        // 描画範囲指定
//        GLES20.glViewport(0, 0, width, height);
//        windowWidth = width;
//        windowHeight = height;
//
//        // AppViewの初期化
//        view.initialize();
////        view.initializeSprite();
//
//        // load models
//        if (LAppLive2DManager.getInstance().getCurrentModel() != currentModel) {
//            LAppLive2DManager.getInstance().changeScene(currentModel);
//        }
//
//        isActive = true;
//    }
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        windowWidth = width;
        windowHeight = height;

        if (view != null) {
            view.initialize();
            // view.initializeSprite(); // 필요 시 추가
        }

        // ✅ 화면 크기 변경 시 projection 갱신 → 모델에도 반영
        if (CubismFramework.isInitialized()) {
            int current = LAppLive2DManager.getInstance().getCurrentModel();
            LAppLive2DManager.getInstance().changeScene(current);
        }

        isActive = true;
    }


    public void run() {
        // 時間更新
        LAppPal.updateTime();

        // 画面初期化
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearDepthf(1.0f);

        if (view != null) {
            view.render();
        }

        // アプリケーションを非アクティブにする
        if (!isActive) {
            activity.finishAndRemoveTask();
        }
    }


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

    // getter, setter群
    public Activity getActivity() {
        return activity;
    }

    public LAppTextureManager getTextureManager() {
        return textureManager;
    }

    public LAppView getView() {
        return view;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    private static LAppDelegate s_instance;

    private LAppDelegate() {
        currentModel = 0;

        // Set up Cubism SDK framework.
        cubismOption.logFunction = new LAppPal.PrintLogFunction();
        cubismOption.loggingLevel = LAppDefine.cubismLoggingLevel;

        CubismFramework.cleanUp();
        CubismFramework.startUp(cubismOption);
    }

    private Activity activity;

    private final CubismFramework.Option cubismOption = new CubismFramework.Option();

    private LAppTextureManager textureManager;
    private LAppView view;
    private int windowWidth;
    private int windowHeight;
    private boolean isActive = true;

    /**
     * モデルシーンインデックス
     */
    private int currentModel;

    /**
     * クリックしているか
     */
    private boolean isCaptured;
    /**
     * マウスのX座標
     */
    private float mouseX;
    /**
     * マウスのY座標
     */
    private float mouseY;
}
