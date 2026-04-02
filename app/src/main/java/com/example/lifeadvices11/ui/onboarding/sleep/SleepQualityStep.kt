package com.example.lifeadvices11.ui.onboarding.sleep

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SleepQualityStep(
    viewModel: SleepOnboardingViewModel,
    onNext: () -> Unit
) {
    var selectedQuality by remember { mutableStateOf("") }

    val qualities = listOf(
        Triple("good", "😊 Хорошее", "Сплю достаточно, просыпаюсь отдохнувшим"),
        Triple("normal", "😐 Нормальное", "Бывают проблемы, но в целом нормально"),
        Triple("poor", "😫 Плохое", "Часто не высыпаюсь, тяжело просыпаюсь")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ШАГ 2 ИЗ 3",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Качество сна",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        qualities.forEach { (key, title, description) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                onClick = {
                    selectedQuality = key
                    viewModel.updateSleepQuality(key)
                },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedQuality == key)
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
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            enabled = selectedQuality.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Далее")
        }
    }
}