package com.example.sps_21.sensorfusion
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import com.google.gson.Gson
import java.io.File

class WifiCollector(context: Context) {
    private val wifiManager: WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val applicationContext = context
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
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        val results = wifiManager.scanResults
//        ... potentially use older scan results ...
    }

    fun startProgram(saveFile: File) {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        applicationContext.registerReceiver(wifiScanReceiver, intentFilter)

        val success = wifiManager.startScan()
        if (!success) {
            // scan failure handling
            scanFailure()
        }
        else {
            val scanResults = wifiManager.scanResults
            val strBuilder = StringBuilder()
            for (scanResult in scanResults) {
                val ssid = scanResult.SSID
                val bssid = scanResult.BSSID
                val signalStrength = scanResult.level
//            resString += "$ssid,$bssid,$signalStrength\n"
                strBuilder.appendLine("$ssid,$bssid,$signalStrength")
            }
//        val outJson = Gson().toJson(strBuilder.toString())
            saveFile.writeText(strBuilder.toString())
        }

    }

    fun getWifiInfo(saveFile: File) {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }
//        val intentFilter = IntentFilter()
//        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
//        applicationContext.registerReceiver(WifiReceiver(), intentFilter)
        wifiManager.startScan()
        val scanResults = wifiManager.scanResults
        var resString = ""
        val strBuilder = StringBuilder()
        for (scanResult in scanResults) {
            val ssid = scanResult.SSID
            val bssid = scanResult.BSSID
            val signalStrength = scanResult.level
//            resString += "$ssid,$bssid,$signalStrength\n"
            strBuilder.appendLine("$ssid,$bssid,$signalStrength")
        }
//        val outJson = Gson().toJson(strBuilder.toString())
        saveFile.writeText(strBuilder.toString())
    }

    fun inference() {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }

        val delim = ":"
        val data = IntArray(10000);
        wifiManager.startScan()
        val scanResults = wifiManager.scanResults
        for (scanResult in scanResults) {
            val bssid = scanResult.BSSID
            val signalStrength = scanResult.level
            val decimal = (bssid.toLong(16) % 10000).toInt()
            data[decimal] = signalStrength
        }


    }
}



//    fun getWifiScanResults(): List<ScanResult> {
//        if (ActivityCompat.checkSelfPermission(
//                applicationContext,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//
//        }
//
//
//        // Ensure that Wi-Fi is enabled
//        if (!wifiManager.isWifiEnabled) {
//            wifiManager.isWifiEnabled = true
//        }
//
//        // Start a Wi-Fi scan
//        wifiManager.startScan()
////        wifiManager.
//        // Retrieve the scan results
//        return wifiManager.scanResults
//    }

//    fun getWifiSignalStrength(): Int {
//        // Get the Wi-Fi info
//        val wifiInfo: WifiInfo = wifiManager.connectionInfo
//
//        // Calculate the signal strength in dBm
//        return wifiInfo.rssi
//    }