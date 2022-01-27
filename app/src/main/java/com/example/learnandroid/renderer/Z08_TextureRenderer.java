package com.example.learnandroid.renderer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

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

    private String vertexShaderCode =
            "precision mediump float;" +  // 声明精度为float类型的中等
                    "attribute vec4 vPosition;" +  // 接收程序传入的顶点
                    "uniform mat4 vMatrix;" +
                    "void main() {" +
                    "   gl_Position = vMatrix*vPosition;" +
                    "}";

    private String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +  // 接收程序传入的颜色
                    "void main() {" +
                    "   gl_FragColor = vColor;" +
                    "}";

    // 三角形的颜色数组，rgba
    private float[] triangleColor = {0.3f, 0.1f, 0.3f, 1f};

    // 数组中3个值作为一个坐标点
    private final int VERTEX_COMPONENT_COUNT = 3;

    // 矩形
    private float vertexData[] = {
            -0.5f, 0.5f, 0,
            0.5f, 0.5f, 0,
            -0.5f, -0.5f, 0,
            0.5f, 0.5f, 0,
            -0.5f, -0.5f, 0,
            0.5f, -0.5f, 0,
    };

    // 顶点坐标数据要转化成FloatBuffer格式
    private FloatBuffer vertexBuffer;

    private int program;

    private long frame_count = 0;

    private FpsCalculator fpsCalculator = new FpsCalculator();

    private MvpMatrix mvpMatrix = new MvpMatrix();

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 执行GLES20.drawArrays()方法后，GPU开始绘制图形，默认的规则是后生成的像素覆盖前面生成的像素。
        // 如果没有GLES20.glEnable(GLES20.GL_DEPTH_TEST)语句，本来处于底面不可见的黑色面会把绿色面覆盖，
        // 看到代码中黑色面的顶点数据是面5在绿色面2的后面， 黑色面和绿色面在x，y坐标上是有重叠的，黑色与绿色重叠部分会被后生成的像素覆盖。
        // z值就是深度信息，GLES20.glEnable(GLES20.GL_DEPTH_TEST)，就可以识别出黑色面和绿色面的前后位置关系，所有位置靠前的会显示在屏幕上，靠后的像素会被隐覆盖掉。

        // 启用深度测试，否则绘制出的颜色混乱
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        program = GLUtil.createProgram(vertexShaderCode, fragmentShaderCode);

        // 应用GL程序到Opengl环境
        GLES20.glUseProgram(program);

        // Java的缓冲区数据存储结构为大端字节序(BigEdian)，而OpenGl的数据为小端字节序（LittleEdian）,
        vertexBuffer = GLUtil.floatArray2FloatBuffer(vertexData);
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
        frame_count++;

        // 重绘背景
        GLES20.glClearColor(0.3f, 0.2f, 0.1f, 1.0f);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);

        drawCube();
    }

    private void drawCube() {
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

        // 设置三角形颜色
        int colorHandle = GLES20.glGetUniformLocation(program, "vColor");
        GLES20.glUniform4fv(colorHandle, 1, triangleColor, 0);


        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexData.length / VERTEX_COMPONENT_COUNT);

        // 关闭顶点数组句柄
        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}
