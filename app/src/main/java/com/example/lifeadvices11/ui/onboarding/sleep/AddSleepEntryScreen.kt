@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.lifeadvices11.ui.sections.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifeadvices11.ui.common.InputValidation
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AddSleepEntryScreen(navController: NavController) {
    val viewModel: SleepViewModel = viewModel()
    val scope = rememberCoroutineScope()

    var bedTime by remember { mutableStateOf("") }
    var wakeTime by remember { mutableStateOf("") }
    var quality by remember { mutableIntStateOf(3) }
    var notes by remember { mutableStateOf("") }

    val bedTimeError = InputValidation.validateTime(bedTime, "Время отхода ко сну")
    val wakeTimeError = InputValidation.validateTime(wakeTime, "Время пробуждения")
    val sleepHours = calculateSleepHours(bedTime, wakeTime)
    val durationError = when {
        bedTime.isBlank() || wakeTime.isBlank() || bedTimeError != null || wakeTimeError != null -> null
        sleepHours <= 0f || sleepHours > 16f -> "Продолжительность сна должна быть от 0.5 до 16 часов"
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить сон") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = bedTime,
                onValueChange = { bedTime = InputValidation.sanitizeTimeInput(it) },
                label = { Text("Время отхода ко сну") },
                placeholder = { Text("23:00") },
                modifier = Modifier.fillMaxWidth(),
                isError = bedTimeError != null,
                supportingText = { bedTimeError?.let { Text(it) } }
            )

            OutlinedTextField(
                value = wakeTime,
                onValueChange = { wakeTime = InputValidation.sanitizeTimeInput(it) },
                label = { Text("Время пробуждения") },
                placeholder = { Text("07:00") },
                modifier = Modifier.fillMaxWidth(),
                isError = wakeTimeError != null || durationError != null,
                supportingText = {
                    when {
                        wakeTimeError != null -> Text(wakeTimeError)
                        durationError != null -> Text(durationError)
                    }
                }
            )

            if (sleepHours > 0f && durationError == null) {
                Text(
                    text = "Продолжительность: ${String.format(Locale.US, "%.1f", sleepHours)} часов",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Text(text = "Качество сна", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 1..5) {
                    FilterChip(
                        selected = quality == i,
                        onClick = { quality = i },
                        label = { Text(getQualityEmoji(i)) }
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Заметки (опционально)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    scope.launch {
                        viewModel.addSleepEntry(
                            sleepHours = sleepHours,
                            bedTime = bedTime,
                            wakeTime = wakeTime,
                            quality = quality,
                            notes = notes
                        )
                        navController.popBackStack()
                    }
                },
                enabled = bedTime.isNotBlank() &&
                    wakeTime.isNotBlank() &&
                    bedTimeError == null &&
                    wakeTimeError == null &&
                    durationError == null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
        }
    }
}

private fun calculateSleepHours(bedTime: String, wakeTime: String): Float {
    if (bedTime.isBlank() || wakeTime.isBlank()) return 0f

    return try {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val bed = format.parse(bedTime) ?: return 0f
        val wake = format.parse(wakeTime) ?: return 0f

        var diff = wake.time - bed.time
        if (diff < 0) diff += 24 * 60 * 60 * 1000

        diff / (1000f * 60 * 60)
    } catch (_: Exception) {
        0f
    }
}

private fun getQualityEmoji(quality: Int): String {
    return when (quality) {
        5 -> "★★★★★"
        4 -> "★★★★"
        3 -> "★★★"
        2 -> "★★"
        else -> "★"
    }
}
