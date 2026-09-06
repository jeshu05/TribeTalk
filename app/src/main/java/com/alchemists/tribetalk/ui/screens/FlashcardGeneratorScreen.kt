package com.alchemists.tribetalk.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alchemists.tribetalk.flashcards.FlashcardGenerator
import com.alchemists.tribetalk.flashcards.FlashcardSet
import com.alchemists.tribetalk.flashcards.NIPUNLearningFramework
import com.alchemists.tribetalk.translation.TranslationEngine

/**
 * Screen 1: Flashcard Set Generator.
 *
 * Allows teachers to select an FLN learning domain, specify skills/competencies,
 * and create visual bilingual flashcards either via 1-tap validated vocabulary or custom Hindi entry.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardGeneratorScreen(
    translationEngine: TranslationEngine,
    onFlashcardSetGenerated: (FlashcardSet) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val generator = remember(translationEngine) { FlashcardGenerator(translationEngine) }

    val presetTopics = remember { NIPUNLearningFramework.presetTopicTemplates.keys.toList() }
    val domains = remember { NIPUNLearningFramework.domains }

    var selectedTopic by remember { mutableStateOf(presetTopics.first()) }
    var customTopicText by remember { mutableStateOf("") }
    var setTitle by remember { mutableStateOf("Animals Around Us") }

    var selectedDomainIndex by remember { mutableIntStateOf(0) }
    val currentDomain = domains[selectedDomainIndex]
    var selectedSkill by remember { mutableStateOf(currentDomain.skills.first()) }

    var selectedGrade by remember { mutableStateOf("Grade 1-3 (FLN)") }
    var customWordsInput by remember { mutableStateOf("") }

    var domainDropdownExpanded by remember { mutableStateOf(false) }
    var skillDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Create Visual Flashcards",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            "NIPUN Bharat & FLN Aligned",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.shadow(1.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // NIPUN Bharat Alignment Info Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = "Pedagogy",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                "SEE ➔ READ ➔ SPEAK ➔ LEARN",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Bilingual Hindi + Santali (Ol Chiki) flashcards with visual aids for foundational classroom learning.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick Topic Presets
            item {
                Text(
                    "Quick Topics (Curriculum Aligned)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presetTopics) { topic ->
                        val isSelected = topic == selectedTopic && customTopicText.isEmpty()
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTopic = topic
                                customTopicText = ""
                                setTitle = topic.substringBefore(" (") + " Bilingual Cards"
                            },
                            label = { Text(topic, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // Set Title & Custom Topic
            item {
                OutlinedTextField(
                    value = setTitle,
                    onValueChange = { setTitle = it },
                    label = { Text("Flashcard Set Title") },
                    placeholder = { Text("e.g., Animals Around Us") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // NIPUN Bharat Learning Domain Dropdown
            item {
                ExposedDropdownMenuBox(
                    expanded = domainDropdownExpanded,
                    onExpandedChange = { domainDropdownExpanded = !domainDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "${currentDomain.title} (${currentDomain.flnGoal})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Learning Domain (FLN Goal)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = domainDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = domainDropdownExpanded,
                        onDismissRequest = { domainDropdownExpanded = false }
                    ) {
                        domains.forEachIndexed { index, domain ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(domain.title, fontWeight = FontWeight.SemiBold)
                                        Text(domain.flnGoal, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                },
                                onClick = {
                                    selectedDomainIndex = index
                                    selectedSkill = domains[index].skills.first()
                                    domainDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Learning Skill / Outcome Dropdown
            item {
                ExposedDropdownMenuBox(
                    expanded = skillDropdownExpanded,
                    onExpandedChange = { skillDropdownExpanded = !skillDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedSkill,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Competency / Learning Skill") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = skillDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = skillDropdownExpanded,
                        onDismissRequest = { skillDropdownExpanded = false }
                    ) {
                        currentDomain.skills.forEach { skill ->
                            DropdownMenuItem(
                                text = { Text(skill) },
                                onClick = {
                                    selectedSkill = skill
                                    skillDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Grade Level Selection
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Balvatika", "Grade 1", "Grade 2", "Grade 3", "Grade 1-3 (FLN)").forEach { grade ->
                        val isSelected = grade == selectedGrade
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { selectedGrade = grade }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = grade,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Instant 1-Tap Generation Action
            item {
                Button(
                    onClick = {
                        val activeTopic = if (customTopicText.isNotBlank()) customTopicText else selectedTopic
                        val flashcardSet = generator.createPresetSet(
                            topic = activeTopic,
                            domain = currentDomain.title,
                            skill = selectedSkill,
                            grade = selectedGrade
                        ).copy(title = setTitle.ifBlank { "$activeTopic Flashcard Set" })

                        Toast.makeText(context, "Generated ${flashcardSet.cards.size} Flashcards", Toast.LENGTH_SHORT).show()
                        onFlashcardSetGenerated(flashcardSet)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = "Create Flashcards", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "CREATE FROM VOCABULARY (1-TAP)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Divider for custom creation
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        "  OR ENTER CUSTOM WORDS  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
            }

            // Custom Hindi Words Entry
            item {
                OutlinedTextField(
                    value = customWordsInput,
                    onValueChange = { customWordsInput = it },
                    label = { Text("Custom Hindi Words (comma or newline separated)") },
                    placeholder = { Text("e.g., गाय, कुत्ता, मछली, पेड़, पानी") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Generate Custom Set Button
            item {
                OutlinedButton(
                    onClick = {
                        val words = customWordsInput.split(Regex("[,\\n]+"))
                            .map { it.trim() }
                            .filter { it.isNotBlank() }

                        if (words.isEmpty()) {
                            Toast.makeText(context, "Please enter at least one Hindi word", Toast.LENGTH_SHORT).show()
                            return@OutlinedButton
                        }

                        val activeTopic = if (customTopicText.isNotBlank()) customTopicText else setTitle
                        val flashcardSet = generator.createCustomSet(
                            title = setTitle.ifBlank { "$activeTopic Set" },
                            topic = activeTopic,
                            domain = currentDomain.title,
                            skill = selectedSkill,
                            grade = selectedGrade,
                            hindiWords = words
                        )

                        Toast.makeText(context, "Created ${flashcardSet.cards.size} Custom Flashcards", Toast.LENGTH_SHORT).show()
                        onFlashcardSetGenerated(flashcardSet)
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Custom Cards", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "CREATE CUSTOM FLASHCARD SET",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
