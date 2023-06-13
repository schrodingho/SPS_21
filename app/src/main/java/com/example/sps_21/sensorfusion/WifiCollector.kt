package com.example.sps_21.sensorfusion
import android.Manifest
import android.content.Context
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

    fun getWifiInfo(saveFile: File) {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }
        wifiManager.startScan()
        val scanResults = wifiManager.scanResults
        var resString = ""
        for (scanResult in scanResults) {
            val ssid = scanResult.SSID
            val bssid = scanResult.BSSID
            val signalStrength = scanResult.level
            resString += "$ssid,$bssid,$signalStrength\n"
        }
        val outJson = Gson().toJson(resString)
        saveFile.writeText(outJson)

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