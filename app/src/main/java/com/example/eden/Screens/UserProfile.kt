package com.example.eden.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar

@Composable
fun UserProfile(navController: NavController)
{
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) }
    ) {innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ){
            Text(text="User Profile")
        }
    }
}