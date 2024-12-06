package com.example.glsurfaceviewdemo;

import android.content.Context;
import android.opengl.GLES30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL;

public class Triangle {
    private final int COORDS_PER_VERTEX = 3;
    private FloatBuffer mVertexBuffer;
    private int[] mIndices = new int[]{0, 1, 2}; // EBO索引数据
    private int mVboId;
    private int mEboId;
    private int mProgram;
    // 定义的三角形顶点坐标数组
    private final float[] mTriangleCoords = new float[]{
        0.0f, 0.2f, 0.0f,   // 顶部
        -0.5f, -0.5f, 0.0f, // 左下角
        0.5f, -0.5f, 0.0f   // 右下角
    };

    public Triangle(Context context) {
        // 初始化顶点数据
        initVertexBuffer();
        // 加载并编译着色器
        initShaders(context);
        // 下面对VBO和EBO的操作，一定要在createGLProgram之后
        // 生成并绑定 VBO
        initVbo();
        // 生成并绑定 EBO
        initEbo();
    }

    // 初始化顶点数据
    private void initVertexBuffer() {
        // 为顶点坐标分配DMA内存空间
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mTriangleCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuffer.asFloatBuffer();
        mVertexBuffer.put(mTriangleCoords);
        mVertexBuffer.position(0);
    }

    // 加载并编译着色器
    private void initShaders(Context context) {
        String vertexShaderCode = ShaderController.loadShaderCodeFromFile("triangle_vertex.glsl", context);
        String fragmentShaderCode = ShaderController.loadShaderCodeFromFile("triangle_fragment.glsl", context);
        mProgram = ShaderController.createGLProgram(vertexShaderCode, fragmentShaderCode);
    }
    private void initVbo() {
        int[] vbos = new int[1];
        GLES30.glGenBuffers(1, vbos, 0);
        mVboId = vbos[0];
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVboId);
        mVertexBuffer.position(0);
        // 指定顶点属性指针，从 VBO 读取数据
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,       // 缓冲区目标：顶点缓冲区
            mVertexBuffer.capacity() * 4, // 总字节大小（mVertexBuffer.capacity()个float，每个占4字节）
            mVertexBuffer,                // 数据源：顶点缓冲区
            GLES30.GL_STATIC_DRAW         // 缓冲区类型：静态数据
        );

    }
    // 生成并绑定 EBO
    private void initEbo() {
        // 生成并绑定 EBO （一定要在createGLProgram之后）
        int[] ebos = new int[1];
        GLES30.glGenBuffers(1, ebos, 0);
        mEboId = ebos[0];
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mEboId);

        // 传递索引数据到 EBO
        ByteBuffer indexBuffer = ByteBuffer.allocateDirect(mIndices.length * 4); // 每个索引是int，4字节
        indexBuffer.order(ByteOrder.nativeOrder());
        IntBuffer intIndexBuffer = indexBuffer.asIntBuffer();
        intIndexBuffer.put(mIndices);
        intIndexBuffer.position(0);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * 1, indexBuffer, GLES30.GL_STATIC_DRAW);
    }

    public void draw() {
        // 定义的fragment的颜色数组，表示每个像素的颜色
        float[] color = new float[]{0.0f, 1.0f, 0.0f, 1.0f};
        // 使用program对象
        GLES30.glUseProgram(mProgram);
        // 绑定 EBO
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mEboId);

        // 获取顶点属性的位置
        int positionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition");
        GLES30.glEnableVertexAttribArray(positionHandle);

        // 绑定 VBO（存储顶点坐标数据）
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVboId);

        // 设置顶点属性指针（告诉OpenGL如何解释顶点数据缓冲区中的数据）
        GLES30.glVertexAttribPointer(
                positionHandle,    // 顶点属性位置句柄，指示OpenGL应该将这些数据连接到哪个着色器属性
                COORDS_PER_VERTEX, // 每个顶点包含的坐标数
                GLES30.GL_FLOAT,   // 数据类型
                false,             // 是否数据应该被标准化，通常用于整数类型的数据
                0,                 // 步长，指定在连续的顶点属性之间的偏移量，如果所有属性是紧密排列在一起的，可以设置为0
                0);                // 0 是为绑定的 缓冲区对象（VBO） 指定偏移，否则，顶点缓冲区（mVertexBuffer）的直接内存地址

        // 设置片元着色器属性指针
        int colorHandle = GLES30.glGetUniformLocation(mProgram, "vColor");
        GLES30.glUniform4fv(colorHandle, 1, color, 0);

        // 绘制三角形
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, mIndices.length, GLES30.GL_UNSIGNED_INT, 0);

        // 禁用顶点属性数组
        GLES30.glDisableVertexAttribArray(positionHandle);
        // 解绑 EBO（索引缓冲区）
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
        // 解绑 VBO（顶点缓冲区）
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }

    // 释放资源
    public void release() {
        GLES30.glDeleteBuffers(1, new int[]{mVboId}, 0); // 释放VBO
        GLES30.glDeleteBuffers(1, new int[]{mEboId}, 0); // 释放EBO
        GLES30.glDeleteProgram(mProgram);
    }
}
