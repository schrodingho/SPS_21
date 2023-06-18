package com.example.sps_21.infer
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.github.psambit9791.jdsp.filter.Butterworth
import com.github.psambit9791.jdsp.transform.ShortTimeFourier
import java.io.File
import java.io.FileOutputStream


class Spectrogram {
    private val frameLength = 10
    private val fourierLength = 1000
        private val overlap = 1
    private val fs: Double = 63333.0
    private val order = 5
    private val butter = Butterworth(fs)
    private val cutoff: Double = 2000.0
    private val normal_cutoff: Double = cutoff
    private val high_cutoff: Double = 10000.0

    fun trans(inputFile: File, outputFile: File?): FloatArray {
        var inputData = inputFile.readBytes()
        val len = inputData!!.size
        var signals: DoubleArray = DoubleArray((len / 2).toInt())
        for (i in 0 until (len / 2).toInt() ) {
            signals[i] = (inputData!![i * 2].toInt() and 0xFF or (inputData!![i * 2 + 1].toInt() shl 8)).toDouble()
        }

        signals = butter.highPassFilter(signals, order, normal_cutoff)
        signals = butter.lowPassFilter(signals, order, high_cutoff)

        var index_f: Int = 0
        var amplitude: Double = 10000.0
        while (index_f < 7000 || index_f > 16000 ) {
            for (i in 0 until signals.size) {
                if (signals[i] > amplitude) {
                    index_f = i
                    break
                }
            }
            amplitude += 100.0
            if (amplitude > 30000.0) break
        }
        val maxData = signals.slice(index_f until index_f + 3000).toDoubleArray().maxOrNull()
        val indices = signals.indices.filter { signals[it] > maxData!! - 5000 }.toTypedArray()
        val filteredIndices = indices.filter { it < index_f + 3000 }
        val id = filteredIndices.maxOrNull()
        if (id != null) {
            signals = signals.slice(id until id + 3000).toDoubleArray()
        }

        val stft = ShortTimeFourier(signals, frameLength, overlap, fourierLength)
        stft.transform()
        var out = stft.getMagnitude(true)
        out = out.map { doublearraydivideby1000(it) }.toTypedArray()
        if (outputFile != null) {
            toGrayScale(out, outputFile)
        }
        out = transpose_array(out)
//        val outJson = Gson().toJson(out)
//        outputFile.writeText(outJson)
        return flattenArray(out)

    }

    fun flattenArray(input: Array<DoubleArray>): FloatArray {
        val row = input.size
        val col = input[0].size
        val output = FloatArray(row * col)
        for (i in 0 until row) {
            for (j in 0 until col) {
                output[i * col + j] = input[i][j].toFloat()
            }
        }
        return output
    }

    fun doublearraydivideby1000(input: DoubleArray): DoubleArray {
        var output = DoubleArray(input.size)
        for (i in 0 until input.size) {
            output[i] = input[i] / 1000.0
        }
        return output
    }

    fun transpose_array(input: Array<DoubleArray>): Array<DoubleArray> {
        val row = input.size
        val col = input[0].size
        val output = Array(col) { DoubleArray(row) }
        for (i in 0 until row) {
            for (j in 0 until col) {
                output[j][i] = input[i][j]
            }
        }
        return output
    }

    fun toGrayScale(input: Array<DoubleArray>, outputFile: File?) {
        var outMax = input[0][0];
        var outMin = input[0][0];
        val width = input[0].size;
        val height = input.size

        for (i in 0 until height) {
            for (j in 0 until width) {
                if (input[i][j] > outMax) {
                    outMax = input[i][j]
                }
                if (input[i][j] < outMin) {
                    outMin = input[i][j]
                }
            }
        }


        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (i in 0 until height) {
            for (j in 0 until width) {
                val normalizedValue = (255 - ((input[i][j] - outMin) / (outMax - outMin) * 255)).toInt()
//                val color = 255 - (out[i][j] * 255).toInt()
                image.setPixel(j, i, Color.rgb(normalizedValue, normalizedValue, normalizedValue))
//                ColorUtils.blendARGB(Color.rgb(normalizedValue, normalizedValue, normalizedValue), Color.BLACK, 0.5f)
//                image.setPixel(i, j, color)
            }
        }
        image.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(outputFile))
    }

}