package com.example.sps_21.page

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.EditText
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.material.TextField
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
import com.example.sps_21.SpectrumData
import com.example.sps_21.database.FileDao
import com.example.sps_21.database.FilesDatabase
import com.example.sps_21.ui.theme.Red600
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun HomePageView(applicationContext: Context) {
    val scaffoldState = rememberScaffoldState()
    // TODO: New notificaiton system needs be added
    val scope = rememberCoroutineScope()
    // TODO: switch view will clear the image, need to fix
    val imageBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val pcmState = remember { mutableStateOf<File?>(null) }
    val openDialog = remember { mutableStateOf(false) }
    var editText = remember { mutableStateOf("") }

    val curContext = applicationContext
    var currentTimestamp = System.currentTimeMillis()

    var pcmName = ""
    var spectrumName = ""
    val recorder by lazy {
        Recorder(applicationContext)
    }
    val player by lazy {
        Player(applicationContext)
    }


    val database = FilesDatabase.getInstance(applicationContext)
    var fileDao: FileDao = database!!.fileDao()


    fun loadImage(spectrumName: String) {
        val bitmap = BitmapFactory.decodeFile(File(curContext.cacheDir, spectrumName).absolutePath)
        imageBitmap.value = bitmap
    }

    fun generateSpectrum(pcmName: String, spectrumName: String) {
        var spectrum = File(curContext.cacheDir.absolutePath, spectrumName)
        var pcm = File(curContext.cacheDir.absolutePath, pcmName)
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
                .fillMaxWidth()
                .wrapContentSize(
                    Alignment.Center
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.padding(10.dp))
            Button(
                modifier = Modifier.width(200.dp),
                onClick = {
//                scope.launch {
//                    scaffoldState.snackbarHostState.showSnackbar(
//                        "Recording & Playing",
//                        duration = SnackbarDuration.Short
//                    )
//                }
                recorder.createRecorder()
                player.createPlayer()
                currentTimestamp = System.currentTimeMillis()
                pcmName = "recording_$currentTimestamp.pcm"
                spectrumName = "spectrum_$currentTimestamp.png"

                File(curContext.cacheDir, pcmName).also {
                    recorder.startRecord(it)
                    player.playChirp()
                    pcmState.value = it
                }



            }) {
                Text(text = "Start Program")
            }
            Button(
                modifier = Modifier.width(200.dp),
                onClick = {
    //                scope.launch {
    //                    scaffoldState.snackbarHostState.showSnackbar(
    //                        "spectrum",
    //                        duration = SnackbarDuration.Short
    //                    )
    //                }
                    generateSpectrum(pcmName, spectrumName)
                    loadImage(spectrumName)
                },
                enabled = pcmState.value != null
            ) {
                Text(text = "Generate spectrum")
            }

            Button(
                modifier = Modifier.width(200.dp),
                onClick = {
                    openDialog.value = true
                },
                enabled = imageBitmap.value != null
            ) {
                Text(text = "Save Data")
            }

            if (openDialog.value) {
                AlertDialog(
                    onDismissRequest = {openDialog.value = false},
                    title = {
                        Text(text = "Save Data")
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            TextField(
                                value = editText.value,
                                onValueChange = { editText.value = it },
                                label = { Text(text = "Room Number") },
                                maxLines = 1
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                openDialog.value = false
                                var pcmBytes = File(curContext.cacheDir.absolutePath, pcmName).readBytes()
//                                var spectrum = File(curContext.cacheDir.absolutePath, spectrumName)
                                var roomNumber = editText.value
                                val newData = SpectrumData(
                                    pcmBytes = pcmBytes,
                                    locID = roomNumber,
                                )

                                Thread {
                                    fileDao.insertData(newData)
                                }.start()

                            }
                        ){
                            Text(text = "Save")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                openDialog.value = false
                            }
                        ){
                            Text(text = "Cancel")
                        }
                    }
                )
            }

            Button(
                modifier = Modifier.width(200.dp),
                onClick = {
//                    scope.launch {
//                        scaffoldState.snackbarHostState.showSnackbar(
//                            "Image deleted",
//                            duration = SnackbarDuration.Short
//                        )
//                    }
                    var spectrum = File(curContext.cacheDir.absolutePath, spectrumName)
                    spectrum.delete()
                    imageBitmap.value = null
                },
                enabled = imageBitmap.value != null
            ) {
                Text(text = "Delete Image")
            }
            Spacer(modifier = Modifier.padding(8.dp))

            imageBitmap.value?.let {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Spectrum",
                    modifier = Modifier
                        .padding(5.dp)
                )
                Text(text = "Spectrum", fontSize = 20.sp, fontStyle = FontStyle.Italic)

            }




        }
    }
}
