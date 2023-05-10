package com.example.sps_21

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import org.apache.commons.math3.transform.DftNormalization
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.min

class SignalProcessing {
    companion object {
        fun pcmToSpectrum(
            pcmFile: File,
            spectrumFile: File,
            sampleRate: Int = 63333,
            fftSize: Int = 16932
        ) {
            val numFrames = pcmFile.length() / 2
            val input = pcmFile.readBytes()

            // Loop through the input data, computing the FFT of each segment and rendering it as a line in the output image
            val transformer = org.apache.commons.math3.transform.FastFourierTransformer(
                DftNormalization.STANDARD
            );
            val inputLength = numFrames.toInt()
            val paddedLength = Integer.highestOneBit(inputLength - 1) shl 1
            val paddled = ShortArray(paddedLength)
            val transform_lenth = min(paddedLength, fftSize)
            var frequencies = DoubleArray(transform_lenth)
            val test_input = DoubleArray(transform_lenth)
            for (i in 0 until transform_lenth) {
                test_input[i] =
                    400000*(Math.sin(2.0 * Math.PI * i.toDouble() / (sampleRate / 500)))//500hz sinwave
            }
            for (i in 0 until transform_lenth) {
                if (i * 2 < input.size) {
                    paddled[i] =
                        (input[i * 2].toShort() and 0X00FF) or ((input[i * 2 + 1].toInt() shl 8).toShort())
                } else {
                    paddled[i] = 0 //pad with 0
                }
            }
            try {
                val transformed = transformer.transform(paddled.map {
                    org.apache.commons.math3.complex.Complex(
                        it.toDouble(),
                        0.0
                    )
                }.toTypedArray(), org.apache.commons.math3.transform.TransformType.FORWARD)
//                 val transformed = transformer.transform(test_input.map { org .apache.commons.math3.complex.Complex(it.toDouble(),0.0)}.toTypedArray(),org.apache.commons.math3.transform.TransformType.FORWARD)
                for (i in 0 until transform_lenth) {
                    frequencies[i] = transformed[i].abs()
                }
            } catch (e: Exception) {
                Log.e("TAG", "Error message: ${e.message}")
                e.printStackTrace()
            }


            //////////////////////////////////////////////////////////
            val chartWidth = 1500 // Width of the chart in pixels
            val chartHeight = 1000 // Height of the chart in pixels
            val chartMargin = 0
            val chartBitmap = Bitmap.createBitmap(chartWidth, chartHeight, Bitmap.Config.ARGB_8888)
            val chartCanvas = Canvas(chartBitmap)
            chartCanvas.drawColor(Color.WHITE)
            val barPaint = Paint()
            barPaint.color = Color.BLACK
            barPaint.style = Paint.Style.FILL
            val barWidth = (chartWidth - 2 * chartMargin) / (transform_lenth.toFloat())
            val barSpacing = 0
            val maxAmplitude = 500000
            // val index = frequencies.indexOfFirst { it==maxAmplitude }
            for (i in frequencies.indices) {
                val x = chartMargin + i * (barWidth + barSpacing)
                val barHeight = chartHeight * frequencies[i].toFloat() / maxAmplitude.toFloat()
                val y = chartHeight - chartMargin - barHeight
                chartCanvas.drawRect(
                    x.toFloat(), y,
                    (x + barWidth).toFloat(), chartHeight - chartMargin.toFloat(), barPaint
                )
            }
            chartBitmap.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(spectrumFile))
        }
    }
}