package com.example.sps_21.page

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.FileUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.sps_21.SpectrumData
import com.example.sps_21.database.FileDao
import com.example.sps_21.database.FilesDatabase
import com.example.sps_21.SignalProcessing
import javax.sql.DataSource
import java.io.File



@Composable
fun DataPageView(applicationContext: Context) {

    val database = FilesDatabase.getInstance(applicationContext)
    var fileDao: FileDao = database!!.fileDao()

    var showingData: List<SpectrumData> = listOf()
//    var fromdb = remember { mutableStateOf(showingData) }
    var strstate = remember { mutableStateOf(showingData) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Spacer(modifier = Modifier.padding(15.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(
                    Alignment.Center,
                ),
        ) {
            Button(onClick = {
                Thread {
                    showingData = fileDao.loadAllData()
                    strstate.value = showingData
                }.start()

            }) {
                Text(text = "Load Data")
            }
            Spacer(modifier = Modifier.padding(2.dp))
            Button(onClick = {
                Thread {
                    fileDao.deleteAllData()
                    strstate.value = listOf()
                }.start()
            }) {
                Text(text = "Delete Data")
            }
        }

        DisplayData(spectrums = strstate.value, applicationContext = applicationContext)
//        Text(text = "Last data: ${fromdb.value}")
    }
}


@Composable
fun DisplayData(spectrums: List<SpectrumData>, applicationContext: Context) {
    var showCardDialog = remember {
        mutableStateOf(false)
    }
    var picSource = remember {
        mutableStateOf(ByteArray(0))
    }

    var pcmName = "tempRecording.pcm"
    var spectrumName = "tempSpectrum.png"
    val pcmDir = File(applicationContext.cacheDir.absolutePath, pcmName)
    val spectrumDir = File(applicationContext.cacheDir.absolutePath, spectrumName)
    val imageBitmap = remember { mutableStateOf<Bitmap?>(null) }

    fun loadImage(spectrumName: String) {
        val bitmap = BitmapFactory.decodeFile(File(applicationContext.cacheDir, spectrumName).absolutePath)
        imageBitmap.value = bitmap
    }

    fun generateSpectrum() {
        SignalProcessing.pcmToSpectrum(pcmDir, spectrumDir)
    }


    LazyColumn {
        items(spectrums.size) {
//            Text(text = "UID: ${spectrums[it].id}, RoomNumber: ${spectrums[it].locID}")
            FileCardView(fileName = "UID: ${spectrums[it].id}, Room: ${spectrums[it].locID}", onClick = {
                showCardDialog.value = true
                pcmDir.writeBytes(spectrums[it].pcmBytes!!)
                generateSpectrum()
                loadImage(spectrumName)
            })
        }
    }

    Column() {
        if (showCardDialog.value) {
            AlertDialog (
                modifier = Modifier
                    .wrapContentSize(Alignment.Center),
                onDismissRequest = {showCardDialog.value = false},
                title = {
                    Text(text = "Spectrum")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        imageBitmap.value?.let {
                            Image(
                                painter = rememberImagePainter(it),
                                contentDescription = "Spectrum",
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {showCardDialog.value = false}) {
                        Text(text = "OK")

                    }
                },
                dismissButton = {
                    Button(onClick = {showCardDialog.value = false}) {
                        Text(text = "Exit")
                    }
                },
            )
        }
    }

}

@Composable
private fun FileCardView(fileName: String, onClick: () -> Unit) {
     Card (
         modifier = Modifier
             .padding(vertical = 2.dp, horizontal = 6.dp)
             .clickable(onClick = onClick),
         elevation = 5.dp,
         border = null,
     ) {
        Row(modifier = Modifier
            .padding(15.dp)
            .fillMaxWidth()) {
            Text(text = fileName)
        }
     }
}

