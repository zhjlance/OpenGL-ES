package com.example.glsurfaceviewdemo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GLSurfaceViewTest extends GLSurfaceView {
    public GLSurfaceViewTest(Context context) {
        super(context);

        // 设置OpenGL ES版本（由于3.0兼容2.0，我们使用3.0）
        setEGLContextClientVersion(3);

        // 设置渲染器Renderer，函数调用后，里面会启动一个新线程构造EGL环境
        setRenderer(new GLRenderTest(context));
    }
}
