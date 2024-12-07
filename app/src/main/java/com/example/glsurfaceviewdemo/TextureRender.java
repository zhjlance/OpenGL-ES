package com.example.glsurfaceviewdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class TextureRender {
    Context mContext;
    private float[] mCoordData = {
            // 顶点坐标          纹理坐标
            -1.0f, 1.0f, 0.0f, 0.0f, 0.0f,  // 左上角
            -1.0f, -1.0f, 0.0f, 0.0f, 1.0f, // 左下角
            1.0f, 1.0f, 0.0f, 1.0f, 0.0f,   // 右上角
            1.0f, -1.0f, 0.0f, 1.0f, 1.0f   // 右下角
    };

    private int mTextureId = -1;
    private FloatBuffer mCoordBuffer;
    private int mProgram = -1;
    private int mVboId;
    private float[] mTranslateMatrix = new float[16];
    private int mPositionHandle = -1; // 顶点位置属性的位置
    private int mTexCoordHandle = -1; // 纹理坐标属性的位置
    private int mTMatrixHandle = -1; // 变换矩阵，用于实现顶点的变换
    private int mSamplerHandle = -1; // 纹理采样器统一变量位置，传递的数据就是纹理单元id

    public TextureRender(Context context) {
        mContext = context;
        // 上传纹理到GPU
        mTextureId = uploadTexture();

        // 初始化坐标数据（包括顶点和纹理的坐标）
        initVertexBuffer();

        // 加载并编译着色器
        initShaders(context);

        // 初始化 VBO
        initVbo();

        // 初始化变换矩阵为单位矩阵, 这将使得后续的变换（如平移、旋转、缩放）从一个默认的状态开始。
        Matrix.setIdentityM(mTranslateMatrix, 0);

        // 获取着色器程序中位置属性的句柄 "aPosition", 这个句柄用于将顶点位置数据传递到着色器中。
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "aPosition");
        if (mPositionHandle == -1) {
            Log.e("TextureRender", "Could not find attribute aPosition.");
        }
        GLES30.glEnableVertexAttribArray(mPositionHandle); // 启用位置属性数组

        // 获取着色器程序中纹理坐标属性的句柄 "aTexCoord", 这个句柄用于将纹理坐标数据传递到着色器中。
        mTexCoordHandle = GLES30.glGetAttribLocation(mProgram, "aTexCoord");
        if (mTexCoordHandle == -1) {
            Log.e("TextureRender", "Could not find attribute aTexCoord.");
        }
        GLES30.glEnableVertexAttribArray(mTexCoordHandle); // 启用纹理坐标属性数组

        // 获取着色器程序中统一变量 "uTMatrix" 的句柄, 该变量用于传递变换矩阵给着色器，以实现顶点的变换。
        mTMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uTMatrix");
        mSamplerHandle = GLES30.glGetUniformLocation(mProgram, "uSampler"); // 获取纹理采样器的统一变量位置

        // 解绑当前的顶点缓冲对象（VBO），确保后续操作不会意外修改它。
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }

    public void draw() {
        // 激活着色器程序 mProgram，以便后面进行图形渲染
        GLES30.glUseProgram(mProgram);
        // 绑定先前生成的 VBO，让 OpenGL 知道将使用这个 VBO
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVboId);

        // 指定位置属性的布局(设置顶点位置数据)
        GLES30.glVertexAttribPointer(mPositionHandle,
                3,                // 每个顶点有 3 个浮点数（x, y, z）
                GLES30.GL_FLOAT,
                false,
                5 * Float.BYTES, // 步长stride为 5*Float.BYTES
                0);                    // 偏移为0（即从数据的起始位置开始）

        // 指定纹理坐标属性的布局（设置纹理坐标数据）
        GLES30.glVertexAttribPointer(mTexCoordHandle,
                2,                // 每个纹理有 2 个浮点数（u, v）
                GLES30.GL_FLOAT,
                false,
                5 * Float.BYTES, // stride 同样为 5*Float.BYTES
                3 * Float.BYTES);      // 偏移为 3*Float.BYTES（前三个是顶点坐标）

        // 将当前的变换矩阵（mTranslateMatrix）上传到着色器中的 uTMatrix 变量
        GLES30.glUniformMatrix4fv(mTMatrixHandle, 1, false, mTranslateMatrix, 0);

        // 激活纹理单元 0，以便后面绑定纹理时使用
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);

        // 绑定之前生成的纹理对象 mTextureId，为后续的渲染提供纹理数据
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureId);

        // 传递纹理单元的索引（这里是0），到着色器中的 uSampler 变量
        GLES30.glUniform1i(mSamplerHandle, 0);

        // 使用绘制命令将数组中的顶点绘制为矩形(从VBO绘制4个顶点，形成一个三角带，以绘制矩形)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        // 解绑当前的 VBO，避免后续操作意外影响到这个缓冲
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        // 检查 OpenGL 错误
        int error = GLES30.glGetError();
        if (error != GLES30.GL_NO_ERROR) {
            Log.e("OpenGL", "OpenGL Error: " + error);
        }
    }

    // 加载并编译着色器
    private void initShaders(Context context) {
        String vertexShaderCode = ShaderController.loadShaderCodeFromFile("texture_vertex_shader.glsl", context);
        String fragmentShaderCode = ShaderController.loadShaderCodeFromFile("texture_fragment_shader.glsl", context);
        mProgram = ShaderController.createGLProgram(vertexShaderCode, fragmentShaderCode);
        if (mProgram == 0) {
            Log.e("TextureRender", "Failed to create OpenGL program.");
        }
    }

    // 初始化坐标数据
    private void initVertexBuffer() {
        // 为顶点坐标分配DMA内存空间
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mCoordData.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mCoordBuffer = byteBuffer.asFloatBuffer();
        mCoordBuffer.put(mCoordData);
        mCoordBuffer.position(0);
    }

    // 初始化 VBO
    private void initVbo() {
        int[] vbos = new int[1];
        GLES30.glGenBuffers(1, vbos, 0);
        mVboId = vbos[0];
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVboId);
        mCoordBuffer.position(0);
        // 指定顶点属性指针，从 VBO 读取数据
        GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,      // 缓冲区目标：顶点缓冲区
                mCoordBuffer.capacity() * 4, // 总字节大小（mVertexBuffer.capacity()个float，每个占4字节）
                mCoordBuffer,                // 数据源：顶点缓冲区
                GLES30.GL_STATIC_DRAW        // 缓冲区类型：静态数据
        );
    }

    // 将图片加载进来
    private Bitmap loadImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.android_logo, options);
    }

    // 上传纹理到GPU
    private int uploadTexture() {
        Bitmap bitmap = loadImage();
        // 创建并绑定纹理
        int[] textureIds = new int[1];
        GLES30.glGenTextures(1, textureIds, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0]);

        // 设置缩小策略
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        // 设置放大策略
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);

        // 纹理上传到GPU
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, bitmap, 0);

        // 回收bitmap
        bitmap.recycle();

        // 解绑纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        return textureIds[0];
    }
}
