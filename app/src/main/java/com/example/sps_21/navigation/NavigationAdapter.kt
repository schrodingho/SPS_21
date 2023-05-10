package com.example.sps_21.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sps_21.page.DataPageView
import com.example.sps_21.page.HomePageView
import com.example.sps_21.page.SettingPageView

@Composable
fun Navigation(navController: NavHostController, applicationContext: Context) {
    NavHost(navController, startDestination = NavigationItem.Home.route) {
        composable(NavigationItem.Home.route) {
            HomePageView(applicationContext)
        }
        composable(NavigationItem.Data.route) {
            DataPageView()
        }
        composable(NavigationItem.Setting.route) {
            SettingPageView()
        }

    }
}