package com.example.byeoldori.ui.screen.SkyMap.render

import android.content.Context
import android.opengl.GLSurfaceView

class CelestialGLView(context: Context) : GLSurfaceView(context) {
    val renderer = SkySphereRenderer()

    init {
        setEGLContextClientVersion(3)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}
