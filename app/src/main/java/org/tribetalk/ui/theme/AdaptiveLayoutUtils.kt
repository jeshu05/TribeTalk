package org.tribetalk.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 standard Window Size Classes for responsive, multi-form-factor layouts.
 */
enum class WindowWidthSizeClass {
    COMPACT,   // < 600dp (standard portrait phones)
    MEDIUM,    // 600dp .. 839dp (foldables unfolded, small 7"-8" tablets)
    EXPANDED   // >= 840dp (large 10"-12" tablets, desktop, chromebooks)
}

enum class WindowHeightSizeClass {
    COMPACT,   // < 480dp (landscape phones)
    MEDIUM,    // 480dp .. 899dp (portrait phones, small tablets)
    EXPANDED   // >= 900dp (large tablets, desktop)
}

data class WindowSizeInfo(
    val widthClass: WindowWidthSizeClass,
    val heightClass: WindowHeightSizeClass,
    val widthDp: Dp,
    val heightDp: Dp,
    val isLandscape: Boolean,
    val isTabletOrExpanded: Boolean,
    val isCompactHeight: Boolean
)

/**
 * Remembers and calculates adaptive window size metrics for the current window configuration.
 * Adapts fluidly between budget 5-inch phones, horizontal landscape stands, and 12-inch classroom tablets.
 */
@Composable
fun rememberWindowSizeInfo(): WindowSizeInfo {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp.dp
    val heightDp = configuration.screenHeightDp.dp
    val isLandscape = widthDp > heightDp

    val widthClass = when {
        widthDp < 600.dp -> WindowWidthSizeClass.COMPACT
        widthDp < 840.dp -> WindowWidthSizeClass.MEDIUM
        else -> WindowWidthSizeClass.EXPANDED
    }

    val heightClass = when {
        heightDp < 480.dp -> WindowHeightSizeClass.COMPACT
        heightDp < 900.dp -> WindowHeightSizeClass.MEDIUM
        else -> WindowHeightSizeClass.EXPANDED
    }

    val isTabletOrExpanded = widthClass == WindowWidthSizeClass.EXPANDED || 
        (widthClass == WindowWidthSizeClass.MEDIUM && isLandscape) ||
        widthDp >= 720.dp

    val isCompactHeight = heightClass == WindowHeightSizeClass.COMPACT

    return WindowSizeInfo(
        widthClass = widthClass,
        heightClass = heightClass,
        widthDp = widthDp,
        heightDp = heightDp,
        isLandscape = isLandscape,
        isTabletOrExpanded = isTabletOrExpanded,
        isCompactHeight = isCompactHeight
    )
}
