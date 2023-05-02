package com.example.sps_21

import android.graphics.Bitmap
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.text.style.LineBackgroundSpan.Standard
import android.util.Log
import org.apache.commons.math3.transform.DftNormalization
import java.io.File
import java.lang.Exception

class SignalProcessing {
    companion object {
        fun pcmToSpectrum(pcmFile: File, spectrumFile: File, sampleRate: Int = 44100, fftSize: Int = 2048) {
            // Compute the number of frames in the input data
            val numFrames = pcmFile.length() / 2

            // Set up the AudioRecord object to read in the PCM data
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val buffer = ByteArray(bufferSize / 2)

            // Set up the output image
            val height = fftSize / 2 // We only need to show the positive frequencies
            val image = Bitmap.createBitmap(numFrames.toInt(), height, Bitmap.Config.ARGB_8888)

            // Loop through the input data, computing the FFT of each segment and rendering it as a line in the output image
            val transformer = org.apache.commons.math3.transform.FastFourierTransformer(
                DftNormalization.STANDARD
            );
            for (i in 0 until numFrames - fftSize step (fftSize / 2).toLong()) {
                // Read in the input data
                Log.i("i","$i")
                val inputStream = pcmFile.inputStream()
                inputStream.skip(i.toLong() * 2) // Assumes 16-bit PCM data
                inputStream.read(buffer, 0, buffer.size)
                inputStream.close()

                // Compute the FFT of the input data
                val input = buffer.map { it.toDouble() }.toDoubleArray()
                val inputLength = input.size
                val paddedLength = Integer.highestOneBit(inputLength - 1) shl 1
                val paddled = DoubleArray(paddedLength) { i ->
                    if (i < inputLength) input[i] else 0.0
                }
                try{
                    val transformed = transformer.transform(paddled.map { org.apache.commons.math3.complex.Complex(it, 0.0) }.toTypedArray(), org.apache.commons.math3.transform.TransformType.FORWARD)
                    // Render the FFT as a line in the output image
                    for (j in 0 until height) {
                        val magnitude = transformed[j].abs()
//                        Log.i("M","mag:,$i $j $magnitude")
                        val color = (255 * magnitude / fftSize).toInt()
                        image.setPixel((i / (fftSize / 2)).toInt(), height - j - 1, Color.rgb(color, 0, color))
                    }
                }catch (e: Exception) {
                    Log.e("TAG", "Error message: ${e.message}")
                    e.printStackTrace()
                }

            }

//            Save the output image to disk
            spectrumFile.outputStream().use { outputStream ->
                image.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
        }
    }
}