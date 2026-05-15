@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.lifeadvices11.ui.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifeadvices11.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class MainSectionItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val content: @Composable () -> Unit
)

@Composable
fun MainScreen(navController: NavController) {
    val viewModel: MainViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val currentDate = remember {
        SimpleDateFormat("d MMMM, EEEE", Locale("ru")).format(Date())
            .replaceFirstChar { it.uppercase() }
    }

    val moodOptions = listOf(
        Triple("awful", 1, Color(0xFFD32F2F)),
        Triple("bad", 2, Color(0xFFF57C00)),
        Triple("neutral", 3, Color(0xFFFBC02D)),
        Triple("good", 4, Color(0xFF7CB342)),
        Triple("great", 5, Color(0xFF2E7D32))
    )

    val sections = listOf(
        MainSectionItem(
            title = "Питание",
            icon = Icons.Filled.Restaurant,
            route = Screen.Nutrition.route,
            content = {
                val nutrition = state.nutrition
                if (nutrition == null) {
                    Text("Цель на сегодня появится после заполнения раздела.")
                } else {
                    Text("${nutrition.actualCalories}/${nutrition.targetCalories} ккал")
                    Text(
                        "Б ${nutrition.actualProtein}/${nutrition.targetProtein} • Ж ${nutrition.actualFat}/${nutrition.targetFat} • У ${nutrition.actualCarbs}/${nutrition.targetCarbs}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        ),
        MainSectionItem(
            title = "Спорт",
            icon = Icons.Filled.FitnessCenter,
            route = Screen.Sport.route,
            content = {
                val sport = state.sport
                if (sport == null) {
                    Text("На сегодня тренировка не запланирована.")
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            sport.workoutName,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        sport.plannedId?.let { plannedId ->
                            Checkbox(
                                checked = sport.isCompleted,
                                onCheckedChange = { checked ->
                                    viewModel.toggleSportCompletion(plannedId, checked)
                                }
                            )
                        }
                    }
                }
            }
        ),
        MainSectionItem(
            title = "Психология",
            icon = Icons.Default.Psychology,
            route = Screen.Psychology.route,
            content = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    moodOptions.forEach { (mood, score, color) ->
                        val isSelected = state.psychology?.mood == mood
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 28.dp else 24.dp)
                                .background(
                                    color = color.copy(alpha = if (isSelected) 1f else 0.65f),
                                    shape = CircleShape
                                )
                                .clickable { viewModel.saveMood(mood, score) }
                        )
                    }
                }
            }
        ),
        MainSectionItem(
            title = "Сон",
            icon = Icons.Filled.Bedtime,
            route = Screen.Sleep.route,
            content = {
                val sleep = state.sleep
                if (sleep == null) {
                    Text("Цель на сегодня появится после заполнения раздела.")
                } else {
                    Text("${formatHours(sleep.actualHours.toDouble())} / ${formatHours(sleep.targetHours)}")
                }
            }
        ),
        MainSectionItem(
            title = "Учеба",
            icon = Icons.Filled.School,
            route = Screen.Study.route,
            content = {
                val study = state.study
                if (study == null) {
                    Text("Цель на сегодня появится после заполнения раздела.")
                } else {
                    Text("${formatHours(study.actualHours.toDouble())} / ${formatHours(study.targetHours)}")
                }
            }
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Life Advices") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                sections.forEach { section ->
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate(section.route) },
                        icon = {
                            Icon(
                                imageVector = section.icon,
                                contentDescription = section.title
                            )
                        },
                        label = null,
                        alwaysShowLabel = false
                    )
                }
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(sections) { section ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(section.route) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = section.title,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = section.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            section.content()
                        }
                    }
                }
            }
        }
    }
}

private fun formatHours(value: Double): String {
    return String.format(Locale.US, "%.1f ч", value)
}
