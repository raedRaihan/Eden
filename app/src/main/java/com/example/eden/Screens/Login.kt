package com.example.eden.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class Login
{

    @Composable
    fun loginScreen(
        onGoggleSignIn: () -> Unit,
        loading: Boolean = false,
        error: String? = null
    )
    {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Text(
                text="Welcome",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onGoggleSignIn, enabled = !loading)
            {
                Text("Login in with Google")
            }
            if(loading)
            {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator()
            }

            if(error != null)
            {
                Spacer(modifier = Modifier.height(24.dp))
                Text(text=error, color = Color.Red)
            }
        }
    }

    companion object
}