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
import org.tribetalk.ui.theme.DarkBorder
import org.tribetalk.ui.theme.DarkBorderGreen
import org.tribetalk.ui.theme.DarkCard
import org.tribetalk.ui.theme.DarkSurface
import org.tribetalk.ui.theme.EmeraldContainerDark
import org.tribetalk.ui.theme.EmeraldGreen
import org.tribetalk.ui.theme.EmeraldMint
import org.tribetalk.ui.theme.PureBlack
import org.tribetalk.ui.theme.PureWhite
import org.tribetalk.ui.theme.WhiteSecondary

/**
 * Main application screen for TribeTalk.
 * High-contrast Green, White, and Black styling.
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
        containerColor = PureBlack,
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
                                fontWeight = FontWeight.ExtraBold,
                                color = PureWhite
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmeraldContainerDark
                            ) {
                                Text(
                                    text = "100% Offline",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Hindi <-> Santali Bidirectional Translation",
                            style = MaterialTheme.typography.bodySmall,
                            color = WhiteSecondary
                        )
                    }
                },
                actions = {
                    if (conversations.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearHistory() }) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Clear History",
                                tint = WhiteSecondary
                            )
                        }
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
                .background(PureBlack)
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
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(50.dp),
                            shape = CircleShape,
                            color = DarkCard,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, DarkBorderGreen)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Ready to Translate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Tap the microphone below to speak in ${if (isHindiToSantali) "Hindi" else "Santali"}, or tap a quick instruction.",
                            style = MaterialTheme.typography.bodySmall,
                            color = WhiteSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Quick Classroom Instructions",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
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
                                    color = DarkCard,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderGreen)
                                ) {
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PureWhite,
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
                                    color = DarkCard,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderGreen)
                                ) {
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PureWhite,
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
                                fontSize = 14.sp,
                                color = WhiteSecondary
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = DarkBorder
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
                            .background(EmeraldGreen)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send Text",
                            tint = PureBlack
                        )
                    }
                }
            }

            // Bottom Waveform and Action Center
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .border(width = 0.5.dp, color = DarkBorder)
                    .padding(top = 12.dp, bottom = 20.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Live Waveform Visualizer
                WaveformVisualizer(
                    amplitude = amplitude,
                    isActive = isRecording || isPlayingAudio,
                    barColor = EmeraldGreen,
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
                            .background(DarkCard)
                            .border(1.dp, DarkBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (showTextInputDrawer) Icons.Rounded.Close else Icons.Rounded.Keyboard,
                            contentDescription = "Toggle Keyboard Input",
                            tint = if (showTextInputDrawer) EmeraldGreen else PureWhite
                        )
                    }

                    // Hero Ripple Mic Button
                    RipplePulseButton(
                        isRecording = isRecording,
                        onClick = { viewModel.toggleRecording() }
                    )

                    // Memory Footprint Chip
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = EmeraldContainerDark
                    ) {
                        Text(
                            text = "< 350 MB",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldMint,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Subtitle Instruction
                Text(
                    text = if (isRecording) "Recording... Tap stop when finished" else "Tap microphone to speak",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = WhiteSecondary
                )
            }
        }
    }
}
