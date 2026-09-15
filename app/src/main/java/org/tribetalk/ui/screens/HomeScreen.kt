package org.tribetalk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tribetalk.ui.components.ConversationCard
import org.tribetalk.ui.components.LanguageSelectorPill
import org.tribetalk.ui.components.RipplePulseButton
import org.tribetalk.ui.components.StatusIndicator
import org.tribetalk.ui.components.WaveformVisualizer
import org.tribetalk.ui.theme.EduPrimary
import org.tribetalk.ui.theme.EduPrimaryDark
import org.tribetalk.ui.theme.EduPrimaryLight
import org.tribetalk.ui.theme.rememberWindowSizeInfo

/**
 * Main application screen for TribeTalk Translator.
 * Clean, modern educational voice & text translation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TranslationViewModel,
    flnViewModel: FlnViewModel? = null,
    onNavigateToFlashcards: (() -> Unit)? = null,
    onNavigateToWorksheets: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isHindiToSantali by viewModel.isHindiToSantali.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val amplitude by viewModel.amplitude.collectAsState()
    val textInput by viewModel.textInput.collectAsState()
    val isPlayingAudio by viewModel.isPlayingAudio.collectAsState()

    var showTextInputDrawer by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val windowSizeInfo = rememberWindowSizeInfo()
    val isCompactHeight = windowSizeInfo.isCompactHeight

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "TribeTalk",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!isCompactHeight) {
                            Text(
                                text = "Hindi <-> Santali Bidirectional Translation",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (conversations.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearHistory() }) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Clear History",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Direction & Status Subheader (Compact in Landscape)
            if (isCompactHeight) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LanguageSelectorPill(
                        isHindiToSantali = isHindiToSantali,
                        onSwapDirection = { viewModel.swapDirection() }
                    )
                    StatusIndicator(state = uiState)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LanguageSelectorPill(
                        isHindiToSantali = isHindiToSantali,
                        onSwapDirection = { viewModel.swapDirection() }
                    )
                    StatusIndicator(state = uiState)
                }
            }

            // Main Conversation Stream or Empty State (Centrally Constrained)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
                    .padding(horizontal = 16.dp)
            ) {
                if (conversations.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = EduPrimaryLight
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = null,
                                    tint = EduPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Ready to Translate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Tap the microphone below to speak in ${if (isHindiToSantali) "Hindi" else "Santali"}, or tap a quick instruction.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Quick Classroom Instructions",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EduPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val quickPrompts = if (isHindiToSantali) {
                            listOf("नमस्ते", "किताब निकालो", "घेरे में बैठो", "ध्यान से सुनो", "हाथ ऊपर करो", "शाबाश")
                        } else {
                            listOf("ᱡᱚᱦᱟᱨ", "ᱫᱩᱲᱩᱵ ᱯᱮ", "ᱯᱟᱲᱦᱟᱣ ᱯᱮ", "ᱟᱸᱡᱚᱢ ᱯᱮ", "ᱵᱮᱥ")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                        ) {
                            quickPrompts.take(3).forEach { prompt ->
                                Surface(
                                    onClick = { viewModel.translateDirect(prompt) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    shadowElevation = 1.dp
                                ) {
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                        ) {
                            quickPrompts.drop(3).forEach { prompt ->
                                Surface(
                                    onClick = { viewModel.translateDirect(prompt) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    shadowElevation = 1.dp
                                ) {
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(conversations) { exchange ->
                            ConversationCard(
                                exchange = exchange,
                                onPlayAudio = { viewModel.playAudio(exchange) },
                                isPlaying = isPlayingAudio,
                                onMakeFlashcard = if (flnViewModel != null) {
                                    { prompt ->
                                        flnViewModel.createCustomFlashcard(prompt)
                                        onNavigateToFlashcards?.invoke()
                                    }
                                } else null,
                                onGenerateWorksheet = if (flnViewModel != null) {
                                    { prompt ->
                                        flnViewModel.generateWorksheetFromTopic(prompt)
                                        onNavigateToWorksheets?.invoke()
                                    }
                                } else null
                            )
                        }
                    }
                }
            }

            // Optional Manual Text Input Drawer
            AnimatedVisibility(
                visible = showTextInputDrawer,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { viewModel.onTextInputChanged(it) },
                        placeholder = {
                            Text(
                                text = if (isHindiToSantali) "Type Devanagari text..." else "Type Ol Chiki text...",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            viewModel.translateManualText()
                            keyboardController?.hide()
                        })
                    )

                    IconButton(
                        onClick = {
                            viewModel.translateManualText()
                            keyboardController?.hide()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send Text",
                            tint = Color.White
                        )
                    }
                }
            }

            // Bottom Waveform and Action Center
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.outline)
                    .padding(
                        top = if (isCompactHeight) 4.dp else 10.dp,
                        bottom = if (isCompactHeight) 8.dp else 18.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 4.dp else 10.dp)
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 840.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 4.dp else 10.dp)
                ) {
                // Live Waveform Visualizer
                WaveformVisualizer(
                    amplitude = amplitude,
                    isActive = isRecording || isPlayingAudio,
                    barColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )

                // Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Toggle Text Drawer
                    IconButton(
                        onClick = { showTextInputDrawer = !showTextInputDrawer },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (showTextInputDrawer) Icons.Rounded.Close else Icons.Rounded.Keyboard,
                            contentDescription = "Toggle Keyboard Input",
                            tint = if (showTextInputDrawer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Hero Ripple Mic Button
                    RipplePulseButton(
                        isRecording = isRecording,
                        onClick = { viewModel.toggleRecording() }
                    )

                    // Balance placeholder to keep Mic Button centered
                    Spacer(modifier = Modifier.size(44.dp))
                }

                // Subtitle Instruction
                Text(
                    text = if (isRecording) "Recording... Tap stop when finished" else "Tap microphone to speak",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
}
