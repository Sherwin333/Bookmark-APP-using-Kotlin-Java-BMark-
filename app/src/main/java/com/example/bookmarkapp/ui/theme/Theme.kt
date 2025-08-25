package com.example.bookmarkapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF4A90E2),
    secondary = Color(0xFF50E3C2),
    surface = Color(0xFFF5F7FA),
    surfaceVariant = Color(0xFFE0E6ED),
    onSurface = Color(0xFF2C3E50),
    onSurfaceVariant = Color(0xFF5C6B7A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF82B1FF),
    secondary = Color(0xFF64FFDA),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurface = Color(0xFFEAEAEA),
    onSurfaceVariant = Color(0xFFB0B0B0),
)

@Composable
fun BookmarkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        } else if (darkTheme) DarkColors else LightColors

    MaterialTheme(colorScheme = colorScheme, content = content)
}
