package com.example.lifeadvices11.ui.onboarding.sleep

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun PersonalSleepInfoStep(
    viewModel: SleepOnboardingViewModel,
    onNext: () -> Unit
) {
    var targetHours by remember { mutableStateOf("") }
    var bedTime by remember { mutableStateOf("") }
    var wakeTime by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ШАГ 1 ИЗ 3",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ваш режим сна",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = targetHours,
            onValueChange = {
                targetHours = it
                viewModel.updateTargetSleepHours(it)
            },
            label = { Text("Сколько часов сна вам нужно?") },
            placeholder = { Text("например, 8") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = { Text("часов") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = bedTime,
            onValueChange = {
                bedTime = it
                viewModel.updateBedTime(it)
            },
            label = { Text("Обычное время отхода ко сну") },
            placeholder = { Text("например, 23:00") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = wakeTime,
            onValueChange = {
                wakeTime = it
                viewModel.updateWakeTime(it)
            },
            label = { Text("Обычное время пробуждения") },
            placeholder = { Text("например, 07:00") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
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