package com.example.lifeadvices11.ui.sections.nutrition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifeadvices11.data.models.DailyNutritionEntity
import com.example.lifeadvices11.data.models.MealEntryEntity
import com.example.lifeadvices11.data.models.NutritionNorm
import com.example.lifeadvices11.data.models.PlannedMealSlot
import com.example.lifeadvices11.data.models.PredefinedMealEntity
import com.example.lifeadvices11.data.models.WeeklyMealPlan
import com.example.lifeadvices11.ui.navigation.Screen
import com.example.lifeadvices11.ui.onboarding.nutrition.NutritionOnboardingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(navController: NavController) {
    val viewModel: NutritionViewModel = viewModel()
    val onboardingViewModel: NutritionOnboardingViewModel = viewModel()

    var isCheckingOnboarding by remember { mutableStateOf(true) }
    var needsOnboarding by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    val todayNutrition by viewModel.todayNutrition.collectAsState()
    val userNorms by viewModel.userNorms.collectAsState()
    val caloriesProgress by viewModel.caloriesProgress.collectAsState()
    val todayMeals by viewModel.todayMeals.collectAsState()
    val weeklyPlan by viewModel.selectedWeeklyPlan.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        val hasOnboarding = withContext(Dispatchers.IO) {
            onboardingViewModel.hasCompletedOnboarding()
        }
        needsOnboarding = !hasOnboarding
        isCheckingOnboarding = false

        if (needsOnboarding) {
            navController.navigate(Screen.NutritionOnboarding.route) {
                popUpTo(Screen.Nutrition.route) { inclusive = true }
            }
        }
    }

    if (isCheckingOnboarding || isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (needsOnboarding) return

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Питание") },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.ShowChart, contentDescription = "Статистика")
                        }
                    }
                )

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Основная") },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Рационы") },
                        icon = { Icon(Icons.Default.Bookmark, contentDescription = null) }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTabIndex) {
            0 -> MainTab(
                paddingValues = paddingValues,
                todayNutrition = todayNutrition,
                userNorms = userNorms,
                caloriesProgress = caloriesProgress,
                meals = todayMeals,
                onAddMealClick = { }
            )

            1 -> WeeklyPlanTab(
                paddingValues = paddingValues,
                weeklyPlan = weeklyPlan,
                onMealClick = { meal ->
                    navController.navigate(Screen.NutritionMealDetail.createRoute(meal.id))
                }
            )
        }
    }
}

@Composable
private fun MainTab(
    paddingValues: PaddingValues,
    todayNutrition: DailyNutritionEntity?,
    userNorms: NutritionNorm?,
    caloriesProgress: Float,
    meals: List<MealEntryEntity>,
    onAddMealClick: () -> Unit
) {
    val mealsByType = meals.groupBy { it.mealType }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        CalorieRingProgress(
            current = todayNutrition?.totalCalories ?: 0,
            goal = userNorms?.calories ?: 2_000,
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        )

        BjuSection(
            protein = Pair(todayNutrition?.totalProtein ?: 0, userNorms?.protein ?: 0),
            fat = Pair(todayNutrition?.totalFat ?: 0, userNorms?.fat ?: 0),
            carbs = Pair(todayNutrition?.totalCarbs ?: 0, userNorms?.carbs ?: 0)
        )

        QuickAddButton(onAddClick = onAddMealClick)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Сегодня",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        MealTypeSection(title = "Завтрак", meals = mealsByType["breakfast"] ?: emptyList())
        MealTypeSection(title = "Обед", meals = mealsByType["lunch"] ?: emptyList())
        MealTypeSection(title = "Ужин", meals = mealsByType["dinner"] ?: emptyList())
        MealTypeSection(title = "Перекусы", meals = mealsByType["snack"] ?: emptyList())

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Прогресс по калориям: ${(caloriesProgress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun WeeklyPlanTab(
    paddingValues: PaddingValues,
    weeklyPlan: WeeklyMealPlan?,
    onMealClick: (PredefinedMealEntity) -> Unit
) {
    if (weeklyPlan == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Пока не удалось подобрать рацион")
        }
        return
    }

    val expandedDays = remember(weeklyPlan.group.id) {
        mutableStateMapOf<String, Boolean>().apply {
            weeklyPlan.days.forEachIndexed { index, day -> put(day.dayLabel, index == 0) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            WeeklyPlanHeader(weeklyPlan = weeklyPlan)
        }

        items(weeklyPlan.days) { day ->
            DayPlanCard(
                dayLabel = day.dayLabel,
                isExpanded = expandedDays[day.dayLabel] == true,
                dailyCalories = day.totalCalories,
                dailyProtein = day.totalProtein,
                dailyFat = day.totalFat,
                dailyCarbs = day.totalCarbs,
                slots = day.slots,
                onToggle = {
                    expandedDays[day.dayLabel] = !(expandedDays[day.dayLabel] ?: false)
                },
                onMealClick = onMealClick
            )
        }
    }
}

@Composable
private fun WeeklyPlanHeader(weeklyPlan: WeeklyMealPlan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Рацион ${weeklyPlan.group.id} из 9",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = weeklyPlan.group.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Подобран по диапазону ${weeklyPlan.group.calorieRange.first}-${weeklyPlan.group.calorieRange.last} ккал и ${weeklyPlan.group.proteinRange.first}-${weeklyPlan.group.proteinRange.last} г белка.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Средняя цель в день: ${weeklyPlan.group.targetCalories} ккал, ${weeklyPlan.group.targetProtein} г белка.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun DayPlanCard(
    dayLabel: String,
    isExpanded: Boolean,
    dailyCalories: Int,
    dailyProtein: Int,
    dailyFat: Int,
    dailyCarbs: Int,
    slots: List<PlannedMealSlot>,
    onToggle: () -> Unit,
    onMealClick: (PredefinedMealEntity) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$dailyCalories ккал • Б $dailyProtein • Ж $dailyFat • У $dailyCarbs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Свернуть" else "Развернуть"
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                slots.forEachIndexed { index, slot ->
                    MealSlotSection(slot = slot, onMealClick = onMealClick)
                    if (index != slots.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MealSlotSection(
    slot: PlannedMealSlot,
    onMealClick: (PredefinedMealEntity) -> Unit
) {
    Column {
        Text(
            text = "${slot.title} • ${slot.totalCalories} ккал • Б ${slot.totalProtein} • Ж ${slot.totalFat} • У ${slot.totalCarbs}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        slot.dishes.forEach { meal ->
            DishLine(meal = meal, onClick = { onMealClick(meal) })
        }
    }
}

@Composable
private fun DishLine(
    meal: PredefinedMealEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = meal.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${meal.category} • ${formatMealTypes(meal.mealTypes)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${meal.calories} ккал",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(8.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = "Открыть блюдо")
        }
    }
}

@Composable
private fun CalorieRingProgress(
    current: Int,
    goal: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (goal > 0) current.toFloat() / goal.toFloat() else 0f

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress.coerceAtMost(1f) },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 12.dp,
            color = when {
                progress < 0.8f -> Color(0xFF43A047)
                progress < 1f -> Color(0xFFFB8C00)
                else -> Color(0xFFE53935)
            }
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$current",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "из $goal ккал",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun BjuSection(
    protein: Pair<Int, Int>,
    fat: Pair<Int, Int>,
    carbs: Pair<Int, Int>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Баланс нутриентов",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            NutrientBar("Белки", protein.first, protein.second, Color(0xFF42A5F5))
            NutrientBar("Жиры", fat.first, fat.second, Color(0xFFFFA726))
            NutrientBar("Углеводы", carbs.first, carbs.second, Color(0xFF66BB6A))
        }
    }
}

@Composable
private fun NutrientBar(
    name: String,
    current: Int,
    goal: Int,
    color: Color
) {
    val progress = if (goal > 0) current.toFloat() / goal.toFloat() else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "$current / $goal г",
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
        }

        LinearProgressIndicator(
            progress = { progress.coerceAtMost(1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun QuickAddButton(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAddClick() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Добавить прием пищи",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MealTypeSection(
    title: String,
    meals: List<MealEntryEntity>
) {
    if (meals.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        meals.forEach { meal ->
            MealCard(meal = meal)
        }
    }
}

@Composable
private fun MealCard(meal: MealEntryEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = meal.foodName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Б:${meal.protein} Ж:${meal.fat} У:${meal.carbs}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Text(
                text = "${meal.calories} ккал",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
