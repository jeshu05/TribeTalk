package org.tribetalk.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.tribetalk.ui.components.FlnCardView
import org.tribetalk.ui.theme.DarkBorder
import org.tribetalk.ui.theme.DarkCard
import org.tribetalk.ui.theme.EmeraldContainerDark
import org.tribetalk.ui.theme.EmeraldGreen
import org.tribetalk.ui.theme.EmeraldMint
import org.tribetalk.ui.theme.PureBlack
import org.tribetalk.ui.theme.PureWhite
import org.tribetalk.ui.theme.WhiteSecondary

/**
 * Interactive NIPUN Bharat bilingual flashcards screen.
 * High-contrast Green, White, and Black design.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    flnViewModel: FlnViewModel,
    modifier: Modifier = Modifier
) {
    val categories by flnViewModel.categories.collectAsState()
    val selectedCategory by flnViewModel.selectedCategory.collectAsState()
    val cards by flnViewModel.cards.collectAsState()
    val currentIndex by flnViewModel.currentCardIndex.collectAsState()
    val isQuizMode by flnViewModel.isQuizMode.collectAsState()
    val isRevealed by flnViewModel.isCardRevealed.collectAsState()
    val isPlayingAudio by flnViewModel.isPlayingAudio.collectAsState()

    val currentCard = cards.getOrNull(currentIndex)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PureBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "NIPUN Flashcards",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = PureWhite
                        )
                        Text(
                            text = "Hindi <-> Santali (Ol Chiki) • Balvatika to Grade 2",
                            style = MaterialTheme.typography.bodySmall,
                            color = WhiteSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { flnViewModel.shuffleCards() }) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle Deck",
                            tint = EmeraldGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureBlack
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PureBlack),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { flnViewModel.selectCategory(category) },
                        label = {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreen,
                            selectedLabelColor = PureBlack,
                            containerColor = DarkCard,
                            labelColor = PureWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) EmeraldGreen else DarkBorder,
                            borderWidth = 1.dp
                        )
                    )
                }
            }

            // Deck Controls & Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (cards.isNotEmpty()) "Card ${currentIndex + 1} of ${cards.size}" else "0 cards",
                    style = MaterialTheme.typography.labelLarge,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold
                )

                // Quiz Mode Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Practice Quiz",
                        style = MaterialTheme.typography.labelMedium,
                        color = PureWhite
                    )
                    Switch(
                        checked = isQuizMode,
                        onCheckedChange = { flnViewModel.toggleQuizMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureBlack,
                            checkedTrackColor = EmeraldGreen,
                            uncheckedThumbColor = WhiteSecondary,
                            uncheckedTrackColor = DarkCard
                        ),
                        thumbContent = {
                            Icon(
                                imageVector = if (isQuizMode) Icons.Rounded.Quiz else Icons.Rounded.School,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        }
                    )
                }
            }

            // Flashcard Display
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (currentCard != null) {
                    FlnCardView(
                        card = currentCard,
                        isQuizMode = isQuizMode,
                        isRevealed = isRevealed,
                        isPlayingAudio = isPlayingAudio,
                        onRevealToggle = { flnViewModel.toggleCardReveal() },
                        onPlayAudio = { flnViewModel.playSantaliAudio(currentCard) }
                    )
                } else {
                    Text(
                        text = "No cards available in this category.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WhiteSecondary
                    )
                }
            }

            // Bottom Carousel Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { flnViewModel.prevCard() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(DarkCard)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Previous Card",
                        tint = EmeraldGreen
                    )
                }

                // Dot progress indicators for the active slide
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val maxDots = 7
                    val startDot = (currentIndex - 3).coerceAtLeast(0)
                    val endDot = (startDot + maxDots).coerceAtMost(cards.size)

                    for (i in startDot until endDot) {
                        Box(
                            modifier = Modifier
                                .size(if (i == currentIndex) 9.dp else 5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i == currentIndex) EmeraldGreen else DarkBorder
                                )
                        )
                    }
                }

                IconButton(
                    onClick = { flnViewModel.nextCard() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(DarkCard)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Next Card",
                        tint = EmeraldGreen
                    )
                }
            }
        }
    }
}
