package com.example.bookmarkapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrandPrimary = Color(0xFF6C5CE7) // soft purple
private val BrandSecondary = Color(0xFF00D2D3) // teal

private val LightScheme = lightColorScheme(
    primary = BrandPrimary,
    secondary = BrandSecondary,
)
private val DarkScheme = darkColorScheme(
    primary = BrandPrimary,
    secondary = BrandSecondary,
)

@Composable
fun BookmarkTheme(content: @Composable () -> Unit) {
    val DarkColors = darkColorScheme(
        primary = Color(0xFFBB86FC),
        secondary = Color(0xFF03DAC6),
        surface = Color(0xFF121212),
        surfaceVariant = Color(0xFF1E1E1E),
        onSurface = Color(0xFFEAEAEA),
        onSurfaceVariant = Color(0xFFB0B0B0),
    )

    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography(),
        content = content
    )
}
