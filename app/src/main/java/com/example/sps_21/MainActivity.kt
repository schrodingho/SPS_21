package com.example.sps_21

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.BottomAppBar
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.ui.graphics.Color
import com.example.sps_21.ui.theme.Primary200
import com.example.sps_21.ui.theme.Primary700
import com.example.sps_21.ui.theme.SPS_21Theme

class MainActivity : ComponentActivity() {
    val recorder by lazy {
        Recorder(applicationContext)
    }
    val player by lazy {
        Player(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            0
        )

        setContent {
            SPS_21Theme {
                ScaffoldDemo()
            }
        }
    }

    fun generateSpectrum() {
        var spectrum = File(cacheDir.absolutePath, "spectrum.png")
        var pcm = File(cacheDir.absolutePath, "recording12.pcm")
        //var filepath = pcm.absolutePath;
        //println("file path:,$filepath")
        SignalProcessing.pcmToSpectrum(pcm, spectrum)
    }

}


@Composable
fun MainActivity.ScaffoldDemo() {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { TopAppBar(title = {Text("TopAppBar")},backgroundColor = Primary200)  },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
                .wrapContentSize(
                    Alignment.Center
                )
        ) {
            Button(onClick = {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(
                        "Recording & Playing",
                        duration = SnackbarDuration.Short
                    )
                }
                recorder.createRecorder()
                File(cacheDir, "recording12.pcm").also {
                    recorder.startRecord(it)
                }
                player.playChirp()
            }) {
                Text(text = "Start Program")
            }
            Button(onClick = {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(
                        "Recording stopped",
                        duration = SnackbarDuration.Short
                    )
                }
                recorder.stopRecord()
            }) {
                Text(text = "Stop recording")
            }
            Button(onClick = {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(
                        "spectrum",
                        duration = SnackbarDuration.Short
                    )
                }
                generateSpectrum()
            }) {
                Text(text = "Show spectrum")
            }
        }
    }
}
