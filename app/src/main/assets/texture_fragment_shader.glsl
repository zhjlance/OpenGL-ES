#version 300 es  // 指定 GLSL 版本
precision mediump float;  // 定义浮点数精度

// 统一变量
uniform sampler2D uSampler;  // 纹理采样器

// 输入变量
in vec2 vTexCoord;  // 从顶点着色器传递的纹理坐标

// 输出变量
out vec4 fragColor;  // 片段着色器的输出颜色

void main() {
    // 通过纹理采样器获取最终颜色
    fragColor = texture(uSampler, vTexCoord);
}