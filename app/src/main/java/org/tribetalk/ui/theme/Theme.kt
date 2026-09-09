package org.tribetalk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EduPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = EduPrimaryLight,
    secondary = EduIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF312E81),
    onSecondaryContainer = EduIndigoLight,
    tertiary = EduAmber,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = EduAmberLight,
    background = EduDarkBackground,
    onBackground = EduDarkTextPrimary,
    surface = EduDarkSurface,
    onSurface = EduDarkTextPrimary,
    surfaceVariant = EduDarkSurfaceElevated,
    onSurfaceVariant = EduDarkTextSecondary,
    outline = EduDarkBorder,
    outlineVariant = EduPrimary.copy(alpha = 0.3f)
)

private val LightColorScheme = lightColorScheme(
    primary = EduPrimary,
    onPrimary = EduTextOnPrimary,
    primaryContainer = EduPrimaryLight,
    onPrimaryContainer = EduPrimaryDark,
    secondary = EduIndigo,
    onSecondary = Color.White,
    secondaryContainer = EduIndigoLight,
    onSecondaryContainer = EduIndigo,
    tertiary = EduAmber,
    onTertiary = Color.White,
    tertiaryContainer = EduAmberLight,
    onTertiaryContainer = EduAmber,
    background = EduBackground,
    onBackground = EduTextPrimary,
    surface = EduSurface,
    onSurface = EduTextPrimary,
    surfaceVariant = EduSurfaceElevated,
    onSurfaceVariant = EduTextSecondary,
    outline = EduBorder,
    outlineVariant = EduBorderSubtle
)

@Composable
fun TribeTalkTheme(
    darkTheme: Boolean = false, // Default to clean, warm, accessible educational light theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
