package com.example.eden.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = EdenLightBlue,
    onPrimary = EdenDarkBlue,
    secondary = EdenDarkBlue,
    onSecondary = EdenWhite
)

private val LightColorScheme = lightColorScheme(
    primary = EdenDarkBlue,
    onPrimary = EdenWhite,
    secondary = EdenLightBlue,
    onSecondary = EdenDarkBlue
)

@Composable
fun EdenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
