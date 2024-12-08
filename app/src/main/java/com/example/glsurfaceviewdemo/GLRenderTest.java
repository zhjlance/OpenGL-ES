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

    private float[] mProjectionMatrix = new float[16]; // 投影矩阵
    private float[] mViewMatrix = new float[16]; // 视图矩阵
    private float[] mMVPMatrix = new float[16]; // mvp矩阵

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

        // calculateViewport(); // 计算视口参数
        calculateViewport2(width, height); // 计算正交投影参数
        mTextureRender.setCustomMVPMatrix(mMVPMatrix); // 设置mvp矩阵给渲染器

        // GLES30.glViewport(mViewportX, mViewportY, mViewportWidth, mViewportHeight); // 视口调整
        GLES30.glViewport(0, 0,  width, height); // 正交矩阵从屏幕左上角开始即可
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
    // 正交投影变换
    private void calculateViewport2(int width, int height) {
        float imageAspectRatio = (float) mBitmap.getWidth() / (float) mBitmap.getHeight();
        float surfaceAspectRatio = (float) width / (float) height;

        if (imageAspectRatio > surfaceAspectRatio) {
            // 图片宽高比大于屏幕,按照宽度填满计算高度
            float tb = imageAspectRatio / surfaceAspectRatio;
            // 计算投影矩阵
            Matrix.orthoM(mProjectionMatrix, 0, -1.0f, 1.0f, -tb, tb, -1.0f, 1.0f);
        } else {
            // 图片宽高比小于等于屏幕,按照高度填满计算宽度
            float tb = surfaceAspectRatio / imageAspectRatio;
            // 计算投影矩阵
            Matrix.orthoM(mProjectionMatrix, 0, -tb, tb, -1.0f, 1.0f, -1.0f, 1.0f);
        }
        // 计算视图矩阵
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        // 计算mvp矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }
}
