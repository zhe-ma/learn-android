package com.example.learnandroid.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLUtil {
    public static FloatBuffer getFloatBuffer(final float[] floatData) {
        // Java的缓冲区数据存储结构为大端字节序(BigEdian)，而OpenGl的数据为小端字节序（LittleEdian）,
        // 使用OpenGl的时候必须要进行下转换
        FloatBuffer floatBuffer = ByteBuffer.allocateDirect(floatData.length * Float.BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        floatBuffer.put(floatData);
        floatBuffer.position(0);
        return floatBuffer;
    }
}
