package com.example.sps_21

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomSheetValue
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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.sps_21.ui.theme.Primary200
import com.example.sps_21.ui.theme.Primary700
import com.example.sps_21.ui.theme.Red600
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
                MainScreen()
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
fun MainActivity.MainScreen() {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val imageBitmap = remember { mutableStateOf<Bitmap?>(null) }

    fun loadImage() {
        val bitmap = BitmapFactory.decodeFile(File(cacheDir, "spectrum.png").absolutePath)
        imageBitmap.value = bitmap
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { TopAppBar(title =
            {
                Text("AudioLoc", fontSize = 25.sp, fontStyle = FontStyle.Italic)
            }, backgroundColor = Red600
        )
     },
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
                loadImage()
            }) {
                Text(text = "Generate spectrum")
            }
            Button(onClick = {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(
                        "Image deleted",
                        duration = SnackbarDuration.Short
                    )
                }
                var spectrum = File(cacheDir.absolutePath, "spectrum.png")
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


@Composable
fun ProfileScreenView() {
    Column {
        Text(text = "Profile Screen")
    }
}

@Composable
fun AboutScreenView() {
    Column {
        Text(text = "Group 21")
        Text(text = "Members: Dinghao Xue & Junyu Lu")
    }
}