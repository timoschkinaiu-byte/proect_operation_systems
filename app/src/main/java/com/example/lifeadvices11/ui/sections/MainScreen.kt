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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifeadvices11.ui.navigation.Screen
import com.example.lifeadvices11.ui.theme.Ink
import com.example.lifeadvices11.ui.theme.LavenderPrimary
import com.example.lifeadvices11.ui.theme.LavenderStrong
import com.example.lifeadvices11.ui.theme.MutedInk
import com.example.lifeadvices11.ui.theme.PeachCard
import com.example.lifeadvices11.ui.theme.RoseCard
import com.example.lifeadvices11.ui.theme.SkyCard
import com.example.lifeadvices11.ui.theme.WhiteSoft
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class MainSectionItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val cardColor: Color,
    val iconTint: Color,
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
        Triple("awful", 1, Color(0xFFE6A0A0)),
        Triple("bad", 2, Color(0xFFF0BC7D)),
        Triple("neutral", 3, Color(0xFFE7D37F)),
        Triple("good", 4, Color(0xFFA8D4B0)),
        Triple("great", 5, Color(0xFF7EBB97))
    )

    val sections = listOf(
        MainSectionItem(
            title = "Питание",
            icon = Icons.Filled.Restaurant,
            route = Screen.Nutrition.route,
            cardColor = SkyCard,
            iconTint = LavenderStrong,
            content = {
                val nutrition = state.nutrition
                if (nutrition == null) {
                    Text("Данные появятся после заполнения раздела.", color = MutedInk)
                } else {
                    Text("${nutrition.actualCalories} / ${nutrition.targetCalories} ккал", color = Ink)
                    Text(
                        "Б ${nutrition.actualProtein}/${nutrition.targetProtein} • Ж ${nutrition.actualFat}/${nutrition.targetFat} • У ${nutrition.actualCarbs}/${nutrition.targetCarbs}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk
                    )
                }
            }
        ),
        MainSectionItem(
            title = "Спорт",
            icon = Icons.Filled.FitnessCenter,
            route = Screen.Sport.route,
            cardColor = PeachCard,
            iconTint = Color(0xFFE2A15D),
            content = {
                val sport = state.sport
                if (sport == null) {
                    Text("На сегодня тренировка не запланирована.", color = MutedInk)
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
                            color = Ink
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
            cardColor = RoseCard,
            iconTint = Color(0xFFD88BAC),
            content = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    moodOptions.forEach { (mood, score, color) ->
                        val isSelected = state.psychology?.mood == mood
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 28.dp else 24.dp)
                                .clip(CircleShape)
                                .background(color = color.copy(alpha = if (isSelected) 1f else 0.7f))
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
            cardColor = Color(0xFFEDEAFE),
            iconTint = Color(0xFF8A7DDA),
            content = {
                val sleep = state.sleep
                if (sleep == null) {
                    Text("Данные появятся после заполнения раздела.", color = MutedInk)
                } else {
                    Text("${formatHours(sleep.actualHours.toDouble())} / ${formatHours(sleep.targetHours)}", color = Ink)
                }
            }
        ),
        MainSectionItem(
            title = "Учеба",
            icon = Icons.Filled.School,
            route = Screen.Study.route,
            cardColor = Color(0xFFEAF6F1),
            iconTint = Color(0xFF6BA68A),
            content = {
                val study = state.study
                if (study == null) {
                    Text("Данные появятся после заполнения раздела.", color = MutedInk)
                } else {
                    Text("${formatHours(study.actualHours.toDouble())} / ${formatHours(study.targetHours)}", color = Ink)
                }
            }
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Life Advices", fontWeight = FontWeight.Bold, color = WhiteSoft)
                        Text(currentDate, style = MaterialTheme.typography.bodySmall, color = WhiteSoft.copy(alpha = 0.88f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(
                    Brush.horizontalGradient(
                        listOf(LavenderPrimary, LavenderStrong)
                    )
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = WhiteSoft,
                tonalElevation = 10.dp
            ) {
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = LavenderPrimary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            itemsIndexed(sections) { _, section ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(section.route) },
                    colors = CardDefaults.cardColors(
                        containerColor = section.cardColor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(WhiteSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = section.icon,
                                contentDescription = section.title,
                                modifier = Modifier.size(24.dp),
                                tint = section.iconTint
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = section.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink
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
