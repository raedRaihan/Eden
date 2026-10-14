package com.example.eden.Screens

import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.eden.R

@Composable
fun Home(navController: NavController) {
    AndroidView(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        factory = { context ->
            val root = LayoutInflater.from(context).inflate(R.layout.dashboard, null)
            val list = root.findViewById<ListView>(R.id.petList)
            val pets = context.resources.getStringArray(R.array.sample_pets)
            list.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, pets)
            root
        }
    )
}
