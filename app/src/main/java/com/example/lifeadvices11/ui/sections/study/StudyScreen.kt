package com.example.lifeadvices11.ui.sections.study

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
import com.example.lifeadvices11.data.entities.DailyStudyEntity
import com.example.lifeadvices11.data.entities.StudyCategoryEntity
import com.example.lifeadvices11.data.entities.StudyProfileEntity
import com.example.lifeadvices11.ui.navigation.Screen
import com.example.lifeadvices11.ui.onboarding.study.StudyOnboardingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(navController: NavController) {
    val viewModel: StudyViewModel = viewModel()
    val onboardingViewModel: StudyOnboardingViewModel = viewModel()

    var isLoading by remember { mutableStateOf(true) }
    var needsOnboarding by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val hasOnboarding = withContext(Dispatchers.IO) {
            onboardingViewModel.hasCompletedOnboarding()
        }
        needsOnboarding = !hasOnboarding
        isLoading = false

        if (needsOnboarding) {
            navController.navigate(Screen.StudyOnboarding.route) {
                popUpTo(Screen.Study.route) { inclusive = true }
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

    val profile by viewModel.studyProfile.collectAsState()
    val todayStudy by viewModel.todayStudy.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val lastWeekStudy by viewModel.lastWeekStudy.collectAsState()
    val isLoadingData by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Учеба") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (isLoadingData) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            StudyContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                profile = profile,
                todayStudy = todayStudy,
                categories = categories,
                lastWeekStudy = lastWeekStudy,
                onSaveHours = viewModel::updateTodayStudyHours
            )
        }
    }
}

@Composable
private fun StudyContent(
    modifier: Modifier,
    profile: StudyProfileEntity?,
    todayStudy: DailyStudyEntity?,
    categories: List<StudyCategoryEntity>,
    lastWeekStudy: List<DailyStudyEntity>,
    onSaveHours: (Float) -> Unit
) {
    var actualHoursInput by remember(todayStudy?.actualStudyHours) {
        mutableStateOf(if ((todayStudy?.actualStudyHours ?: 0f) > 0f) todayStudy?.actualStudyHours.toString() else "")
    }

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
                Text("Цель на сегодня", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${String.format(Locale.US, "%.1f", todayStudy?.actualStudyHours ?: 0f)} / ${String.format(Locale.US, "%.1f", profile?.targetStudyHours ?: 0.0)} ч",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Лучшее время: ${profile?.preferredStudyTime.orEmpty()} • Сессия: ${profile?.sessionDurationMinutes ?: 0} мин",
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
                Text("Фактическое время за сегодня", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = actualHoursInput,
                    onValueChange = { actualHoursInput = it },
                    label = { Text("Сколько часов реально учились сегодня?") },
                    placeholder = { Text("Например, 2.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onSaveHours(actualHoursInput.toFloatOrNull() ?: 0f) },
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
                Text("Предметы и задачи", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (profile?.planningStyle) {
                        "strict_plan" -> "Стиль планирования: четкий план"
                        "priority_list" -> "Стиль планирования: по приоритету"
                        "flexible" -> "Стиль планирования: гибкий режим"
                        else -> "Стиль планирования пока не задан"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (categories.isEmpty()) {
                    Text("Категории пока не добавлены.")
                } else {
                    categories.forEach { category ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(category.name)
                            Text("${String.format(Locale.US, "%.1f", category.plannedHours)} ч")
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        if (lastWeekStudy.isNotEmpty()) {
            val formatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
            Spacer(modifier = Modifier.height(16.dp))
            Text("История учебы", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            lastWeekStudy.forEach { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatter.format(Date(entry.date)))
                        Text("${String.format(Locale.US, "%.1f", entry.actualStudyHours)} ч")
                    }
                }
            }
        }
    }
}
