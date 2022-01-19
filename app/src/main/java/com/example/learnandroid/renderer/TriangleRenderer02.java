package com.example.learnandroid.renderer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

// 简单的三角形
public class TriangleRenderer02 implements GLSurfaceView.Renderer {
    private static final String TAG = "TriangleRenderer02";

    // attribute，uniform, varying的区别：
    // 1. attribute: attribute变量是只能在vertex shader中使用的变量。
    // （它不能在fragment shader中声明attribute变量，也不能被fragment shader中使用）
    //  一般用attribute变量来表示一些顶点的数据，如：顶点坐标，法线，纹理坐标，顶点颜色等。
    //  在application中，一般用函数glBindAttribLocation（）来绑定每个attribute变量的位置，
    //  然后用函数glVertexAttribPointer（）为每个attribute变量赋值。
    // 2.uniform: uniform变量是外部程序传递给（vertex和fragment）shader的变量。
    //  因此它是application通过函数glUniform**（）函数赋值的。在（vertex和fragment）shader程序内部，
    //  uniform变量就像是C语言里面的常量（const ），它不能被shader程序修改。（shader只能用，不能改）
    //  如果uniform变量在vertex和fragment两者之间声明方式完全一样，则它可以在vertex和fragment共享使用。
    // （相当于一个被vertex和fragment shader共享的全局变量）
    //  uniform变量一般用来表示：变换矩阵，材质，光照参数和颜色等信息。
    // 3. varying: varying变量是vertex和fragment shader之间做数据传递用的。
    // 一般vertex shader修改varying变量的值，然后fragment shader使用该varying变量的值。
    // 因此varying变量在vertex和fragment shader二者之间的声明必须是一致的。application不能使用此变量。

    private String vertexShaderCode =
            "precision mediump float;" +  // 声明精度为float类型的中等
                    "attribute vec4 vPosition;" +  // 接收程序传入的顶点
                    "void main() {" +
                    "   gl_Position = vPosition;" +
                    "}";

    private String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +  // 接收程序传入的颜色
                    "void main() {" +
                    "   gl_FragColor = vColor;" +
                    "}";

    // 数组中3个值作为一个坐标点
    private final int VERTEX_COMPONENT_COUNT = 3;

    // 三角形的坐标数组
    private float vertexData[] = {
            0.0f, 0.5f, 0.0f, // top
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f  // bottom right
    };

    // 三角形顶点个数，这里是三个顶点
    private final int VERTEX_COUNT = vertexData.length / VERTEX_COMPONENT_COUNT;

    // 三角形的颜色数组，rgba
    private float[] triangleColor = {0.3f, 0.1f, 0.3f, 1f};

    // 顶点坐标数据要转化成FloatBuffer格式
    private FloatBuffer vertexBuffer;

    private int program;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 加载和编译顶点着色器
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vertexShaderCode);
        GLES20.glCompileShader(vertexShader);

        // 加载和编译片段着色器
        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShader);

        // 创建空的GL程序，并且把着色器放进去
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);

        // 链接GL程序
        GLES20.glLinkProgram(program);

        // 应用GL程序到Opengl环境
        GLES20.glUseProgram(program);

        // 将三角形数据放到buffer中。
        // Java的缓冲区数据存储结构为大端字节序(BigEdian)，而OpenGl的数据为小端字节序（LittleEdian）,
        // 使用OpenGl的时候必须要进行下转换
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * Float.BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(vertexData);
        vertexBuffer.position(0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 重绘背景
        GLES20.glClearColor(0.3f, 0.2f, 0.1f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

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

        // 使用TRIANGLES方式渲染，顶点数量为3个
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, VERTEX_COUNT);
    }
}
