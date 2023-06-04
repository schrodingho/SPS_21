package com.example.sps_21.infer

import android.content.Context
import android.util.Log
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.cloud.aiplatform.v1.PredictionServiceClient
import com.google.cloud.aiplatform.v1.PredictionServiceSettings
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import uk.me.berndporr.iirj.Butterworth
import java.io.File
import java.io.FileInputStream
import java.security.interfaces.RSAPrivateKey
import java.util.Date
import kotlin.math.abs


//https://github.com/StevenJokess/Pytorch-Kotlin-Demo/blob/master/app/src/main/java/com/say/pytorchkotlindemo/ImageClassificationOperation.kt
class Transformer {
    private var module: Module? = null
    private var inputData: ByteArray? = null
    private val echoLength = 1500
    private val fs: Double = 63333.0
    private val cutoffFreq: Double = 18000.0
    private val filterOrder: Int = 5
    private val bwFilter = Butterworth()
    fun readData(inputFile: File, modelPath: String): Int? {
        module = Module.load(modelPath)
        inputData = inputFile.readBytes()
        val len = inputData!!.size
        var inputSamples: DoubleArray = DoubleArray((len / 2).toInt())
        var cut_index: Int? = null
//        var abs_inputSamples = IntArray((len / 2).toInt())
        bwFilter.highPass(filterOrder, fs, cutoffFreq, 2)
        for (i in 0 until (len / 2).toInt() ) {
            inputSamples[i] = (inputData!![i * 2].toInt() and 0xFF or (inputData!![i * 2 + 1].toInt() shl 8)).toDouble()
        }

//        Log.v("bw_fitler_out", "${bwFilter.filter(-12.0)}")
        for (i in 0 until inputSamples.size) {
            inputSamples[i] = bwFilter.filter(inputSamples[i])
            if (abs(inputSamples[i]) > 1000.0) {
                cut_index = i
            }
        }

        Log.v("cutindex", "$cut_index")
        cut_index = cut_index?.plus(320)
        Log.v("shorttrans", "${inputSamples[len / 2 - 1]}, ${inputSamples[len / 2 - 2]}, ${inputSamples[len / 2 - 3]} ")

        if (cut_index == null) {
            cut_index = 11000
        }
        inputSamples = inputSamples.sliceArray(IntRange(cut_index, cut_index + echoLength - 1))
        var floatInput = FloatArray(echoLength * 2)
        for (i in 0 until echoLength) {
            floatInput[i] = inputSamples[i].toFloat()
        }
        for (i in 0 until echoLength) {
            floatInput[i + echoLength] = i.toFloat()
        }
        //  batch size should be 1
        Log.v("sampleslength", "${floatInput.size}")
        var inputTensor = Tensor.fromBlob(floatInput, longArrayOf(1, (floatInput.size / 2).toLong(), 2))
        Log.v("TensorShape", "${inputTensor?.shape()?.contentToString()}")
        Log.v("TensorData", "${inputTensor?.dataAsFloatArray?.contentToString()}")
        Log.v("FloatData", "${floatInput[1500]}")
        // TODO: Fix model input bug
        val outputTensor = module?.forward(IValue.from(inputTensor))?.toTensor()
        Log.v("OutputTensor", "${outputTensor?.dataAsFloatArray?.contentToString()}")
        var outFloatArray = outputTensor?.dataAsFloatArray
        fun <T : Comparable<T>> Iterable<T>.argmax(): Int? {
            return withIndex().maxByOrNull { it.value }?.index
        }
        var maxIndex = outFloatArray?.asList()?.argmax()
        val identifiedPosition = maxIndex?.plus(1)
        Log.v("maxIndex", "$maxIndex")
        return identifiedPosition
    }


    fun pythonInit(applicationContext: Context, inputFile: File, modelPath: String): Int? {
        module = Module.load(modelPath)
        inputData = inputFile.readBytes()
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(applicationContext))
        }
        val py = Python.getInstance()
        val module_py = py.getModule("temp")
        var inputSamples = module_py.callAttr("temp", inputData).toJava(FloatArray::class.java)

        Log.v("python", "python res: ${inputSamples.size}")

        var cut_index: Int? = null
        for (i in 0 until inputSamples.size) {
            if (abs(inputSamples[i]) > 1000.0) {
                cut_index = i
            }
        }

        cut_index = cut_index?.plus(320)
//        Log.v("shorttrans", "${inputSamples[len / 2 - 1]}, ${inputSamples[len / 2 - 2]}, ${inputSamples[len / 2 - 3]} ")

        if (cut_index == null) {
            cut_index = 11000
        }

        inputSamples = inputSamples.sliceArray(IntRange(cut_index, cut_index + echoLength - 1))

        var floatInput = FloatArray(echoLength * 2)
        for (i in 0 until echoLength) {
            floatInput[i] = inputSamples[i]
        }
        for (i in 0 until echoLength) {
            floatInput[i + echoLength] = i.toFloat()
        }
        var inputTensor = Tensor.fromBlob(floatInput, longArrayOf(1, (floatInput.size / 2).toLong(), 2))
        val outputTensor = module?.forward(IValue.from(inputTensor))?.toTensor()
        var outFloatArray = outputTensor?.dataAsFloatArray
        fun <T : Comparable<T>> Iterable<T>.argmax(): Int? {
            return withIndex().maxByOrNull { it.value }?.index
        }
        var maxIndex = outFloatArray?.asList()?.argmax()
        val identifiedPosition = maxIndex?.plus(1)
        Log.v("maxIndex", "$maxIndex")
        return identifiedPosition
    }

    fun cloudInfer(applicationContext: Context, inputFile: File) {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(applicationContext))
        }
        val py = Python.getInstance()
        val module_py = py.getModule("temp")
        var inputSamples = module_py.callAttr("cloud_infer", inputData).toJava(Int::class.java)
        Log.v("cloud_infer", "cloud_infer res: ${inputSamples}")
    }


    fun cloudjwt(applicationContext: Context, inputFile: File, jwtFile: File) {
//        val credential = GoogleCredential.fromStream(FileInputStream(jwtFile))
//        val privateKey = credential.serviceAccountPrivateKey
//        val privateKeyId = credential.serviceAccountPrivateKeyId
//
//        val now = System.currentTimeMillis()
//        try {
//            val algorithm = Algorithm.RSA256(null, privateKey as RSAPrivateKey?)
//            val signedJwt: String = JWT.create()
//                .withKeyId(privateKeyId)
//                .withIssuer("test-680@stable-sign-388019.iam.gserviceaccount.com")
//                .withSubject("test-680@stable-sign-388019.iam.gserviceaccount.com")
//                .withAudience("https://firestore.googleapis.com/")
//                .withIssuedAt(Date(now))
//                .withExpiresAt(Date(now + 3600 * 1000L))
//                .sign(algorithm)
//        } catch (e: JWTCreationException) {
//            Log.v("JWT", "JWT error: ${e.message}")
//            //Invalid Signing configuration / Couldn't convert Claims.
//        }
//        val client = PredictionServiceClient.create(
//            PredictionServiceSettings.newBuilder()
//                .setCredentialsProvider { credential }
//                .setEndpoint()
//                .build()
//        )
    }
//    fun model2(applicationContext: Context, inputFile: File, modelPath: String): Int? {
//        module = Module.load(modelPath)
//        inputData = inputFile.readBytes()
//        if (!Python.isStarted()) {
//            Python.start(AndroidPlatform(applicationContext))
//        }
//        val py = Python.getInstance()
//        val module_py = py.getModule("temp")
//        var inputSamples = module_py.callAttr("temp", inputData).toJava(FloatArray::class.java)
//
//        Log.v("python", "python res: ${inputSamples.size}")
//        var inputTensor = Tensor.fromBlob(inputSamples, longArrayOf(1, 333, 501))
////        Log.v()
//        Log.v("TensorShape", "${inputTensor?.shape()?.contentToString()}")
//        Log.v("TensorData", "${inputTensor?.dataAsFloatArray?.contentToString()}")
////        for
//
////        Log.v("TensorData", "${inputTensor?}")
//        val outputTensor = module?.forward(IValue.from(inputTensor))?.toTensor()
//        var outFloatArray = outputTensor?.dataAsFloatArray
//        fun <T : Comparable<T>> Iterable<T>.argmax(): Int? {
//            return withIndex().maxByOrNull { it.value }?.index
//        }
//        var maxIndex = outFloatArray?.asList()?.argmax()
//        val identifiedPosition = maxIndex?.plus(5)
//        Log.v("maxIndex", "$maxIndex")
//        return identifiedPosition
//    }



}