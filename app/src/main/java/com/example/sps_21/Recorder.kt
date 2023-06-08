package com.example.sps_21

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import kotlin.experimental.and

class Recorder(applicationContext: Context) {
    private val SAMPLE_RATE = 63333
    private val CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private val ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING)
    private var outputStream: FileOutputStream? = null
    private var recordingThread: Thread? = null
    private var isRecording = false
    private var recorder: AudioRecord? = null
    val curContext = applicationContext

    fun createRecorder(saveFile: File) {
        if (ContextCompat.checkSelfPermission(
                curContext,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        recorder =
            AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, ENCODING, BUFFER_SIZE)
        outputStream = FileOutputStream(saveFile)

    }
    fun createRecordThread() {
        recordingThread = Thread(Runnable {
            recorder?.startRecording()
            isRecording = true
            var recordDuration = 10
            val buffer = ShortArray(BUFFER_SIZE)
            while (isRecording) {
                recordDuration -= 1
                if (recordDuration == 0) {
                    stopRecord()
                }
                val read = recorder?.read(buffer, 0, BUFFER_SIZE) ?: 0
                outputStream?.write(toByteArray(buffer), 0, read * 2)
            }
        })
    }

    fun startRecord() {
        recordingThread?.start()
    }


    fun stopRecord() {
        isRecording = false
        recorder?.stop()
        recorder?.release()
        recorder = null
        recordingThread = null
        outputStream?.close()
    }

    private fun toByteArray(shortArray: ShortArray): ByteArray {
        val byteArray = ByteArray(shortArray.size * 2)
        for (i in shortArray.indices) {
            byteArray[i * 2] = (shortArray[i] and 0xff).toByte()
            byteArray[i * 2 + 1] = (shortArray[i].toInt() shr 8).toByte()
        }
        return byteArray
    }


}