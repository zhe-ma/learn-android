package com.example.learnandroid.renderer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

// 等腰三角形
public class TriangleRenderer03 implements GLSurfaceView.Renderer {
    private static final String TAG = "TriangleRenderer03";

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

    // 相机位置矩阵
    private final float[] viewMatrix = new float[16];
    // 投影矩阵
    private final float[] projectionMatrix = new float[16];
    // 变换矩阵，投影矩阵*相机位置矩阵的结果，最终传给着色器
    private final float[] mvpMatrix = new float[16];

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

        // 参考：
        // 《OpenGL 学习系列---投影矩阵》https://cloud.tencent.com/developer/article/1472455
        // 《Android OpenGL ES 4.正交投影》https://www.jianshu.com/p/51a405bc52ed

        // 设置相机位置: Matrix.setLookAtM
        // 相机位置：相机的位置是比较好理解的，就是相机在3D空间里面的坐标点。
        // 相机观察方向：相机的观察方向，表示的是相机镜头的朝向，你可以朝前拍、朝后拍、也可以朝左朝右，或者其他的方向。
        // 相机UP方向：相机的UP方向，可以理解为相机顶端指向的方向。比如你把相机斜着拿着，拍出来的照片就是斜着的，你倒着拿着，拍出来的就是倒着的。
        // Matrix.setLookAtM (float[] rm,  //接收相机变换矩阵
        //                    int rmOffset,  //变换矩阵的起始位置（偏移量）
        //                    float eyeX,float eyeY, float eyeZ,  //相机位置
        //                    float centerX,float centerY,float centerZ,  //观测点位置
        //                    float upX,float upY,float upZ)  //up向量在xyz上的分量
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 7f, 0, 0, 0, 0, 1, 0);

        // 不管是正交投影还是透视投影，最终都是将视景体内的物体投影在近平面上，这也是 3D 坐标转换到 2D 坐标的关键一步。

        // 使用正交投影，物体呈现出来的大小不会随着其距离视点的远近而发生变化。
        // Matrix.orthoM (float[] m,  //接收正交投影的变换矩阵
        //                int mOffset,  //变换矩阵的起始位置（偏移量）
        //                float left,  //相对观察点近面的左边距，x的最小值
        //                float right,  //相对观察点近面的右边距，x的最大值
        //                float bottom,  //相对观察点近面的下边距，y的最小值
        //                float top,  //相对观察点近面的上边距，y的最大值
        //                float near,  //相对观察点近面距离
        //                float far)  //相对观察点远面距离
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

            // 如下赋值，将坐标轴范围改为(-2 ,2)，三角形的顶点是0.5位置,那么画出来的三角形则是四分之一的长度。
//            Matrix.orthoM(projectionMatrix, 0, -2, 2, -ratio * 2, ratio * 2, 1, 10);
        }

        // 使用透视投影，物体离视点越远，呈现出来的越小。离视点越近，呈现出来的越大。
        // Matrix.frustumM (float[] m,  //接收透视投影的变换矩阵
        //                 int mOffset,  //变换矩阵的起始位置（偏移量）
        //                 float left,  //相对观察点近面的左边距
        //                 float right,  //相对观察点近面的右边距
        //                 float bottom,  //相对观察点近面的下边距
        //                 float top,  //相对观察点近面的上边距
        //                 float near,  //相对观察点近面距离
        //                 float far)  //相对观察点远面距离
//        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

        // 计算变换矩阵
        // Matrix.multiplyMM (float[] result,  //接收相乘结果
        //                    int resultOffset,  //接收矩阵的起始位置（偏移量）
        //                    float[] lhs,  //左矩阵
        //                    int lhsOffset,  //左矩阵的起始位置（偏移量）
        //                    float[] rhs,  //右矩阵
        //                    int rhsOffset)  //右矩阵的起始位置（偏移量）
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
    }

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

        // 设置三角形颜色
        int colorHandle = GLES20.glGetUniformLocation(program, "vColor");
        GLES20.glUniform4fv(colorHandle, 1, triangleColor, 0);

        // 使用TRIANGLES方式渲染，顶点数量为3个
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, VERTEX_COUNT);

        // 关闭顶点数组句柄
        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}
