package com.example.learnandroid.utils

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer

object WinkAudioUtils {
    private const val TAG = "WinkAudioUtils"
    private const val MIME_TYPE = "audio/mp4a-latm"
    private const val TIMEOUT_USEC = 10000
    private const val SAMPLE_RATE = 44100
    private const val CHANNEL_COUNT = 1
    private const val BIT_RATE = 64000
    private const val BUFFER_SIZE = 16384 // 16KB

    /**
     * 已知视频文件的音频为aac格式，提取音频到文件
     */
    fun extractAudioToFile(videoPath: String, audioPath: String): Boolean {
        var mediaExtractor: MediaExtractor? = null
        var mediaMuxer: MediaMuxer? = null
        var audioTrackIndex = -1
        var muxerStarted = false
        
        try {
            // 检查输入文件是否存在
            val videoFile = File(videoPath)
            if (!videoFile.exists()) {
                return false
            }
            
            // 创建输出目录
            val audioFile = File(audioPath)
            audioFile.parentFile?.mkdirs()
            
            // 创建媒体提取器
            mediaExtractor = MediaExtractor()
            mediaExtractor.setDataSource(videoPath)
            
            // 查找音频轨道
            val numTracks = mediaExtractor.trackCount
            for (i in 0 until numTracks) {
                val format = mediaExtractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("audio/") == true) {
                    audioTrackIndex = i
                    break
                }
            }
            
            if (audioTrackIndex == -1) {
                // 没有找到音频轨道
                return false
            }
            
            // 选择音频轨道
            mediaExtractor.selectTrack(audioTrackIndex)
            val audioFormat = mediaExtractor.getTrackFormat(audioTrackIndex)
            
            // 创建复用器
            mediaMuxer = MediaMuxer(audioPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val muxerTrackIndex = mediaMuxer.addTrack(audioFormat)
            mediaMuxer.start()
            muxerStarted = true
            
            val bufferInfo = MediaCodec.BufferInfo()
            val buffer = ByteBuffer.allocate(64 * 1024) // 64KB缓冲区
            
            // 提取并写入音频数据
            while (true) {
                buffer.clear()
                val sampleSize = mediaExtractor.readSampleData(buffer, 0)
                if (sampleSize < 0) {
                    break
                }
                
                bufferInfo.set(0, sampleSize, mediaExtractor.sampleTime, mediaExtractor.sampleFlags)
                buffer.position(0)
                buffer.limit(sampleSize)
                mediaMuxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                mediaExtractor.advance()
            }
            
            return true
            
        } catch (e: Exception) {
            return false
        } finally {
            try {
                mediaExtractor?.release()
                if (muxerStarted) {
                    mediaMuxer?.stop()
                }
                mediaMuxer?.release()
            } catch (e: Exception) {
            }
        }
    }

    fun convertPcmToAac(
        pcmFile: String,
        aacFile: String,
        channelCount: Int = CHANNEL_COUNT,
        sampleRate: Int = SAMPLE_RATE,
        bitRate: Int = BIT_RATE
    ): Boolean {
        var mediaCodec: MediaCodec? = null
        var mediaMuxer: MediaMuxer? = null
        var inputStream: FileInputStream? = null
        
        try {
            // 检查输入文件是否存在
            val inputFile = File(pcmFile)
            if (!inputFile.exists()) {
                return false
            }
            
            // 创建输出目录
            val outputFile = File(aacFile)
            outputFile.parentFile?.mkdirs()
            
            // 配置音频格式
            val audioFormat = MediaFormat().apply {
                setString(MediaFormat.KEY_MIME, MIME_TYPE)
                setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelCount)
                setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate)
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, BUFFER_SIZE)
            }
            
            // 创建编码器
            mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE)
            mediaCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            mediaCodec.start()
            
            // 创建复用器
            mediaMuxer = MediaMuxer(aacFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            
            // 打开PCM文件
            inputStream = FileInputStream(inputFile)
            
            val inputBuffers = mediaCodec.inputBuffers
            val outputBuffers = mediaCodec.outputBuffers
            val bufferInfo = MediaCodec.BufferInfo()
            
            var audioTrackIndex = -1
            var muxerStarted = false
            var presentationTimeUs = 0L
            val frameSize = SAMPLE_RATE * CHANNEL_COUNT * 2 / 50 // 20ms per frame
            val pcmBuffer = ByteArray(frameSize)
            var endOfStream = false
            
            while (!endOfStream) {
                // 输入PCM数据
                if (!endOfStream) {
                    val inputBufferIndex = mediaCodec.dequeueInputBuffer(TIMEOUT_USEC.toLong())
                    if (inputBufferIndex >= 0) {
                        val inputBuffer = inputBuffers[inputBufferIndex]
                        inputBuffer.clear()
                        
                        val bytesRead = inputStream.read(pcmBuffer)
                        if (bytesRead > 0) {
                            inputBuffer.put(pcmBuffer, 0, bytesRead)
                            mediaCodec.queueInputBuffer(inputBufferIndex, 0, bytesRead, presentationTimeUs, 0)
                            presentationTimeUs += (bytesRead * 1000000L) / (SAMPLE_RATE * CHANNEL_COUNT * 2)
                        } else {
                            // 输入结束
                            mediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            endOfStream = true
                        }
                    }
                }
                
                // 获取编码后的数据
                val outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC.toLong())
                when {
                    outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        // 没有可用的输出缓冲区，继续循环
                    }
                    outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        // 输出格式改变，添加音频轨道
                        val newFormat = mediaCodec.outputFormat
                        audioTrackIndex = mediaMuxer.addTrack(newFormat)
                        mediaMuxer.start()
                        muxerStarted = true
                    }
                    outputBufferIndex >= 0 -> {
                        val outputBuffer = outputBuffers[outputBufferIndex]
                        
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                            // 编解码器配置数据，不写入文件
                            bufferInfo.size = 0
                        }
                        
                        if (bufferInfo.size > 0 && muxerStarted) {
                            outputBuffer.position(bufferInfo.offset)
                            outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                            mediaMuxer.writeSampleData(audioTrackIndex, outputBuffer, bufferInfo)
                        }
                        
                        mediaCodec.releaseOutputBuffer(outputBufferIndex, false)
                        
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            break
                        }
                    }
                }
            }

            return true
            
        } catch (e: Exception) {
            return false
        } finally {
            try {
                inputStream?.close()
                mediaCodec?.stop()
                mediaCodec?.release()
                mediaMuxer?.stop()
                mediaMuxer?.release()
            } catch (e: Exception) {
            }
        }
    }
}