@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.lifeadvices11.ui.sections.sport

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifeadvices11.data.models.Exercise
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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private data class SelectedWorkoutState(
    val workout: WorkoutProgram,
    val plannedId: Int?
)

private data class TechniqueFrame(
    val title: String,
    val hint: String,
    val pose: String
)

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
    var selectedWorkout by remember { mutableStateOf<SelectedWorkoutState?>(null) }
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

    if (needsOnboarding) return

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
            onOpenWorkout = { selectedWorkout = SelectedWorkoutState(it.workout, it.plannedId) },
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
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Основной") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Все тренировки") })
            }

            when {
                selectedWorkout != null -> WorkoutDetailScreen(
                    workout = selectedWorkout!!.workout,
                    plannedId = selectedWorkout!!.plannedId,
                    availableExercises = viewModel.allExercises,
                    onBack = { selectedWorkout = null },
                    onAddToPlan = { workout -> addWorkoutTarget = workout },
                    onDeleteWorkout = if (selectedWorkout!!.plannedId != null) {
                        {
                            viewModel.deletePlannedWorkout(selectedWorkout!!.plannedId!!)
                            selectedWorkout = null
                        }
                    } else null,
                    onSaveChanges = if (selectedWorkout!!.plannedId != null) {
                        { exercises ->
                            viewModel.updateWorkoutExercises(selectedWorkout!!.plannedId!!, exercises)
                            selectedWorkout = selectedWorkout?.copy(
                                workout = selectedWorkout!!.workout.copy(
                                    exercises = exercises,
                                    durationMinutes = calculateWorkoutDuration(exercises, selectedWorkout!!.workout.durationMinutes),
                                    caloriesBurn = calculateWorkoutCalories(selectedWorkout!!.workout, exercises)
                                )
                            )
                        }
                    } else null,
                    recommendedReplacements = viewModel::recommendedReplacementExercises
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
                    onOpenHistory = { showHistoryDialog = true },
                    onOpenWorkout = { selectedWorkout = SelectedWorkoutState(it.workout, it.plannedId) }
                )

                else -> AllWorkoutsTab(
                    workouts = viewModel.allPrograms,
                    onOpenWorkout = { selectedWorkout = SelectedWorkoutState(it, null) }
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
    onOpenHistory: () -> Unit,
    onOpenWorkout: (PlannedWorkout) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            summary?.let {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
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
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
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
            Text("Тренировка на сегодня", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (todayWorkouts.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Text(
                        text = "На сегодня тренировки не запланированы. Добавь их в план тренировок.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(todayWorkouts) { plannedWorkout ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenWorkout(plannedWorkout) },
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
                            onCheckedChange = { checked -> onToggleCompleted(plannedWorkout.plannedId, checked) }
                        )
                    }
                }
            }
        }

        item {
            Button(onClick = onOpenPlan, modifier = Modifier.fillMaxWidth()) { Text("План тренировок") }
        }

        item {
            Button(onClick = onOpenHistory, modifier = Modifier.fillMaxWidth()) { Text("История") }
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
    plannedId: Int?,
    availableExercises: List<Exercise>,
    onBack: () -> Unit,
    onAddToPlan: (WorkoutProgram) -> Unit,
    onDeleteWorkout: (() -> Unit)?,
    onSaveChanges: ((List<Exercise>) -> Unit)?,
    recommendedReplacements: (Exercise, WorkoutProgram) -> Pair<List<Exercise>, List<Exercise>>
) {
    var isEditMode by remember(workout.id, plannedId) { mutableStateOf(false) }
    var exercises by remember(workout, plannedId) { mutableStateOf(workout.exercises) }
    var replaceIndex by remember { mutableStateOf<Int?>(null) }

    val editedWorkout = workout.copy(
        exercises = exercises,
        durationMinutes = calculateWorkoutDuration(exercises, workout.durationMinutes),
        caloriesBurn = calculateWorkoutCalories(workout, exercises)
    )

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
                Text(editedWorkout.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Row {
                if (plannedId == null) {
                    IconButton(onClick = { onAddToPlan(editedWorkout) }) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить в план")
                    }
                }
                onDeleteWorkout?.let {
                    IconButton(onClick = it) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить тренировку")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(editedWorkout.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text("${editedWorkout.durationMinutes} мин • ${editedWorkout.caloriesBurn} ккал", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Упражнения", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = { isEditMode = !isEditMode }) {
                Icon(Icons.Default.Edit, contentDescription = "Редактировать тренировку")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f, fill = false)) {
            items(exercises.size) { index ->
                val exercise = exercises[index]
                ExerciseCard(
                    exercise = exercise,
                    editMode = isEditMode,
                    isFirst = index == 0,
                    isLast = index == exercises.lastIndex,
                    onMoveUp = {
                        if (index > 0) exercises = exercises.toMutableList().apply {
                            add(index - 1, removeAt(index))
                        }
                    },
                    onMoveDown = {
                        if (index < exercises.lastIndex) exercises = exercises.toMutableList().apply {
                            add(index + 1, removeAt(index))
                        }
                    },
                    onDelete = {
                        if (exercises.size > 1) {
                            exercises = exercises.toMutableList().apply { removeAt(index) }
                        }
                    },
                    onReplace = { replaceIndex = index },
                    onUpdate = { updated ->
                        exercises = exercises.toMutableList().apply { set(index, updated) }
                    }
                )
            }
        }

        if (plannedId != null && isEditMode) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onSaveChanges?.invoke(exercises) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить изменения")
            }
        }
    }

    replaceIndex?.let { index ->
        val current = exercises[index]
        val (recommended, others) = recommendedReplacements(current, editedWorkout)
        ReplaceExerciseDialog(
            current = current,
            recommended = recommended,
            others = availableExercises.filterNot { it.id == current.id }.filter { item ->
                recommended.none { it.id == item.id }
            } + others.filter { extra -> recommended.none { it.id == extra.id } },
            onDismiss = { replaceIndex = null },
            onSelect = { replacement ->
                exercises = exercises.toMutableList().apply {
                    set(
                        index,
                        replacement.copy(
                            defaultSets = current.defaultSets,
                            defaultReps = replacement.defaultReps,
                            defaultDurationMinutes = replacement.defaultDurationMinutes,
                            weightKg = replacement.weightKg
                        )
                    )
                }
                replaceIndex = null
            }
        )
    }
}

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    editMode: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    onReplace: () -> Unit,
    onUpdate: (Exercise) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(exercise.name, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(exercise.description, style = MaterialTheme.typography.bodySmall)
                }
                if (editMode) {
                    Row {
                        IconButton(onClick = onMoveUp, enabled = !isFirst) {
                            Icon(Icons.Default.ArrowDropUp, contentDescription = "Выше")
                        }
                        IconButton(onClick = onMoveDown, enabled = !isLast) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Ниже")
                        }
                        IconButton(onClick = onReplace) {
                            Icon(Icons.Default.Edit, contentDescription = "Заменить упражнение")
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить упражнение")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TechniqueIllustrationRow(exercise)
            Spacer(modifier = Modifier.height(8.dp))

            if (editMode) {
                OutlinedTextField(
                    value = exercise.defaultReps,
                    onValueChange = { onUpdate(exercise.copy(defaultReps = it)) },
                    label = { Text("Повторения") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = (exercise.defaultDurationMinutes ?: 0).takeIf { it > 0 }?.toString().orEmpty(),
                        onValueChange = { onUpdate(exercise.copy(defaultDurationMinutes = it.toIntOrNull())) },
                        label = { Text("Минуты") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = exercise.weightKg?.toString().orEmpty(),
                        onValueChange = { onUpdate(exercise.copy(weightKg = it.toIntOrNull())) },
                        label = { Text("Вес кг") },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text(
                    buildString {
                        append("${exercise.defaultSets} подхода • ${exercise.defaultReps}")
                        exercise.defaultDurationMinutes?.let { append(" • $it мин") }
                        exercise.weightKg?.let { append(" • $it кг") }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TechniqueIllustrationRow(exercise: Exercise) {
    val frames = techniqueFramesFor(exercise)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        frames.forEach { frame ->
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TechniqueFigure(pose = frame.pose)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(frame.title, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(frame.hint, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun TechniqueFigure(pose: String) {
    Canvas(modifier = Modifier.size(72.dp)) {
        val stroke = Stroke(width = 6f, cap = StrokeCap.Round)
        val centerX = size.width / 2f
        val headY = 12f
        val neckY = 22f
        val hipY = 42f
        val floorY = 60f
        val accent = Color(0xFF4E7A5D)

        drawCircle(color = accent, radius = 8f, center = Offset(centerX, headY))

        fun line(start: Offset, end: Offset) {
            drawLine(color = accent, start = start, end = end, strokeWidth = 6f, cap = StrokeCap.Round)
        }

        when (pose) {
            "squat" -> {
                line(Offset(centerX, neckY), Offset(centerX, hipY - 4))
                line(Offset(centerX, 28f), Offset(centerX - 14f, 34f))
                line(Offset(centerX, 28f), Offset(centerX + 14f, 34f))
                line(Offset(centerX, hipY - 4), Offset(centerX - 14f, 52f))
                line(Offset(centerX - 14f, 52f), Offset(centerX - 2f, floorY))
                line(Offset(centerX, hipY - 4), Offset(centerX + 14f, 52f))
                line(Offset(centerX + 14f, 52f), Offset(centerX + 2f, floorY))
            }
            "plank" -> {
                line(Offset(18f, 30f), Offset(54f, 30f))
                line(Offset(18f, 30f), Offset(12f, 48f))
                line(Offset(54f, 30f), Offset(60f, 48f))
                line(Offset(22f, 26f), Offset(14f, 20f))
            }
            "lunge" -> {
                line(Offset(centerX, neckY), Offset(centerX, hipY))
                line(Offset(centerX, 28f), Offset(centerX - 14f, 30f))
                line(Offset(centerX, 28f), Offset(centerX + 14f, 26f))
                line(Offset(centerX, hipY), Offset(centerX - 12f, 58f))
                line(Offset(centerX, hipY), Offset(centerX + 18f, 50f))
                line(Offset(centerX + 18f, 50f), Offset(centerX + 28f, 60f))
            }
            "bridge" -> {
                line(Offset(18f, 48f), Offset(30f, 40f))
                line(Offset(30f, 40f), Offset(44f, 34f))
                line(Offset(44f, 34f), Offset(56f, 48f))
                line(Offset(22f, 48f), Offset(14f, 60f))
                line(Offset(52f, 48f), Offset(60f, 60f))
            }
            "twist" -> {
                line(Offset(centerX, neckY + 4f), Offset(centerX, hipY))
                line(Offset(centerX, 30f), Offset(centerX - 18f, 24f))
                line(Offset(centerX, 30f), Offset(centerX + 18f, 36f))
                line(Offset(centerX, hipY), Offset(centerX - 14f, 56f))
                line(Offset(centerX, hipY), Offset(centerX + 14f, 56f))
            }
            "overhead" -> {
                line(Offset(centerX, neckY), Offset(centerX, hipY))
                line(Offset(centerX, 26f), Offset(centerX - 12f, 12f))
                line(Offset(centerX, 26f), Offset(centerX + 12f, 12f))
                line(Offset(centerX, hipY), Offset(centerX - 10f, floorY))
                line(Offset(centerX, hipY), Offset(centerX + 10f, floorY))
            }
            "floor_legs" -> {
                line(Offset(18f, 48f), Offset(36f, 40f))
                line(Offset(36f, 40f), Offset(48f, 24f))
                line(Offset(36f, 40f), Offset(56f, 40f))
            }
            else -> {
                line(Offset(centerX, neckY), Offset(centerX, hipY))
                line(Offset(centerX, 28f), Offset(centerX - 14f, 34f))
                line(Offset(centerX, 28f), Offset(centerX + 14f, 34f))
                line(Offset(centerX, hipY), Offset(centerX - 10f, floorY))
                line(Offset(centerX, hipY), Offset(centerX + 10f, floorY))
            }
        }
    }
}

@Composable
private fun ReplaceExerciseDialog(
    current: Exercise,
    recommended: List<Exercise>,
    others: List<Exercise>,
    onDismiss: () -> Unit,
    onSelect: (Exercise) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text("Замена упражнения", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Сейчас: ${current.name}")
                }
                if (recommended.isNotEmpty()) {
                    item {
                        Text("Рекомендуемые", fontWeight = FontWeight.SemiBold)
                    }
                    items(recommended) { exercise ->
                        ReplacementRow(exercise = exercise, onSelect = { onSelect(exercise) })
                    }
                }
                item {
                    Text("Все остальные", fontWeight = FontWeight.SemiBold)
                }
                items(others.distinctBy { it.id }) { exercise ->
                    ReplacementRow(exercise = exercise, onSelect = { onSelect(exercise) })
                }
                item {
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("Закрыть")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplacementRow(
    exercise: Exercise,
    onSelect: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.name, fontWeight = FontWeight.SemiBold)
                Text(
                    buildString {
                        append("${exercise.defaultSets} подхода • ${exercise.defaultReps}")
                        exercise.defaultDurationMinutes?.let { append(" • $it мин") }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onSelect) {
                Icon(Icons.Default.Add, contentDescription = "Поставить это упражнение")
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
    onOpenWorkout: (PlannedWorkout) -> Unit,
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
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
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
                        .combinedClickable(onClick = {
                            if (movingWorkout != null) {
                                onDropToDay(day)
                            }
                        }),
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
                                            onClick = { onOpenWorkout(workout) },
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
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
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
                        WorkoutHistoryStatus.COMPLETED -> Text("Тренировка выполнена", color = Color(0xFF2E7D32))
                        WorkoutHistoryStatus.MISSED -> Text("Тренировка пропущена", color = Color(0xFFC62828))
                        WorkoutHistoryStatus.NONE -> Text("На этот день тренировка не запланирована")
                    }
                    if (day.workoutNames.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        day.workoutNames.forEach { name -> Text("• $name") }
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
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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

private fun calculateWorkoutDuration(exercises: List<Exercise>, fallback: Int): Int {
    val computed = exercises.sumOf { it.defaultDurationMinutes ?: max(1, (it.defaultSets * max(it.restSeconds, 20)) / 60) }
    return if (computed > 0) computed else fallback
}

private fun calculateWorkoutCalories(base: WorkoutProgram, exercises: List<Exercise>): Int {
    val duration = calculateWorkoutDuration(exercises, base.durationMinutes)
    return max(40, (base.caloriesBurn * (duration.toDouble() / max(base.durationMinutes, 1))).roundToInt())
}

private fun techniqueFramesFor(exercise: Exercise): List<TechniqueFrame> {
    return when (exercise.id) {
        "squats" -> listOf(
            TechniqueFrame("Старт", "Стопы на ширине плеч", "standing"),
            TechniqueFrame("Низ", "Таз назад и вниз", "squat"),
            TechniqueFrame("Подъем", "Толкайся пятками", "standing")
        )
        "pushups" -> listOf(
            TechniqueFrame("Планка", "Корпус прямой", "plank"),
            TechniqueFrame("Низ", "Грудь ближе к полу", "plank"),
            TechniqueFrame("Вверх", "Выжми себя ровно", "plank")
        )
        "lunges" -> listOf(
            TechniqueFrame("Шаг", "Сделай длинный шаг", "standing"),
            TechniqueFrame("Низ", "Опускайся вертикально", "lunge"),
            TechniqueFrame("Возврат", "Поднимись и вернись", "standing")
        )
        "glute_bridge" -> listOf(
            TechniqueFrame("Лежа", "Ступни ближе к тазу", "floor_legs"),
            TechniqueFrame("Вверх", "Подними таз", "bridge"),
            TechniqueFrame("Фиксация", "Сожми ягодицы", "bridge")
        )
        "rows", "bicep_curls", "overhead_press" -> listOf(
            TechniqueFrame("Старт", "Стабильный корпус", "standing"),
            TechniqueFrame("Усилие", "Контроль движения", "overhead"),
            TechniqueFrame("Возврат", "Опускай медленно", "standing")
        )
        "crunches", "leg_raises" -> listOf(
            TechniqueFrame("Старт", "Поясница прижата", "floor_legs"),
            TechniqueFrame("Подъем", "Работает пресс", "floor_legs"),
            TechniqueFrame("Возврат", "Опускай медленно", "floor_legs")
        )
        "russian_twist" -> listOf(
            TechniqueFrame("Старт", "Сядь и держи баланс", "twist"),
            TechniqueFrame("Поворот", "Разверни плечи", "twist"),
            TechniqueFrame("Смена", "Плавно в другую сторону", "twist")
        )
        "plank", "mountain_climbers", "burpees" -> listOf(
            TechniqueFrame("Опора", "Плечи над кистями", "plank"),
            TechniqueFrame("Работа", "Кор крепкий", "plank"),
            TechniqueFrame("Финиш", "Сохраняй темп", "plank")
        )
        else -> listOf(
            TechniqueFrame("Старт", "Займи устойчивое положение", "standing"),
            TechniqueFrame("Движение", "Работай без рывков", "standing"),
            TechniqueFrame("Возврат", "Контролируй технику", "standing")
        )
    }
}
