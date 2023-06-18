package com.example.sps_21.page

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.sps_21.Player
import com.example.sps_21.R
import com.example.sps_21.Recorder
import com.example.sps_21.SignalProcessing
import com.example.sps_21.database.FileDao
import com.example.sps_21.database.FilesDatabase
import com.example.sps_21.infer.Spectrogram
import com.example.sps_21.infer.Transformer
import com.example.sps_21.infer.WifiInfer
import com.example.sps_21.sensorfusion.WifiCollector
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun HomePageView(applicationContext: Context) {
    val scaffoldState = rememberScaffoldState()
    // TODO: New notificaiton system needs be added
    // TODO: switch view will clear the image, need to fix
    val imageBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val pcmState = remember { mutableStateOf<File?>(null) }

    var editRoom = remember { mutableStateOf("") }
    var autoState = remember { mutableStateOf(false) }
    var classResult = remember { mutableStateOf<String?>(null) }
    classResult.value = "None"
    val allclass = remember {
        mutableStateOf<FloatArray?>(null)
    }
    val buttonCLicked = remember {
        mutableStateOf(false)
    }
    val buttonCLicked2 = remember {
        mutableStateOf(false)
    }
    val coroutine = rememberCoroutineScope()
    val coroutine2 = rememberCoroutineScope()
    val startButton = remember { mutableStateOf(false) }
    val inferButton = remember { mutableStateOf(false) }
    val wifiButton = remember { mutableStateOf(true) }

    val wifiResult = remember { mutableStateOf<String?>(null) }
    val wifiOn = remember {
        mutableStateOf(true)
    }
    val wifiReady = remember {
        mutableStateOf<Int>(0)
    }

    var top3Cell = remember {
        mutableStateOf<List<Int>?>(null)
    }

    val startButton_2 = remember {
        mutableStateOf(false)
    }
//    var final_result: FloatArray? = null

    val curContext = applicationContext
    var currentTimestamp = System.currentTimeMillis()

    var pcmName = ""
    var spectrumName = ""

//    var localPath = curContext.getExternalFilesDir(null)?.absolutePath


    val exFolderPath = curContext.getExternalFilesDir(null)?.absolutePath
    val wifiFolder = File(exFolderPath, "WifiData")
    wifiFolder.mkdir()
    val wifiFilePath = wifiFolder.absolutePath

    val audioFolder = File(exFolderPath, "AudioData")
    audioFolder.mkdir()
    val localPath = audioFolder.absolutePath

    val recorder by lazy {
        Recorder(applicationContext)
    }
    val player by lazy {
        Player(applicationContext)
    }
    val moduleFileAbsoluteFilePath = File(assetFilePath(applicationContext, "model_m20.ptl")).absolutePath

    val inferModel by lazy {
        Transformer()
    }
    val wifiCollector by lazy {
        WifiCollector(applicationContext)
    }
    val wifimodel = File(assetFilePath(applicationContext, "model_m_wifi.pt")).absolutePath

    val wifiInfer by lazy {
        WifiInfer(applicationContext, wifimodel)
    }

//    val database = FilesDatabase.getInstance(applicationContext)
//    var fileDao: FileDao = database!!.fileDao()
    val spect = Spectrogram()



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

//    val cacheFilePath = File(curContext.cacheDir, "recording_temp8.pcm")
//                    inferModel.pythonInit(applicationContext, cacheFilePath)

    fun generateSpectrogram(pcmName: String, spectrumName: String) {
        var spectrogram = File(curContext.cacheDir.absolutePath, spectrumName)
        var pcm = File(curContext.cacheDir.absolutePath, pcmName)
        val get = spect.trans(pcm, spectrogram)
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
                .padding(10.dp)
                .wrapContentSize(
                    Alignment.Center
                )){
                Button(onClick = {
                    buttonCLicked.value = true
                     },
                    enabled = editRoom.value != "",

                )
                {
                    Text(text = "TrainC")
                }
                LaunchedEffect(buttonCLicked.value) {
                    if (buttonCLicked.value) {
                        autoState.value = true
                        player.createPlayer()
                        for (i in 0 until 100) {
                            coroutine.launch {
                                currentTimestamp = System.currentTimeMillis()
                                pcmName = "recording_${currentTimestamp}_${editRoom.value}.pcm"
                                val localFile = File(localPath, pcmName)
                                recorder.createRecorder(localFile)
                                recorder.createRecordThread()
                                recorder.startRecord()
//                                player.playChirp()
                                player.playManyTimes()
                            }

                            delay(600L) // Delay for 1 second between each button click
                            buttonCLicked.value = true // Trigger the button click again
                        }
                    }
                    buttonCLicked.value = false
//                    recorder.stopRecord()
                }
                Button(onClick = {
                    buttonCLicked2.value = true
                },
                    enabled = editRoom.value != ""
                ){
                    Text(text = "TestC")
                }
                LaunchedEffect(buttonCLicked2.value) {
                    if (buttonCLicked2.value) {
                        autoState.value = true
                        player.createPlayer()
//                        for (i in 0 until 1) {
//                            coroutine.launch {
                                currentTimestamp = System.currentTimeMillis()
                                pcmName = "recording_${currentTimestamp}_${editRoom.value}.pcm"
                                val localFile = File(localPath, pcmName)
                                recorder.createRecorder(localFile)
                                recorder.createRecordThread()
                                recorder.startRecord()
//                                player.playChirp()
                                player.playManyTimes()
//                            }

                            delay(600L) // Delay for 1 second between each button click
                            buttonCLicked2.value = true // Trigger the button click again
//                        }
                    }
                    buttonCLicked2.value = false
//                    recorder.stopRecord()
                }

                Button(onClick = {
                    coroutine.launch(newSingleThreadContext("WifiThread")) {
                        wifiButton.value = false
                        for (i in 0 until 30 ) {
                            val curTime = System.currentTimeMillis()
                            wifiCollector.startProgram(File(wifiFilePath, "wifiInfo_${curTime}_${editRoom.value}.txt"))
                            delay(7000L)
                        }
                        wifiButton.value = true
                    }
                },
                    enabled = editRoom.value != "" && wifiButton.value
                )
                {
                    Text(text = "WifiC")
                }


                TextField(
                    modifier = Modifier
                        .width(70.dp),
                    value = editRoom.value,
                    onValueChange = { editRoom.value = it },
                    label = { Text(text = "num") },
                    maxLines = 1
                )
            }

            Row (modifier = Modifier
                .padding(vertical = 10.dp)
                .wrapContentSize(
                    Alignment.Center
                )) {
                Button(
                    modifier = Modifier
                        .width(80.dp)
                        .padding(horizontal = 1.dp),
                    onClick = {
                        pcmName = "recording_temp.pcm"
                        classResult.value = "None"
                        startButton.value = true
                        top3Cell.value = null
                        imageBitmap.value = null
                    },
                    enabled = !autoState.value && !startButton_2.value
                ) {
                    Text(text = "Start1")
                }
                LaunchedEffect(startButton.value) {
                    player.createPlayer()
                    if (startButton.value) {
                        val cacheFilePath = File(curContext.cacheDir, "recording_temp.pcm")
                        pcmState.value = cacheFilePath
                        recorder.createRecorder(cacheFilePath)
                        recorder.createRecordThread(10)
                        recorder.startRecord()
                        player.playManyTimes()

                        delay(600L) // Delay for 1 second between each button click
                        startButton.value = true // Trigger the button click again
                        inferButton.value = true
                    }
                    if (inferButton.value) {
//                    coroutine.launch(newSingleThreadContext("InferenceThread")) {
                        val cacheFilePath = File(curContext.cacheDir, "recording_temp.pcm")
                        allclass.value = inferModel.localInfer(cacheFilePath, moduleFileAbsoluteFilePath)
                        fun <T : Comparable<T>> Iterable<T>.argmax(): Int? {
                            return withIndex().maxByOrNull { it.value }?.index
                        }
                        fun <T : Comparable<T>> Iterable<T>.argmax_n(n: Int): List<Int> {
                            return withIndex().sortedByDescending { it.value }.take(n).map { it.index }
                        }
                        top3Cell.value = allclass.value?.asList()?.argmax_n(3)
                        var maxIndex = allclass.value?.asList()?.argmax()
                        classResult.value = maxIndex?.plus(1).toString()
//                    }
                    }
                    startButton.value = false
                }


                Button(
                    modifier = Modifier
                        .width(80.dp)
                        .padding(horizontal = 1.dp),
                    onClick = {
                        pcmName = "recording_temp.pcm"
                        classResult.value = "None"
                        startButton_2.value = true
                        top3Cell.value = null
                        imageBitmap.value = null
                    },
                    enabled = !autoState.value && !startButton.value
                ) {
                    Text(text = "Start2")
                }
            LaunchedEffect(startButton_2.value) {
                player.createPlayer()
                if (startButton_2.value) {
                    val wifi_result_1 = wifiInfer.wifialways()
                    val cacheFilePath = File(curContext.cacheDir, "recording_temp.pcm")
                    pcmState.value = cacheFilePath
                    recorder.createRecorder(cacheFilePath)
                    recorder.createRecordThread(10)
                    recorder.startRecord()
                    player.playManyTimes()

                    delay(600L) // Delay for 1 second between each button click
                    startButton_2.value = true // Trigger the button click again
                    inferButton.value = true

                    if (inferButton.value) {
                        val cacheFilePath = File(curContext.cacheDir, "recording_temp.pcm")
                        allclass.value = inferModel.localInfer(cacheFilePath, moduleFileAbsoluteFilePath)
                    }
//                delay(1000L)
                    delay(6000L)
                    val wifi_result_2 = wifiInfer.wifialways()
                    if (wifi_result_1 != wifi_result_2) {
                        Log.v("wifi", "wifi changed")
                    }

                    if (inferButton.value && wifi_result_1 != wifi_result_2) {
                        var wifiModelOut = wifiInfer.inference2(wifi_result_2)
                        var dotProduct = 0.0
                        var normA = 0.0
                        var normB = 0.0
                        for (i in 0 until 16) {
                            dotProduct += allclass.value!![i] * wifiModelOut!![i]
                            normA += Math.pow(allclass.value!![i].toDouble(), 2.0)
                            normB += Math.pow(wifiModelOut[i].toDouble(), 2.0)
                        }
                        val alpha = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB))

                        var final_result: FloatArray = FloatArray(16)
                        for (i in 0 until 16) {
                            final_result[i] = alpha.toFloat() * allclass.value!![i] + (1 - alpha.toFloat()) * wifiModelOut!![i]
//                            allclass.value!![i] = allclass.value!![i] / (1 + alpha)
                        }


                        fun <T : Comparable<T>> Iterable<T>.argmax(): Int? {
                            return withIndex().maxByOrNull { it.value }?.index
                        }
                        fun <T : Comparable<T>> Iterable<T>.argmax_n(n: Int): List<Int> {
                            return withIndex().sortedByDescending { it.value }.take(n).map { it.index }
                        }
                        top3Cell.value = final_result.asList().argmax_n(3)
//                        var maxIndex = allclass.value?.asList()?.argmax()
                        var maxIndex = final_result.asList().argmax()
                        for (i in 0 until 16) {
                            allclass.value!![i] = final_result[i]
                        }

                        classResult.value = maxIndex?.plus(1).toString()
                    }
                    startButton_2.value = false
                }
            }


                Button(
                    modifier = Modifier
                        .width(80.dp)
                        .padding(horizontal = 1.dp),
                    onClick = {
//                    generateSpectrum(pcmName, spectrumName)
                        spectrumName = "spectrum_temp.png"
                        pcmName = "recording_temp.pcm"
                        generateSpectrogram(pcmName, spectrumName)
                        loadImage(spectrumName)
                    },
                    enabled = pcmState.value != null  && !autoState.value && !startButton.value && !startButton_2.value
                ) {
                    Text(text = "Spect")
                }

//          Inference Part
//            Button(
//                modifier = Modifier.width(80.dp),
//                enabled = pcmState.value != null,
//                onClick = {
//                    classResult.value = "None"
////                    inferButton.value = true
//                    coroutine.launch(newSingleThreadContext("InferenceThread")) {
//                        val cacheFilePath = File(curContext.cacheDir, "recording_temp.pcm")
//                        classResult.value = inferModel.localInfer(cacheFilePath, moduleFileAbsoluteFilePath).toString()
//                    }
//                }) {
//                Text(text = "Inference")
//            }

                Button(
                    modifier = Modifier
                        .width(85.dp)
                        .padding(horizontal = 1.dp),
                    onClick = {
                        spectrumName = "spectrum_temp.png"
                        var spectrum = File(curContext.cacheDir.absolutePath, spectrumName)
                        spectrum.delete()
                        imageBitmap.value = null
                    },
                    enabled = !autoState.value && imageBitmap.value != null
                ) {
                    Text(text = "Delete")
                }

            }
            // background wifi
//            LaunchedEffect(wifiOn.value) {
//                coroutine2.launch {
//                    while (wifiOn.value) {
//
//                        val results = wifiInfer.wifialways()
//                        delay(1000L)
//                        var result = wifiInfer.inference2(results)
//
//                        fun <T : Comparable<T>> Iterable<T>.argmax(): Int? {
//                                return withIndex().maxByOrNull { it.value }?.index
//                        }
//                        var maxIndex = result?.asList()?.argmax()
//                        wifiResult.value = maxIndex?.plus(1).toString()
//                        wifiReady.value += 1
//                    }
//                }
//            }


//            Spacer(modifier = Modifier.padding(10.dp))


            Spacer(modifier = Modifier.padding(2.dp))

            classResult.value?.let {
                Text(text = "Cell $it", fontSize = 15.sp, fontStyle = FontStyle.Italic)
                top3Cell.value?.let {
                    showTopN(topIndices = top3Cell.value!!)
                }
//                Text(text = "${allclass.value?.asList()}", fontSize = 10.sp, fontStyle = FontStyle.Italic)
            }

//            wifiResult.value?.let {
//                Text(text = "Room Number(Wifi): $it", fontSize = 15.sp, fontStyle = FontStyle.Italic)
//            }
//
//
//            Text(text = "Wifi Ready: ${wifiReady.value}", fontSize = 15.sp, fontStyle = FontStyle.Italic)
//            drawMaps()
//
            Column(modifier = Modifier.padding(1.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {

                Image(painter = painterResource(id = R.drawable.layout_new), contentDescription = "layout", modifier = Modifier
                    .size(200.dp)
                    .padding(1.dp)

                )
                
                Spacer(modifier = Modifier.padding(1.dp))
//                Spacer(modifier = Modifier.weight(1f))
                imageBitmap.value?.let {
                    Image(
                        painter = rememberImagePainter(it),
                        contentDescription = "Spectrum",
                        modifier = Modifier
                            .size(150.dp)
                            .padding(1.dp)
                            .border(BorderStroke(1.dp, androidx.compose.ui.graphics.Color.Black))
//                        .size(100.dp)
                    )
                    Text(text = "Spectrum", fontSize = 13.sp, fontStyle = FontStyle.Italic)
                }
            }

        }
    }
}
@Composable
fun showTopN(topIndices: List<Int>) {
    Row (Modifier.padding(1.dp)) {
        Text(text = "Top 3: ")
        for (i in 0 until topIndices.size) {
            if (i == topIndices.size - 1) {
                Text(text = "C" + topIndices[i].plus(1).toString())
            } else {
                Text(text = "C" + topIndices[i].plus(1).toString()+", ")
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

@Composable
fun TwoTexts(
    text1: String,
    text2: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(top = 4.dp)
                .wrapContentHeight(Alignment.CenterVertically),
            text = text1
        )

        Divider(
            color = androidx.compose.ui.graphics.Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .width(1.dp)
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 4.dp)
                .wrapContentHeight(Alignment.Bottom),
            text = text2
        )
    }
}

//@OptIn(ExperimentalTextApi::class)
//@Composable
//fun drawMaps(modifier: Modifier = Modifier) {
//    val scale = 30
//    val y_offset = 30f
//    val textMeasurer = rememberTextMeasurer()
//    Box(modifier = Modifier.fillMaxWidth().wrapContentWidth()) {
//        Canvas(modifier = Modifier
//            .fillMaxWidth()
//        ) {
//            val canvasWidth = size.width
//            val canvasHeight = size.height
//            // west: C1 - C3
//            drawLine(
//                start = Offset(x = 0f, y = (1.56f) * scale),
//                end = Offset(x = 4.80f * 3 * scale, y = (1.56f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = 0f, y = (1.56f) * scale),
//                end = Offset(x = 0f, y = (1.56f + 2.54f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = 0f, y = (1.56f + 2.54f) * scale),
//                end = Offset(x = 4.80f * 3 * scale, y = (1.56f + 2.54f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = 4.80f * 1 * scale, y = (1.56f) * scale),
//                end = Offset(x = 4.80f * 1 * scale, y = (1.56f + 2.54f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = 4.80f * 2 * scale, y = (1.56f) * scale),
//                end = Offset(x = 4.80f * 2 * scale, y = (1.56f + 2.54f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = 4.80f * 3 * scale, y = (1.56f) * scale),
//                end = Offset(x = 4.80f * 3 * scale, y = (1.56f + 2.54f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            // C8 - C9
//            drawLine(
//                start = Offset(x = 4.80f * 3 * scale, y = (1.56f) * scale),
//                end = Offset(x = 4.80f * 3 * scale, y = 0f),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = 4.80f * 3 * scale, y = 0f),
//                end = Offset(x = ((4.80f) * 3 +3.57f) * scale , y = 0f),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = ((4.80f) * 3 +3.57f) * scale, y = 0f),
//                end = Offset(x = ((4.80f) * 3 +3.57f) * scale, y = (3.26f + 2.75f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = 4.80f * 3 * scale, y = (3.26f + 2.75f) * scale),
//                end = Offset(x = ((4.80f) * 3 + 3.57f) * scale, y = (3.26f + 2.75f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = 4.80f * 3 * scale, y = (3.26f + 2.75f) * scale),
//                end = Offset(x = 4.80f * 3 * scale, y = (2.54f + 1.56f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = 4.80f * 3 * scale, y = (3.26f) * scale),
//                end = Offset(x = ((4.80f) * 3 + 3.57f) * scale, y = (3.26f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//
//            // C10
//            drawLine(
//                start = Offset(x = 4.80f * 3 * scale, y = (3.26f + 2.75f) * scale),
//                end = Offset(x = 4.80f * 3 * scale, y = (3.26f + 2.75f + 3.81f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = (4.80f * 3 + 1.24f) * scale, y = (3.26f + 2.75f) * scale),
//                end = Offset(x = (4.80f * 3 + 1.24f) * scale, y = (3.26f + 2.75f + 3.81f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = (4.80f * 3 + 1.24f) * scale, y = (3.26f + 2.75f + 3.81f) * scale),
//                end = Offset(x = (4.80f * 3) * scale, y = (3.26f + 2.75f + 3.81f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//
//
//            // C7
//            drawLine(
//                start = Offset(x = (4.80f * 2) * scale, y = (2.54f + 1.56f) * scale),
//                end = Offset(x = (4.80f * 2) * scale, y = (2.54f + 1.56f + 1.79f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = (4.80f * 2) * scale, y = (2.54f + 1.56f + 1.79f) * scale),
//                end = Offset(x = (4.80f * 3) * scale, y = (2.54f + 1.56f + 1.79f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = (4.80f * 3) * scale, y = (2.54f + 1.56f) * scale),
//                end = Offset(x = (4.80f * 3) * scale, y = (2.54f + 1.56f + 1.79f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            //
//
//            // C4 - C6
//            drawLine(
//                start = Offset(x = (4.80f * 1 + 1.25f) * scale, y = (2.54f + 1.56f) * scale),
//                end = Offset(x = (4.80f * 1 + 1.25f) * scale, y = (2.54f + 1.56f + 4.30f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = (4.80f * 1 + 1.25f) * scale, y = (2.54f + 1.56f + 4.30f) * scale),
//                end = Offset(x = (4.80f * 1 + 1.25f + 2.30f) * scale, y = (2.54f + 1.56f + 4.30f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = (4.80f * 1 + 1.25f + 2.30f) * scale, y = (2.54f + 1.56f) * scale),
//                end = Offset(x = (4.80f * 1 + 1.25f + 2.30f) * scale, y = (2.54f + 1.56f + 4.30f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            //
//            drawText(textMeasurer, "C1", topLeft = Offset(x = (4.80f * 3 * 1 / 6 * scale), y = (2.54f / 3 + 1.56f) * scale))
//            drawText(textMeasurer, "C2", topLeft = Offset(x = (4.80f * 3 * 2 / 3 * 3 / 4 * scale), y = (2.54f / 3 + 1.56f) * scale))
//            drawText(textMeasurer, "C3", topLeft = Offset(x = (4.80f * 3 * 3 / 3 * 5 / 6 * scale), y = (2.54f / 3 + 1.56f) * scale))
//            drawText(textMeasurer, "C8", topLeft = Offset(x = (4.80f * 3 * scale) * 10 / 9, y = (3.26f / 2) * scale))
//            drawText(textMeasurer, "C9", topLeft = Offset(x = (4.80f * 3 * scale) * 10 / 9, y = ((3.26f + 2.54f) / 3 + 1.56f) * scale))
//            drawText(textMeasurer, "C10", topLeft = Offset(x = (4.80f * 3 * scale + 0.5f), y = (3.26f + 2.54f + 3.81f / 3) * scale))
//
////
////        //east
////        //
//
//
//            //C11
//            drawLine(
//                start = Offset(x = 4.80f * 3 * scale, y = (3.26f + 2.75f) * scale + y_offset - (3.26f + 2.75f + 3.81f) * scale),
//                end = Offset(x = 4.80f * 3 * scale, y = (3.26f + 2.75f + 3.81f) * scale + y_offset - (3.26f + 2.75f + 3.81f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = (4.80f * 3 + 1.24f) * scale, y = (3.26f + 2.75f) * scale + y_offset - (3.26f + 2.75f + 3.81f) * scale),
//                end = Offset(x = (4.80f * 3 + 1.24f) * scale, y = (3.26f + 2.75f + 3.81f) * scale + y_offset - (3.26f + 2.75f + 3.81f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = (4.80f * 3 + 1.24f) * scale, y = (3.26f + 2.75f + 3.81f) * scale + y_offset - (3.26f + 2.75f + 3.81f + 3.81f) * scale),
//                end = Offset(x = (4.80f * 3) * scale, y = (3.26f + 2.75f + 3.81f) * scale + y_offset - (3.26f + 2.75f + 3.81f + 3.81f) * scale),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            //
//
//
//            drawLine(
//                start = Offset(x = 0f, y = (1.56f) * scale + y_offset),
//                end = Offset(x = 4.80f * 3 * scale, y = (1.56f) * scale + y_offset),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = 0f, y = (1.56f) * scale + y_offset),
//                end = Offset(x = 0f, y = (1.56f + 2.54f) * scale + y_offset),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = 0f, y = (1.56f + 2.54f) * scale + y_offset),
//                end = Offset(x = 4.80f * 3 * scale, y = (1.56f + 2.54f) * scale + y_offset),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = 4.80f * 1 * scale, y = (1.56f) * scale + y_offset),
//                end = Offset(x = 4.80f * 1 * scale, y = (1.56f + 2.54f) * scale + y_offset),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = 4.80f * 2 * scale, y = (1.56f) * scale + y_offset),
//                end = Offset(x = 4.80f * 2 * scale, y = (1.56f + 2.54f) * scale + y_offset),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = 4.80f * 3 * scale, y = (1.56f) * scale + y_offset),
//                end = Offset(x = 4.80f * 3 * scale, y = (1.56f + 2.54f) * scale + y_offset),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            // C8 - C9
//            drawLine(
//                start = Offset(x = 4.80f * 3 * scale, y = (1.56f) * scale + y_offset),
//                end = Offset(x = 4.80f * 3 * scale, y = 0f + y_offset),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = 4.80f * 3 * scale, y = 0f + y_offset),
//                end = Offset(x = ((4.80f) * 3 +3.57f) * scale , y = 0f + y_offset),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = ((4.80f) * 3 +3.57f) * scale, y = 0f + y_offset),
//                end = Offset(x = ((4.80f) * 3 +3.57f) * scale, y = (3.26f + 2.75f) * scale + y_offset),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = 4.80f * 3 * scale, y = (3.26f + 2.75f) * scale + y_offset),
//                end = Offset(x = ((4.80f) * 3 + 3.57f) * scale, y = (3.26f + 2.75f) * scale + y_offset),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = 4.80f * 3 * scale, y = (3.26f + 2.75f) * scale + y_offset),
//                end = Offset(x = 4.80f * 3 * scale, y = (2.54f + 1.56f) * scale + y_offset),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//            drawLine(
//                start = Offset(x = 4.80f * 3 * scale, y = (3.26f) * scale + y_offset),
//                end = Offset(x = ((4.80f) * 3 + 3.57f) * scale, y = (3.26f) * scale + y_offset),
//                color = androidx.compose.ui.graphics.Color.Black
//            )
//
//            // C7
//
//            //
//            // C4 - C6
//            drawText(textMeasurer, "C16", topLeft = Offset(x = (4.80f * 3 * 1 / 6 * scale), y = (2.54f / 3 + 1.56f) * scale + y_offset))
//            drawText(textMeasurer, "C15", topLeft = Offset(x = (4.80f * 3 * 2 / 3 * 3 / 4 * scale), y = (2.54f / 3 + 1.56f) * scale + y_offset))
//            drawText(textMeasurer, "C14", topLeft = Offset(x = (4.80f * 3 * 3 / 3 * 5 / 6 * scale), y = (2.54f / 3 + 1.56f) * scale + y_offset))
//            drawText(textMeasurer, "C12", topLeft = Offset(x = (4.80f * 3 * scale) * 10 / 9, y = (3.26f / 2) * scale + y_offset ))
//            drawText(textMeasurer, "C13", topLeft = Offset(x = (4.80f * 3 * scale) * 10 / 9, y = ((3.26f + 2.54f) / 3 + 1.56f) * scale + y_offset))
//            drawText(textMeasurer, "C11", topLeft = Offset(x = (4.80f * 3 * scale + 0.5f), y = (3.26f + 2.75f + 3.81f) * scale + y_offset - (3.26f + 2.75f + 3.81f + 3.81f / 2) * scale))
//
//        }
//    }
//
//}