package com.example.eden.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun Home(navController: NavController)
{
    Column(Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    )
    {
        Text(text="Home")
    }
    /*
    if(loggedInEmail != null)
    {
        Column()
        {
            Button(
                enabled = !loading,
                onClick = {
                    loading= true
                    errorMessage = null

                    loginManager.signOut(
                        onSucc = {
                            loggedInEmail = null
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
            }*/
}