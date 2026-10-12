package com.example.eden

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.backendless.Backendless
import com.example.eden.ui.theme.EdenTheme
import com.example.eden.util.Constants.API_KEY
import com.example.eden.util.Constants.APP_ID

import com.example.eden.Screens.Login
import com.example.eden.authentication.googleLogin

class MainActivity : ComponentActivity() {
    /*
    var displayName by mutableStateOf<String?>(null)
    var loading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    lateinit var loginManager: googleLogin

    val authorizationLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult())
    {
        result -> if(result.data !=null && result.resultCode == Activity.RESULT_OK)
        {
            loginManager.authorization(result.data!!)
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Backendless.initApp(
            this,
            APP_ID,
            API_KEY
        )
        //val authorizationLauncher = registerForActivityResult()
        enableEdgeToEdge()
        // This is where the UI hierarchy starts
        setContent {
            EdenTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->  Box(modifier = Modifier.padding(innerPadding))
                    val loginPage = Login()
                    loginPage.loginScreen({
                        Log.d(
                            "Login",
                            "Google Sign in clicked"
                        )
                    })
                }
            }
        }
    }
}

