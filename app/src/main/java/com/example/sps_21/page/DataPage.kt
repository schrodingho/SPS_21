package com.example.sps_21.page

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.sps_21.SpectrumData
import com.example.sps_21.database.FileDao
import com.example.sps_21.database.FilesDatabase

import javax.sql.DataSource


@Composable
fun DisplayData(spectrums: List<SpectrumData>) {
    LazyColumn {
        items(spectrums.size) {
            Text(text = "UID: ${spectrums[it].id}, RoomNumber: ${spectrums[it].locID}")
        }
    }
}

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
            .background(Color.White)
            .wrapContentSize(Alignment.Center)
    ) {
        Button(onClick = {
            Thread {
                showingData = fileDao.loadAllData()
                strstate.value = showingData
            }.start()

        }) {
            Text(text = "Load Data")
        }

        Button(onClick = {
            Thread {
                fileDao.deleteAllData()
                strstate.value = listOf()
            }.start()
        }) {
            Text(text = "Delete Data")
        }
        DisplayData(spectrums = strstate.value)
//        Text(text = "Last data: ${fromdb.value}")
    }
}