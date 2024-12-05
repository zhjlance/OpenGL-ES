package com.example.glsurfaceviewdemo;

import android.content.Context;
import android.opengl.GLES30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL;

public class Triangle {
    // 顶点数据是float类型，因此，使用这个存储
    private FloatBuffer mVertexBuffer;
    // VBO存储顶点数据
    private int mVboId;

    private int mProgram;
    // 定义的三角形顶点坐标数组
    private final float[] mTriangleCoords = new float[]{
            0.0f, 0.2f, 0.0f,   // 顶部
            -0.5f, -0.5f, 0.0f, // 左下角
            0.5f, -0.5f, 0.0f   // 右下角
    };

    public Triangle(Context context) {
        // 为顶点坐标分配DMA内存空间
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mTriangleCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder()); // 设置字节顺序为本地字节顺序(会根据硬件架构自适应大小端)
        mVertexBuffer = byteBuffer.asFloatBuffer(); // 将字节缓冲区转换为浮点缓冲区
        mVertexBuffer.put(mTriangleCoords); // 将顶点三角形坐标放入缓冲区
        mVertexBuffer.position(0);

        // 2.加载并编译vertexShader和fragmentShader
        String vertexShaderCode = ShaderController.loadShaderCodeFromFile("triangle_vertex.glsl", context);
        String fragmentShaderCode = ShaderController.loadShaderCodeFromFile("triangle_fragment.glsl", context);

        // 3.创建一个OpenGL程序，并链接程序
        mProgram = ShaderController.createGLProgram(vertexShaderCode, fragmentShaderCode);

        // 生成并绑定 VBO （一定要在createGLProgram之后）
        int[] vbos = new int[1];
        GLES30.glGenBuffers(1, vbos, 0);
        mVboId = vbos[0];
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVboId);

        // 传递顶点数据到 VBO
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, byteBuffer.capacity(), byteBuffer, GLES30.GL_STATIC_DRAW);
    }

    // 定义的fragment的颜色数组，表示每个像素的颜色
    private final float[] mColor = new float[]{0.0f, 1.0f, 0.0f, 1.0f};
    // 顶点着色器的位置句柄
    private int mPositionHandle = 0;
    // 片元着色器的位置句柄
    private int mColorHandle = 0;
    private final int COORDS_PER_VERTEX = 3;

    public void draw() {
        // 使用program
        GLES30.glUseProgram(mProgram);
        // 确保绑定 VBO （保险措施）
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVboId);

        // 设置顶点属性指针
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition");
        GLES30.glEnableVertexAttribArray(mPositionHandle);
        GLES30.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES30.GL_FLOAT, false, 0, 0);

        // 获取片元着色器的颜色句柄
        mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor");
        // 设置绘制三角形的颜色
        GLES30.glUniform4fv(mColorHandle, 1, mColor, 0);

        // 绘制三角形
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, mTriangleCoords.length / COORDS_PER_VERTEX);

        // 禁用顶点属性数组
        GLES30.glDisableVertexAttribArray(mPositionHandle);
        // 解绑 VBO
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }
}
