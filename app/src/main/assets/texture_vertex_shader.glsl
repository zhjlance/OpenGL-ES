#version 300 es  // 指定 GLSL 版本
precision mediump float;  // 定义浮点数精度

// 统一变量
uniform mat4 uMVPMatrix;  // 变换矩阵

// 属性变量
in vec4 aPosition;  // 顶点位置
in vec2 aTexCoord;  // 纹理坐标

// 输出变量
out vec2 vTexCoord;  // 传递给片段着色器的纹理坐标

void main() {
    // 应用变换矩阵并设置顶点位置
    gl_Position = uMVPMatrix * aPosition;

    // 将纹理坐标传递给片段着色器
    vTexCoord = aTexCoord;
}