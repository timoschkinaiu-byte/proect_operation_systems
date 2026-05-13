package com.example.lifeadvices11.ui.onboarding.sleep

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SleepIssuesStep(
    viewModel: SleepOnboardingViewModel,
    onComplete: () -> Unit
) {
    val selectedIssue by viewModel.sleepIssues.collectAsState()
    val preferredWakeTime by viewModel.preferredWakeTime.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    val issues = listOf(
        Triple("insomnia", "Бессонница", "Трудно заснуть или часто просыпаетесь ночью."),
        Triple("snoring", "Храп", "Храп мешает вам или окружающим."),
        Triple("night_wakeups", "Ночные пробуждения", "Просыпаетесь посреди ночи и тяжело засыпаете снова."),
        Triple("none", "Нет выраженных проблем", "Нужны базовые рекомендации и контроль режима сна.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Шаг 3 из 3",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Проблемы и цель",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Последний шаг нужен, чтобы сформировать персональные практики для улучшения засыпания и пробуждения.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        issues.forEach { (key, title, description) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                onClick = { viewModel.updateSleepIssues(key) },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedIssue == key) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedIssue == key,
                        onClick = { viewModel.updateSleepIssues(key) }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
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
            onValueChange = viewModel::updatePreferredWakeTime,
            label = { Text("Во сколько хотите просыпаться?") },
            placeholder = { Text("Например, 07:00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = {
                Text("Это время будет использоваться как ориентир для режима сна.")
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onComplete,
            enabled = selectedIssue.isNotBlank() && preferredWakeTime.isNotBlank() && !isSaving,
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
