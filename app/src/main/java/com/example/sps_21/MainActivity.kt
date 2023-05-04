package com.example.sps_21

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.AudioTrack
import android.media.AudioManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.experimental.and

import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.rememberScaffoldState
import com.example.sps_21.ui.theme.SPS_21Theme


class MainActivity : ComponentActivity() {
    private val SAMPLE_RATE = 83333
    private val CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private val ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING)
    private var outputStream: FileOutputStream? = null
    private var recordingThread: Thread? = null
    private var isRecording = false
    private var recorder: AudioRecord? = null

    // generate 20kHz tone
    private val genFreq = 20000
    private val PLAYER_CHANNEL = AudioFormat.CHANNEL_OUT_MONO;
    private var TRACK_BUFFER_SIZE = 0
    private val PLAY_DURATION = 3
    private val numSamples = SAMPLE_RATE * PLAY_DURATION
    private var samples = DoubleArray(numSamples)
    private var gSnd = ByteArray(2 * numSamples)
    private var playingThread: Thread? = null

    private var player: AudioTrack? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            0
        )
        setContent {
            SPS_21Theme {
                    ScaffoldDemo()
                }
            }
        }
    fun createRecorder() {
        if (ActivityCompat.checkSelfPermission(
                this,
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
        recorder = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, ENCODING, BUFFER_SIZE)
        recorder?.startRecording()
        isRecording = true

        outputStream = FileOutputStream(cacheDir.absolutePath + "/recording12.pcm")

        recordingThread = Thread( Runnable {
            val buffer = ShortArray(BUFFER_SIZE)
            while (isRecording) {
                val read = recorder?.read(buffer, 0, BUFFER_SIZE) ?: 0
                outputStream?.write(toByteArray(buffer), 0 , read * 2)
            }
        })
        recordingThread?.start()
    }

    fun stopRecording() {
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

    private fun createPlayer() {
        try {
            TRACK_BUFFER_SIZE = AudioTrack.getMinBufferSize(numSamples, PLAYER_CHANNEL, ENCODING)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        player = AudioTrack(
            AudioManager.STREAM_MUSIC,
            SAMPLE_RATE,
            PLAYER_CHANNEL,
            ENCODING,
            TRACK_BUFFER_SIZE,
            AudioTrack.MODE_STREAM
        )
        player?.write(gSnd, 0, gSnd.size)
        player?.play()
    }

    private fun generateChirp() {
        for (i in 0 until numSamples) {
            samples[i] = Math.sin(2.0 * Math.PI * i.toDouble() / (SAMPLE_RATE / genFreq))
        }

        var idx = 0
        for (dVal in samples) {
            val shortVal = (dVal * 32767).toInt().toShort()
            gSnd[idx++] = (shortVal and 0x00ff).toByte()
            gSnd[idx++] = (shortVal and 0xff00.toShort()).toInt().ushr(8).toByte()
        }
    }

    fun playChirp() {
        playingThread = Thread(
            Runnable {
                generateChirp()
                createPlayer()
            }
        )
        playingThread?.start()
    }

    fun generateSpectrum(){
        var spectrum = File(cacheDir.absolutePath,"spectrum.png")
        var pcm = File(cacheDir.absolutePath,"recording12.pcm")
        //var filepath = pcm.absolutePath;
        //println("file path:,$filepath")
        SignalProcessing.pcmToSpectrum(pcm,spectrum)
    }

}


@Composable
fun MainActivity.ScaffoldDemo() {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
    ) {
            innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues = innerPadding).wrapContentSize(
            Alignment.Center)) {
            Button(onClick = {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar("Recording & Playing", duration = SnackbarDuration.Short)
                    delay(5000)
                }
                createRecorder()
                playChirp()
            }) {
                Text(text = "Start Program")
            }
            Button(onClick = {
                scope.launch { scaffoldState.snackbarHostState.showSnackbar("Recording stopped", duration = SnackbarDuration.Short) }
                stopRecording()
            }) {
                Text(text = "Stop recording")
            }
            Button(onClick = {
                scope.launch { scaffoldState.snackbarHostState.showSnackbar("spectrum", duration = SnackbarDuration.Short) }
                generateSpectrum()
            }) {
                Text(text = "Show spectrum")
            }
        }
    }
}
