package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = WaypointLime,
    onPrimary = WaypointDarkBg,
    primaryContainer = WaypointLimeContainer,
    onPrimaryContainer = WaypointLime,
    secondary = WaypointMint,
    onSecondary = WaypointDarkBg,
    secondaryContainer = WaypointCardBg,
    onSecondaryContainer = TextPrimary,
    tertiary = WaypointCyan,
    onTertiary = WaypointDarkBg,
    background = WaypointDarkBg,
    onBackground = TextPrimary,
    surface = WaypointSurface,
    onSurface = TextPrimary,
    surfaceVariant = WaypointCardBg,
    onSurfaceVariant = TextSecondary,
    outline = WaypointBorder,
    error = WaypointRed,
    onError = WaypointDarkBg
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
