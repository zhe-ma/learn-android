package com.example.learnandroid.renderer;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.example.learnandroid.utils.FileUtil;
import com.example.learnandroid.utils.FpsCalculator;
import com.example.learnandroid.utils.GLUtil;
import com.example.learnandroid.utils.MvpMatrix;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/*
绘制纹理到矩形
 */
public class Z08_TextureRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "Z08_TextureRenderer";

    // 纹理贴图：把一个纹理（对于2D贴图，可以简单的理解为图片），按照所期望的方式显示在图形的表面。

    // 与渐变色接近，但有些区别：
    // 渐变色：光栅化过程中，计算出颜色值，然后在片段着色器的时候可以直接赋值
    // 纹理：光栅化过程中，计算出当前片段在纹理上的坐标位置，然后在片段着色器的中，根据这个纹理上的坐标，去纹理中取出相应的颜色值。

    // 纹理映射
    // 如果想把一幅纹理映射到相应的几何图元，就必须告诉GPU如何进行纹理映射，也就是为图元的顶点指定恰当的纹理坐标。
    // 纹理坐标用浮点数来表示，范围一般从0.0到1.0，左上角坐标为（0.0，0.0），右上角坐标为（1.0，0.0），
    // 左下角坐标为（0.0，1.0），右下角坐标为（1.0，1.0）

    private Bitmap bitmap;

    private String vertexShaderCode =
            "precision mediump float;" +  // 声明精度为float类型的中等
                    "attribute vec4 vPosition;" +  // 接收程序传入的顶点
                    "attribute vec2 vCoordinate;" +  // 接收传入的顶点纹理位置
                    "uniform mat4 vMatrix;" +
                    "varying vec2 aCoordinate;" +
                    "void main() {" +
                    "   gl_Position = vMatrix*vPosition;" +
                    "   aCoordinate = vCoordinate;" +
                    "}";

    private String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform sampler2D vTexture;" +  // 纹理采样器
                    "varying vec2 aCoordinate;" +  // 纹理坐标
                    "void main() {" +
                    "   gl_FragColor = texture2D(vTexture, aCoordinate);" +  // 进行纹理采样
                    "}";

    // 数组中3个值作为一个坐标点
    private final int VERTEX_COMPONENT_COUNT = 3;

    // 矩形
    private float vertexData[] = {
            -0.5f, 0.5f, 0,   // 点A
            0.5f, 0.5f, 0,    // 点B
            -0.5f, -0.5f, 0,  // 点C
            0.5f, 0.5f, 0,    // 点B
            -0.5f, -0.5f, 0,  // 点C
            0.5f, -0.5f, 0,   // 点D
    };

    private float textureCoord[] = {
            0.0f, 0.0f,  // 点A
            1.0f, 0.0f,  // 点B
            0.0f, 1.0f,  // 点C
            1.0f, 0.0f,  // 点B
            0.0f, 1.0f,  // 点C
            1.0f, 1.0f,  // 点D
    };

    // 顶点坐标数据要转化成FloatBuffer格式
    private FloatBuffer vertexBuffer;

    private FloatBuffer textureCoordBuffer;

    private int textureId;

    private int program;

    private FpsCalculator fpsCalculator = new FpsCalculator();

    private MvpMatrix mvpMatrix = new MvpMatrix();

    public Z08_TextureRenderer(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 启用深度测试，否则绘制出的颜色混乱
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        program = GLUtil.createProgram(vertexShaderCode, fragmentShaderCode);

        // 应用GL程序到Opengl环境
        GLES20.glUseProgram(program);

        // Java的缓冲区数据存储结构为大端字节序(BigEdian)，而OpenGl的数据为小端字节序（LittleEdian）,
        vertexBuffer = GLUtil.floatArray2FloatBuffer(vertexData);
        textureCoordBuffer = GLUtil.floatArray2FloatBuffer(textureCoord);

        textureId = GLUtil.loadTexture(bitmap);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        mvpMatrix.setViewMatrix(0, 0, 7, 0, 0, 0, 0, 1, 0);

        if (width > height) {
            // 横屏
            float ratio = (float) width / (float) height;
            mvpMatrix.setOrthoMatrix(-ratio, ratio, -1, 1, 1, 10);
        } else {
            // 竖屏
            float ratio = (float)height / (float)width;
            // left，right, bottom, top 可以理解为当前屏幕归一化的坐标系范围。
            // 例如如下赋值湿的横坐标的范围是(-1, 1)，纵坐标的范围是(-ratio, ratio)。
            // 赋值前横纵坐标的范围都是(-1, 1)，但是竖屏高度大于宽度，所以纵轴均分到(-1， 1)的单位长度更长，
            // 使得画出三角形的纵轴更长。将纵轴的范围改为(-ratio, ratio)，将纵轴划分更多的份数，那么就可以
            // 将纵轴的范围长度和横轴的一样，这样画出来的横纵则一样。
            mvpMatrix.setOrthoMatrix(-1, 1, -ratio, ratio, 1, 10);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        fpsCalculator.fps();

        // 重绘背景
        GLES20.glClearColor(0.3f, 0.2f, 0.1f, 1.0f);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);

        draw();
    }

    private void draw() {
        // 获取顶点着色器中的变换矩阵
        int matrixHandle = GLES20.glGetUniformLocation(program, "vMatrix");
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, mvpMatrix.getMvpMatrix(), 0);

        // 获取顶点着色器中字段vPosition的句柄
        int positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        // 启用该句柄的属性
        GLES20.glEnableVertexAttribArray(positionHandle);
        // 设置vPosition的坐标数据
        GLES20.glVertexAttribPointer(positionHandle, VERTEX_COMPONENT_COUNT, GLES20.GL_FLOAT,
                false, Float.BYTES * VERTEX_COMPONENT_COUNT, vertexBuffer);

        // 设置当前活动的纹理单元为纹理单元0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // 将纹理ID绑定到当前活动的纹理单元上
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        int textureHandle = GLES20.glGetAttribLocation(program, "vCoordinate");
        GLES20.glEnableVertexAttribArray(textureHandle);
        GLES20.glVertexAttribPointer(textureHandle, 2, GLES20.GL_FLOAT, false, Float.BYTES * 2, textureCoordBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexData.length / VERTEX_COMPONENT_COUNT);

        // 关闭顶点数组句柄
        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}
