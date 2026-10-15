package com.example.eden.uiElements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eden.authentication.googleLogin


var loading by mutableStateOf(false)
var errorMessage by mutableStateOf<String?>(null)

@Composable
fun LogOutButton(navController: NavController, loginManager: googleLogin)
{
        Button(
            enabled = !loading,
            onClick = {
                loading= true
                errorMessage = null

                loginManager.signOut(
                    onSucc = {
                        loading = false
                        errorMessage = null

                        navController.navigate("login"){
                            popUpTo(navController.graph.id) { inclusive = false }

                            launchSingleTop = true
                        }
                    },
                    onError = {
                            error -> loading = false
                        errorMessage = error
                    }
                )
            }

        )
        {
            Text("Logout")
        }

        if(loading)
        {
            CircularProgressIndicator()
        }
        if(errorMessage != null)
        {
            Text(text=errorMessage + "", color= androidx.compose.ui.graphics.Color.Red)
        }
}

@Composable
fun TopBar(navController: NavController, loginManager: googleLogin)
{
    LogOutButton(navController, loginManager)
}