package org.tribetalk.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tribetalk.flashcards.Flashcard
import org.tribetalk.flashcards.FlashcardGenerator
import org.tribetalk.flashcards.FlashcardSet
import org.tribetalk.flashcards.FlashcardImageLoader
import org.tribetalk.flashcards.FlashcardVisual
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Screen 2: Visual Flashcard Preview & Classroom Study Mode.
 *
 * Displays one bilingual visual card at a time with large visual aids, Devanagari Hindi,
 * Santali Ol Chiki + Latin, interactive "Show Santali" reveal mode for classroom teaching,
 * and in-place teacher editing with "Translate to Santali" support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardPreviewScreen(
    initialSet: FlashcardSet,
    onBack: () -> Unit,
    onCreateWorksheet: ((org.tribetalk.worksheet.Worksheet) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentSet by remember { mutableStateOf(initialSet) }
    var currentCardIndex by remember { mutableIntStateOf(0) }
    var isStudyMode by remember { mutableStateOf(false) }
    var isSantaliRevealed by remember { mutableStateOf(false) }
    var isEditingCurrentCard by remember { mutableStateOf(false) }

    val cards = currentSet.cards
    val totalCards = cards.size
    val currentCard = if (cards.isNotEmpty() && currentCardIndex in cards.indices) cards[currentCardIndex] else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            currentSet.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            "${currentSet.domain} • ${currentSet.grade ?: "FLN"}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Create Worksheet from Deck (Phase 12)
                    if (onCreateWorksheet != null && cards.isNotEmpty()) {
                        IconButton(onClick = {
                            val ws = org.tribetalk.worksheet.WorksheetGenerator().createWorksheetFromFlashcards(currentSet)
                            onCreateWorksheet(ws)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = "Create Worksheet from Deck",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Study Mode Toggle
                    FilledTonalIconToggleButton(
                        checked = isStudyMode,
                        onCheckedChange = {
                            isStudyMode = it
                            isSantaliRevealed = !it
                        }
                    ) {
                        Icon(
                            imageVector = if (isStudyMode) Icons.Default.School else Icons.Default.Visibility,
                            contentDescription = "Study Mode"
                        )
                    }

                    // Edit Current Card
                    if (currentCard != null) {
                        IconButton(onClick = { isEditingCurrentCard = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Card",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.shadow(1.dp)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button
                    OutlinedButton(
                        onClick = {
                            if (currentCardIndex > 0) {
                                currentCardIndex--
                                if (isStudyMode) isSantaliRevealed = false
                            }
                        },
                        enabled = currentCardIndex > 0,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PREV")
                    }

                    // Card Counter
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${if (totalCards > 0) currentCardIndex + 1 else 0} / $totalCards",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isStudyMode) "Study Mode" else "Preview Mode",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    // Next Button
                    Button(
                        onClick = {
                            if (currentCardIndex < totalCards - 1) {
                                currentCardIndex++
                                if (isStudyMode) isSantaliRevealed = false
                            }
                        },
                        enabled = currentCardIndex < totalCards - 1,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("NEXT")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", modifier = Modifier.size(18.dp))
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // NIPUN Bharat Learning Outcome Badge
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Skill: ${currentSet.learningOutcome}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = currentSet.topic,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Flashcard View
            if (currentCard != null) {
                BilingualFlashcardView(
                    card = currentCard,
                    isStudyMode = isStudyMode,
                    isSantaliRevealed = isSantaliRevealed,
                    onRevealSantali = { isSantaliRevealed = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No flashcards available.", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Teacher Edit Card Dialog
    if (isEditingCurrentCard && currentCard != null) {
        TeacherEditFlashcardDialog(
            card = currentCard,
            cardNumber = currentCardIndex + 1,
            onDismiss = { isEditingCurrentCard = false },
            onSave = { updatedCard ->
                val updatedList = currentSet.cards.toMutableList().apply {
                    set(currentCardIndex, updatedCard)
                }
                currentSet = currentSet.copy(cards = updatedList)
                isEditingCurrentCard = false
                Toast.makeText(context, "Updated Card #${currentCardIndex + 1}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * Large, classroom-readable bilingual flashcard component.
 */
@Composable
fun BilingualFlashcardView(
    card: Flashcard,
    isStudyMode: Boolean,
    isSantaliRevealed: Boolean,
    onRevealSantali: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Visual Area (Primary Element)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                FlashcardVisual(
                    imageUri = card.imageUri,
                    iconType = card.iconType,
                    imageEmoji = card.imageEmoji,
                    contentDescription = card.hindiText,
                    maxImageSize = 130.dp,
                    emojiFontSize = 80.sp,
                    vectorGraphicSize = 96.dp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hindi Text (Large Devanagari)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "हिन्दी",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = card.hindiText,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                modifier = Modifier.padding(vertical = 10.dp)
            )

            // Santali Area (Revealed or Hidden in Study Mode)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (isStudyMode && !isSantaliRevealed) {
                    // Reveal Button in Classroom Study Mode
                    Button(
                        onClick = onRevealSantali,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2F8F83)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = "Reveal", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "SHOW SANTALI (ᱥᱟᱱᱛᱟᱲᱤ)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    // Santali Translation Content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Santali (ᱥᱟᱱᱛᱟᱲᱤ)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2F8F83)
                        )

                        // Ol Chiki rendering
                        if (!card.santaliOlChiki.isNullOrBlank()) {
                            Text(
                                text = card.santaliOlChiki,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                ),
                                color = Color(0xFF1E6B61),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Latin / Phonetic Santali
                        Text(
                            text = card.santaliText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        // Phonetic Guide in Devanagari
                        if (!card.phoneticGuide.isNullOrBlank()) {
                            Text(
                                text = "उच्चारण: ${card.phoneticGuide}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // Example Sentence (Optional)
            if (!card.exampleSentenceHindi.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "उदा: ${card.exampleSentenceHindi}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!isStudyMode || isSantaliRevealed) {
                            if (!card.exampleSentenceSantali.isNullOrBlank()) {
                                Text(
                                    text = "ᱞᱮᱠᱟᱛᱮ: ${card.exampleSentenceSantali}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF2F8F83)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Teacher Editing Dialog for a Flashcard.
 *
 * Supports Hindi edit -> Santali re-translation, Ol Chiki editing, and local image selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherEditFlashcardDialog(
    card: Flashcard,
    cardNumber: Int,
    onDismiss: () -> Unit,
    onSave: (Flashcard) -> Unit
) {
    var hindiText by remember { mutableStateOf(card.hindiText) }
    var santaliText by remember { mutableStateOf(card.santaliText) }
    var santaliOlChiki by remember { mutableStateOf(card.santaliOlChiki ?: "") }
    var phoneticGuide by remember { mutableStateOf(card.phoneticGuide ?: "") }
    var imageUri by remember { mutableStateOf(card.imageUri) }
    var exampleHindi by remember { mutableStateOf(card.exampleSentenceHindi ?: "") }
    var exampleSantali by remember { mutableStateOf(card.exampleSentenceSantali ?: "") }

    var isTranslating by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val generator = remember { FlashcardGenerator() }

    val context = LocalContext.current

    // Gallery Image Picker launcher with safe persistent internal storage copy
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val persistentPath = FlashcardImageLoader.saveTeacherPhoto(context, uri)
            if (persistentPath != null) {
                imageUri = persistentPath
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Flashcard #$cardNumber", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Image Attachment Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Visual Image:", style = MaterialTheme.typography.labelMedium)
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Pick Image", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (imageUri != null) "Change Photo" else "Pick Local Photo")
                    }
                }

                // Hindi Text Field
                OutlinedTextField(
                    value = hindiText,
                    onValueChange = {
                        hindiText = it
                        statusMessage = null
                    },
                    label = { Text("Hindi Word / Text (हिन्दी)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Secondary Action: Translate to Santali
                OutlinedButton(
                    onClick = {
                        if (hindiText.isBlank()) {
                            statusMessage = "Please enter Hindi text to translate."
                            return@OutlinedButton
                        }
                        isTranslating = true
                        statusMessage = null
                        coroutineScope.launch {
                            try {
                                val result = withContext(Dispatchers.Default) {
                                    generator.retranslateCard(hindiText)
                                }
                                santaliText = result.santaliText
                                if (result.santaliOlChiki != null) {
                                    santaliOlChiki = result.santaliOlChiki
                                }
                                if (result.phoneticGuide != null) {
                                    phoneticGuide = result.phoneticGuide
                                }
                                if (result.santaliText == "Translation unavailable") {
                                    statusMessage = "Translation unavailable for this word."
                                } else {
                                    statusMessage = "✓ Translated from edited Hindi"
                                }
                            } catch (e: Exception) {
                                statusMessage = "Unable to translate. Please try again."
                            } finally {
                                isTranslating = false
                            }
                        }
                    },
                    enabled = !isTranslating && hindiText.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isTranslating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Translating...", style = MaterialTheme.typography.labelMedium)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Translate to Santali",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "TRANSLATE TO SANTALI",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                if (statusMessage != null) {
                    Text(
                        text = statusMessage ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (statusMessage?.startsWith("✓") == true) Color(0xFF2F8F83) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // Santali Text Field (Latin)
                OutlinedTextField(
                    value = santaliText,
                    onValueChange = {
                        santaliText = it
                        statusMessage = null
                    },
                    label = { Text("Santali Latin (ᱥᱟᱱᱛᱟᱲᱤ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Ol Chiki Field
                OutlinedTextField(
                    value = santaliOlChiki,
                    onValueChange = { santaliOlChiki = it },
                    label = { Text("Santali Ol Chiki (ᱚᱞ ᱪᱤᱠᱤ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Example Hindi Sentence
                OutlinedTextField(
                    value = exampleHindi,
                    onValueChange = { exampleHindi = it },
                    label = { Text("Example Sentence (Hindi)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = card.copy(
                        hindiText = hindiText.trim(),
                        santaliText = santaliText.trim(),
                        santaliOlChiki = santaliOlChiki.trim().ifBlank { null },
                        phoneticGuide = phoneticGuide.trim().ifBlank { null },
                        imageUri = imageUri,
                        exampleSentenceHindi = exampleHindi.trim().ifBlank { null },
                        exampleSentenceSantali = exampleSantali.trim().ifBlank { null }
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("SAVE CHANGES", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
