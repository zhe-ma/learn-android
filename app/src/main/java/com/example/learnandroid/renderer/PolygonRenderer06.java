package com.example.learnandroid.renderer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.learnandroid.utils.GLUtil;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/*
使用索引法绘制多边形
 */
public class PolygonRenderer06 implements GLSurfaceView.Renderer {
    private static final String TAG = "PolygonRenderer06";

    private String vertexShaderCode =
            "precision mediump float;" +  // 声明精度为float类型的中等
                    "attribute vec4 vPosition;" +  // 接收程序传入的顶点
                    "uniform mat4 vMatrix;" +
                    "attribute vec4 aColor;" +  // 颜色可以直接传给fragmentShader，这里为了学习varying的使用
                    "varying vec4 vColor;" +  // 传给fragmentShader的变量
                    "void main() {" +
                    "   gl_Position = vMatrix*vPosition;" +
                    "   vColor = aColor;" +
                    "}";

    private String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +  // 接收顶点着色器传来的变量
                    "void main() {" +
                    "   gl_FragColor = vColor;" +
                    "}";

    // 数组中3个值作为一个坐标点
    private final int VERTEX_COMPONENT_COUNT = 3;

    // 多边形的坐标数组
    private float vertexData[] = {
            -0.5f, 0.5f, 0.0f,  // 0号顶点
            0.5f, 0.5f, 0.0f,  // 1号顶点
            -0.5f, -0.5f, 0.0f,  // 2号顶点
            0.5f, -0.5f, 0.0f,  // 3号顶点
    };

    // 绘制三角形的顶点索引
    private short indexes[] = {
            0, 1, 3,  // 第一个三角形
            0, 1, 2,  // 第二个三角形
    };

    private float[] colorData = {
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
    };

    // 顶点坐标数据要转化成FloatBuffer格式
    private FloatBuffer vertexBuffer;

    // 颜色数据要转为FloatBuffer格式
    private FloatBuffer colorBuffer;

    private ShortBuffer indexBuffer;

    private int program;

    // 相机位置矩阵
    private final float[] viewMatrix = new float[16];
    // 投影矩阵
    private final float[] projectionMatrix = new float[16];
    // 变换矩阵，投影矩阵*相机位置矩阵的结果，最终传给着色器
    private final float[] mvpMatrix = new float[16];

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        program = GLUtil.createProgram(vertexShaderCode, fragmentShaderCode);

        // 应用GL程序到Opengl环境
        GLES20.glUseProgram(program);

        // Java的缓冲区数据存储结构为大端字节序(BigEdian)，而OpenGl的数据为小端字节序（LittleEdian）,
        vertexBuffer = GLUtil.floatArray2FloatBuffer(vertexData);
        colorBuffer = GLUtil.floatArray2FloatBuffer(colorData);
        indexBuffer = GLUtil.shortArray2ShortBuffer(indexes);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 7f, 0, 0, 0, 0, 1, 0);
        if (width > height) {
            // 横屏
            float ratio = (float) width / (float) height;
            Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1, 10);
        } else {
            // 竖屏
            float ratio = (float)height / (float)width;
            // left，right, bottom, top 可以理解为当前屏幕归一化的坐标系范围。
            // 例如如下赋值湿的横坐标的范围是(-1, 1)，纵坐标的范围是(-ratio, ratio)。
            // 赋值前横纵坐标的范围都是(-1, 1)，但是竖屏高度大于宽度，所以纵轴均分到(-1， 1)的单位长度更长，
            // 使得画出三角形的纵轴更长。将纵轴的范围改为(-ratio, ratio)，将纵轴划分更多的份数，那么就可以
            // 将纵轴的范围长度和横轴的一样，这样画出来的横纵则一样。
            Matrix.orthoM(projectionMatrix, 0, -1, 1, -ratio, ratio, 1, 10);
        }

        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
    }

    // 顶点法和索引法
    // 顶点法: GLES20.glDrawArrays,根据传入的定点顺序进行绘制的。
    // 索引法: GLES20.glDrawElements，是根据索引序列，在顶点序列中找到对应的顶点，并根据绘制的方式，组成相应的图元进行绘制。可以相对顶点法减少很多重复顶点占用的空间。

    // 绘制方式：
    // GL_POINTS：将传入的顶点坐标作为单独的点绘制
    // GL_LINES： 将传入的坐标作为单独线条绘制，ABCDEFG六个顶点，绘制AB、CD、EF三条线
    // GL_LINE_STRIP：将传入的顶点作为折线绘制，ABCD四个顶点，绘制AB、BC、CD三条线
    // GL_LINE_LOOP：将传入的顶点作为闭合折线绘制，ABCD四个顶点，绘制AB、BC、CD、DA四条线。
    // GL_TRIANGLES：将传入的顶点作为单独的三角形绘制，ABCDEF绘制ABC,DEF两个三角形
    // GL_TRIANGLE_FAN：将传入的顶点作为扇面绘制，ABCDEF绘制ABC、ACD、ADE、AEF四个三角形
    // GL_TRIANGLE_STRIP：将传入的顶点作为三角条带绘制，ABCDEF绘制ABC,BCD,CDE,DEF四个三角形

    @Override
    public void onDrawFrame(GL10 gl) {
        // 重绘背景
        GLES20.glClearColor(0.3f, 0.2f, 0.1f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 获取顶点着色器中的变换矩阵
        int matrixHandle = GLES20.glGetUniformLocation(program, "vMatrix");
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, mvpMatrix, 0);

        // 获取顶点着色器中字段vPosition的句柄
        int positionHandle = GLES20.glGetAttribLocation(program, "vPosition");

        // 启用该句柄的属性
        GLES20.glEnableVertexAttribArray(positionHandle);

        // 设置vPosition的坐标数据
        GLES20.glVertexAttribPointer(positionHandle, VERTEX_COMPONENT_COUNT, GLES20.GL_FLOAT,
                false, Float.BYTES * VERTEX_COMPONENT_COUNT, vertexBuffer);

        int colorHandle = GLES20.glGetAttribLocation(program, "aColor");
        GLES20.glEnableVertexAttribArray(colorHandle);
        // Color信息是4个float值为一组值
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);

        // 索引法绘制
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexes.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        // 关闭顶点数组句柄
        GLES20.glDisableVertexAttribArray(positionHandle);

        // 关闭颜色数组句柄
        GLES20.glDisableVertexAttribArray(colorHandle);
    }
}
