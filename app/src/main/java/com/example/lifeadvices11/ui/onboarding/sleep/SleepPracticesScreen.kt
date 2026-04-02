package com.example.lifeadvices11.ui.sections.sleep

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifeadvices11.data.entities.SleepPracticeEntity
import com.example.lifeadvices11.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepPracticesScreen(navController: NavController) {
    val viewModel: SleepViewModel = viewModel()
    val practices by viewModel.practices.collectAsState()
    var selectedCategory by remember { mutableStateOf("all") }

    val categories = listOf(
        "all" to "Все",
        "breathing" to "Дыхание",
        "relaxation" to "Расслабление",
        "meditation" to "Медитация",
        "habit" to "Привычки",
        "distraction" to "Отвлечение"
    )

    val filteredPractices = if (selectedCategory == "all") {
        practices
    } else {
        practices.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Практики для сна") },
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
        ) {
            ScrollableTabRow(
                selectedTabIndex = categories.indexOfFirst { it.first == selectedCategory },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEachIndexed { index, (key, title) ->
                    Tab(
                        selected = selectedCategory == key,
                        onClick = { selectedCategory = key },
                        text = { Text(title) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPractices) { practice ->
                    PracticeCard(
                        practice = practice,
                        onToggle = { viewModel.togglePracticeCompletion(practice) },
                        onClick = {
                            navController.navigate("${Screen.SleepPracticeDetail.route.replace("{practiceId}", practice.id.toString())}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PracticeCard(
    practice: SleepPracticeEntity,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (practice.isCompleted) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = practice.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (practice.isCompleted) TextDecoration.LineThrough else null
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = practice.shortDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Длительность: ${practice.duration} минут",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (practice.isCompleted) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.RadioButtonUnchecked
                    },
                    contentDescription = if (practice.isCompleted) "Выполнено" else "Отметить",
                    tint = if (practice.isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}