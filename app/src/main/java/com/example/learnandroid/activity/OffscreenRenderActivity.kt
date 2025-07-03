package com.example.learnandroid.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.opengl.GLES20
import android.opengl.GLUtils
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.learnandroid.R
import com.example.learnandroid.utils.EGLHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder

class OffscreenRenderActivity : AppCompatActivity() {

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    private val eglHelper = EGLHelper()
    private var renderView: ImageView? = null
    private var pickerBtn: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offscreen_render)
        renderView = findViewById(R.id.renderView)
        pickerBtn = findViewById(R.id.pickerButton)
        pickerBtn?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        renderColor()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImage: Uri? = data.data
            selectedImage?.let {
                renderPickerImage(it)
            }
        }
    }

    private fun renderPickerImage(imageUri: Uri) {
        eglHelper.makeContext(300, 300)

        // 1. 从URI加载图像到Bitmap
        val inputStream = contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // 2. 创建原始纹理
        val originalTextureIds = IntArray(1)
        GLES20.glGenTextures(1, originalTextureIds, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, originalTextureIds[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, originalBitmap, 0)

        // 3. 创建模糊纹理
        val blurredTextureIds = IntArray(1)
        GLES20.glGenTextures(1, blurredTextureIds, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, blurredTextureIds[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 300, 300, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)

        // 4. 创建帧缓冲
        val frameBuffers = IntArray(1)
        GLES20.glGenFramebuffers(1, frameBuffers, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0])
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, blurredTextureIds[0], 0)

        // 5. 检查帧缓冲状态
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            return
        }

        // 6. 创建高斯模糊着色器程序
        val vertexShader = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                vTexCoord = aTexCoord;
            }
        """.trimIndent()

        val fragmentShader = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D uTexture;
            uniform float uRadius;
            uniform vec2 uResolution;
            
            void main() {
                vec4 sum = vec4(0.0);
                vec2 texelSize = 1.0 / uResolution;
                
                // 高斯模糊核 (9个采样点)
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        vec2 offset = vec2(float(i), float(j)) * uRadius * texelSize;
                        sum += texture2D(uTexture, vTexCoord + offset) * 0.111;
                    }
                }
                
                gl_FragColor = sum;
            }
        """.trimIndent()

        val program = createProgram(vertexShader, fragmentShader)
        GLES20.glUseProgram(program)

        // 7. 设置着色器参数
        val positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        val texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        val textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        val radiusHandle = GLES20.glGetUniformLocation(program, "uRadius")
        val resolutionHandle = GLES20.glGetUniformLocation(program, "uResolution")

        GLES20.glUniform1f(radiusHandle, 3.0f)
        GLES20.glUniform2f(resolutionHandle, 300.0f, 300.0f)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, originalTextureIds[0])
        GLES20.glUniform1i(textureHandle, 0)

        // 8. 绘制全屏四边形
        val vertices = floatArrayOf(
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 0.0f
        )

        val texCoords = floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
        )

        val vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices)
        vertexBuffer.position(0)

        val texBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(texCoords)
        texBuffer.position(0)

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texBuffer)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        // 9. 读取模糊后的纹理到Bitmap
        val buffer = ByteBuffer.allocateDirect(300 * 300 * 4)
        GLES20.glReadPixels(0, 0, 300, 300, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer)

        val blurredBitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
        buffer.rewind()
        blurredBitmap.copyPixelsFromBuffer(buffer)

        // 10. 清理资源
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glDeleteTextures(1, originalTextureIds, 0)
        GLES20.glDeleteTextures(1, blurredTextureIds, 0)
        GLES20.glDeleteFramebuffers(1, frameBuffers, 0)
        GLES20.glDeleteProgram(program)

        eglHelper.unmakeCurrent()

        // 11. 显示结果
        renderView?.setImageBitmap(blurredBitmap)
    }

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        return program
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }

    private fun renderColor() {
        eglHelper.makeContext(300, 300)

        // Create a texture
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 300, 300, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)

        // Create a framebuffer and bind the texture
        val frameBuffers = IntArray(1)
        GLES20.glGenFramebuffers(1, frameBuffers, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0])
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureIds[0], 0)

        // Check framebuffer status
        val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            return
        }

        // Set the clear color to yellow (RGBA: 1, 1, 0, 1)
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f)
        // Clear the color buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Read pixels to buffer
        val buffer = ByteBuffer.allocateDirect(300 * 300 * 4)
        GLES20.glReadPixels(0, 0, 300, 300, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer)

        // Create Bitmap from buffer
        val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
        buffer.rewind()
        bitmap.copyPixelsFromBuffer(buffer)

        // Unbind resources
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        eglHelper.unmakeCurrent()

        renderView?.setImageBitmap(bitmap)
    }
}