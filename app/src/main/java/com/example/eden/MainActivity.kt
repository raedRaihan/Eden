package com.example.eden

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.example.eden.Screens.AccountSettings
import com.example.eden.Screens.AdoptionApproval
import com.example.eden.Screens.AdoptionForm
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
import com.example.eden.Screens.SearchResults
import com.example.eden.Screens.SignUp
import com.example.eden.Screens.UserProfile
import com.example.eden.Screens.VisitationForm
import com.example.eden.authentication.googleLogin
import com.example.eden.users.Adopter
import com.example.eden.users.Users
import com.example.eden.users.UsersController

class MainActivity : ComponentActivity() {
    companion object
    {
        var loggedInUser by mutableStateOf<BackendlessUser?>(null)
        var loggedInEmail by mutableStateOf<String?>(null)
        var loggedInName by mutableStateOf<String?>(null)
        var loggedInFirst by mutableStateOf("")
        var loggedInLast by mutableStateOf("")
        var loggedInAccountType by mutableStateOf("")
        var loading by mutableStateOf(false)
        var errorMessage by mutableStateOf<String?>(null)
        var registrationMessage by mutableStateOf<String?>(null)
        var registrationLoading by mutableStateOf(false)
        var registrationError by mutableStateOf<String?>(null)

        var queryResultsGlobal:List<Map<*, *>> = emptyList()
        var chosenPetGlobal: HashMap<*, *> = hashMapOf<Any?, Any?>()

        lateinit var loginManager: googleLogin
        lateinit var userController: UsersController
    }



    val authorizationLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult())
    {
        result -> if(result.data !=null && result.resultCode == Activity.RESULT_OK)
        {
            loginManager.authorizeIntent(result.data!!)
        }
        else
        {
            loading= false
            errorMessage = "Google sign in has been canceled"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Backendless.initApp(
            this,
            APP_ID,
            API_KEY
        )

        loginManager = googleLogin(activity = this,
            launchAuth = { request: IntentSenderRequest -> authorizationLauncher.launch(request)},
            onLoginSuc = { user -> loggedInEmail = user.getProperty("email")?.toString() ?: "Unknown Email"

                loggedInName = user.getProperty("name")?.toString() ?: "Unknown User"
                loading= false
                errorMessage = null
                         },
            onLoginErr= {error ->
                loading = false
                errorMessage =error
            }
        )
        userController = UsersController()

        enableEdgeToEdge()
        // This is where the UI hierarchy starts
        setContent {

            // navigation
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "login")
            {
                composable("login")
                {
                        // LaunchEffect is used to safely suspend functions
                        LaunchedEffect(loggedInEmail){
                            if(loggedInEmail != null)
                            {
                                navController.navigate("home"){
                                    popUpTo("login"){
                                        inclusive = true
                                    }

                                    launchSingleTop = true
                                }

                            }
                        }
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->  Box(modifier = Modifier.padding(innerPadding)){

                            if(loggedInEmail == null)
                            {
                                val loginPage = Login()
                                loginPage.loginScreen(
                                    message = registrationMessage,
                                    loading = loading,
                                    error = errorMessage,
                                    onSubmit = { email, password ->
                                        loading = true
                                        errorMessage = null
                                        registrationMessage = null
                                        userController.login(
                                            email = email,
                                            password = password,
                                            onSuccess = { user ->
                                                runOnUiThread {
                                                    loggedInUser = user
                                                    loggedInEmail = user.getProperty("email")?.toString().orEmpty()
                                                    loggedInFirst = user.getProperty("first")?.toString().orEmpty()
                                                    loggedInLast = user.getProperty("last")?.toString().orEmpty()
                                                    loggedInAccountType = user.getProperty("userType")?.toString()
                                                        ?: "Adopter"
                                                    loggedInName = listOf(loggedInFirst, loggedInLast)
                                                        .filter { it.isNotBlank() }
                                                        .joinToString(" ")
                                                    loading = false
                                                }
                                            },
                                            onError = { error ->
                                                runOnUiThread {
                                                    loading = false
                                                    errorMessage = error
                                                }
                                            }
                                        )
                                    },
                                    onSignUp = {
                                        registrationMessage = null
                                        registrationError = null
                                        navController.navigate("signUp")
                                    }
                                )
                            }
                            else
                            {
                                CircularProgressIndicator()
                            }


                        }



                    }
                }
                composable("signUp")
                {
                    SignUp(
                        loading = registrationLoading,
                        backendError = registrationError,
                        onSubmit = { first, last, email, password ->
                            registrationLoading = true
                            registrationError = null
                            userController.registerAdopter(
                                first = first,
                                last = last,
                                email = email,
                                password = password,
                                onSuccess = {
                                    runOnUiThread {
                                        registrationLoading = false
                                        registrationMessage = "Account created. You can now log in."
                                        navController.popBackStack()
                                    }
                                },
                                onError = { error ->
                                    runOnUiThread {
                                        registrationLoading = false
                                        registrationError = error
                                    }
                                }
                            )
                        },
                        onBackToLogin = {
                            registrationError = null
                            navController.popBackStack()
                        }
                    )
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
                composable("adoptionForm")
                {
                    AdoptionForm(navController= navController, chosenPetGlobal)
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
                    PetProfile(navController= navController, chosenPetGlobal)
                }
                composable("preferences")
                {
                    Preferences(navController= navController)
                }
                composable("userProfile")
                {
                    UserProfile(
                        navController = navController,
                        first = loggedInFirst,
                        last = loggedInLast,
                        email = loggedInEmail.orEmpty(),
                        accountType = loggedInAccountType,
                        backendlessUser = loggedInUser
                    )
                }
                composable("visitationForm")
                {
                    VisitationForm(navController= navController, chosenPetGlobal)
                }
                composable("searchResults")
                {
                    SearchResults(navController= navController, queryResultsGlobal)
                }

            }


        }
    }
}

