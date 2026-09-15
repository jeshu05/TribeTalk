package org.tribetalk.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tribetalk.ui.theme.*

enum class AppTab(val title: String, val icon: ImageVector) {
    TRANSLATOR("Translator", Icons.Rounded.Translate),
    FLASHCARDS("Flashcards", Icons.Rounded.Style),
    WORKSHEETS("Worksheets", Icons.Rounded.Description)
}

/**
 * Top-level adaptive container for TribeTalk.
 * - In landscape or on wide/tablet screens (>= 840dp or horizontal orientation):
 *   Uses a vertical NavigationRail pinned to the left edge to save vertical height.
 * - In compact portrait phone mode:
 *   Uses standard bottom NavigationBar.
 */
@Composable
fun MainAppScreen(
    translationViewModel: TranslationViewModel,
    flnViewModel: FlnViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(AppTab.TRANSLATOR) }
    val windowSizeInfo = rememberWindowSizeInfo()

    val useNavigationRail = windowSizeInfo.isLandscape || 
        windowSizeInfo.widthClass == WindowWidthSizeClass.EXPANDED

    if (useNavigationRail) {
        Row(modifier = modifier.fillMaxSize()) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                header = {
                    Surface(
                        shape = CircleShape,
                        color = EduPrimaryLight,
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "TT",
                                fontWeight = FontWeight.ExtraBold,
                                color = EduPrimaryDark,
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                AppTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationRailItem(
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
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                when (currentTab) {
                    AppTab.TRANSLATOR -> {
                        HomeScreen(
                            viewModel = translationViewModel,
                            flnViewModel = flnViewModel,
                            onNavigateToFlashcards = { currentTab = AppTab.FLASHCARDS },
                            onNavigateToWorksheets = { currentTab = AppTab.WORKSHEETS }
                        )
                    }
                    AppTab.FLASHCARDS -> {
                        FlashcardsScreen(
                            flnViewModel = flnViewModel,
                            onNavigateToWorksheets = { currentTab = AppTab.WORKSHEETS }
                        )
                    }
                    AppTab.WORKSHEETS -> {
                        WorksheetsScreen(
                            flnViewModel = flnViewModel
                        )
                    }
                }
            }
        }
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline
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
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    AppTab.TRANSLATOR -> {
                        HomeScreen(
                            viewModel = translationViewModel,
                            flnViewModel = flnViewModel,
                            onNavigateToFlashcards = { currentTab = AppTab.FLASHCARDS },
                            onNavigateToWorksheets = { currentTab = AppTab.WORKSHEETS }
                        )
                    }
                    AppTab.FLASHCARDS -> {
                        FlashcardsScreen(
                            flnViewModel = flnViewModel,
                            onNavigateToWorksheets = { currentTab = AppTab.WORKSHEETS }
                        )
                    }
                    AppTab.WORKSHEETS -> {
                        WorksheetsScreen(
                            flnViewModel = flnViewModel
                        )
                    }
                }
            }
        }
    }

    // Modal Studio Dialog for Interactive Content Generation
    val isStudioOpen by flnViewModel.isStudioOpen.collectAsState()
    val isGenerating by flnViewModel.isGeneratingStudioContent.collectAsState()
    val progressStep by flnViewModel.studioProgressStep.collectAsState()
    val studioGeneratedSpec by flnViewModel.studioGeneratedSpec.collectAsState()
    val activeObjective by flnViewModel.activeObjective.collectAsState()
    val teachingContext by flnViewModel.teachingContext.collectAsState()

    if (isStudioOpen) {
        org.tribetalk.ui.components.ContentStudioDialog(
            context = teachingContext,
            objective = activeObjective,
            onDismiss = { flnViewModel.closeStudio() },
            onGenerate = { diff, theme, genFc, genWs ->
                flnViewModel.generateStudioMaterials(diff, theme, genFc, genWs)
            },
            onUseContent = { spec ->
                flnViewModel.applyGeneratedSpec(spec)
                flnViewModel.closeStudio()
            },
            generatedSpec = studioGeneratedSpec,
            isGenerating = isGenerating,
            progressStep = progressStep
        )
    }
}
