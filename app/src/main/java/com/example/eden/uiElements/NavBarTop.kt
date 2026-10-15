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
import com.example.eden.MainActivity
import com.example.eden.authentication.googleLogin



@Composable
fun LogOutButton(navController: NavController)
{
        Button(
            
            onClick = {
                MainActivity.loading = true
                MainActivity.errorMessage = null

                MainActivity.loginManager.signOut(
                    onSucc = {
                        MainActivity.loggedInEmail=null
                        MainActivity.loggedInName = null
                        MainActivity.loading = false
                        MainActivity.errorMessage = null

                        navController.navigate("login"){
                            popUpTo(navController.graph.id) { inclusive = false }

                            launchSingleTop = true
                        }
                    },
                    onError = {
                            error -> MainActivity.loading = false
                        MainActivity.errorMessage = error
                    }
                )
            }

        )
        {
            Text("Logout")
        }

        if(MainActivity.loading)
        {
            CircularProgressIndicator()
        }
        if(MainActivity.errorMessage != null)
        {
            Text(text=MainActivity.errorMessage + "", color= androidx.compose.ui.graphics.Color.Red)
        }
}

@Composable
fun TopBar(navController: NavController)
{
    LogOutButton(navController)
}