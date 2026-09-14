package org.tribetalk.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tribetalk.flashcards.Flashcard
import org.tribetalk.ui.theme.*

/**
 * Interactive Dual-Column Matching Game for FLN Bilingual Practice.
 * Left: Hindi Items (Prompt)
 * Right: Santali Ol Chiki Items (Response)
 */
@Composable
fun MatchingGameView(
    cards: List<Flashcard>,
    onMatchSuccess: (cardId: String) -> Unit,
    onResetGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gameCards = remember(cards) { cards.take(5) }
    val hindiItems = remember(gameCards) { gameCards.shuffled() }
    val santaliItems = remember(gameCards) { gameCards.shuffled() }

    var selectedHindiId by remember { mutableStateOf<String?>(null) }
    var selectedSantaliId by remember { mutableStateOf<String?>(null) }
    val matchedIds = remember { mutableStateListOf<String>() }
    var mismatchCardId by remember { mutableStateOf<String?>(null) }

    val isAllMatched = gameCards.isNotEmpty() && matchedIds.size == gameCards.size

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Status and Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isAllMatched) EduPrimaryLight else EduIndigoLight,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Matched: ${matchedIds.size} / ${gameCards.size}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isAllMatched) EduPrimaryDark else EduIndigo,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                IconButton(onClick = {
                    matchedIds.clear()
                    selectedHindiId = null
                    selectedSantaliId = null
                    mismatchCardId = null
                    onResetGame()
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Reset Game",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = "Tap a Hindi item, then tap its Santali match",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            // Completion Celebration Banner
            if (isAllMatched) {
                Surface(
                    color = EduPrimaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, EduPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Success",
                            tint = EduPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "🎉 All ${gameCards.size} Pairs Matched!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EduPrimaryDark
                        )
                        Text(
                            text = "बहुत बढ़िया! (Excellent work)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                matchedIds.clear()
                                selectedHindiId = null
                                selectedSantaliId = null
                                mismatchCardId = null
                                onResetGame()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EduPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Play Again", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Dual Column Matching Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left Column: Hindi Items
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "हिन्दी (Hindi)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )

                        hindiItems.forEach { card ->
                            val isMatched = matchedIds.contains(card.id)
                            val isSelected = selectedHindiId == card.id
                            val isMismatch = mismatchCardId == card.id

                            val bgColor = when {
                                isMatched -> EduPrimaryLight
                                isMismatch -> Color(0xFFFEE2E2)
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            val borderColor = when {
                                isMatched -> EduPrimary
                                isMismatch -> Color(0xFFEF4444)
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = bgColor,
                                border = BorderStroke(1.dp, borderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isMatched) {
                                        selectedHindiId = card.id
                                        mismatchCardId = null
                                        if (selectedSantaliId != null) {
                                            // Validate match
                                            if (selectedSantaliId == card.id) {
                                                matchedIds.add(card.id)
                                                onMatchSuccess(card.id)
                                                selectedHindiId = null
                                                selectedSantaliId = null
                                            } else {
                                                mismatchCardId = card.id
                                                selectedHindiId = null
                                                selectedSantaliId = null
                                            }
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (!card.imageEmoji.isNullOrBlank()) {
                                        Text(text = card.imageEmoji, fontSize = 18.sp)
                                    }
                                    Text(
                                        text = card.hindiText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected || isMatched) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isMatched) EduPrimaryDark else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Right Column: Santali Ol Chiki Items
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ᱥᱟᱱᱛᱟᱲᱤ (Santali)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EduPrimaryDark
                        )

                        santaliItems.forEach { card ->
                            val isMatched = matchedIds.contains(card.id)
                            val isSelected = selectedSantaliId == card.id
                            val isMismatch = mismatchCardId == card.id

                            val bgColor = when {
                                isMatched -> EduPrimaryLight
                                isMismatch -> Color(0xFFFEE2E2)
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            val borderColor = when {
                                isMatched -> EduPrimary
                                isMismatch -> Color(0xFFEF4444)
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = bgColor,
                                border = BorderStroke(1.dp, borderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isMatched) {
                                        selectedSantaliId = card.id
                                        mismatchCardId = null
                                        if (selectedHindiId != null) {
                                            // Validate match
                                            if (selectedHindiId == card.id) {
                                                matchedIds.add(card.id)
                                                onMatchSuccess(card.id)
                                                selectedHindiId = null
                                                selectedSantaliId = null
                                            } else {
                                                mismatchCardId = card.id
                                                selectedHindiId = null
                                                selectedSantaliId = null
                                            }
                                        }
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = card.santaliOlChiki ?: card.santaliText,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMatched) EduPrimaryDark else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!card.santaliOlChiki.isNullOrBlank() && card.santaliText.isNotBlank()) {
                                        Text(
                                            text = card.santaliText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
