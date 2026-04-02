package com.example.lifeadvices11.ui.sections.sleep

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSleepEntryScreen(navController: NavController) {
    val viewModel: SleepViewModel = viewModel()
    val scope = rememberCoroutineScope()

    var bedTime by remember { mutableStateOf("") }
    var wakeTime by remember { mutableStateOf("") }
    var quality by remember { mutableStateOf(3) }
    var notes by remember { mutableStateOf("") }

    val sleepHours = calculateSleepHours(bedTime, wakeTime)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить сон") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
                onValueChange = { bedTime = it },
                label = { Text("Время отхода ко сну") },
                placeholder = { Text("23:00") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = wakeTime,
                onValueChange = { wakeTime = it },
                label = { Text("Время пробуждения") },
                placeholder = { Text("07:00") },
                modifier = Modifier.fillMaxWidth()
            )

            if (sleepHours > 0) {
                Text(
                    text = "💰 Продолжительность: ${String.format("%.1f", sleepHours)} часов",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Text(
                text = "Качество сна",
                style = MaterialTheme.typography.titleMedium
            )

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
                enabled = bedTime.isNotBlank() && wakeTime.isNotBlank(),
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

        (diff / (1000f * 60 * 60)).toFloat()
    } catch (e: Exception) {
        0f
    }
}

private fun getQualityEmoji(quality: Int): String {
    return when (quality) {
        5 -> "⭐⭐⭐⭐⭐"
        4 -> "⭐⭐⭐⭐"
        3 -> "⭐⭐⭐"
        2 -> "⭐⭐"
        else -> "⭐"
    }
}