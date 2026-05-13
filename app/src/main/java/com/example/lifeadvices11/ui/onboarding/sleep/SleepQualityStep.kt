package com.example.lifeadvices11.ui.onboarding.sleep

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SleepQualityStep(
    viewModel: SleepOnboardingViewModel,
    onNext: () -> Unit
) {
    val selectedQuality by viewModel.sleepQuality.collectAsState()

    val qualities = listOf(
        Triple("good", "Хорошее", "Чаще всего высыпаетесь и просыпаетесь бодрым."),
        Triple("normal", "Нормальное", "Иногда сон сбивается, но в целом режим устраивает."),
        Triple("poor", "Плохое", "Часто не высыпаетесь, тяжело уснуть или проснуться.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Шаг 2 из 3",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Качество сна",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Оценка сна нужна, чтобы подобрать практики для засыпания и дальнейшие советы в основном разделе.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        qualities.forEach { (key, title, description) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                onClick = { viewModel.updateSleepQuality(key) },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedQuality == key) {
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
                        selected = selectedQuality == key,
                        onClick = { viewModel.updateSleepQuality(key) }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
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
