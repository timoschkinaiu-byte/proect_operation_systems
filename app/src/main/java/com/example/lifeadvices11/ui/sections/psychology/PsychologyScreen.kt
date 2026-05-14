package com.example.lifeadvices11.ui.sections.psychology

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifeadvices11.data.entities.DailyEmotionEntity
import com.example.lifeadvices11.data.entities.PsychologyPracticeEntity
import com.example.lifeadvices11.data.entities.PsychologyProfileEntity
import com.example.lifeadvices11.data.entities.PsychologyTestResult
import com.example.lifeadvices11.ui.navigation.Screen
import com.example.lifeadvices11.ui.onboarding.psychology.PsychologyOnboardingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private data class MoodOption(
    val key: String,
    val title: String,
    val score: Int,
    val color: Color
)

private data class TestOption(
    val text: String,
    val score: Int
)

private data class TestOutcome(
    val resultLabel: String,
    val emotionalState: String,
    val advice: String
)

private data class PsychologyTestDefinition(
    val id: String,
    val title: String,
    val shortDescription: String,
    val aboutParagraphs: List<String>,
    val questions: List<String>,
    val options: List<TestOption>,
    val evaluator: (Int, Int) -> TestOutcome
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PsychologyScreen(navController: NavController) {
    val viewModel: PsychologyViewModel = viewModel()
    val onboardingViewModel: PsychologyOnboardingViewModel = viewModel()

    var isLoading by remember { mutableStateOf(true) }
    var needsOnboarding by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val hasOnboarding = withContext(Dispatchers.IO) {
            onboardingViewModel.hasCompletedOnboarding()
        }
        needsOnboarding = !hasOnboarding
        isLoading = false

        if (needsOnboarding) {
            navController.navigate(Screen.PsychologyOnboarding.route) {
                popUpTo(Screen.Psychology.route) { inclusive = true }
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (needsOnboarding) return

    val profile by viewModel.profile.collectAsState()
    val todayEmotion by viewModel.todayEmotion.collectAsState()
    val practices by viewModel.practices.collectAsState()
    val calendarDays by viewModel.calendarDays.collectAsState()
    val monthOffset by viewModel.calendarMonthOffset.collectAsState()
    val testResults by viewModel.testResults.collectAsState()
    val isLoadingData by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Психология") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Основная") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Тесты") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Практики") })
                }
            }
        }
    ) { paddingValues ->
        if (isLoadingData) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            when (selectedTab) {
                0 -> PsychologyMainTab(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    profile = profile,
                    todayEmotion = todayEmotion,
                    practices = practices.take(4),
                    monthOffset = monthOffset,
                    calendarDays = calendarDays,
                    onPreviousMonth = viewModel::previousCalendarMonth,
                    onNextMonth = viewModel::nextCalendarMonth,
                    onSaveMood = viewModel::saveTodayEmotion
                )

                1 -> PsychologyTestsTab(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    savedResults = testResults,
                    onSaveResult = viewModel::saveTestResult
                )

                else -> PsychologyPracticesTab(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    practices = practices
                )
            }
        }
    }
}

@Composable
private fun PsychologyMainTab(
    modifier: Modifier,
    profile: PsychologyProfileEntity?,
    todayEmotion: DailyEmotionEntity?,
    practices: List<PsychologyPracticeEntity>,
    monthOffset: Int,
    calendarDays: List<MoodCalendarDay>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSaveMood: (String, Int, String) -> Unit
) {
    var selectedMood by remember(todayEmotion?.mood) { mutableStateOf(todayEmotion?.mood ?: "") }
    var note by remember(todayEmotion?.note) { mutableStateOf(todayEmotion?.note ?: "") }
    var showMoodHistory by remember { mutableStateOf(false) }

    val moods = listOf(
        MoodOption("awful", "Ужасно", 1, Color(0xFFD32F2F)),
        MoodOption("bad", "Плохо", 2, Color(0xFFF57C00)),
        MoodOption("neutral", "Нормально", 3, Color(0xFFFBC02D)),
        MoodOption("good", "Хорошо", 4, Color(0xFF7CB342)),
        MoodOption("great", "Отлично", 5, Color(0xFF2E7D32))
    )

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Сегодняшнее состояние", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (profile?.goal) {
                        "reduce_anxiety" -> "Фокус: снижение тревожности"
                        "improve_mood" -> "Фокус: стабилизация настроения"
                        "understand_emotions" -> "Фокус: понимание эмоций"
                        "fight_burnout" -> "Фокус: снижение перегрузки"
                        else -> "Фокус будет персонализирован"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Удобное время чек-ина: ${profile?.preferredCheckInTime.orEmpty()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (todayEmotion != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Последняя запись сегодня: ${moodTitle(todayEmotion.mood)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Отметить настроение", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moods.forEach { mood ->
                        MoodChoiceChip(
                            option = mood,
                            selected = selectedMood == mood.key,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedMood = mood.key }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Короткая заметка") },
                    placeholder = { Text("Что повлияло на состояние сегодня?") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val selected = moods.firstOrNull { it.key == selectedMood } ?: moods[2]
                        onSaveMood(selected.key, selected.score, note)
                    },
                    enabled = selectedMood.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сохранить")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showMoodHistory = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("История настроения")
        }

        if (practices.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Рекомендованные практики",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            practices.forEach { practice ->
                PracticeCard(practice)
            }
        }
    }

    if (showMoodHistory) {
        MoodHistoryDialog(
            monthOffset = monthOffset,
            days = calendarDays,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onDismiss = { showMoodHistory = false }
        )
    }
}

@Composable
private fun MoodChoiceChip(
    option: MoodOption,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (selected) option.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val backgroundColor = if (selected) option.color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(width = 24.dp, height = 6.dp)
                    .background(option.color, RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = option.title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MoodHistoryDialog(
    monthOffset: Int,
    days: List<MoodCalendarDay>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, monthOffset)
    }
    val monthLabel = remember(monthOffset) {
        SimpleDateFormat("LLLL yyyy", Locale("ru")).format(calendar.time).replaceFirstChar { it.uppercase() }
    }
    val leadingOffset = remember(monthOffset) {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        (dayOfWeek + 5) % 7
    }
    val cells = remember(days, leadingOffset) {
        buildList<MoodCalendarDay?> {
            repeat(leadingOffset) { add(null) }
            addAll(days)
        }
    }
    var selectedDay by remember(days) { mutableStateOf<MoodCalendarDay?>(days.firstOrNull { it.entryCount > 0 }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPreviousMonth) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Предыдущий месяц")
                    }
                    Text(monthLabel, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onNextMonth) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Следующий месяц")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { label ->
                        Text(label, modifier = Modifier.width(36.dp), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        },
        text = {
            Column {
                cells.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        week.forEach { day ->
                            MoodCalendarCell(day = day, onClick = { selectedDay = day })
                        }
                        repeat(7 - week.size) {
                            Spacer(modifier = Modifier.size(36.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedDay != null && selectedDay?.entryCount ?: 0 > 0) {
                    Text("День ${selectedDay?.dayOfMonth}: ${averageMoodLabel(selectedDay?.averageMoodScore)}")
                    Text("Количество отметок: ${selectedDay?.entryCount}")
                } else {
                    Text("Выберите день с отметкой, чтобы посмотреть среднее настроение.")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
private fun MoodCalendarCell(
    day: MoodCalendarDay?,
    onClick: () -> Unit
) {
    if (day == null) {
        Spacer(modifier = Modifier.size(36.dp))
        return
    }

    val color = moodColorForAverage(day.averageMoodScore)
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(color, CircleShape)
            .clickable(enabled = day.entryCount > 0, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = if (day.entryCount == 0) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
        )
    }
}

@Composable
private fun PsychologyTestsTab(
    modifier: Modifier,
    savedResults: Map<String, PsychologyTestResult>,
    onSaveResult: (PsychologyTestResult) -> Unit
) {
    val tests = remember { psychologyTests() }
    var selectedTestId by remember { mutableStateOf<String?>(null) }

    if (selectedTestId == null) {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Психологические тесты", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Тесты помогают заметить возможные признаки состояния и лучше понять эмоциональный фон. Они не заменяют консультацию специалиста.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            tests.forEach { test ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { selectedTestId = test.id },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(test.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(test.shortDescription, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        savedResults[test.id]?.let { result ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = result.summaryLine,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    } else {
        val test = tests.first { it.id == selectedTestId }
        PsychologyTestDetail(
            modifier = modifier,
            test = test,
            savedResult = savedResults[test.id],
            onBack = { selectedTestId = null },
            onSaveResult = onSaveResult
        )
    }
}

@Composable
private fun PsychologyTestDetail(
    modifier: Modifier,
    test: PsychologyTestDefinition,
    savedResult: PsychologyTestResult?,
    onBack: () -> Unit,
    onSaveResult: (PsychologyTestResult) -> Unit
) {
    var isStarted by remember(test.id) { mutableStateOf(false) }
    var currentQuestionIndex by remember(test.id) { mutableIntStateOf(0) }
    var answers by remember(test.id) { mutableStateOf(List(test.questions.size) { -1 }) }
    var completedResult by remember(test.id) { mutableStateOf<PsychologyTestResult?>(null) }

    val activeResult = completedResult ?: savedResult
    val currentQuestion = test.questions[currentQuestionIndex]

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                if (isStarted && currentQuestionIndex > 0) {
                    currentQuestionIndex -= 1
                } else if (isStarted) {
                    isStarted = false
                } else {
                    onBack()
                }
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
            }
            Text(test.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!isStarted) {
            test.aboutParagraphs.forEach { paragraph ->
                Text(paragraph, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
            }

            activeResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Последний результат", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(result.summaryLine)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    isStarted = true
                    currentQuestionIndex = 0
                    answers = List(test.questions.size) { -1 }
                    completedResult = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Пройти тест")
            }
        } else if (completedResult == null) {
            Text(
                text = "Вопрос ${currentQuestionIndex + 1} из ${test.questions.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(currentQuestion, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            test.options.forEach { option ->
                OutlinedButton(
                    onClick = {
                        val updatedAnswers = answers.toMutableList()
                        updatedAnswers[currentQuestionIndex] = option.score
                        answers = updatedAnswers

                        if (currentQuestionIndex == test.questions.lastIndex) {
                            val score = updatedAnswers.filter { it >= 0 }.sum()
                            val maxScore = test.questions.size * test.options.maxOf { it.score }
                            val outcome = test.evaluator(score, maxScore)
                            val result = PsychologyTestResult(
                                testId = test.id,
                                testTitle = test.title,
                                score = score,
                                maxScore = maxScore,
                                resultLabel = outcome.resultLabel,
                                summaryLine = "${test.title}: ${outcome.resultLabel.lowercase()} ($score из $maxScore)",
                                emotionalState = outcome.emotionalState,
                                advice = outcome.advice,
                                completedAt = System.currentTimeMillis()
                            )
                            completedResult = result
                            onSaveResult(result)
                        } else {
                            currentQuestionIndex += 1
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(option.text)
                }
            }
        } else if (completedResult != null) {
            val result = completedResult!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(result.resultLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Эмоциональное состояние: ${result.emotionalState}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Советы: ${result.advice}")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        isStarted = false
                        currentQuestionIndex = 0
                        answers = List(test.questions.size) { -1 }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("К описанию")
                }
                Button(
                    onClick = {
                        currentQuestionIndex = 0
                        answers = List(test.questions.size) { -1 }
                        completedResult = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Пройти снова")
                }
            }
        }
    }
}

@Composable
private fun PsychologyPracticesTab(
    modifier: Modifier,
    practices: List<PsychologyPracticeEntity>
) {
    val groupedPractices = practices.groupBy { it.category }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Психологические практики", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        groupedPractices.forEach { (category, categoryPractices) ->
            Text(
                text = categoryTitle(category),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            categoryPractices.forEach { practice ->
                PracticeCard(practice)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PracticeCard(practice: PsychologyPracticeEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = practice.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (practice.isRecommended) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
                ) {
                    Text(
                        text = if (practice.isRecommended) "Рекомендуем" else "Библиотека",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(practice.shortDescription, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${practice.durationMinutes} мин • ${categoryTitle(practice.category)} • ${goalTitle(practice.targetGoal)}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(practice.fullDescription, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun psychologyTests(): List<PsychologyTestDefinition> {
    val frequencyOptions = listOf(
        TestOption("Никогда", 0),
        TestOption("Иногда", 1),
        TestOption("Часто", 2),
        TestOption("Почти постоянно", 3)
    )

    return listOf(
        PsychologyTestDefinition(
            id = "depression",
            title = "Тест на депрессию",
            shortDescription = "Проверка признаков сниженного настроения, апатии и потери интереса.",
            aboutParagraphs = listOf(
                "Депрессия может проявляться не только грустью, но и потерей сил, интереса и внутренней энергии.",
                "Человеку становится сложнее радоваться, концентрироваться и возвращаться к обычному ритму.",
                "Нередко появляются нарушения сна, самооценки и ощущение бессмысленности происходящего.",
                "Такое состояние может нарастать постепенно и долго оставаться незамеченным.",
                "Тест помогает заметить возможные признаки, но не заменяет очную диагностику."
            ),
            questions = listOf(
                "Вам стало труднее испытывать интерес к привычным делам?",
                "Часто ли вы чувствуете пустоту или эмоциональное онемение?",
                "Сложно ли вам вставать и включаться в день?",
                "Стало ли меньше ощущения радости даже от хороших событий?",
                "Трудно ли вам сосредоточиться на простых задачах?",
                "Появилось ли чувство собственной бесполезности?",
                "Стали ли вы сильнее уставать без явной причины?",
                "Есть ли ощущение, что всё даётся слишком тяжело?",
                "Часто ли хочется отдалиться от людей?",
                "Нарушился ли сон или привычный режим отдыха?",
                "Стало ли труднее принимать решения?",
                "Появились ли мысли, что впереди мало хорошего?",
                "Часто ли вы критикуете себя жёстче обычного?",
                "Сложно ли почувствовать мотивацию даже для небольшого шага?",
                "Есть ли ощущение затяжного внутреннего спада?"
            ),
            options = frequencyOptions,
            evaluator = { score, maxScore ->
                evaluateTest(
                    score = score,
                    maxScore = maxScore,
                    lowLabel = "Выраженных признаков депрессивного состояния сейчас немного",
                    moderateLabel = "Есть заметные признаки депрессивного состояния",
                    highLabel = "Есть выраженные признаки депрессивного состояния",
                    lowState = "Эмоциональный фон выглядит относительно устойчивым, хотя отдельные симптомы могли появляться ситуативно.",
                    moderateState = "Похоже, что настроение и уровень энергии заметно просели, а нагрузка ощущается тяжелее обычного.",
                    highState = "Состояние похоже на глубокий эмоциональный спад с выраженной усталостью, снижением интереса и ощущением тяжести.",
                    lowAdvice = "Продолжайте наблюдать за состоянием, поддерживайте режим сна и отдыха, не игнорируйте первые сигналы перегрузки.",
                    moderateAdvice = "Снизьте нагрузку, подключите поддержку близких и постарайтесь обратиться к психологу, если состояние держится дольше двух недель.",
                    highAdvice = "Лучше не оставаться с этим состоянием в одиночку и как можно скорее обратиться к специалисту за очной помощью."
                )
            }
        ),
        PsychologyTestDefinition(
            id = "anxiety",
            title = "Тест на тревожность",
            shortDescription = "Оценка внутреннего напряжения, тревожных мыслей и телесной тревоги.",
            aboutParagraphs = listOf(
                "Тревожность часто проявляется как постоянное внутреннее напряжение и ожидание чего-то плохого.",
                "Она может отражаться не только в мыслях, но и в теле: учащённом пульсе, зажимах, сложности расслабиться.",
                "Иногда тревога мешает концентрироваться, отдыхать и нормально спать.",
                "При длительной тревожности человек начинает жить в режиме постоянной настороженности.",
                "Тест помогает понять, насколько выражены такие проявления прямо сейчас."
            ),
            questions = listOf(
                "Часто ли вы ловите себя на тревожных сценариях?",
                "Сложно ли вам остановить поток беспокойных мыслей?",
                "Есть ли ощущение внутренней настороженности даже без причины?",
                "Становится ли телу трудно расслабиться к вечеру?",
                "Часто ли вы напряжены в груди, плечах или животе?",
                "Трудно ли вам переносить неопределённость?",
                "Проверяете ли вы одно и то же по нескольку раз из-за тревоги?",
                "Есть ли ощущение, что всё может резко пойти не так?",
                "Трудно ли вам успокоиться после стресса?",
                "Мешает ли тревога сосредоточиться на обычных делах?",
                "Часто ли вы ожидаете неприятные новости?",
                "Бывает ли трудно заснуть из-за мыслей?",
                "Есть ли ощущение, что тело постоянно в готовности?",
                "Трудно ли вам отпустить контроль?",
                "Часто ли беспокойство остаётся даже после того, как всё уже решилось?"
            ),
            options = frequencyOptions,
            evaluator = { score, maxScore ->
                evaluateTest(
                    score = score,
                    maxScore = maxScore,
                    lowLabel = "Выраженных признаков тревожного состояния сейчас немного",
                    moderateLabel = "Есть заметные признаки тревожного состояния",
                    highLabel = "Есть выраженные признаки тревожного состояния",
                    lowState = "Фон тревоги выглядит контролируемым, хотя отдельные напряжённые реакции могут появляться на стресс.",
                    moderateState = "Похоже, тревога уже влияет на концентрацию, отдых и ощущение безопасности.",
                    highState = "Состояние похоже на устойчивую тревожную перегрузку, которая затрагивает мысли, тело и повседневные дела.",
                    lowAdvice = "Полезно продолжать регулярные чек-ины и использовать короткие техники заземления в напряжённые дни.",
                    moderateAdvice = "Снизьте перегрузку, используйте дыхательные практики и подумайте о разговоре со специалистом, если напряжение держится часто.",
                    highAdvice = "Лучше обратиться к психологу или психотерапевту, особенно если тревога мешает спать, работать или чувствовать контроль над жизнью."
                )
            }
        ),
        PsychologyTestDefinition(
            id = "eating_disorder",
            title = "Тест на РПП",
            shortDescription = "Оценка тревоги вокруг еды, тела и контроля питания.",
            aboutParagraphs = listOf(
                "Расстройства пищевого поведения затрагивают не только питание, но и отношение к телу, контролю и самооценке.",
                "Человек может постоянно думать о еде, весе, правилах питания и бояться потерять контроль.",
                "Иногда чередуются строгие ограничения, чувство вины и эпизоды переедания.",
                "Такие состояния нередко поддерживаются тревогой, стыдом и жёсткой самокритикой.",
                "Тест нужен, чтобы заметить подозрительные сигналы и не игнорировать их."
            ),
            questions = listOf(
                "Часто ли вы тревожитесь из-за еды сильнее, чем хотелось бы?",
                "Бывает ли чувство вины после обычного приёма пищи?",
                "Сильно ли настроение зависит от веса или формы тела?",
                "Склонны ли вы жёстко ограничивать себя в еде?",
                "Случаются ли эпизоды, когда трудно остановиться в еде?",
                "Часто ли вы сравниваете своё тело с другими?",
                "Есть ли ощущение, что еда требует постоянного контроля?",
                "Трудно ли вам есть спокойно без внутренних правил?",
                "Влияет ли цифра на весах на самооценку?",
                "Избегаете ли вы некоторых продуктов из страха потерять контроль?",
                "Есть ли чувство стыда, связанное с телом или приёмом пищи?",
                "Часто ли вы думаете, что должны похудеть, даже если это не необходимо?",
                "Бывает ли, что вы скрываете свои пищевые привычки?",
                "Сильно ли вас пугает набор веса?",
                "Есть ли ощущение, что отношения с едой стали напряжёнными?"
            ),
            options = frequencyOptions,
            evaluator = { score, maxScore ->
                evaluateTest(
                    score = score,
                    maxScore = maxScore,
                    lowLabel = "Явных признаков выраженного РПП сейчас немного",
                    moderateLabel = "Есть заметные признаки напряжённых отношений с едой и телом",
                    highLabel = "Есть выраженные признаки риска расстройства пищевого поведения",
                    lowState = "Сейчас отношение к еде и телу выглядит относительно устойчивым, хотя отдельные тревожные мысли могут появляться.",
                    moderateState = "Похоже, что тема еды, контроля и самооценки уже вызывает заметное напряжение.",
                    highState = "Состояние похоже на выраженную тревогу вокруг питания и тела, которая может перерасти в стойкое расстройство.",
                    lowAdvice = "Полезно отслеживать жёсткие правила, которые появляются вокруг еды, и не усиливать контроль без необходимости.",
                    moderateAdvice = "Лучше мягко снизить жёсткость ограничений и обсудить ситуацию со специалистом, особенно если есть циклы запретов и срывов.",
                    highAdvice = "Стоит обратиться к психологу или психотерапевту с опытом работы с РПП и не пытаться справляться только самоконтролем."
                )
            }
        ),
        PsychologyTestDefinition(
            id = "irritability",
            title = "Тест на раздражительность",
            shortDescription = "Проверка уровня вспыльчивости, внутреннего напряжения и усталости от контактов.",
            aboutParagraphs = listOf(
                "Раздражительность может быть сигналом переутомления, перегруза, нехватки отдыха или накопленного стресса.",
                "Она часто проявляется как вспышки, нетерпимость к мелочам и ощущение, что всё цепляет сильнее обычного.",
                "Иногда за раздражительностью скрываются усталость, бессилие или непрожитые эмоции.",
                "Если такие реакции становятся постоянными, они начинают влиять на отношения и самочувствие.",
                "Тест помогает понять, насколько сильно напряжение уже сказывается на вас."
            ),
            questions = listOf(
                "Мелочи раздражают вас сильнее, чем раньше?",
                "Трудно ли вам сдерживать резкие реакции?",
                "Возникает ли ощущение, что окружающие быстро утомляют?",
                "Часто ли вы злитесь из-за задержек и ожидания?",
                "Есть ли чувство внутреннего кипения без явной причины?",
                "Раздражают ли вас обычные бытовые звуки и ситуации?",
                "Сложно ли спокойно воспринимать чужие ошибки?",
                "Чувствуете ли вы, что терпения стало меньше?",
                "Бывает ли, что после вспышки сложно быстро успокоиться?",
                "Есть ли ощущение, что вы на пределе?",
                "Раздражает ли необходимость много общаться?",
                "Трудно ли вам выдерживать плотный график без срывов?",
                "Бывает ли, что вы грубите больше, чем хотите?",
                "Ощущаете ли вы физическое напряжение перед вспышками?",
                "Есть ли чувство, что даже отдых не сразу снимает эту резкость?"
            ),
            options = frequencyOptions,
            evaluator = { score, maxScore ->
                evaluateTest(
                    score = score,
                    maxScore = maxScore,
                    lowLabel = "Выраженная раздражительность сейчас не выглядит ведущей проблемой",
                    moderateLabel = "Есть заметные признаки повышенной раздражительности",
                    highLabel = "Есть выраженные признаки перегрузки и раздражительности",
                    lowState = "Раздражение возникает ситуативно и пока не выглядит устойчивым фоном.",
                    moderateState = "Похоже, нервная система уже утомлена, а терпение и ресурс быстро заканчиваются.",
                    highState = "Состояние похоже на выраженную нервную перегрузку, когда даже мелочи вызывают сильную реакцию.",
                    lowAdvice = "Следите, что чаще всего запускает раздражение, и старайтесь не накапливать усталость.",
                    moderateAdvice = "Снизьте темп, возвращайте короткие паузы в день и не игнорируйте сигналы переутомления.",
                    highAdvice = "Лучше пересмотреть нагрузку и обратиться за поддержкой, если вспышки мешают отношениям, работе или вашему ощущению контроля."
                )
            }
        ),
        PsychologyTestDefinition(
            id = "hopelessness",
            title = "Тест на безнадёжность",
            shortDescription = "Оценка ощущения тупика, отсутствия опоры и веры в изменения.",
            aboutParagraphs = listOf(
                "Безнадёжность часто ощущается как внутреннее убеждение, что ничего не изменится к лучшему.",
                "При этом человеку трудно строить планы, ждать хорошего и видеть смысл в усилиях.",
                "Такой фон может сопровождать депрессивные и тревожные состояния, а также длительное выгорание.",
                "Особенно важно замечать его вовремя, если начинает казаться, что будущего будто нет.",
                "Тест нужен как ранняя проверка этого опасного эмоционального сигнала."
            ),
            questions = listOf(
                "Часто ли кажется, что впереди мало хорошего?",
                "Трудно ли вам верить, что ситуация может измениться?",
                "Есть ли ощущение тупика в важных сферах жизни?",
                "Сложно ли строить планы, потому что не верится в результат?",
                "Кажется ли, что ваши усилия мало что меняют?",
                "Чувствуете ли вы внутреннюю потерю опоры?",
                "Часто ли вы ждёте скорее плохого, чем хорошего?",
                "Есть ли чувство, что ресурсы заканчиваются без шанса восстановиться?",
                "Трудно ли вам представить желаемое будущее?",
                "Появляется ли мысль, что стараться уже бессмысленно?",
                "Есть ли ощущение, что помощь вряд ли что-то изменит?",
                "Сложно ли заметить даже небольшие поводы для надежды?",
                "Часто ли вы думаете, что всё повторится по плохому сценарию?",
                "Есть ли эмоциональная пустота вместо ожиданий?",
                "Кажется ли, что выхода почти нет?"
            ),
            options = frequencyOptions,
            evaluator = { score, maxScore ->
                evaluateTest(
                    score = score,
                    maxScore = maxScore,
                    lowLabel = "Сильных признаков безнадёжности сейчас немного",
                    moderateLabel = "Есть заметные признаки безнадёжности",
                    highLabel = "Есть выраженные признаки безнадёжности",
                    lowState = "Фон будущего пока выглядит достаточно устойчивым, хотя в трудные дни могут появляться мрачные мысли.",
                    moderateState = "Похоже, вера в изменения заметно просела, а будущее ощущается менее опорным.",
                    highState = "Состояние похоже на тяжёлое ощущение тупика и потери надежды, которое требует особого внимания.",
                    lowAdvice = "Продолжайте замечать даже маленькие шаги и опоры, которые помогают удерживать связь с реальностью.",
                    moderateAdvice = "Важно не оставаться с этим в одиночку и обсудить состояние с близким человеком или специалистом.",
                    highAdvice = "Лучше как можно скорее обратиться за профессиональной поддержкой, особенно если ощущение тупика сохраняется или усиливается."
                )
            }
        ),
        PsychologyTestDefinition(
            id = "burnout_work",
            title = "Тест на выгорание на работе",
            shortDescription = "Проверка истощения, цинизма и утраты ресурса в рабочей сфере.",
            aboutParagraphs = listOf(
                "Рабочее выгорание развивается постепенно и часто начинается с обычной усталости, которая уже не проходит после выходных.",
                "Со временем снижается интерес, появляется отстранённость, раздражение и ощущение, что сил всё меньше.",
                "Человек может продолжать работать по инерции, но внутренне чувствовать опустошение.",
                "Выгорание влияет не только на продуктивность, но и на самооценку, сон и отношения.",
                "Тест помогает увидеть, насколько работа уже истощает ваш ресурс."
            ),
            questions = listOf(
                "После работы вы чувствуете себя полностью выжатым?",
                "Стало ли труднее начинать рабочий день?",
                "Есть ли ощущение, что работа забирает почти все силы?",
                "Появилась ли эмоциональная отстранённость от задач?",
                "Трудно ли вам почувствовать интерес к своей работе?",
                "Часто ли вы делаете всё на автопилоте?",
                "Раздражают ли вас рабочие контакты сильнее обычного?",
                "Есть ли ощущение бессмысленности части задач?",
                "Снизилось ли чувство удовлетворения от результата?",
                "Бывает ли, что даже выходные не возвращают ресурс?",
                "Трудно ли вам включиться после отдыха?",
                "Есть ли ощущение, что вас слишком много требуют?",
                "Часто ли хочется отложить даже посильные задачи?",
                "Появился ли цинизм по отношению к работе или коллегам?",
                "Есть ли чувство, что вы больше не справляетесь так, как раньше?"
            ),
            options = frequencyOptions,
            evaluator = { score, maxScore ->
                evaluateTest(
                    score = score,
                    maxScore = maxScore,
                    lowLabel = "Выраженных признаков рабочего выгорания сейчас немного",
                    moderateLabel = "Есть заметные признаки рабочего выгорания",
                    highLabel = "Есть выраженные признаки рабочего выгорания",
                    lowState = "Рабочая нагрузка пока выглядит управляемой, хотя отдельные сигналы усталости могут уже появляться.",
                    moderateState = "Похоже, ресурс заметно просел, а работа всё чаще ощущается тяжёлой и эмоционально затратной.",
                    highState = "Состояние похоже на выраженное выгорание с истощением, отстранённостью и снижением внутренней отдачи.",
                    lowAdvice = "Полезно заранее защищать отдых, разделять рабочее и личное время и не ждать сильного истощения.",
                    moderateAdvice = "Стоит пересмотреть объём нагрузки, взять паузы и обсудить границы и поддержку на работе.",
                    highAdvice = "Лучше серьёзно пересмотреть режим, нагрузку и обратиться за поддержкой, чтобы не доводить состояние до срыва."
                )
            }
        ),
        PsychologyTestDefinition(
            id = "self_acceptance",
            title = "Тест на принятие себя",
            shortDescription = "Оценка жёсткости самокритики и способности относиться к себе бережно.",
            aboutParagraphs = listOf(
                "Принятие себя не означает идеализировать себя или перестать развиваться.",
                "Речь идёт о способности видеть свои ограничения без унижения и сохранять к себе уважение.",
                "Когда принятия себя мало, усиливается стыд, жёсткая самокритика и зависимость самооценки от ошибок.",
                "Это делает любые трудности болезненнее и отнимает много внутреннего ресурса.",
                "Тест помогает понять, насколько вы сейчас находитесь в контакте с собой без постоянного давления."
            ),
            questions = listOf(
                "Трудно ли вам относиться к своим ошибкам спокойно?",
                "Часто ли вы сравниваете себя с другими не в свою пользу?",
                "Есть ли ощущение, что вы недостаточно хороши?",
                "Склонны ли вы жёстко ругать себя за промахи?",
                "Зависит ли самооценка от того, как вы сегодня справились?",
                "Трудно ли вам принимать свои слабые стороны?",
                "Есть ли чувство, что к себе вы строже, чем к другим?",
                "Часто ли вы обесцениваете собственные достижения?",
                "Сложно ли вам поверить в свою ценность без доказательств?",
                "Есть ли чувство стыда за несовершенство?",
                "Трудно ли вам говорить с собой поддерживающе?",
                "Часто ли вам кажется, что вы должны быть лучше, чем есть?",
                "Бывает ли трудно принимать комплименты и признание?",
                "Есть ли ощущение, что любовь к себе нужно заслужить?",
                "Сложно ли вам оставаться на своей стороне в трудный момент?"
            ),
            options = frequencyOptions,
            evaluator = { score, maxScore ->
                evaluateTest(
                    score = score,
                    maxScore = maxScore,
                    lowLabel = "Уровень принятия себя выглядит относительно устойчивым",
                    moderateLabel = "Есть заметные трудности с принятием себя",
                    highLabel = "Есть выраженные трудности с принятием себя",
                    lowState = "Несмотря на отдельную самокритику, внутри есть опора и способность сохранять уважение к себе.",
                    moderateState = "Похоже, самокритика и сравнение уже заметно влияют на самооценку и эмоциональный фон.",
                    highState = "Состояние похоже на жёсткое внутреннее давление, при котором себя трудно поддерживать и принимать.",
                    lowAdvice = "Продолжайте замечать свои сильные стороны и не опирайтесь только на результат дня.",
                    moderateAdvice = "Полезно тренировать более поддерживающий внутренний диалог и уменьшать автоматическое сравнение с другими.",
                    highAdvice = "Стоит отдельно поработать с самооценкой и самокритикой, желательно вместе со специалистом."
                )
            }
        ),
        PsychologyTestDefinition(
            id = "bipolar",
            title = "Тест на биполярное расстройство",
            shortDescription = "Проверка признаков выраженных перепадов настроения и энергии.",
            aboutParagraphs = listOf(
                "Биполярное расстройство связано не просто со сменой настроения, а с выраженными фазами подъёма и спада.",
                "Во время подъёма может быть слишком много энергии, идей, импульсивности и снижаться потребность во сне.",
                "Во время спада, наоборот, появляются истощение, апатия и потеря интереса.",
                "Такие перепады обычно заметно влияют на поведение, решения и повседневную жизнь.",
                "Тест может показать только возможные признаки и не является медицинским диагнозом."
            ),
            questions = listOf(
                "Бывают ли у вас периоды необычно высокого подъёма и энергии?",
                "Случается ли, что вы почти не хотите спать, но не чувствуете усталости?",
                "Бывает ли, что мысли становятся слишком быстрыми?",
                "Есть ли периоды, когда вы становитесь необычно разговорчивы?",
                "Совершаете ли вы в такие периоды более рискованные поступки?",
                "Бывает ли, что уверенность в себе резко возрастает сверх обычного?",
                "Есть ли фазы, когда вы берётесь сразу за много дел?",
                "Сложно ли вам остановить активность во время подъёма?",
                "После таких периодов случается ли заметный эмоциональный спад?",
                "Бывают ли перепады настроения, которые длятся дольше обычной смены эмоций?",
                "Замечают ли близкие резкие изменения вашего состояния?",
                "Есть ли периоды импульсивных решений, о которых потом жалеете?",
                "Случается ли, что настроение резко скачет вместе с уровнем энергии?",
                "Есть ли фазы сильного возбуждения, а затем опустошения?",
                "Похоже ли это на повторяющийся цикл, а не на случайные эпизоды?"
            ),
            options = frequencyOptions,
            evaluator = { score, maxScore ->
                evaluateTest(
                    score = score,
                    maxScore = maxScore,
                    lowLabel = "Явных признаков выраженных фазовых перепадов сейчас немного",
                    moderateLabel = "Есть отдельные признаки заметных перепадов настроения и энергии",
                    highLabel = "Есть выраженные признаки перепадов настроения и энергии",
                    lowState = "Состояние не похоже на ярко выраженные фазовые колебания, хотя отдельные эмоциональные перепады могут случаться у любого человека.",
                    moderateState = "Похоже, есть колебания настроения и энергии, которые уже стоит наблюдать внимательнее.",
                    highState = "Состояние похоже на выраженные перепады настроения и энергии, которые лучше обсудить со специалистом.",
                    lowAdvice = "Продолжайте отслеживать режим сна, перепады энергии и значимые изменения поведения.",
                    moderateAdvice = "Полезно начать фиксировать эпизоды подъёма и спада по датам и обсудить это с психологом или психиатром.",
                    highAdvice = "Лучше обратиться к психиатру или психотерапевту, особенно если перепады влияют на сон, деньги, отношения и решения."
                )
            }
        )
    )
}

private fun evaluateTest(
    score: Int,
    maxScore: Int,
    lowLabel: String,
    moderateLabel: String,
    highLabel: String,
    lowState: String,
    moderateState: String,
    highState: String,
    lowAdvice: String,
    moderateAdvice: String,
    highAdvice: String
): TestOutcome {
    val ratio = if (maxScore == 0) 0f else score.toFloat() / maxScore.toFloat()
    return when {
        ratio < 0.34f -> TestOutcome(lowLabel, lowState, lowAdvice)
        ratio < 0.67f -> TestOutcome(moderateLabel, moderateState, moderateAdvice)
        else -> TestOutcome(highLabel, highState, highAdvice)
    }
}

private fun categoryTitle(category: String): String = when (category) {
    "breathing" -> "Дыхательные техники"
    "grounding" -> "Заземление"
    "journaling" -> "Записи и рефлексия"
    "awareness" -> "Осознанность"
    "self_support" -> "Самоподдержка"
    "body" -> "Работа с телом"
    "reflection" -> "Рефлексия"
    "activation" -> "Активация"
    "routine" -> "Восстанавливающие ритуалы"
    else -> category
}

private fun goalTitle(goal: String): String = when (goal) {
    "reduce_anxiety" -> "тревога"
    "improve_mood" -> "настроение"
    "understand_emotions" -> "эмоции"
    "fight_burnout" -> "перегрузка"
    else -> "состояние"
}

private fun moodTitle(mood: String): String = when (mood) {
    "awful" -> "Ужасно"
    "bad" -> "Плохо"
    "neutral" -> "Нормально"
    "good" -> "Хорошо"
    "great" -> "Отлично"
    else -> "Не выбрано"
}

private fun averageMoodLabel(score: Float?): String = when {
    score == null -> "Нет данных"
    score < 1.5f -> "Ужасно"
    score < 2.5f -> "Плохо"
    score < 3.5f -> "Нормально"
    score < 4.5f -> "Хорошо"
    else -> "Отлично"
}

private fun moodColorForAverage(score: Float?): Color {
    return when {
        score == null -> Color(0xFFE0E0E0)
        score < 1.5f -> Color(0xFFD32F2F)
        score < 2.5f -> Color(0xFFF57C00)
        score < 3.5f -> Color(0xFFFBC02D)
        score < 4.5f -> Color(0xFF7CB342)
        else -> Color(0xFF2E7D32)
    }
}
