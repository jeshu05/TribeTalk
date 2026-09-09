package org.tribetalk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.tribetalk.ui.components.ConversationCard
import org.tribetalk.ui.components.LanguageSelectorPill
import org.tribetalk.ui.components.RipplePulseButton
import org.tribetalk.ui.components.StatusIndicator
import org.tribetalk.ui.components.WaveformVisualizer

/**
 * Main application screen for TribeTalk.
 * Provides live speech translation, visualizer, history stream, and manual input.
 * Built strictly with Material 3 design and zero unicode emojis.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TranslationViewModel,
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "TribeTalk",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "100% Offline",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Hindi <-> Santali Bidirectional Translation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Direction & Status Subheader
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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

            // Main Conversation Stream or Empty State
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (conversations.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Ready to Translate",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Tap the microphone below to speak in ${if (isHindiToSantali) "Hindi" else "Santali"}, or toggle keyboard input.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
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
                                isPlaying = isPlayingAudio
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
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
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
                    .padding(top = 12.dp, bottom = 20.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Live Waveform Visualizer
                WaveformVisualizer(
                    amplitude = amplitude,
                    isActive = isRecording || isPlayingAudio,
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
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = if (showTextInputDrawer) Icons.Rounded.Close else Icons.Rounded.Keyboard,
                            contentDescription = "Toggle Keyboard Input",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Hero Ripple Mic Button
                    RipplePulseButton(
                        isRecording = isRecording,
                        onClick = { viewModel.toggleRecording() }
                    )

                    // Memory Footprint Chip / Indicator
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "< 350 MB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Subtitle Instruction
                Text(
                    text = if (isRecording) "Recording... Tap stop when finished" else "Tap to speak",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
