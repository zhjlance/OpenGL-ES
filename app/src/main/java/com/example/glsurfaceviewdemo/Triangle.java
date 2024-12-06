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
    private int mVaoId; // 添加 VAO ID
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
        // 使用 VAO，对 VBO 和 EBO 的绑定进行封装（简化 draw）
        initVao();
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
    // 初始化 VAO，封装 VBO 和 EBO 的绑定
    private void initVao() {
        // 生成 VAO
        int[] vaos = new int[1];
        GLES30.glGenVertexArrays(1, vaos, 0);
        mVaoId = vaos[0];
        GLES30.glBindVertexArray(mVaoId); // 绑定 VAO

        // 初始化 VBO
        initVbo();

        // 初始化 EBO
        initEbo();

        // 配置顶点属性
        int positionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition");
        GLES30.glEnableVertexAttribArray(positionHandle); // 启用顶点属性
        GLES30.glVertexAttribPointer(
                positionHandle,
                COORDS_PER_VERTEX, // 每个顶点的分量个数
                GLES30.GL_FLOAT,   // 数据类型
                false,
                0,
                0  // 偏移量（VBO 内部偏移位置）
        );

        // 解绑 VAO（可选，防止后续操作误改 VAO 状态）
        GLES30.glBindVertexArray(0);
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
        // 设置片元着色器的颜色
        float[] color = new float[]{0.0f, 1.0f, 0.0f, 1.0f};

        // 使用 Shader Program
        GLES30.glUseProgram(mProgram);

        // 绑定 VAO（自动恢复顶点属性、VBO 和 EBO 的绑定状态）
        GLES30.glBindVertexArray(mVaoId);

        // 设置片段着色器的颜色值
        int colorHandle = GLES30.glGetUniformLocation(mProgram, "vColor");
        GLES30.glUniform4fv(colorHandle, 1, color, 0);

        // 绘制三角形
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, mIndices.length, GLES30.GL_UNSIGNED_INT, 0);

        // 解绑 VAO（可选，防止影响其他绘图操作）
        GLES30.glBindVertexArray(0);
    }

    // 释放资源
    public void release() {
        GLES30.glDeleteBuffers(1, new int[]{mVboId}, 0); // 删除 VBO
        GLES30.glDeleteBuffers(1, new int[]{mEboId}, 0); // 删除 EBO
        GLES30.glDeleteVertexArrays(1, new int[]{mVaoId}, 0); // 删除 VAO
        GLES30.glDeleteProgram(mProgram); // 删除 shader program
    }
}
