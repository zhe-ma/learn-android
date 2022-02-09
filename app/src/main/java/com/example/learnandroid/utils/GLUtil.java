package com.example.learnandroid.utils;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

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

    public static ShortBuffer shortArray2ShortBuffer(final short[] shortData) {
        ShortBuffer shortBuffer = ByteBuffer.allocateDirect(shortData.length * Short.BYTES)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        shortBuffer.put(shortData);
        shortBuffer.position(0);
        return shortBuffer;
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

    public static int loadTexture(Bitmap bitmap) {
        int textures[] = new int[1];

        // 创建纹理对象，1代表创建一个纹理对象
        GLES20.glGenTextures(1, textures, 0);

        if (textures[0] == 0) {
            Log.e(TAG, "Failed to create texture shader");
            return 0;
        }

        // 绑定纹理id到环境的纹理单元
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        // 设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
        // 设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
        // 设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
        // 设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);

        // 加载纹理到显存。 GLES20.GL_TEXTURE_2D是纹理类型，0是纹理的层次，0代表基本图像。最后一个参数是纹理边框尺寸
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        // 解绑纹理id
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        return textures[0];
    }


    public static Bitmap readPixels2Bitmap(int x, int y, int width, int height) {
        ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        // GPU渲染完数据在显存，回传内存的唯一方式glReadPixels函数。
        // 显存也被叫做显示内存、帧缓存，它是用来存储显示芯片处理过或者即将读取的渲染数据。
        // glReadPixels：读取帧缓冲区的像素。
        // x， y是描画的图像的左下角的坐标，具体说来，就是相对显示窗的左下角为（0,0）原点的坐标点
        GLES20.glReadPixels(x, y, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
        buf.rewind();

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(buf);

        return bmp;
    }
}
