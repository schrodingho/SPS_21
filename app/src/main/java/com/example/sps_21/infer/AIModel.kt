package com.example.sps_21.infer

import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor

//https://github.com/StevenJokess/Pytorch-Kotlin-Demo/blob/master/app/src/main/java/com/say/pytorchkotlindemo/ImageClassificationOperation.kt
class AIModel {
    private var module: Module? = null
    private var inputTensor: Tensor? = null
    private var outputTensor: Tensor? = null

    fun loadModel(modelPath: String) {
        module = Module.load(modelPath)
    }

    fun setInputTensor(inputTensor: Tensor) {
        this.inputTensor = inputTensor
    }

    fun runModel(): FloatArray {
        val outputTensor = module?.forward(IValue.from(inputTensor))?.toTensor()
        val scores = outputTensor?.dataAsFloatArray
        return scores!!
    }

}