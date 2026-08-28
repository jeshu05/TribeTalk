package com.alchemists.tribetalk.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define a unified clean educational color scheme for both light and dark systems
private val CleanColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    secondary = CoralSecondary,
    tertiary = AmberAccent,
    background = AppBackground,
    surface = AppSurface,
    onPrimary = AppSurface,
    onSecondary = AppSurface,
    onBackground = TextCharcoal,
    onSurface = TextCharcoal
)

@Composable
fun TribeTalkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Force the CleanColorScheme to maintain off-white backgrounds and white cards,
    // preventing the dark-theme indigo background issue.
    val colorScheme = CleanColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val context = view.context
            if (context is Activity) {
                val window = context.window
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
