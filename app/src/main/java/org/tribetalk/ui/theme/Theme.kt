package org.tribetalk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ForestTealLight,
    onPrimary = SlateBackgroundDark,
    primaryContainer = ForestTealDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = TerracottaLight,
    onSecondary = SlateBackgroundDark,
    secondaryContainer = TerracottaBase,
    onSecondaryContainer = TextPrimaryDark,
    background = SlateBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SlateSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SlateCardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = SlateBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = ForestTealBase,
    onPrimary = CrispSurfaceLight,
    primaryContainer = ForestTealContainer,
    onPrimaryContainer = ForestTealDark,
    secondary = TerracottaBase,
    onSecondary = CrispSurfaceLight,
    secondaryContainer = TerracottaContainer,
    onSecondaryContainer = TerracottaBase,
    background = CrispBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = CrispSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = CrispCardLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = CrispBorderLight
)

@Composable
fun TribeTalkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
