package com.example.eden.uiElements

import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.eden.MainActivity
import com.example.eden.MainActivity.Companion.loggedInEmail
import com.example.eden.R
import com.example.eden.notifications.notificationController
import kotlin.collections.get


fun LogOutButton(navController: NavController)
{

            MainActivity.loading = true
            MainActivity.errorMessage = null

            MainActivity.loginManager.signOut(
                onSucc = {
                    MainActivity.loggedInUser = null
                    MainActivity.loggedInEmail = null
                    MainActivity.loggedInName = null
                    MainActivity.loggedInFirst = ""
                    MainActivity.loggedInLast = ""
                    MainActivity.loggedInAccountType = ""
                    MainActivity.loggedInIsAdmin = false
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

@Composable
//@Preview
fun BottomBar(navController: NavController)
{
    val roleSpecificItem = if (MainActivity.loggedInIsAdmin) {
        NavItem(
            "Admin Dashboard",
            "adminDashboard",
            icon = Icons.Default.Star,
        )
    } else {
        NavItem(
            "Applications",
            "adoptionApproval",
            icon = Icons.AutoMirrored.Filled.List,
        )
    }
    val navItemList = listOf(
        NavItem("Home", "home", icon = Icons.Default.Home),
        NavItem("Calendar", "calendar", icon = Icons.Default.DateRange),
        NavItem("My Profile", "userProfile", icon = Icons.Default.Person),
        roleSpecificItem,
    )


    val backStackEntryState by navController.currentBackStackEntryAsState()
    val currentRoute =backStackEntryState?.destination?.route
            NavigationBar() {

                navItemList.forEach { (label, route, icon) ->
                    NavigationBarItem(
                        selected = currentRoute == route,
                        onClick = {
                            if(currentRoute != route)
                            {
                                navController.navigate(route){
                                    launchSingleTop = true
                                }

                            }
                        },
                        icon = {
                            androidx.compose.material3.Icon(imageVector = icon, contentDescription = label)
                        },
                        label = {Text(label)}
                    )
                }
            }

}

@Composable
//@Preview
fun TopBar(navController: NavController, title: String? = null)
{
    val backStackEntryState by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntryState?.destination?.route
    var notificationCount by remember(loggedInEmail) { mutableStateOf(0) }

    LaunchedEffect(loggedInEmail, currentRoute) {
        val email = loggedInEmail
        if (email.isNullOrBlank()) {
            notificationCount = 0
            return@LaunchedEffect
        }

        notificationController().searchForNotifications(
            email = email,
            onSuccess = { response ->
                notificationCount = response.count { notification ->
                    notification["read"] == false && notification["active"] == true
                }
            },
            onError = { message ->
                Log.e("Notifications", "Count failure: $message")
            },
        )
    }

    Column {
            NavigationBar(modifier = Modifier.statusBarsPadding()) {
                if (navController.previousBackStackEntry != null) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.popBackStack() },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        },
                        label = { Text("Back") },
                    )
                }

                    NavigationBarItem(
                        selected = false,
                        enabled = !MainActivity.skipLoginForDevelopment,
                        onClick = {
                            LogOutButton(navController)
                        },
                        icon = {
                            Icon(Icons.Default.Clear, contentDescription = "Logout")

                        },
                        label = {Text("Logout")}
                    )

                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate("search")
                        {
                            popUpTo(navController.graph.id) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")

                    },
                    label = {Text("Search")}
                )

                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate("notifications")
                        {
                            popUpTo(navController.graph.id) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        if(notificationCount > 0)
                        {
                            Text(
                                text = "+$notificationCount",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Top),
                            )
                        }


                    },
                    label = {Text("Notifications")}
                )

            }

        if (title != null) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .heightIn(min = 56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() }
                )
            }
        }
    }
}
