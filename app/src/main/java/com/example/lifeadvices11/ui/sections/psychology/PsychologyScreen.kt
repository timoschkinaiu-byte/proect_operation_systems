package com.example.lifeadvices11.ui.sections.psychology

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifeadvices11.data.entities.DailyEmotionEntity
import com.example.lifeadvices11.data.entities.PsychologyPracticeEntity
import com.example.lifeadvices11.data.entities.PsychologyProfileEntity
import com.example.lifeadvices11.ui.navigation.Screen
import com.example.lifeadvices11.ui.onboarding.psychology.PsychologyOnboardingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PsychologyScreen(navController: NavController) {
    val viewModel: PsychologyViewModel = viewModel()
    val onboardingViewModel: PsychologyOnboardingViewModel = viewModel()

    var isLoading by remember { mutableStateOf(true) }
    var needsOnboarding by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

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
    val lastWeekEmotions by viewModel.lastWeekEmotions.collectAsState()
    val practices by viewModel.practices.collectAsState()
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
                    lastWeekEmotions = lastWeekEmotions,
                    practices = practices.take(4),
                    onSaveMood = viewModel::saveTodayEmotion
                )
                1 -> PsychologyTestsTab(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
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
    lastWeekEmotions: List<DailyEmotionEntity>,
    practices: List<PsychologyPracticeEntity>,
    onSaveMood: (String, Int, String) -> Unit
) {
    var selectedMood by remember(todayEmotion?.mood) { mutableStateOf(todayEmotion?.mood ?: "") }
    var note by remember(todayEmotion?.note) { mutableStateOf(todayEmotion?.note ?: "") }
    val moods = listOf(
        Triple("great", "Отлично", 5),
        Triple("good", "Хорошо", 4),
        Triple("neutral", "Нормально", 3),
        Triple("bad", "Тяжело", 2),
        Triple("awful", "Очень тяжело", 1)
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
                moods.forEach { (key, title, _) ->
                    Button(
                        onClick = { selectedMood = key },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(title)
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
                        val score = moods.firstOrNull { it.first == selectedMood }?.third ?: 3
                        onSaveMood(selectedMood, score, note)
                    },
                    enabled = selectedMood.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сохранить")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("История настроения", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (lastWeekEmotions.isEmpty()) {
                    Text("Пока нет отметок за последние дни.")
                } else {
                    val formatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
                    lastWeekEmotions.forEach { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(formatter.format(Date(entry.date)))
                            Text(entry.mood.ifBlank { "без отметки" })
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        if (practices.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Рекомендованные практики", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            practices.forEach { practice ->
                PracticeCard(practice)
            }
        }
    }
}

@Composable
private fun PsychologyTestsTab(modifier: Modifier) {
    val tests = listOf(
        "Шкала стресса: короткая самопроверка уровня напряжения.",
        "Самооценка настроения: помогает заметить динамику состояния.",
        "Проверка перегрузки: ориентир на признаки эмоционального выгорания."
    )

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Базовые тесты", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        tests.forEach { test ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = test,
                    modifier = Modifier.padding(16.dp)
                )
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
        Text(
            text = "Библиотека собрана по категориям, чтобы быстрее находить подходящий формат: дыхание, записи, заземление, самоподдержка и восстановление.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
