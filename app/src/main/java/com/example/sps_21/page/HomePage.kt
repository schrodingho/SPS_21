package com.example.sps_21.page

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment.getExternalStorageDirectory
import android.util.Log
import android.widget.EditText
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

import com.example.sps_21.infer.Transformer
import java.io.FileOutputStream
import java.io.IOException

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
    var editRoom = remember { mutableStateOf("") }
    var autoState = remember { mutableStateOf(false) }
    var classResult = remember { mutableStateOf<Int?>(null) }
    val buttonCLicked = remember {
        mutableStateOf(false)
    }
    val coroutine = rememberCoroutineScope()


    val curContext = applicationContext
    var currentTimestamp = System.currentTimeMillis()

    var pcmName = ""
    var spectrumName = ""

    var localPath = curContext.getExternalFilesDir(null)?.absolutePath
    val recorder by lazy {
        Recorder(applicationContext)
    }
    val player by lazy {
        Player(applicationContext)
    }

    val inferModel = Transformer();

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
            Row (modifier = Modifier
                .padding(20.dp)
                .wrapContentSize(
                    Alignment.Center
                )){
                Button(onClick = {
                    buttonCLicked.value = true

                     },
                    enabled = editRoom.value != ""
                )
                {
                    Text(text = "Auto Click")
                }
                LaunchedEffect(buttonCLicked.value) {
                    if (buttonCLicked.value) {
                        autoState.value = true
                        player.createPlayer()
                        for (i in 0 until 100) {
                            coroutine.launch {
                                delay(1000L) // Delay for 1 second before each button click
                                currentTimestamp = System.currentTimeMillis()
                                pcmName = "recording_${currentTimestamp}_${editRoom.value}.pcm"
                                val localFile = File(localPath, pcmName)
                                recorder.createRecorder(localFile)
                                recorder.createRecordThread()
                                recorder.startRecord()
//                                player.playChirp()
                                player.playManyTimes()
                            }

                            delay(1000L) // Delay for 1 second between each button click
                            buttonCLicked.value = true // Trigger the button click again
                        }
                    }
                }
                TextField(
                    modifier = Modifier
                        .width(100.dp),
                    value = editRoom.value,
                    onValueChange = { editRoom.value = it },
                    label = { Text(text = "number") },
                    maxLines = 1
                )
            }

//            Spacer(modifier = Modifier.padding(10.dp))
            Button(
                modifier = Modifier.width(200.dp),
                onClick = {
                    val cacheFilePath = File(curContext.cacheDir, "recording_temp.pcm")
                    recorder.createRecorder(cacheFilePath)
                    player.createPlayer()
                    currentTimestamp = System.currentTimeMillis()
                    pcmName = "recording_temp.pcm"
                    spectrumName = "spectrum_temp.png"

                    recorder.createRecordThread()
                    recorder.startRecord()
//                    player.playChirp()
                    player.playManyTimes()


//                    player.playChirp()
                    pcmState.value = cacheFilePath

            },
                enabled = !autoState.value
            ) {
                Text(text = "Start Program")
            }

            Button(
                modifier = Modifier.width(200.dp),
                onClick = {
                    generateSpectrum(pcmName, spectrumName)
                    loadImage(spectrumName)
                },
                enabled = pcmState.value != null
            ) {
                Text(text = "Generate spectrum")
            }

//          Inference Part
            Button(
                modifier = Modifier.width(200.dp),
                enabled = pcmState.value != null,
                onClick = {
                    val cacheFilePath = File(curContext.cacheDir, "recording_temp.pcm")

                    val moduleFileAbsoluteFilePath = File(
                        assetFilePath(applicationContext, "model_1.pt")
                    ).absolutePath
//                    inferModel.loadModel(moduleFileAbsoluteFilePath)
                    classResult.value = inferModel.readData(cacheFilePath, moduleFileAbsoluteFilePath)
                }) {
                Text(text = "Inference")
            }

            Button(
                modifier = Modifier.width(200.dp),
                onClick = {
                    openDialog.value = true
                },
                enabled = !autoState.value && imageBitmap.value != null
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
                                currentTimestamp = System.currentTimeMillis()
                                val file = File(localPath, "pcmbytes_${currentTimestamp}_$roomNumber.pcm")
                                file.writeBytes(pcmBytes)

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
                enabled = !autoState.value && imageBitmap.value != null
            ) {
                Text(text = "Delete Image")
            }

            Spacer(modifier = Modifier.padding(8.dp))

            classResult.value?.let {
                Text(text = "Room Number: $it", fontSize = 20.sp, fontStyle = FontStyle.Italic)
            }
            imageBitmap.value?.let {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Spectrum",
                    modifier = Modifier
                        .padding(3.dp)
                )
                Text(text = "Spectrum", fontSize = 10.sp, fontStyle = FontStyle.Italic)
            }
        }
    }
}


fun assetFilePath(context: Context, assetName: String): String? {
    val file = File(context.filesDir, assetName)

    try {
        context.assets.open(assetName).use { `is` ->
            FileOutputStream(file).use { os ->
                val buffer = ByteArray(4 * 1024)
                while (true) {
                    val length = `is`.read(buffer)
                    if (length <= 0)
                        break
                    os.write(buffer, 0, length)
                }
                os.flush()
                os.close()
            }
            return file.absolutePath
        }
    } catch (e: IOException) {
        Log.e("ModelLoading", "Error process asset $assetName to file path")
    }

    return null
}
