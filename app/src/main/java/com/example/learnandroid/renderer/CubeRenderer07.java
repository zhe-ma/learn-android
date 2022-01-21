package com.example.learnandroid.renderer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.learnandroid.utils.FpsCalculator;
import com.example.learnandroid.utils.GLUtil;
import com.example.learnandroid.utils.MvpMatrix;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/*
使用GL_TRIANGLES三角形绘制方式绘制旋转立方体
注意要启用深度测试
 */
public class CubeRenderer07 implements GLSurfaceView.Renderer {
    private static final String TAG = "CubeRenderer07";

    // MVP （Model View Projection）矩阵变换。
    // Model：模型变换，施加在模型上的空间变换，包含平移变换（translateM）、旋转变换（rotateM）、对称变换（transposeM）、缩放变换（scaleM）
    // View：观测变换，施加在观测点上的变换，用于调整观测点位置、观测朝向、观测正方向；
    // Projection：透视变换，施加在视觉上的变换，用于调整模型的透视效果（如：矩形的透视效果是梯形）。
    // 上述变换依次叠加，得到一个总的变换矩阵，即 MVP 变换矩阵，
    // mvpMatrix = projectionMatrix * viewMatrix * modelMatrix，MVP 变换作用到模型的原始坐标矩阵上，得到的最终坐标矩阵即为用户观测到的模型状态

    // 设三维空间中的任意向量按照以上规则映射到四维空间中的向量为 v = [a, b, c, 1]'，变换矩阵为 A 。
    // OpenGL 为通用化接口，在获取变换矩阵时，会左乘一个初始矩阵 M，即将 MA 作为最终的变换矩阵，通常情况 M 为单位矩阵（E），
    // 即 M = E。(v 为列向量，A、M、E 都是 4x4 矩阵)
    // 任何矩阵与单位矩阵相乘都等于本身
    // https://blog.csdn.net/m0_37602827/article/details/120818853

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

    // 立方体顶点数组
    // 每个面都由两个三角形组成
    private float vertexData[] = {
            // 顶面
            -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,

            // 底面
            -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f,

            // 左侧面
            -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f,

            // 右侧面
            0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f,

            // 前面
            -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f,

            // 后面
            -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f,
    };

    private float[] colorData = {
            // 顶面黑色
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,

            // 底面白色
            1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,

            // 左侧面红色
            1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f,

            // 右侧面绿色
            0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f,

            // 前面蓝色
            0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,

            // 后面黄色
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
    };

    // 顶点坐标数据要转化成FloatBuffer格式
    private FloatBuffer vertexBuffer;

    // 颜色数据要转为FloatBuffer格式
    private FloatBuffer colorBuffer;

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
        colorBuffer = GLUtil.floatArray2FloatBuffer(colorData);
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

        // 可以使用以下标志位
        // GL_COLOR_BUFFER_BIT: 当前可写的颜色缓冲
        // GL_DEPTH_BUFFER_BIT: 深度缓冲
        // GL_ACCUM_BUFFER_BIT: 累积缓冲
        // GL_STENCIL_BUFFER_BIT: 模板缓冲

        // 颜色缓冲区（COLOR_BUFFER）就是帧缓冲区（FRAME_BUFFER）,你需要渲染的场景最终每一个像素都要写入该缓冲区,然后由它在渲染到屏幕上显示.
        // 深度缓冲区（DEPTH_BUFFER）与帧缓冲区对应,用于记录上面每个像素的深度值,通过深度缓冲区,我们可以进行深度测试,从而确定像素的遮挡关系,保证渲染正确.
        // 模版缓冲（STENCIL_BUFFER）与深度缓冲大小相同,通过设置模版缓冲每个像素的值,我们可以指定在渲染的时候只渲染某些像素,从而可以达到一些特殊的效果.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);

        mvpMatrix.pushMatrix();
        mvpMatrix.translate(0.0f, 1f, 0);
        mvpMatrix.scale(0.8f, 0.8f, 0.8f);
        mvpMatrix.rotate(frame_count % 360, frame_count % 360, frame_count % 720, frame_count % 180);
        drawCube();
        mvpMatrix.popMatrix();


        mvpMatrix.pushMatrix();
        mvpMatrix.translate((frame_count % 100 - 50) * 0.01f, -1f, 0);
        mvpMatrix.scale(0.5f, 0.5f, 0.5f);
        mvpMatrix.rotate(frame_count * 3 % 360, -1, 2, 3);
        drawCube();
        mvpMatrix.popMatrix();
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

        int colorHandle = GLES20.glGetAttribLocation(program, "aColor");
        GLES20.glEnableVertexAttribArray(colorHandle);
        // Color信息是4个float值为一组值
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexData.length / VERTEX_COMPONENT_COUNT);

        // 关闭顶点数组句柄
        GLES20.glDisableVertexAttribArray(positionHandle);

        // 关闭颜色数组句柄
        GLES20.glDisableVertexAttribArray(colorHandle);
    }
}
