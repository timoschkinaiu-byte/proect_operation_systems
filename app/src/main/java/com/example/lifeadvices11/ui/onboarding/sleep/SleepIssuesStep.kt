package com.example.lifeadvices11.ui.onboarding.sleep

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SleepIssuesStep(
    viewModel: SleepOnboardingViewModel,
    onComplete: () -> Unit
) {
    var selectedIssues by remember { mutableStateOf("") }
    var preferredWakeTime by remember { mutableStateOf("") }
    val isSaving by viewModel.isSaving.collectAsState()

    val issues = listOf(
        Triple("insomnia", "🌙 Бессонница", "Трудно заснуть или частые пробуждения"),
        Triple("snoring", "😴 Храп", "Храплю или мешаю другим"),
        Triple("night_wakeups", "🔄 Ночные пробуждения", "Просыпаюсь ночью и не могу заснуть"),
        Triple("none", "✅ Нет проблем", "У меня нет проблем со сном")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ШАГ 3 ИЗ 3",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Проблемы со сном",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Какие проблемы вас беспокоят?",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(issues) { (key, title, description) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        selectedIssues = key
                        viewModel.updateSleepIssues(key)
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedIssues == key)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = preferredWakeTime,
            onValueChange = {
                preferredWakeTime = it
                viewModel.updatePreferredWakeTime(it)
            },
            label = { Text("В какое время хотите просыпаться?") },
            placeholder = { Text("например, 07:00") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onComplete,
            enabled = selectedIssues.isNotBlank() && !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Завершить")
            }
        }
    }
}