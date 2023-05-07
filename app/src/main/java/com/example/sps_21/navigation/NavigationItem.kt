package com.example.sps_21.navigation

import com.example.sps_21.R

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    object Home : NavigationItem("home", R.drawable.baseline_home_24, "Home")
    object Data : NavigationItem("data", R.drawable.baseline_audio_file_24, "Data")
    object Setting : NavigationItem("setting", R.drawable.baseline_settings_24, "Setting")
}