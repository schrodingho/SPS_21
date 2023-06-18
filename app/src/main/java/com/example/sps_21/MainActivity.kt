package com.example.sps_21

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
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
import androidx.navigation.Navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.sps_21.component.BottomNavBar
import com.example.sps_21.ui.theme.Primary200
import com.example.sps_21.ui.theme.Primary700
import com.example.sps_21.ui.theme.Red600
import com.example.sps_21.ui.theme.SPS_21Theme
import com.example.sps_21.navigation.Navigation

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
            0
        )

        setContent {
            SPS_21Theme {
                MainScreen(applicationContext)
            }
        }
    }

}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreen(applicationContext: Context) {
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            TopAppBar(
                title =
                {
                    Text("AudioLoc", fontSize = 25.sp, fontStyle = FontStyle.Italic)
                }, backgroundColor = Red600
            )
        },
        bottomBar = {
            BottomNavBar(navController)
        }
    ) {
        Navigation(navController = navController, applicationContext = applicationContext)
    }
}


