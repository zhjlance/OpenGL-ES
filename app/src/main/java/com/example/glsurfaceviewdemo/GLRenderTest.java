package com.example.glsurfaceviewdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderTest implements GLSurfaceView.Renderer {
    private Triangle mTriangle;
    private TextureRender mTextureRender;
    private Context mContext;
    private int mSurfaceWidth = 0; // 窗口宽
    private int mSurfaceHeight = 0; // 窗口高
    private int mViewportWidth = 0; // 视口宽
    private int mViewportHeight = 0; // 视口高
    private int mViewportX = 0; // 视口起始横坐标
    private int mViewportY = 0; // 视口起始纵坐标
    private Bitmap mBitmap;

    public GLRenderTest(Context context) {
        this.mContext = context;
        mBitmap = loadImage();
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        // mTriangle = new Triangle(mContext);
        mTextureRender = new TextureRender(mContext, mBitmap);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        calculateViewport(); // 计算视口参数

        GLES30.glViewport(mViewportX, mViewportY, mViewportWidth, mViewportHeight); // 设置视口
        // GLES30.glViewport(0, 0,  width, height); // 视口变换时候不能从屏幕左上角开始
    }

    @Override
    public void onDrawFrame(GL10 gl){
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        // mTriangle.draw();
        mTextureRender.draw();
    }

    public void onDestroy() {
        // mTriangle.release();
        mBitmap.recycle(); // 回收bitmap
        mTextureRender.release();
    }

    // 加载图片
    private Bitmap loadImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.android_logo, options);
    }

    // 视口变换：通过调整视口大小，保证图片不被拉伸
    private void calculateViewport() {
        // 获取图片的宽高比
        float imageRatio = (float) mBitmap.getWidth() / mBitmap.getHeight();
        // 获取surface（窗口）的宽高比
        float surfaceRatio = (float) mSurfaceWidth / mSurfaceHeight;

        if (imageRatio > surfaceRatio) {
            // 图片宽高比大于窗口的宽高比，按照宽度填满
            mViewportWidth = mSurfaceWidth;
            mViewportHeight = (int) (mSurfaceWidth / imageRatio);
        } else {
            // 图片宽高比小于等于窗口的宽高比，按照高度填满
            mViewportWidth = (int) (mSurfaceHeight * imageRatio);
            mViewportHeight = mSurfaceHeight;
        }

        // 计算视口的中心位置
        mViewportX = (mSurfaceWidth - mViewportWidth) / 2;
        mViewportY = (mSurfaceHeight - mViewportHeight) / 2;
    }
}
