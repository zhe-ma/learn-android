package com.example.learnandroid.renderer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

// 背景色
public class ClearRenderer01 implements GLSurfaceView.Renderer {
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // 计算机清理机制是，将数据覆盖，而不是像扫地一样将垃圾清理干净
        // glClearColor specifies the red, green, blue, and alpha values used by glClear to clear the color buffers.
        // 设置颜色缓存的清除值
        // glClearColor函数设置的值，就是将要用来覆盖存储空间的值。
        GLES20.glClearColor(0.2f, 0.3f, 0.4f, 1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        // glViewport(GLint x,GLint y,GLsizei width,GLsizei height)
        // 为归一化坐标转换为屏幕坐标的接口。
        // 换言之将整张纹理上的数据，转换到屏幕上具体的像素点的接口。
        // x，y为以控件左下角为起始坐标，对应渲染纹理的左下角。
        // 右为x轴的正方向。
        // 上为y轴的正方向。
        // width，height是以x，y为起始位置的宽和高，用来确定渲染出的数据到屏幕的位置。
        // 可以在屏幕上正常渲染出来的像素范围为x轴：0--width，y轴：0--height。超出部分将不显示。
        // 用户可以通过该接口，控制数据渲染到屏幕的具体位置和范围。
        // 如果超过该空间的屏幕像素，将不显示。但并不意味着opengGL没有绘制超出显示部分的纹理数据。
        GLES20.glViewport(100, 100, i, i1);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        // 将缓存清除为预先的设置值
        // glClear清理缓存就是将覆盖新的数据

        // 可以使用以下标志位
        // GL_COLOR_BUFFER_BIT: 当前可写的颜色缓冲
        // GL_DEPTH_BUFFER_BIT: 深度缓冲
        // GL_ACCUM_BUFFER_BIT: 累积缓冲
        // GL_STENCIL_BUFFER_BIT: 模板缓冲


        // 颜色缓冲区（COLOR_BUFFER）就是帧缓冲区（FRAME_BUFFER）,你需要渲染的场景最终每一个像素都要写入该缓冲区,然后由它在渲染到屏幕上显示.
        // 深度缓冲区（DEPTH_BUFFER）与帧缓冲区对应,用于记录上面每个像素的深度值,通过深度缓冲区,我们可以进行深度测试,从而确定像素的遮挡关系,保证渲染正确.
        // 模版缓冲（STENCIL_BUFFER）与深度缓冲大小相同,通过设置模版缓冲每个像素的值,我们可以指定在渲染的时候只渲染某些像素,从而可以达到一些特殊的效果.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }
}
