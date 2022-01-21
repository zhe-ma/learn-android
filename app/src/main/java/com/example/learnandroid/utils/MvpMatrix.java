package com.example.learnandroid.utils;

import android.opengl.Matrix;

import java.util.Arrays;
import java.util.Stack;

public class MvpMatrix {
    private static final String TAG = "MvpMatrix";

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

    private float[] modelMatrix = {  // 模型变换矩阵
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1,
    };

    private float[] viewMatrix = new float[16];  // 相机矩阵
    private float[] projectionMatrix = new float[16];  // 投影矩阵

    private float[] mvpMatrix = new float[16];

    private int matrixOffset = 0;

    private Stack<float[]> matrixStack = new Stack<>();

    public void setViewMatrix(float eyeX, float eyeY, float eyeZ,
                              float centerX, float centerY, float centerZ,
                              float upX, float upY, float upZ) {
        Matrix.setLookAtM(viewMatrix, matrixOffset, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    }

    public void setOrthoMatrix(float left, float right, float bottom,
                               float top, float near, float far) {
        Matrix.orthoM(projectionMatrix, matrixOffset, left, right, bottom, top, near, far);
    }

    public float[] getMvpMatrix() {
        // mvpMatrix = projectionMatrix * viewMatrix * modelMatrix
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
        return mvpMatrix;
    }

    public void translate(float x, float y, float z) {
        Matrix.translateM(modelMatrix, matrixOffset, x, y, z);
    }

    public void scale(float x, float y, float z) {
        Matrix.scaleM(modelMatrix, matrixOffset, x, y, z);
    }

    public void rotate(float angleDegree, float x, float y, float z) {
        Matrix.rotateM(modelMatrix, matrixOffset, angleDegree, x, y, z);
    }

    /*
    保存当前模型变换矩阵
     */
    public void pushMatrix() {
        matrixStack.push(Arrays.copyOf(modelMatrix, modelMatrix.length));
    }

    /*
    恢复之前的模型变换矩阵
     */
    public void popMatrix() {
        modelMatrix = matrixStack.pop();
    }
}
