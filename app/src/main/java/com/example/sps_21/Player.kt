package com.example.sps_21

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import java.io.File
import kotlin.experimental.and

// generate 20kHz tone

class Player(applicationContext: Context) {
    private val SAMPLE_RATE = 63333
    private val ENCODING = AudioFormat.ENCODING_PCM_16BIT

    private val genFreq = 20000
    private val PLAYER_CHANNEL = AudioFormat.CHANNEL_OUT_MONO
    private var TRACK_BUFFER_SIZE = 0
    private val PLAY_DURATION: Double = 0.005
    private val numSamples = (SAMPLE_RATE.toDouble() * PLAY_DURATION).toInt()
    private var samples = DoubleArray(numSamples)
    private var gSnd = ByteArray(2 * numSamples)
    private var playingThread: Thread? = null
    var isPlaying = false
    val curContext = applicationContext
    private var player: AudioTrack? = null
    fun createPlayer() {
//        Log.v("numSamples", "$numSamples")
        try {
            TRACK_BUFFER_SIZE = AudioTrack.getMinBufferSize(SAMPLE_RATE, PLAYER_CHANNEL, ENCODING)
            Log.v("buffersize", "$TRACK_BUFFER_SIZE")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        player = AudioTrack(
            AudioManager.STREAM_MUSIC,
            SAMPLE_RATE,
            PLAYER_CHANNEL,
            ENCODING,
            TRACK_BUFFER_SIZE,
            AudioTrack.MODE_STATIC
        )
        generateChirp()
        player?.write(gSnd, 0, gSnd.size)
    }

    fun generateChirp() {
        for (i in 0 until numSamples) {
            samples[i] = Math.sin(2.0 * Math.PI * i.toDouble() / (SAMPLE_RATE / genFreq))
        }

        var idx = 0
        for (dVal in samples) {
            val shortVal = (dVal * 32767).toInt().toShort()
            gSnd[idx++] = (shortVal and 0x00ff).toByte()
            gSnd[idx++] = (shortVal and 0xff00.toShort()).toInt().ushr(8).toByte()
        }
//        val local_file = File(curContext.cacheDir.absolutePath, "generated_chirp.pcm")
//        local_file.writeBytes(gSnd)

    }

    fun playChirp() {
        playingThread = Thread(
            Runnable {
                player?.play()
            }
        )
        playingThread?.start()
    }

    fun stopChirp() {
        isPlaying = false
        playingThread?.interrupt()
        playingThread = null
    }

}