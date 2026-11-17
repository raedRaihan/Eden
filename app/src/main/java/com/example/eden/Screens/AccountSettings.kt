package com.example.eden.Screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.eden.R
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar

@Composable
fun AccountSettings(navController: NavController) {
    Scaffold(
        topBar = { TopBar(navController, stringResource(R.string.screen_settings)) },
        bottomBar = { BottomBar(navController) }
    ) { _ -> }
}
