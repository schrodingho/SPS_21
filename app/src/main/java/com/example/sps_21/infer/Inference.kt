package com.example.sps_21.infer
import android.content.Context
import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File


//https://github.com/StevenJokess/Pytorch-Kotlin-Demo/blob/master/app/src/main/java/com/say/pytorchkotlindemo/ImageClassificationOperation.kt
class Transformer {
    private var module: Module? = null
    private val spectrogram = Spectrogram()
//    private var loadingThread1: Thread? = null
//    private var loadingThread2: Thread? = null
//    private var inferThread: Thread? = null
    private var inputSamples: FloatArray? = null
    private var identifiedPosition: Int? = null
    suspend fun localInfer(inputFile: File, modelPath: String): Int? {
        if (module == null) {
            module = Module.load(modelPath)
        }
        coroutineScope {
            inputSamples = spectrogram.trans(inputFile, null)
//            Log.v("inputSamples", "${inputSamples?.size}")
            var inputTensor = Tensor.fromBlob(inputSamples!!, longArrayOf(1, 333, 513))
            val outputTensor = module?.forward(IValue.from(inputTensor))?.toTensor()

            var outFloatArray = outputTensor?.dataAsFloatArray
//            Log.v("outFloatArray_String", "${outFloatArray.contentToString()}")
            fun <T : Comparable<T>> Iterable<T>.argmax(): Int? {
                return withIndex().maxByOrNull { it.value }?.index
            }

            var maxIndex = outFloatArray?.asList()?.argmax()
            identifiedPosition = maxIndex?.plus(1)
        }
        return identifiedPosition!!



//        Log.v("maxIndex", "$maxIndex")

//        loadingThread1?.start()
//        loadingThread2?.start()
//        loadingThread1?.join()
//        loadingThread2?.join()




//        var t1 = System.currentTimeMillis()
//        module = Module.load(modelPath)
//        var t2 = System.currentTimeMillis()
//        Log.v("Processing Time", "Time: ${t2 - t1}")
//        var t3 = System.currentTimeMillis()




//        var inputTensor = Tensor.fromBlob(inputSamples!!, longArrayOf(1, 333, 513))
//        val outputTensor = module?.forward(IValue.from(inputTensor))?.toTensor()
//
//        var outFloatArray = outputTensor?.dataAsFloatArray
//        fun <T : Comparable<T>> Iterable<T>.argmax(): Int? {
//            return withIndex().maxByOrNull { it.value }?.index
//        }
//        var maxIndex = outFloatArray?.asList()?.argmax()
//        val identifiedPosition = maxIndex?.plus(1)
////        var t4 = System.currentTimeMillis()
////        Log.v("Pytorch Time", "Time: ${t4 - t3}")
////        Log.v("maxIndex", "$maxIndex")
//        return identifiedPosition
    }
}