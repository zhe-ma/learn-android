package com.example.learnandroid.utils;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLUtil {
    private static final String TAG = "GLUtil";

    public static FloatBuffer floatArray2FloatBuffer(final float[] floatData) {
        // Java的缓冲区数据存储结构为大端字节序(BigEdian)，而OpenGl的数据为小端字节序（LittleEdian）,
        // 使用OpenGl的时候必须要进行下转换
        FloatBuffer floatBuffer = ByteBuffer.allocateDirect(floatData.length * Float.BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        floatBuffer.put(floatData);
        floatBuffer.position(0);
        return floatBuffer;
    }

    public static int createProgram(String vertexShaderSource, String fragmentShaderSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource);
        int program = GLES20.glCreateProgram();
        // 添加顶点着色器到GL程序中
        GLES20.glAttachShader(program, vertexShader);
        // 添加片段着色器到GL程序中
        GLES20.glAttachShader(program, fragmentShader);
        // 链接程序
        GLES20.glLinkProgram(program);
        return program;
    }

    public static int loadShader(int shaderType, String shaderSource) {
        int shader = GLES20.glCreateShader(shaderType);
        GLES20.glShaderSource(shader, shaderSource);
        GLES20.glCompileShader(shader);

        int[] compiled = new int[1];
        // 检测着色器编译是否成功
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Failed to compile shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
        }

        return shader;
    }
}
