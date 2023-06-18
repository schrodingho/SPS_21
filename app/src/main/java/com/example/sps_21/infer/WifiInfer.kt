package com.example.sps_21.infer
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

import android.content.pm.PackageManager
import android.net.wifi.ScanResult

import android.net.wifi.WifiManager

import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

import org.pytorch.Module
import org.pytorch.Tensor

import org.pytorch.IValue
import java.io.File

class WifiInfer(context: Context, modelPath: String) {
    private var module: Module? = Module.load(modelPath)
    private val wifiManager: WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val applicationContext = context
    private var outputArray: FloatArray? = null
    val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                scanSuccess()
            } else {
                scanFailure()
            }
        }
    }
    private fun scanSuccess() {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }
        val results = wifiManager.scanResults
//        ... use new scan results ...
    }

    private fun scanFailure() {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }
        val results = wifiManager.scanResults
    }

    fun wifialways(): List<ScanResult>? {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }
//        val intentFilter = IntentFilter()
//        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
//        applicationContext.registerReceiver(wifiScanReceiver, intentFilter)

        wifiManager.startScan()
//        if (success) {
        val results = wifiManager.scanResults
        return results
//        } else {
//            return null
//        }

    }

    fun inference2(scanResults: List<ScanResult>?): FloatArray? {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }
        val delim = ":"

        val data = IntArray(10000);
//        wifiManager.startScan()
//        val scanResults2 = wifiManager.scanResults

//        wifiManager.startScan()
//        val scanResults = wifiManager.scanResults

        for (scanResult in scanResults!!) {
            val bssid = scanResult.BSSID
            val signalStrength = scanResult.level
            val hex_id = bssid.replace(delim, "")
            val decimal = (hex_id.toLong(16) % 10000).toInt()
            data[decimal] = signalStrength
        }

        var inputTensor = Tensor.fromBlob(data, longArrayOf(1, 100, 100))
        var outputTensor = module!!.forward(IValue.from(inputTensor)).toTensor()
        outputArray = outputTensor.dataAsFloatArray
//        fun <T : Comparable<T>> Iterable<T>.argmax(): Int? {
//            return withIndex().maxByOrNull { it.value }?.index
//        }
//
//        var maxIndex = outputArray?.asList()?.argmax()
//        val identifiedPosition = maxIndex?.plus(1)
        return outputArray!!
    }

    fun inference(): Int? {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }
        val delim = ":"

        val data = IntArray(10000);
//        wifiManager.startScan()
//        val scanResults2 = wifiManager.scanResults

        wifiManager.startScan()
        val scanResults = wifiManager.scanResults

        for (scanResult in scanResults) {
            val bssid = scanResult.BSSID
            val signalStrength = scanResult.level
            val hex_id = bssid.replace(delim, "")
            val decimal = (hex_id.toLong(16) % 10000).toInt()
            data[decimal] = signalStrength
        }

        var inputTensor = Tensor.fromBlob(data, longArrayOf(1, 100, 100))
        var outputTensor = module!!.forward(IValue.from(inputTensor)).toTensor()
        val outputArray = outputTensor.dataAsFloatArray
        fun <T : Comparable<T>> Iterable<T>.argmax(): Int? {
            return withIndex().maxByOrNull { it.value }?.index
        }
        var maxIndex = outputArray?.asList()?.argmax()
        val identifiedPosition = maxIndex?.plus(1)
        return identifiedPosition
    }

//    fun temp_inference(Inputfile: File): Int? {
//        if (ActivityCompat.checkSelfPermission(
//                applicationContext,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//
//        }
//        val delim = ":"
//        val data = IntArray(10000);
//        wifiManager.startScan()
//        val scanResults = wifiManager.scanResults
//        for (scanResult in scanResults) {
//            val bssid = scanResult.BSSID
//            val signalStrength = scanResult.level
//            val decimal = (bssid.toLong(16) % 10000).toInt()
//            data[decimal] = signalStrength
//        }
//        var inputTensor = Tensor.fromBlob(data, longArrayOf(100, 100))
//        var outputTensor = module!!.forward(IValue.from(inputTensor)).toTensor()
//        val outputArray = outputTensor.dataAsIntArray
//        fun <T : Comparable<T>> Iterable<T>.argmax(): Int? {
//            return withIndex().maxByOrNull { it.value }?.index
//        }
//        var maxIndex = outputArray?.asList()?.argmax()
//        val identifiedPosition = maxIndex?.plus(1)
//        return identifiedPosition
//    }

}




