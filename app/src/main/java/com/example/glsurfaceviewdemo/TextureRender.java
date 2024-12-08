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
    private Context mContext;
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

    private int mPositionHandle = -1; // 顶点位置属性的操作句柄
    private int mTexCoordHandle = -1; // 纹理坐标属性的操作句柄
    private int mMVPMatrixHandle = -1;  // 变换矩阵操作句柄，用于实现顶点的变换
    private int mSamplerHandle = -1;  // 纹理采样器操作句柄，相当于一个指向某个纹理单元的指针
    Bitmap mBitmap;
    private float[] mMVPMatrix; // mvp矩阵

    public TextureRender(Context context, Bitmap bitmap) {
        mContext = context;
        mBitmap = bitmap;
        initialize();
    }

    private void initialize() {
        mTextureId = uploadTexture(); // 上传纹理到GPU
        initVertexBuffer();           // 初始化坐标数据
        initShaders(mContext);        // 加载并编译着色器
        initVbo();                    // 初始化 VBO
        initHandles();                // 获取GPU和Shader的一些操作接口
    }

    // 获取GPU和Shader的一些操作接口
    private void initHandles() {
        // 获取顶点坐标操作接口的句柄
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "aPosition");
        validateAttributeLocation(mPositionHandle, "aPosition");
        GLES30.glEnableVertexAttribArray(mPositionHandle); // 启用位置属性数组
        // 获取纹理坐标操作接口的句柄
        mTexCoordHandle = GLES30.glGetAttribLocation(mProgram, "aTexCoord");
        validateAttributeLocation(mTexCoordHandle, "aTexCoord");
        GLES30.glEnableVertexAttribArray(mTexCoordHandle); // 启用纹理坐标属性数组
        // 获取变换矩阵操作接口的句柄
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");
        // 用于获取Shader当中纹理采样器的操作接口的句柄
        mSamplerHandle = GLES30.glGetUniformLocation(mProgram, "uSampler");
    }
    // 用于验证属性句柄的有效性
    private void validateAttributeLocation(int handle, String attributeName) {
        if (handle == -1) {
            Log.e("TextureRender", "Could not find attribute " + attributeName);
        }
    }
    // 绘制纹理
    public void draw() {
        // 激活着色器程序
        GLES30.glUseProgram(mProgram);
        // 绑定VBO
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVboId);
        // 设置顶点坐标和纹理坐标给OpenGL
        setupVertexAttribPointer();
        // 上传变换矩阵
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        // 绑定纹理并设置采样器
        bindTexture();
        // 绘制矩形
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        // 解绑当前的 VBO，避免后续操作意外影响到这个缓冲
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        // 检查 OpenGL 错误
        checkOpenGLError();
    }

    // 接收mvp矩阵
    public void setCustomMVPMatrix(float[] mvpMatrix) {
        if (mvpMatrix.length == 16) { // 确保传入的数组长度为 16
            mMVPMatrix = new float[16];
            System.arraycopy(mvpMatrix, 0, mMVPMatrix, 0, 16);
        } else {
            Log.e("TextureRender", "mvp Matrix length invalid!");
        }
    }

    // 设置顶点坐标和纹理坐标
    private void setupVertexAttribPointer() {
        // 指定位置属性的布局(设置顶点位置数据)
        // 3表示：每个顶点有3个浮点数（x, y, z），步长stride为 5*Float.BYTES，偏移量是0
        GLES30.glVertexAttribPointer(mPositionHandle, 3, GLES30.GL_FLOAT, false, 5 * Float.BYTES, 0);
        // 指定纹理坐标属性的布局（设置纹理坐标数据）
        // 2表示：每个纹理有 2 个浮点数（u, v），步长是同样是 5*Float.BYTES，偏移量是3*Float.BYTES（前三个是顶点坐标）
        GLES30.glVertexAttribPointer(mTexCoordHandle, 2, GLES30.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
    }
    // 绑定纹理
    private void bindTexture() {
        // 激活纹理单元 0
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        // 绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureId);
        // 上传纹理单元索引到Shader中的 uSampler 变量
        GLES30.glUniform1i(mSamplerHandle, 0);
    }
    // 检查OpengGL的错误
    private void checkOpenGLError() {
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
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mCoordBuffer.capacity() * 4, mCoordBuffer, GLES30.GL_STATIC_DRAW);
    }

    // 上传纹理到GPU
    private int uploadTexture() {
        int[] textureIds = new int[1];
        GLES30.glGenTextures(1, textureIds, 0); // 创建纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0]); // 绑定纹理
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR); // 设置缩小策略
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR); // 设置放大策略
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, mBitmap, 0); // 纹理上传到GPU
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0); // 解绑纹理，避免后续误操作
        return textureIds[0];
    }

    public void release() {
        GLES30.glDeleteBuffers(1, new int[]{mVboId}, 0); // 删除 VBO
        GLES30.glDeleteProgram(mProgram); // 删除 shader program
    }
}

