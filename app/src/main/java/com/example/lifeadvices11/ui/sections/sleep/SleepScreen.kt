package com.example.lifeadvices11.ui.sections.sleep

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifeadvices11.data.entities.DailySleepEntity
import com.example.lifeadvices11.data.entities.SleepProfileEntity
import com.example.lifeadvices11.ui.navigation.Screen
import com.example.lifeadvices11.ui.onboarding.sleep.SleepOnboardingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(navController: NavController) {
    val viewModel: SleepViewModel = viewModel()
    val onboardingViewModel: SleepOnboardingViewModel = viewModel()

    var isLoading by remember { mutableStateOf(true) }
    var needsOnboarding by remember { mutableStateOf(false) }

    // ПРОВЕРКА ОНБОРДИНГА
    LaunchedEffect(Unit) {
        val hasOnboarding = withContext(Dispatchers.IO) {
            onboardingViewModel.hasCompletedOnboarding()
        }
        needsOnboarding = !hasOnboarding
        isLoading = false

        if (needsOnboarding) {
            navController.navigate(Screen.SleepOnboarding.route) {
                popUpTo(Screen.Sleep.route) { inclusive = true }
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (needsOnboarding) {
        return
    }

    val sleepProfile by viewModel.sleepProfile.collectAsState()
    val todaySleep by viewModel.todaySleep.collectAsState()
    val lastWeekSleep by viewModel.lastWeekSleep.collectAsState()
    val isLoadingData by viewModel.isLoading.collectAsState()

    val targetHours = sleepProfile?.targetSleepHours ?: 8.0
    val todayHours = todaySleep?.sleepHours ?: 0f
    val progress = (todayHours / targetHours).toFloat().coerceIn(0f, 1f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Сон", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.SleepPractices.route) }) {
                        Icon(Icons.Default.Spa, contentDescription = "Практики")
                    }
                    IconButton(onClick = { /* Статистика */ }) {
                        Icon(Icons.Default.ShowChart, contentDescription = "Статистика")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.SleepAddEntry.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { paddingValues ->
        if (isLoadingData) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { SleepProgressCard(targetHours, todayHours, progress) }
                item { TodaySleepCard(todaySleep, targetHours) }
                item { SleepRecommendationCard(sleepProfile) }

                if (lastWeekSleep.isNotEmpty()) {
                    item {
                        Text(
                            "История сна",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(lastWeekSleep) { sleep ->
                        SleepHistoryItem(sleep)
                    }
                }
            }
        }
    }
}

// Остальные функции без изменений...
@Composable
fun SleepProgressCard(targetHours: Double, todayHours: Float, progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Цель на сегодня", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(120.dp),
                    strokeWidth = 12.dp,
                    color = when {
                        progress >= 1f -> androidx.compose.ui.graphics.Color.Green
                        progress >= 0.8f -> androidx.compose.ui.graphics.Color(0xFFFFA500)
                        else -> androidx.compose.ui.graphics.Color.Red
                    }
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        String.format("%.1f", todayHours),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("из ${targetHours.toInt()} ч", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun TodaySleepCard(todaySleep: DailySleepEntity?, targetHours: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        if (todaySleep == null) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Нет записи за сегодня", style = MaterialTheme.typography.bodyLarge)
                Text("Нажмите + чтобы добавить", style = MaterialTheme.typography.bodySmall)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Сегодняшний сон", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${todaySleep.bedTime} -> ${todaySleep.wakeTime}")
                    Text("${String.format("%.1f", todaySleep.sleepHours)} / ${targetHours.toInt()} ч")
                    Text(getQualityText(todaySleep.quality))
                    if (todaySleep.notes.isNotBlank()) Text(todaySleep.notes)
                }
                Icon(Icons.Default.CheckCircle, contentDescription = "Готово")
            }
        }
    }
}

@Composable
fun SleepRecommendationCard(profile: SleepProfileEntity?) {
    if (profile == null) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Рекомендация", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            val recommendation = when {
                profile.targetSleepHours < 7 -> "Вам нужно спать больше. Старайтесь спать минимум 7-8 часов."
                profile.targetSleepHours > 9 -> "Оптимально спать 7-9 часов. Попробуйте сократить время сна."
                profile.sleepQuality == "poor" -> "Качество сна низкое. Ложитесь в одно время, уберите телефон за час до сна."
                profile.sleepIssues == "insomnia" -> "При бессоннице помогает тёплая ванна и отказ от кофеина после обеда."
                else -> "Отличный режим! Продолжайте в том же духе."
            }
            Text(recommendation)
        }
    }
}

@Composable
fun SleepHistoryItem(sleep: DailySleepEntity) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(dateFormat.format(Date(sleep.date)), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${sleep.bedTime} -> ${sleep.wakeTime}", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${String.format("%.1f", sleep.sleepHours)} ч", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(getQualityText(sleep.quality), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun getQualityText(quality: Int): String = when (quality) {
    5 -> "Отлично"
    4 -> "Хорошо"
    3 -> "Нормально"
    2 -> "Плохо"
    else -> "Ужасно"
}