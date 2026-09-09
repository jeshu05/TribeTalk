package org.tribetalk.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.tribetalk.ui.theme.DarkBorder
import org.tribetalk.ui.theme.EmeraldGreen
import org.tribetalk.ui.theme.PureBlack
import org.tribetalk.ui.theme.PureWhite

enum class AppTab(val title: String, val icon: ImageVector) {
    TRANSLATOR("Translator", Icons.Rounded.Translate),
    FLASHCARDS("Flashcards", Icons.Rounded.Style),
    WORKSHEETS("Worksheets", Icons.Rounded.Description)
}

/**
 * Top-level container for TribeTalk with high-contrast Green, White, and Black aesthetics.
 */
@Composable
fun MainAppScreen(
    translationViewModel: TranslationViewModel,
    flnViewModel: FlnViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(AppTab.TRANSLATOR) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = PureBlack,
                tonalElevation = 8.dp,
                modifier = Modifier.border(
                    width = 0.5.dp,
                    color = DarkBorder
                )
            ) {
                AppTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PureBlack,
                            selectedTextColor = EmeraldGreen,
                            indicatorColor = EmeraldGreen,
                            unselectedIconColor = PureWhite.copy(alpha = 0.65f),
                            unselectedTextColor = PureWhite.copy(alpha = 0.65f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        when (currentTab) {
            AppTab.TRANSLATOR -> {
                HomeScreen(
                    viewModel = translationViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            AppTab.FLASHCARDS -> {
                FlashcardsScreen(
                    flnViewModel = flnViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            AppTab.WORKSHEETS -> {
                WorksheetsScreen(
                    flnViewModel = flnViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}
