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
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.backendless.Backendless
import com.example.eden.Screens.AccountSettings
import com.example.eden.Screens.AdoptionApproval
import com.example.eden.Screens.Calendar
import com.example.eden.Screens.CreateAnimalProfile
import com.example.eden.Screens.CreateUserProfile
import com.example.eden.Screens.Home
import com.example.eden.ui.theme.EdenTheme
import com.example.eden.util.Constants.API_KEY
import com.example.eden.util.Constants.APP_ID

import com.example.eden.Screens.Login
import com.example.eden.Screens.Notifications
import com.example.eden.Screens.PetProfile
import com.example.eden.Screens.Preferences
import com.example.eden.Screens.Search
import com.example.eden.Screens.UserProfile
import com.example.eden.Screens.VisitationForm
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
            // navigation
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "login")
            {
                composable("login")
                {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->  Box(modifier = Modifier.padding(innerPadding))
                        val loginPage = Login()
                        loginPage.loginScreen({
                            Log.d(
                                "Login",
                                "Google Sign in clicked"
                            )
                        }, navController= navController)
                    }
                }
                composable("search")
                {
                    Search(navController= navController)
                }
                composable("accountSettings")
                {
                    AccountSettings(navController= navController)
                }
                composable("adoptionApproval")
                {
                    AdoptionApproval(navController= navController)
                }
                composable("calendar")
                {
                    Calendar(navController= navController)
                }
                composable("createAnimalProfile")
                {
                    CreateAnimalProfile(navController= navController)
                }
                composable("createUserProfile")
                {
                    CreateUserProfile(navController= navController)
                }
                composable("home")
                {
                    Home(navController= navController)
                }
                composable("notifications")
                {
                    Notifications(navController= navController)
                }
                composable("petProfile")
                {
                    PetProfile(navController= navController)
                }
                composable("preferences")
                {
                    Preferences(navController= navController)
                }
                composable("userProfile")
                {
                    UserProfile(navController= navController)
                }
                composable("visitationForm")
                {
                    VisitationForm(navController= navController)
                }
            }


        }
    }
}

