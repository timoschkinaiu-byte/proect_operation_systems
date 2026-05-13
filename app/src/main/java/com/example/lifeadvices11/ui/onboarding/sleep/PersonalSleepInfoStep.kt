package com.example.lifeadvices11.ui.onboarding.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun PersonalSleepInfoStep(
    viewModel: SleepOnboardingViewModel,
    onNext: () -> Unit
) {
    val targetHours by viewModel.targetSleepHours.collectAsState()
    val bedTime by viewModel.bedTime.collectAsState()
    val wakeTime by viewModel.wakeTime.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Шаг 1 из 3",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Режим сна",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Укажем вашу целевую продолжительность сна и привычный график, чтобы настроить рекомендации и трекинг.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = targetHours,
            onValueChange = viewModel::updateTargetSleepHours,
            label = { Text("Сколько часов сна хотите получать?") },
            placeholder = { Text("Например, 8") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = {
                Text("Это значение будет показываться как ежедневная цель в разделе сна.")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = bedTime,
            onValueChange = viewModel::updateBedTime,
            label = { Text("Во сколько обычно ложитесь спать?") },
            placeholder = { Text("Например, 23:00") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = wakeTime,
            onValueChange = viewModel::updateWakeTime,
            label = { Text("Во сколько обычно просыпаетесь?") },
            placeholder = { Text("Например, 07:00") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Формат времени можно вводить в привычном виде, например 07:30 или 23:15.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            enabled = targetHours.isNotBlank() && bedTime.isNotBlank() && wakeTime.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Далее")
        }
    }
}
