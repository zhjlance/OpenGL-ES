package com.example.glsurfaceviewdemo;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShaderController {
    /**
     * 从 assets 文件夹中读取指定文件的内容并返回为字符串
     *
     * @param filename 文件名
     * @param context  上下文对象
     * @return 读取的文件内容字符串
     */
    public static String loadShaderCodeFromFile(String filename, Context context) {
        // 用于存储读取的着色器代码的字符串
        StringBuilder shaderCode = new StringBuilder();
        try {
            InputStream inputStream = context.getAssets().open(filename);
            // 使用 BufferedReader 包装输入流，以便逐行读取文件内容
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            // 逐行读取文件内容并将每行内容追加到 shaderCode 中
            while ((line = bufferedReader.readLine()) != null) {
                shaderCode.append(line).append("\n");
            }
            // 关闭 BufferedReader
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 返回读取的文件内容字符串
        return shaderCode.toString();
    }

    // 创建并编译着色器
    public static int compileShader(int type, String shaderCode) {
        // 创建一个着色器
        int shader = GLES30.glCreateShader(type);
        // 将着色器代码设置到着色器对象中
        GLES30.glShaderSource(shader, shaderCode);
        // 编译着色器
        GLES30.glCompileShader(shader);
        return shader;
    }

    /**
     * 创建 OpenGL Program 对象，用于链接顶点着色器和片段着色器
     *
     * @param vertexShader   顶点着色器源代码
     * @param fragmentShader 片段着色器源代码
     * @return 创建的 OpenGL Program 对象 ID
     */
    public static int createGLProgram(String vertexShader, String fragmentShader) {
        // 编译生成顶点着色器
        int vShader = compileShader(GLES30.GL_VERTEX_SHADER, vertexShader);
        if (vShader == 0) {
            Log.e("GLProgram", "Failed to compile vertex shader.");
            return 0; // 返回0表示创建失败
        }
        // 编译生成片元着色器
        int fShader = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentShader);
        if (fShader == 0) {
            Log.e("GLProgram", "Failed to compile fragment shader.");
            GLES30.glDeleteShader(vShader); // 删除已经生成的顶点着色器
            return 0;
        }

        // 创建一个OpenGL程序
        int program = GLES30.glCreateProgram();
        if (program == 0) {
            Log.e("GLProgram", "Failed to create OpenGL program.");
            GLES30.glDeleteShader(vShader);
            GLES30.glDeleteShader(fShader);
            return 0;
        }

        // attach两个编译好的着色器到program当中
        GLES30.glAttachShader(program, vShader);
        GLES30.glAttachShader(program, fShader);

        // 链接OpenGL程序
        GLES30.glLinkProgram(program);

        // 检查链接结果是否成功
        int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            Log.e("GLProgram", "Failed to link program: " + GLES30.glGetProgramInfoLog(program));
            GLES30.glDeleteProgram(program);
            GLES30.glDeleteShader(vShader);
            GLES30.glDeleteShader(fShader);
            return 0;
        }

        // 删除着色器，因为已经链接到程序中，不再需要保留
        GLES30.glDeleteShader(vShader);
        GLES30.glDeleteShader(fShader);

        Log.i("GLProgram", "GL program created successfully.");
        return program;
    }
}
