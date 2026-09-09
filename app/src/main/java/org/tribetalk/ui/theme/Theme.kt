package org.tribetalk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldGreen,
    onPrimary = PureBlack,
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = EmeraldMint,
    secondary = PureWhite,
    onSecondary = PureBlack,
    secondaryContainer = DarkCardElevated,
    onSecondaryContainer = PureWhite,
    tertiary = EmeraldPrimary,
    onTertiary = PureBlack,
    tertiaryContainer = EmeraldContainerDark,
    onTertiaryContainer = EmeraldMint,
    background = PureBlack,
    onBackground = PureWhite,
    surface = DarkSurface,
    onSurface = PureWhite,
    surfaceVariant = DarkCard,
    onSurfaceVariant = WhiteSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderGreen
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = PureWhite,
    primaryContainer = EmeraldContainerLight,
    onPrimaryContainer = EmeraldContainerDark,
    secondary = PureBlack,
    onSecondary = PureWhite,
    secondaryContainer = OffWhite,
    onSecondaryContainer = PureBlack,
    tertiary = EmeraldPrimary,
    onTertiary = PureWhite,
    tertiaryContainer = EmeraldContainerLight,
    onTertiaryContainer = EmeraldDark,
    background = PureWhite,
    onBackground = PureBlack,
    surface = PureWhite,
    onSurface = PureBlack,
    surfaceVariant = OffWhite,
    onSurfaceVariant = Color(0xFF52525B),
    outline = CrispBorderLight,
    outlineVariant = EmeraldPrimary.copy(alpha = 0.4f)
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
