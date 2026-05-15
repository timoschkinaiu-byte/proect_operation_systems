package com.example.lifeadvices11.ui.sections.sleep

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifeadvices11.data.entities.DailySleepEntity
import com.example.lifeadvices11.data.entities.SleepProfileEntity
import com.example.lifeadvices11.ui.navigation.Screen
import com.example.lifeadvices11.ui.onboarding.sleep.SleepOnboardingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(navController: NavController) {
    val viewModel: SleepViewModel = viewModel()
    val onboardingViewModel: SleepOnboardingViewModel = viewModel()
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var needsOnboarding by remember { mutableStateOf(false) }
    var reminderEnabled by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.sleepProfile.value?.typicalBedTime?.takeIf { it.isNotBlank() }?.let {
                SleepReminderScheduler.schedule(context, it)
                reminderEnabled = true
            }
        } else {
            reminderEnabled = false
        }
    }

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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (needsOnboarding) return

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
                    IconButton(onClick = { navController.navigate(Screen.SleepInsights.route) }) {
                        Icon(Icons.Default.ShowChart, contentDescription = "Статистика сна")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.SleepAddEntry.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить запись")
            }
        }
    ) { paddingValues ->
        if (isLoadingData) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                item {
                    SleepReminderCard(
                        enabled = reminderEnabled,
                        targetBedTime = sleepProfile?.typicalBedTime.orEmpty(),
                        onToggle = { checked ->
                            if (!checked) {
                                reminderEnabled = false
                                return@SleepReminderCard
                            }

                            val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                sleepProfile?.typicalBedTime?.takeIf { it.isNotBlank() }?.let {
                                    SleepReminderScheduler.schedule(context, it)
                                    reminderEnabled = true
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                }

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

@Composable
private fun SleepReminderCard(
    enabled: Boolean,
    targetBedTime: String,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Напоминание о сне", fontWeight = FontWeight.SemiBold)
                Text(
                    if (targetBedTime.isNotBlank()) {
                        "Напомнить за час до $targetBedTime"
                    } else {
                        "Станет доступно после настройки времени сна"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                enabled = targetBedTime.isNotBlank()
            )
        }
    }
}

@Composable
fun SleepProgressCard(targetHours: Double, todayHours: Float, progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Цель на сегодня", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Box(contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(0.3f),
                    strokeWidth = 12.dp,
                    color = when {
                        progress >= 1f -> Color(0xFF2E7D32)
                        progress >= 0.8f -> Color(0xFFF9A825)
                        else -> Color(0xFFC62828)
                    }
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(String.format(Locale.US, "%.1f", todayHours), fontSize = 28.sp, fontWeight = FontWeight.Bold)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        if (todaySleep == null) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Сегодня запись сна еще не добавлена", style = MaterialTheme.typography.bodyLarge)
                Text("Нажмите +, чтобы сохранить сон", style = MaterialTheme.typography.bodySmall)
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
                    Text("${todaySleep.bedTime} - ${todaySleep.wakeTime}")
                    Text("${String.format(Locale.US, "%.1f", todaySleep.sleepHours)} / ${targetHours.toInt()} ч")
                    Text(getQualityText(todaySleep.quality))
                    if (todaySleep.notes.isNotBlank()) Text(todaySleep.notes)
                }
                Icon(Icons.Default.CheckCircle, contentDescription = "Запись добавлена")
            }
        }
    }
}

@Composable
fun SleepRecommendationCard(profile: SleepProfileEntity?) {
    if (profile == null) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Рекомендация", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            val recommendation = when {
                profile.targetSleepHours < 7 -> "Старайтесь увеличить сон хотя бы до 7-8 часов, чтобы восстановление было стабильнее."
                profile.targetSleepHours > 9 -> "Попробуйте держаться в диапазоне 7-9 часов, чтобы режим оставался ровным."
                profile.sleepQuality == "poor" -> "Если качество сна низкое, полезно убирать экран телефона хотя бы за час до сна."
                profile.sleepIssues == "insomnia" -> "При трудностях с засыпанием часто помогает спокойный вечерний ритуал и отказ от кофеина после обеда."
                else -> "Режим сна выглядит устойчиво. Сохраняйте его и следите за регулярностью отхода ко сну."
            }
            Text(recommendation)
        }
    }
}

@Composable
fun SleepHistoryItem(sleep: DailySleepEntity) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale("ru"))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    dateFormat.format(Date(sleep.date)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("${sleep.bedTime} - ${sleep.wakeTime}", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${String.format(Locale.US, "%.1f", sleep.sleepHours)} ч",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
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
    else -> "Очень плохо"
}
