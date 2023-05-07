package com.example.sps_21.page

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.sps_21.Player
import com.example.sps_21.Recorder
import com.example.sps_21.SignalProcessing
import com.example.sps_21.ui.theme.Red600
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun HomePageView(applicationContext: Context) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val imageBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val curContext = applicationContext

    val recorder by lazy {
        Recorder(applicationContext)
    }
    val player by lazy {
        Player(applicationContext)
    }

    fun loadImage() {
        val bitmap = BitmapFactory.decodeFile(File(curContext.cacheDir, "spectrum.png").absolutePath)
        imageBitmap.value = bitmap
    }

    fun generateSpectrum() {
        var spectrum = File(curContext.cacheDir.absolutePath, "spectrum.png")
        var pcm = File(curContext.cacheDir.absolutePath, "recording12.pcm")
        //var filepath = pcm.absolutePath;
        //println("file path:,$filepath")
        SignalProcessing.pcmToSpectrum(pcm, spectrum)
    }

    Scaffold(
        scaffoldState = scaffoldState,

    ) { innerPadding ->
        Column(
            modifier = Modifier
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
                player.playChirp()
                recorder.createRecorder()
                File(curContext.cacheDir, "recording12.pcm").also {
                    recorder.startRecord(it)
                }

            }) {
                Text(text = "Start Program")
            }
            Button(onClick = {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(
                        "spectrum",
                        duration = SnackbarDuration.Short
                    )
                }
                generateSpectrum()
                loadImage()
            }) {
                Text(text = "Generate spectrum")
            }
            Button(
                onClick = {
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(
                            "Image deleted",
                            duration = SnackbarDuration.Short
                        )
                    }
                    var spectrum = File(curContext.cacheDir.absolutePath, "spectrum.png")
                    spectrum.delete()
                    imageBitmap.value = null
                },
                enabled = imageBitmap.value != null
            ) {
                Text(text = "Delete Image")
            }
            Spacer(modifier = Modifier.padding(10.dp))

            imageBitmap.value?.let {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Spectrum",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                )
            }

        }
    }
}