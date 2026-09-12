package org.tribetalk.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import org.tribetalk.fln.image.LowMemoryImageLoader
import org.tribetalk.fln.model.FlnCard
import org.tribetalk.fln.repository.FlnCurriculumRepository
import org.tribetalk.ui.theme.*

/**
 * Kid-friendly, focused Santali-First Bilingual Flashcards.
 * Features:
 * - Santali (Ol Chiki) as the hero target learning language
 * - Real educational storybook illustrations loaded with low-memory Coil pipeline (< 130 KB RAM/image)
 * - NIPUN Bharat learning outcome competencies (L1.1 to L2.5, N1.1 to N2.1)
 * - Uncluttered, joyful interface with Move, Flip, and Sound
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
    val isRevealed by flnViewModel.isCardRevealed.collectAsState()
    val isPlayingAudio by flnViewModel.isPlayingAudio.collectAsState()

    val currentCard = cards.getOrNull(currentIndex)
    val totalCount = cards.size
    val progress = if (totalCount > 0) (currentIndex + 1).toFloat() / totalCount.toFloat() else 0f

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = EduBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ᱥᱟᱱᱛᱟᱲᱤ ᱯᱷᱞᱮᱥᱠᱟᱨᱰ (Santali Flashcards)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = EduTextPrimary
                        )
                        Text(
                            text = "संथाली (Ol Chiki) • NIPUN भारत FLN • 100% Offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = EduTextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { flnViewModel.shuffleCards() }) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle",
                            tint = EduPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EduSurface
                ),
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(EduBackground),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Simple, Clean Topic Pills
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { flnViewModel.selectCategory(category) },
                        shape = RoundedCornerShape(20.dp),
                        label = {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EduPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = EduSurface,
                            labelColor = EduTextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) EduPrimary else EduBorder,
                            borderWidth = 1.dp
                        )
                    )
                }
            }

            // 2. Card Counter & Subtle Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = EduAmberLight,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, EduAmber.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("⭐", fontSize = 13.sp)
                        Text(
                            text = if (totalCount > 0) "${currentIndex + 1} / $totalCount" else "0 / 0",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = EduAmber
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .height(6.dp)
                        .clip(CircleShape),
                    color = EduPrimary,
                    trackColor = EduBorder
                )

                Text(
                    text = "स्वाइप करें ↔",
                    style = MaterialTheme.typography.labelSmall,
                    color = EduTextMuted
                )
            }

            // 3. Central Big Kid-Friendly Santali-First Flashcard
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (currentCard != null) {
                    KidBilingualCard(
                        card = currentCard,
                        isRevealed = isRevealed,
                        isPlayingAudio = isPlayingAudio,
                        onFlip = { flnViewModel.toggleCardReveal() },
                        onPlaySound = { flnViewModel.playSantaliAudio(currentCard) },
                        onSwipeLeft = { flnViewModel.nextCard() },
                        onSwipeRight = { flnViewModel.prevCard() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.96f)
                    )
                } else {
                    Text(
                        text = "इस विषय में कोई कार्ड उपलब्ध नहीं है।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EduTextSecondary
                    )
                }
            }

            // 4. Kid-Friendly Control Dock: Move, Flip, and Sound
            CardControlDock(
                isRevealed = isRevealed,
                isPlayingAudio = isPlayingAudio,
                onPrev = { flnViewModel.prevCard() },
                onFlip = { flnViewModel.toggleCardReveal() },
                onPlaySound = {
                    if (currentCard != null) flnViewModel.playSantaliAudio(currentCard)
                },
                onNext = { flnViewModel.nextCard() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}

/**
 * Big, kid-friendly Santali-First Card with full 3D flip animation,
 * low-memory WebP image loading, rich Ol Chiki typography, and bilingual sentences.
 */
@Composable
private fun KidBilingualCard(
    card: FlnCard,
    isRevealed: Boolean,
    isPlayingAudio: Boolean,
    onFlip: () -> Unit,
    onPlaySound: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val imageLoader = remember(context) { LowMemoryImageLoader.get(context) }

    // Smooth 3D Flip Rotation
    val rotation by animateFloatAsState(
        targetValue = if (isRevealed) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "card_flip_3d"
    )

    val isBackFace = rotation > 90f

    // Soft thematic pastel card palettes for kids
    val (cardBg, cardBorder, accentColor) = when (card.category) {
        FlnCurriculumRepository.CATEGORY_AKSHAR -> Triple(Color(0xFFF0FDF4), Color(0xFFBBF7D0), Color(0xFF16A34A))
        FlnCurriculumRepository.CATEGORY_NUMBERS -> Triple(Color(0xFFF0F9FF), Color(0xFFBAE6FD), Color(0xFF0284C7))
        FlnCurriculumRepository.CATEGORY_ANIMALS -> Triple(Color(0xFFFFFBEB), Color(0xFFFDE68A), Color(0xFFD97706))
        FlnCurriculumRepository.CATEGORY_NATURE -> Triple(Color(0xFFF0FDF4), Color(0xFFBBF7D0), Color(0xFF059669))
        FlnCurriculumRepository.CATEGORY_FRUITS -> Triple(Color(0xFFFFF1F2), Color(0xFFFECDD3), Color(0xFFE11D48))
        FlnCurriculumRepository.CATEGORY_COLORS -> Triple(Color(0xFFFAF5FF), Color(0xFFE9D5FF), Color(0xFF7C3AED))
        else -> Triple(Color(0xFFFFFFFF), Color(0xFFE2E8F0), EduPrimary)
    }

    Card(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount },
                    onDragEnd = {
                        if (totalDrag < -50) onSwipeLeft()
                        else if (totalDrag > 50) onSwipeRight()
                    }
                )
            }
            .clickable { onFlip() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(2.dp, cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!isBackFace) {
                // =============================================================
                // FRONT FACE: SANTALI-FIRST (Ol Chiki + Real Image + Phonics + Audio)
                // =============================================================
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Bar: NIPUN Code + Category
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = accentColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = card.category,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }

                        Surface(
                            color = EduIndigoLight,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "NIPUN ${card.nipunCode}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EduIndigo,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Center Content: Real Educational Image + Massive Ol Chiki Word
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Real Educational Illustration (256x256 WebP decoded in RGB_565)
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(card.imageAssetPath)
                                .crossfade(true)
                                .build(),
                            imageLoader = imageLoader,
                            contentDescription = card.hindiText,
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(accentColor.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = accentColor
                                    )
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(accentColor.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = card.santaliOlChiki.take(1),
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                            },
                            modifier = Modifier
                                .size(148.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .border(2.dp, cardBorder, RoundedCornerShape(22.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Hero Santali Word in Ol Chiki (Massive & Clear)
                        Text(
                            text = card.santaliOlChiki,
                            style = MaterialTheme.typography.displayMedium,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EduPrimary,
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.5.sp
                        )

                        // Teacher Phonics Pronunciation Guide (Devanagari + Latin)
                        Surface(
                            color = EduAmberLight,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, EduAmber.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RecordVoiceOver,
                                    contentDescription = "Voice",
                                    tint = EduAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "उच्चारण: ${card.teacherPhoneticGuide}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EduAmber
                                )
                            }
                        }
                    }

                    // Bottom Tap Prompt (With Audio Action)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onPlaySound,
                            shape = RoundedCornerShape(18.dp),
                            color = EduPrimary,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = "Sound",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isPlayingAudio) "बोल रहा है..." else "🔊 सुनो",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            color = accentColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "हिन्दी अर्थ 🔄",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                            }
                        }
                    }
                }
            } else {
                // =============================================================
                // BACK FACE: HINDI MEANING + BILINGUAL CONTEXT SENTENCES
                // =============================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Header
                    Surface(
                        color = EduPrimaryLight,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "हिन्दी अर्थ व वाक्य • Hindi Meaning & Sentence",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = EduPrimaryDark,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    // Main Hindi Word & English Gloss
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = card.hindiText,
                            style = MaterialTheme.typography.displayMedium,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EduTextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = card.englishGloss,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EduTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        // Full Contextual Santali Sentence
                        card.exampleSentenceSantali?.let { sSentence ->
                            Surface(
                                color = Color.White.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, EduPrimary.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "ᱥᱟᱱᱛᱟᱲᱤ ᱵᱟᱠᱭᱚ (Santali):",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EduPrimaryDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = sSentence,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EduPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Hindi Sentence Translation
                        card.exampleSentenceHindi?.let { hSentence ->
                            Surface(
                                color = Color.White.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, cardBorder.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "हिन्दी अनुवाद (Hindi):",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EduTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "“$hSentence”",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = EduTextPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Direct Sound Button on Back Face
                    FilledTonalButton(
                        onClick = onPlaySound,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = EduPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Play Sound",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPlayingAudio) "बोल रहा है... 🔊" else "🔊 संथाली उच्चारण सुनो",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clean, kid-friendly Control Dock: Move (Prev/Next), Flip, and Sound.
 */
@Composable
private fun CardControlDock(
    isRevealed: Boolean,
    isPlayingAudio: Boolean,
    onPrev: () -> Unit,
    onFlip: () -> Unit,
    onPlaySound: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Move: Previous Card (◀)
        FilledTonalIconButton(
            onClick = onPrev,
            modifier = Modifier.size(52.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = EduSurface,
                contentColor = EduTextPrimary
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Previous Card",
                tint = EduTextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        // 2. Flip: Flip Card (🔄)
        FilledTonalButton(
            onClick = onFlip,
            modifier = Modifier.height(50.dp),
            shape = RoundedCornerShape(25.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = if (isRevealed) EduPrimaryLight else EduSurface,
                contentColor = if (isRevealed) EduPrimaryDark else EduTextPrimary
            ),
            border = BorderStroke(
                1.5.dp,
                if (isRevealed) EduPrimary else EduBorder
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.FlipCameraAndroid,
                contentDescription = "Flip Card",
                tint = if (isRevealed) EduPrimaryDark else EduTextPrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isRevealed) "संथाली" else "हिन्दी",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isRevealed) EduPrimaryDark else EduTextPrimary
            )
        }

        // 3. Sound: Listen to Santali Audio (🔊)
        Surface(
            onClick = onPlaySound,
            modifier = Modifier.size(52.dp),
            shape = CircleShape,
            color = EduPrimary,
            shadowElevation = 3.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = "Play Audio",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // 4. Move: Next Card (▶)
        FilledTonalIconButton(
            onClick = onNext,
            modifier = Modifier.size(52.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = EduPrimary,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Next Card",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
