package com.example.glsurfaceviewdemo;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderTest implements GLSurfaceView.Renderer {
    private Triangle mTriangle;
    private Context mContext;

    public GLRenderTest(Context context) {
        this.mContext = context;
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        mTriangle = new Triangle(mContext);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0,  width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl){
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        mTriangle.draw();
    }

    public void onDestroy() {
        mTriangle.release();
    }
}
