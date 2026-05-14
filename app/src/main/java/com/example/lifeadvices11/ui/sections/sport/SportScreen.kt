@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.lifeadvices11.ui.sections.sport

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifeadvices11.data.models.WeekDay
import com.example.lifeadvices11.data.models.WorkoutLevel
import com.example.lifeadvices11.data.models.WorkoutProgram
import com.example.lifeadvices11.data.repositories.PlannedWorkout
import com.example.lifeadvices11.data.repositories.SportProfileSummary
import com.example.lifeadvices11.data.repositories.WorkoutHistoryDay
import com.example.lifeadvices11.data.repositories.WorkoutHistoryMonth
import com.example.lifeadvices11.data.repositories.WorkoutHistoryStatus
import com.example.lifeadvices11.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SportScreen(navController: NavController) {
    val viewModel: SportViewModel = viewModel()
    val isCheckingOnboarding by viewModel.isCheckingOnboarding.collectAsState()
    val needsOnboarding by viewModel.needsOnboarding.collectAsState()
    val todayWorkouts by viewModel.todayWorkouts.collectAsState()
    val plannedWorkouts by viewModel.plannedWorkouts.collectAsState()
    val levelInfo by viewModel.levelInfo.collectAsState()
    val profileSummary by viewModel.profileSummary.collectAsState()
    val historyMonth by viewModel.historyMonth.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showPlanScreen by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var selectedWorkout by remember { mutableStateOf<WorkoutProgram?>(null) }
    var addWorkoutTarget by remember { mutableStateOf<WorkoutProgram?>(null) }
    var movingWorkout by remember { mutableStateOf<PlannedWorkout?>(null) }

    LaunchedEffect(needsOnboarding, isCheckingOnboarding) {
        if (!isCheckingOnboarding && needsOnboarding) {
            navController.navigate(Screen.SportOnboarding.route) {
                popUpTo(Screen.Sport.route) { inclusive = true }
            }
        }
    }

    if (isCheckingOnboarding) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (needsOnboarding) {
        return
    }

    if (showPlanScreen) {
        TrainingPlanScreen(
            weekDays = viewModel.weekDays,
            plannedWorkouts = plannedWorkouts,
            movingWorkout = movingWorkout,
            onBack = {
                movingWorkout = null
                showPlanScreen = false
            },
            onDeleteWorkout = viewModel::deletePlannedWorkout,
            onStartMoving = { movingWorkout = it },
            onDropToDay = { day ->
                movingWorkout?.let { viewModel.movePlannedWorkout(it.plannedId, day.key) }
                movingWorkout = null
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Спорт") },
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
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Основной") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Все тренировки") }
                )
            }

            when {
                selectedWorkout != null -> WorkoutDetailScreen(
                    workout = selectedWorkout!!,
                    onBack = { selectedWorkout = null },
                    onAddToPlan = { addWorkoutTarget = selectedWorkout }
                )

                selectedTab == 0 -> SportMainTab(
                    todayWorkouts = todayWorkouts,
                    levelInfo = levelInfo,
                    summary = profileSummary,
                    goalTitle = { viewModel.goalTitle(it) },
                    focusTitle = { viewModel.focusTitle(it) },
                    levelTitle = { viewModel.levelTitle(it) },
                    onToggleCompleted = viewModel::toggleWorkoutCompletion,
                    onOpenPlan = { showPlanScreen = true },
                    onOpenHistory = { showHistoryDialog = true }
                )

                else -> AllWorkoutsTab(
                    workouts = viewModel.allPrograms,
                    onOpenWorkout = { selectedWorkout = it }
                )
            }
        }
    }

    addWorkoutTarget?.let { workout ->
        AddWorkoutToDayDialog(
            workout = workout,
            weekDays = viewModel.weekDays,
            onDismiss = { addWorkoutTarget = null },
            onSelectDay = { day ->
                viewModel.addWorkoutToDay(workout, day.key)
                addWorkoutTarget = null
            }
        )
    }

    if (showHistoryDialog && historyMonth != null) {
        WorkoutHistoryDialog(
            historyMonth = historyMonth!!,
            onDismiss = { showHistoryDialog = false },
            onPreviousMonth = viewModel::loadPreviousHistoryMonth,
            onNextMonth = viewModel::loadNextHistoryMonth
        )
    }
}

@Composable
private fun SportMainTab(
    todayWorkouts: List<PlannedWorkout>,
    levelInfo: WorkoutLevel?,
    summary: SportProfileSummary?,
    goalTitle: (String) -> String,
    focusTitle: (String) -> String,
    levelTitle: (String) -> String,
    onToggleCompleted: (Int, Boolean) -> Unit,
    onOpenPlan: () -> Unit,
    onOpenHistory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            summary?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Твой план спорта", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Цель: ${goalTitle(it.sportGoal)}")
                        Text("Фокус: ${focusTitle(it.bodyFocus)}")
                        Text("Уровень: ${levelTitle(it.fitnessLevel)}")
                        Text("Тренировок в неделю: ${it.trainingCount}")
                    }
                }
            }
        }

        item {
            levelInfo?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Уровень тренировок", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Уровень ${it.level}")
                        Text("Подходы: ${it.sets}")
                        Text("Повторения: ${it.reps}")
                        Text("Нагрузка: ${it.weight}")
                    }
                }
            }
        }

        item {
            Text(
                "Тренировка на сегодня",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (todayWorkouts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "На сегодня тренировки не запланированы. Добавь их в план тренировок.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(todayWorkouts) { plannedWorkout ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(plannedWorkout.workout.name, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${plannedWorkout.workout.durationMinutes} мин • ${plannedWorkout.workout.caloriesBurn} ккал",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Checkbox(
                            checked = plannedWorkout.isCompletedToday,
                            onCheckedChange = { checked ->
                                onToggleCompleted(plannedWorkout.plannedId, checked)
                            }
                        )
                    }
                }
            }
        }

        item {
            Button(onClick = onOpenPlan, modifier = Modifier.fillMaxWidth()) {
                Text("План тренировок")
            }
        }

        item {
            Button(onClick = onOpenHistory, modifier = Modifier.fillMaxWidth()) {
                Text("История")
            }
        }
    }
}

@Composable
private fun AllWorkoutsTab(
    workouts: List<WorkoutProgram>,
    onOpenWorkout: (WorkoutProgram) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(workouts) { workout ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = { onOpenWorkout(workout) }),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(workout.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(workout.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${workout.durationMinutes} мин • ${workout.caloriesBurn} ккал • ${workout.difficulty}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutDetailScreen(
    workout: WorkoutProgram,
    onBack: () -> Unit,
    onAddToPlan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                }
                Text(workout.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onAddToPlan) {
                Icon(Icons.Default.Add, contentDescription = "Добавить в план")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(workout.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text("${workout.durationMinutes} мин • ${workout.caloriesBurn} ккал", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Упражнения", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(workout.exercises) { exercise ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(exercise.name, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(exercise.description, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${exercise.defaultSets} подхода • ${exercise.defaultReps}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainingPlanScreen(
    weekDays: List<WeekDay>,
    plannedWorkouts: List<PlannedWorkout>,
    movingWorkout: PlannedWorkout?,
    onBack: () -> Unit,
    onDeleteWorkout: (Int) -> Unit,
    onStartMoving: (PlannedWorkout) -> Unit,
    onDropToDay: (WeekDay) -> Unit
) {
    val grouped = plannedWorkouts.groupBy { it.dayOfWeek }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("План тренировок") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (movingWorkout != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = "Выбрана тренировка \"${movingWorkout.workout.name}\". Нажми на день недели, чтобы перенести её.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            items(weekDays) { day ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                if (movingWorkout != null) {
                                    onDropToDay(day)
                                }
                            }
                        ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(day.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        val workouts = grouped[day.key].orEmpty()
                        if (workouts.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Тренировок нет", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            workouts.forEach { workout ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {},
                                            onLongClick = { onStartMoving(workout) }
                                        ),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(workout.workout.name)
                                        Text(
                                            "${workout.workout.durationMinutes} мин",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { onDeleteWorkout(workout.plannedId) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddWorkoutToDayDialog(
    workout: WorkoutProgram,
    weekDays: List<WeekDay>,
    onDismiss: () -> Unit,
    onSelectDay: (WeekDay) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить в план") },
        text = {
            Column {
                Text("Выбери день недели для тренировки \"${workout.name}\".")
                Spacer(modifier = Modifier.height(12.dp))
                weekDays.forEach { day ->
                    Button(
                        onClick = { onSelectDay(day) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(day.fullName)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
private fun WorkoutHistoryDialog(
    historyMonth: WorkoutHistoryMonth,
    onDismiss: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    var selectedDay by remember(historyMonth.monthStart) {
        mutableStateOf(historyMonth.days.firstOrNull { it.status != WorkoutHistoryStatus.NONE })
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPreviousMonth) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Предыдущий месяц")
                    }
                    Text(historyMonth.monthLabel, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onNextMonth) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Следующий месяц")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                CalendarHeader()
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(historyMonth.firstDayOffset) {
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                    historyMonth.days.forEach { day ->
                        HistoryDayCell(
                            day = day,
                            isSelected = selectedDay?.date == day.date,
                            onClick = { selectedDay = day }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text("Тренировок подряд: ${historyMonth.streak}", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))

                selectedDay?.let { day ->
                    val formatter = remember { SimpleDateFormat("d MMMM", Locale("ru")) }
                    Text(formatter.format(Date(day.date)), fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    when (day.status) {
                        WorkoutHistoryStatus.COMPLETED -> {
                            Text("Тренировка выполнена", color = Color(0xFF2E7D32))
                        }
                        WorkoutHistoryStatus.MISSED -> {
                            Text("Тренировка пропущена", color = Color(0xFFC62828))
                        }
                        WorkoutHistoryStatus.NONE -> {
                            Text("На этот день тренировка не запланирована")
                        }
                    }
                    if (day.workoutNames.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        day.workoutNames.forEach { name ->
                            Text("• $name")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Закрыть")
                }
            }
        }
    }
}

@Composable
private fun CalendarHeader() {
    val weekHeaders = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weekHeaders.forEach { day ->
            Text(day, modifier = Modifier.width(40.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HistoryDayCell(
    day: WorkoutHistoryDay,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val background = when (day.status) {
        WorkoutHistoryStatus.COMPLETED -> Color(0xFFB8E6C1)
        WorkoutHistoryStatus.MISSED -> Color(0xFFF5B7B1)
        WorkoutHistoryStatus.NONE -> MaterialTheme.colorScheme.surfaceVariant
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(background, shape = MaterialTheme.shapes.small)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(day.dayOfMonth.toString())
    }
}
