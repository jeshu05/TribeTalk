package com.alchemists.tribetalk.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alchemists.tribetalk.curriculum.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
private fun NipunScaffold(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        if (subtitle != null) {
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (action != null) action()
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.shadow(1.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        content(innerPadding)
    }
}

// ---------------------------------------------------------------------------
// 1) Teacher Login / Register (local, offline-friendly)
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NipunTeacherLoginScreen(
    vm: NipunViewModel,
    onLoginSuccess: (Teacher) -> Unit,
    onBack: () -> Unit
) {
    var isRegister by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val error by vm.error.observeAsState()

    NipunScaffold(
        title = if (isRegister) "Teacher Registration" else "Teacher Login",
        subtitle = "Works fully offline",
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (isRegister) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Full Name") },
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") },
                        singleLine = true
                    )
                    if (error != null) {
                        Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = {
                            if (isRegister) {
                                vm.register(name, email, password) { t ->
                                    if (t != null) onLoginSuccess(t)
                                }
                            } else {
                                vm.login(email, password) { t ->
                                    if (t != null) onLoginSuccess(t)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (isRegister) "Register & Continue" else "Login", fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = if (isRegister) "Already registered? Login" else "New teacher? Register",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable { isRegister = !isRegister }
                            .padding(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 2) Classroom setup: how many classes + which classes
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NipunClassroomSetupScreen(
    vm: NipunViewModel,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val teacher by vm.teacher.observeAsState()
    var selected by remember { mutableStateOf(setOf<Int>()) }
    var classrooms by remember { mutableStateOf<List<Classroom>>(emptyList()) }

    LaunchedEffect(Unit) {
        vm.loadAllClassrooms { list -> classrooms = list }
    }

    LaunchedEffect(teacher) {
        teacher?.let { t ->
            if (vm.classrooms.value.isNotEmpty()) {
                selected = vm.classrooms.value!!.map { it.grade }.toSet()
            }
        }
    }

    NipunScaffold(
        title = "Select Your Classes",
        subtitle = "How many classes do you handle? (${selected.size})",
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Text(
                "Choose the classes whose NIPUN/FLN content you need.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            classrooms.forEach { c ->
                val checked = c.grade in selected
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            selected = if (checked) selected - c.grade else selected + c.grade
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (checked) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = checked, onCheckedChange = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            c.label,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (selected.isEmpty()) return@Button
                    vm.setTeacherClassrooms(selected.toList()) { onDone() }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = selected.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Continue to ${selected.size} class(es)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 3) NIPUN Home: shows teacher's classes to pick a class
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NipunHomeScreen(
    vm: NipunViewModel,
    onPickClass: (Int, ClassRoomLabel) -> Unit,
    onManageClasses: () -> Unit,
    onBack: () -> Unit
) {
    val classrooms by vm.classrooms.observeAsState()
    val list = classrooms.orEmpty()

    NipunScaffold(
        title = "NIPUN / FLN Curriculum",
        subtitle = "Choose a class to view its content",
        onBack = onBack,
        action = {
            IconButton(onClick = onManageClasses) {
                Icon(Icons.Default.Build, contentDescription = "Manage classes", tint = MaterialTheme.colorScheme.primary)
            }
        }
    ) { padding ->
        if (list.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No classes selected yet. Tap the settings icon.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(list) { c ->
                    MenuGridCard(
                        title = c.label,
                        icon = Icons.Default.School,
                        onClick = { onPickClass(c.grade, ClassRoomLabel(c.id, c.label)) }
                    )
                }
            }
        }
    }
}

data class ClassRoomLabel(val id: Long, val label: String)

// ---------------------------------------------------------------------------
// 4) Class content hub (grade-specific)
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NipunClassContentScreen(
    vm: NipunViewModel,
    grade: Int,
    classLabel: String,
    onOpenOutcomes: (List<LearningOutcome>) -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenWorksheets: () -> Unit,
    onOpenResources: () -> Unit,
    onOpenStudents: () -> Unit,
    onBack: () -> Unit
) {
    var outcomes by remember { mutableStateOf<List<LearningOutcome>>(emptyList()) }

    LaunchedEffect(grade) {
        vm.loadOutcomes(grade) { outcomes = it }
    }

    val m1 = MenuItem("Literacy", Icons.Default.MenuBook, "Foundational Literacy")
    val m2 = MenuItem("Numeracy", Icons.Default.Calculate, "Foundational Numeracy")

    NipunScaffold(
        title = classLabel,
        subtitle = "NIPUN / FLN content for this class",
        onBack = onBack
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(20.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item { MenuGridCard(m1.title, m1.icon, onClick = { onOpenOutcomes(outcomes) }) }
            item { MenuGridCard(m2.title, m2.icon, onClick = { onOpenOutcomes(outcomes) }) }
            item { MenuGridCard("Learning Outcomes", Icons.Default.EmojiEvents, onClick = { onOpenOutcomes(outcomes) }) }
            item { MenuGridCard("Flashcards", Icons.Default.Style, onClick = onOpenFlashcards) }
            item { MenuGridCard("Worksheets", Icons.Default.Description, onClick = onOpenWorksheets) }
            item { MenuGridCard("Resources", Icons.Default.Folder, onClick = onOpenResources) }
            item { MenuGridCard("Assessments", Icons.Default.Rule, onClick = { onOpenOutcomes(outcomes) }) }
            item { MenuGridCard("Students & Progress", Icons.Default.Groups, onClick = onOpenStudents) }
        }
    }
}

private data class MenuItem(val title: String, val icon: ImageVector, val desc: String)

// ---------------------------------------------------------------------------
// 5) Learning outcomes list (for a grade)
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NipunOutcomesScreen(
    vm: NipunViewModel,
    grade: Int,
    classLabel: String,
    onOpenOutcome: (LearningOutcome) -> Unit,
    onBack: () -> Unit
) {
    var outcomes by remember { mutableStateOf<List<LearningOutcome>>(emptyList()) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(grade) {
        vm.loadOutcomes(grade) { outcomes = it }
    }
    val filtered = if (query.isBlank()) outcomes else outcomes.filter {
        it.title.contains(query, ignoreCase = true) || (it.description?.contains(query, ignoreCase = true) == true)
    }

    NipunScaffold(title = "Learning Outcomes", subtitle = classLabel, onBack = onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search outcomes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors()
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { lo ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenOutcome(lo) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (lo.code != null) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            lo.code!!,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                }
                                if (!lo.isOfficial) {
                                    Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.small) {
                                        Text(
                                            "DEMO",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                lo.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            lo.description?.let {
                                Spacer(Modifier.height(4.dp))
                                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 6) Learning outcome detail: lessons / activities / assessment / flashcards
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NipunOutcomeDetailScreen(
    vm: NipunViewModel,
    grade: Int,
    lo: LearningOutcome,
    onOpenFlashcards: (LearningOutcome) -> Unit,
    onOpenWorksheets: (LearningOutcome) -> Unit,
    onBack: () -> Unit
) {
    val lessons by vm.lessons.observeAsState()
    val activities by vm.activities.observeAsState()
    val assessments by vm.assessments.observeAsState()

    LaunchedEffect(lo.id) { vm.loadOutcomeDetail(grade, lo.id) }

    NipunScaffold(
        title = "Learning Outcome",
        subtitle = lo.code ?: "LO",
        onBack = onBack
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(lo.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        lo.description?.let {
                            Spacer(Modifier.height(6.dp))
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row {
                            OutlinedButton(onClick = { onOpenFlashcards(lo) }) {
                                Icon(Icons.Default.Style, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Flashcards")
                            }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(onClick = { onOpenWorksheets(lo) }) {
                                Icon(Icons.Default.Description, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Worksheets")
                            }
                        }
                    }
                }
            }
            item { SectionHeader("Lessons", Icons.Default.MenuBook) }
            val sortedLessons = lessons.orEmpty()
            if (sortedLessons.isEmpty()) {
                item { EmptyNote("No lessons linked to this outcome") }
            } else {
                items(sortedLessons) { lesson -> LessonCard(lesson) }
            }
            item { SectionHeader("Activities", Icons.Default.Handyman) }
            val sortedActivities = activities.orEmpty()
            if (sortedActivities.isEmpty()) {
                item { EmptyNote("No activities linked to this outcome") }
            } else {
                items(sortedActivities) { act ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(act.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            act.instructions?.let {
                                Spacer(Modifier.height(4.dp)); Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                            act.type?.let {
                                Spacer(Modifier.height(6.dp))
                                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                                    Text(it, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
            item { SectionHeader("Assessments", Icons.Default.Rule) }
            val sortedAssessments = assessments.orEmpty()
            if (sortedAssessments.isEmpty()) {
                item { EmptyNote("No assessments linked to this outcome") }
            } else {
                items(sortedAssessments) { a ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(a.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            a.instruction?.let { Spacer(Modifier.height(4.dp)); Text(it, style = MaterialTheme.typography.bodySmall) }
                            a.prompt?.let { Spacer(Modifier.height(6.dp)); Text("Prompt: ${it}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EmptyNote(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
}

@Composable
private fun LessonCard(lesson: Lesson) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Text(lesson.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            lesson.content?.let { Spacer(Modifier.height(6.dp)); Text(it, style = MaterialTheme.typography.bodySmall) }
            lesson.story?.let {
                Spacer(Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Story / Rhyme", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 7) Flashcards
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NipunFlashcardsScreen(
    vm: NipunViewModel,
    grade: Int,
    lo: LearningOutcome?,
    classLabel: String,
    onBack: () -> Unit
) {
    var cards by remember { mutableStateOf<List<Flashcard>>(emptyList()) }
    var index by remember { mutableStateOf(0) }
    var flipped by remember { mutableStateOf(false) }
    val cardsLive by vm.flashcards.observeAsState()

    LaunchedEffect(grade, lo?.id) {
        vm.loadFlashcards(grade, lo?.id) {
            index = 0
            flipped = false
        }
    }
    cards = cardsLive.orEmpty()

    NipunScaffold(
        title = "Flashcards",
        subtitle = if (lo != null) "For: ${lo.title}" else classLabel,
        onBack = onBack
    ) { padding ->
        if (cards.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No flashcards for this selection yet.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (index in cards.indices) {
                    val card = cards[index]
                    Card(
                        modifier = Modifier.fillMaxWidth().height(280.dp).clickable { flipped = !flipped },
                        colors = CardDefaults.cardColors(
                            containerColor = if (flipped) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                                Text(
                                    if (flipped && !card.back.isNullOrBlank()) card.back!! else card.front,
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(card.cardType, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text("${index + 1} / ${cards.size}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = { index = (index - 1 + cards.size) % cards.size; flipped = false },
                            enabled = cards.size > 1
                        ) { Text("Previous") }
                        Button(
                            onClick = { index = (index + 1) % cards.size; flipped = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) { Text("Next") }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 8) Worksheets list + simple offline generator (local question bank)
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NipunWorksheetsScreen(
    vm: NipunViewModel,
    grade: Int,
    lo: LearningOutcome?,
    classLabel: String,
    onBack: () -> Unit
) {
    var worksheets by remember { mutableStateOf<List<Worksheet>>(emptyList()) }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedContent by remember { mutableStateOf("") }
    val worksheetsLive by vm.worksheets.observeAsState()

    LaunchedEffect(grade, lo?.id) {
        vm.loadWorksheets(grade, lo?.id)
    }
    worksheets = worksheetsLive.orEmpty()

    NipunScaffold(
        title = "Worksheets",
        subtitle = if (lo != null) "For: ${lo.title}" else classLabel,
        onBack = onBack
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Button(
                onClick = {
                    generatedContent = generateLocalWorksheet(lo)
                    isGenerating = true
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Generate Worksheet (local)")
            }

            if (isGenerating && generatedContent.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Preview", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(Modifier.height(8.dp))
                        Text(generatedContent, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "This worksheet is generated locally from the bundled question bank (demo).",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (worksheets.isEmpty() && !isGenerating) {
                    item { EmptyNote("No stored worksheets for this selection. Use Generate above.") }
                }
                items(worksheets) { w ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(w.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(Modifier.height(4.dp))
                            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                                Text(w.questionType, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun generateLocalWorksheet(lo: LearningOutcome?): String {
    val sb = StringBuilder()
    sb.appendLine("Worksheet — ${lo?.title ?: "General"}")
    sb.appendLine("(Generated offline from local question bank — DEMO)")
    sb.appendLine("=".repeat(30))
    sb.appendLine()
    if (lo?.code?.contains("COUNT") == true || lo?.code?.contains("NUMBER") == true) {
        sb.appendLine("1. Count the objects and write the number.")
        sb.appendLine("2. Match the numeral to its word: 3 -> three, 7 -> seven.")
        sb.appendLine("3. Circle the bigger number:  4  9  2")
    } else if (lo?.code?.contains("OPS") == true) {
        sb.appendLine("1. 2 + 3 = ____")
        sb.appendLine("2. 8 - 3 = ____")
        sb.appendLine("3. Draw 4 stars, then add 2 more. How many? ____")
    } else if (lo?.code?.contains("SHAPE") == true) {
        sb.appendLine("1. Circle all the squares.")
        sb.appendLine("2. Complete the pattern: ○ □ ○ □ __ __")
        sb.appendLine("3. Name the shape that has 3 sides: ____")
    } else {
        sb.appendLine("1. Write the missing letter: क_ताब, क_लम")
        sb.appendLine("2. Circle the word that starts with 'क':  कलम  घर  नाम")
        sb.appendLine("3. Draw and label one thing you see at school.")
    }
    return sb.toString()
}

// ---------------------------------------------------------------------------
// 9) Resources (uploaded PDF/PPT stored locally)
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NipunResourcesScreen(
    vm: NipunViewModel,
    grade: Int,
    classLabel: String,
    context: Context,
    onBack: () -> Unit
) {
    val resources by vm.userResources.observeAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val name = queryDisplayName(context, uri)
            val ext = name.substringAfterLast('.', "").lowercase()
            val fileType = when (ext) {
                "pdf" -> "pdf"
                "ppt" -> "ppt"
                "pptx" -> "pptx"
                else -> "unknown"
            }
            val copied = copyToPrivateStorage(context, uri, name, fileType)
            if (copied != null) {
                vm.addUserResource(grade, null, null, name, copied.path, fileType, null, copied.length())
            }
        }
    }

    LaunchedEffect(grade) { vm.loadUserResources(grade) }

    NipunScaffold(
        title = "Resources",
        subtitle = "$classLabel · PDF/PPT stored offline",
        onBack = onBack
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Button(
                onClick = { launcher.launch(arrayOf("application/pdf", "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation")) },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.UploadFile, null); Spacer(Modifier.width(6.dp)); Text("Add PDF / PPT / PPTX")
            }
            val list = resources.orEmpty()
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (list.isEmpty()) {
                    item { EmptyNote("No uploaded resources yet. Files are saved on this device.") }
                }
                items(list) { r ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.InsertDriveFile,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(r.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    "${r.fileType.uppercase()} · ${r.sizeBytes / 1024} KB",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.OpenInNew, contentDescription = "Open", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun queryDisplayName(context: Context, uri: android.net.Uri): String {
    return runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) c.getString(idx) ?: "resource" else "resource"
        } ?: "resource"
    }.getOrDefault("resource")
}

private fun copyToPrivateStorage(context: Context, uri: android.net.Uri, name: String, ext: String): File? {
    return runCatching {
        val dir = File(context.filesDir, "resources").apply { mkdirs() }
        val safeName = name.ifBlank { "resource_${System.currentTimeMillis()}" }
        val dest = File(dir, safeName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        dest
    }.getOrNull()
}

// ---------------------------------------------------------------------------
// 10) Students & progress
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NipunStudentsScreen(
    vm: NipunViewModel,
    grade: Int,
    classroomId: Long,
    classLabel: String,
    onBack: () -> Unit
) {
    val students by vm.students.observeAsState()
    var showAdd by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    LaunchedEffect(classroomId) { vm.loadStudents(classroomId) }
    LaunchedEffect(grade) { vm.loadProgress(grade) }

    NipunScaffold(
        title = "Students & Progress",
        subtitle = classLabel,
        onBack = onBack,
        action = {
            IconButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.PersonAdd, "Add student", tint = MaterialTheme.colorScheme.primary)
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (showAdd) {
                Card(Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Student name") },
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        Row {
                            Button(
                                onClick = {
                                    if (newName.isNotBlank()) {
                                        vm.addStudent(classroomId, newName.trim())
                                        newName = ""
                                        showAdd = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) { Text("Add") }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(onClick = { showAdd = false }) { Text("Cancel") }
                        }
                    }
                }
            }
            val list = students.orEmpty()
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (list.isEmpty()) {
                    item { EmptyNote("No students added yet. Tap + to add.") }
                }
                items(list) { s ->
                    StudentProgressCard(vm, grade, s, vm.progress.value.orEmpty())
                }
            }
        }
    }
}

@Composable
private fun StudentProgressCard(
    vm: NipunViewModel,
    grade: Int,
    student: Student,
    progress: List<StudentOutcomeProgress>
) {
    val statuses = listOf("NOT_STARTED", "NEEDS_SUPPORT", "DEVELOPING", "ACHIEVED")
    val labels = mapOf(
        "NOT_STARTED" to "Not Started",
        "NEEDS_SUPPORT" to "Needs Support",
        "DEVELOPING" to "Developing",
        "ACHIEVED" to "Achieved"
    )
    val count = progress.count { it.studentId == student.id }
    val achieved = progress.count { it.studentId == student.id && it.status == "ACHIEVED" }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(student.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text(
                        "$achieved / ${maxOf(count,1)} outcomes achieved",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                statuses.forEach { st ->
                    FilterChip(
                        selected = progress.any { it.studentId == student.id && it.status == st },
                        onClick = { vm.setProgress(student.id, 0, grade, st) },
                        label = { Text(labels[st]!!, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            Text(
                "Tap a status to update this student's progress.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
