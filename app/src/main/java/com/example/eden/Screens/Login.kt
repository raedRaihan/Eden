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
import androidx.navigation.NavController

class Login
{

    @Composable
    fun loginScreen(
        onGoggleSignIn: () -> Unit,
        loading: Boolean = false,
        error: String? = null,
        navController: NavController
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
            Text(
                text="Will delete these buttons below later",
                style = MaterialTheme.typography.headlineMedium
            )
            // will delete all these buttons later
            Button(onClick = {navController.navigate("accountSettings")})
            {
                Text("Go to Account Settings")
            }
            Button(onClick = {navController.navigate("adoptionApproval")})
            {
                Text("Go to Adoption Approval")
            }
            Button(onClick = {navController.navigate("calendar")})
            {
                Text("Go to Calendar")
            }
            Button(onClick = {navController.navigate("createAnimalProfile")})
            {
                Text("Go to Create Animal Profile")
            }
            Button(onClick = {navController.navigate("createUserProfile")})
            {
                Text("Go to Create User Profile")
            }
            Button(onClick = {navController.navigate("home")})
            {
                Text("Go to Home")
            }
            Button(onClick = {navController.navigate("notifications")})
            {
                Text("Go to Notifications")
            }
            Button(onClick = {navController.navigate("petProfile")})
            {
                Text("Go to Pet Profile")
            }
            Button(onClick = {navController.navigate("preferences")})
            {
                Text("Go to Preferences")
            }
            Button(onClick = {navController.navigate("userProfile")})
            {
                Text("Go to User Profile")
            }
            Button(onClick = {navController.navigate("visitationForm")})
            {
                Text("Go to Visitation Form")
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