package com.example.lifeadvices11.ui.sections.nutrition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import java.util.Calendar

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
    val allPredefinedMeals by viewModel.allPredefinedMeals.collectAsState()
    val latestWeight by viewModel.latestWeight.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val refreshTokenFlow = navController.currentBackStackEntry?.savedStateHandle?.getStateFlow("nutrition_refresh", 0L)
    val refreshToken by (refreshTokenFlow?.collectAsState() ?: remember { mutableStateOf(0L) })

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

    LaunchedEffect(refreshToken) {
        if (refreshToken != 0L) {
            viewModel.refreshData()
            navController.currentBackStackEntry?.savedStateHandle?.set("nutrition_refresh", 0L)
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
                        IconButton(onClick = { navController.navigate(Screen.NutritionProgress.route) }) {
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
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = { Text("Блюда") },
                        icon = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
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
                    weeklyPlan = weeklyPlan,
                    latestWeight = latestWeight,
                    onAddMealClick = { },
                    onAddMealSlot = { slot -> viewModel.addPlannedMealSlot(slot) },
                    onMealClick = { meal ->
                        navController.navigate(Screen.NutritionMealDetail.createRoute(meal.id))
                    },
                    onRemoveMealClick = { mealEntryId -> viewModel.removeTodayMeal(mealEntryId) },
                    onSaveWeight = { weight -> viewModel.saveWeight(weight) }
                )

                1 -> WeeklyPlanTab(
                    paddingValues = paddingValues,
                    weeklyPlan = weeklyPlan,
                    allMeals = allPredefinedMeals,
                    onMealClick = { meal ->
                        navController.navigate(Screen.NutritionMealDetail.createRoute(meal.id))
                    },
                    onReplaceMeal = { dayLabel, slotTitle, oldMealId, newMeal ->
                        viewModel.replaceMealInWeeklyPlan(dayLabel, slotTitle, oldMealId, newMeal)
                    }
                )
                2 -> DishesTab(
                    paddingValues = paddingValues,
                    meals = allPredefinedMeals,
                    onAddCustomClick = {
                        navController.navigate(Screen.NutritionCreateMeal.route)
                    },
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
    weeklyPlan: WeeklyMealPlan?,
    latestWeight: Float?,
    onAddMealClick: () -> Unit,
    onAddMealSlot: (PlannedMealSlot) -> Unit,
    onMealClick: (PredefinedMealEntity) -> Unit,
    onRemoveMealClick: (Long) -> Unit,
    onSaveWeight: (Float) -> Unit
) {
    val mealsByType = meals.groupBy { it.mealType }
    var showTodayMenuDialog by remember(weeklyPlan) { mutableStateOf(false) }
    val todayPlan = remember(weeklyPlan) { weeklyPlan?.days?.firstOrNull { it.dayLabel == currentDayLabel() } }
    var weightInput by remember(latestWeight) { mutableStateOf(latestWeight?.let { trimWeight(it) } ?: "") }

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

        Text(
            text = "Сегодня",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        MealTypeSection(
            title = "Завтрак",
            meals = mealsByType["breakfast"] ?: emptyList(),
            onRemoveMealClick = onRemoveMealClick
        )
        MealTypeSection(
            title = "Обед",
            meals = mealsByType["lunch"] ?: emptyList(),
            onRemoveMealClick = onRemoveMealClick
        )
        MealTypeSection(
            title = "Ужин",
            meals = mealsByType["dinner"] ?: emptyList(),
            onRemoveMealClick = onRemoveMealClick
        )
        MealTypeSection(
            title = "Перекусы",
            meals = mealsByType["snack"] ?: emptyList(),
            onRemoveMealClick = onRemoveMealClick
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Прогресс по калориям: ${(caloriesProgress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))
        WeightInputCard(
            weight = weightInput,
            onWeightChange = { weightInput = normalizeWeightInput(it) },
            onSaveClick = {
                weightInput.toFloatOrNull()?.let(onSaveWeight)
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TodayMenuButton(onClick = { showTodayMenuDialog = true })
        Spacer(modifier = Modifier.height(8.dp))
        QuickAddButton(onAddClick = { showTodayMenuDialog = true })
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showTodayMenuDialog) {
        TodayMenuDialog(
            todayPlan = todayPlan,
            onDismiss = { showTodayMenuDialog = false },
            onAddMealSlot = onAddMealSlot,
            onMealClick = onMealClick
        )
    }
}

@Composable
private fun WeeklyPlanTab(
    paddingValues: PaddingValues,
    weeklyPlan: WeeklyMealPlan?,
    allMeals: List<PredefinedMealEntity>,
    onMealClick: (PredefinedMealEntity) -> Unit,
    onReplaceMeal: (String, String, Long, PredefinedMealEntity) -> Unit
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
                allMeals = allMeals,
                onMealClick = onMealClick,
                onReplaceMeal = onReplaceMeal
            )
        }
    }
}

@Composable
private fun DishesTab(
    paddingValues: PaddingValues,
    meals: List<PredefinedMealEntity>,
    onAddCustomClick: () -> Unit,
    onMealClick: (PredefinedMealEntity) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("all") }
    var selectedMealType by remember { mutableStateOf("all") }
    var onlyMyMeals by remember { mutableStateOf(false) }

    val categoryOptions = listOf(
        "all" to "Все",
        "закуски" to "Закуски",
        "салаты" to "Салаты",
        "основные блюда" to "Основные",
        "супы" to "Супы",
        "десерты" to "Десерты",
        "напитки" to "Напитки"
    )
    val mealTypeOptions = listOf(
        "all" to "Все",
        "breakfast" to "Завтрак",
        "lunch" to "Обед",
        "snack" to "Перекус",
        "dinner" to "Ужин"
    )

    val filteredMeals = remember(meals, selectedCategory, selectedMealType, onlyMyMeals) {
        meals.filter { meal ->
            val categoryMatches = selectedCategory == "all" || meal.category == selectedCategory
            val mealTypeMatches = selectedMealType == "all" || mealSupportsType(meal, selectedMealType)
            val myMealsMatches = !onlyMyMeals || meal.isCustom
            categoryMatches && mealTypeMatches && myMealsMatches
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Button(
            onClick = onAddCustomClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Добавить свое")
        }

        FilterSection(
            title = "Категория",
            options = categoryOptions,
            selected = selectedCategory,
            onSelected = { selectedCategory = it }
        )
        FilterSection(
            title = "Прием пищи",
            options = mealTypeOptions,
            selected = selectedMealType,
            onSelected = { selectedMealType = it }
        )

        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = onlyMyMeals,
                onCheckedChange = { onlyMyMeals = it }
            )
            Text(
                text = "Мои блюда",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredMeals) { meal ->
                DishBrowserRow(
                    meal = meal,
                    onClick = { onMealClick(meal) }
                )
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (value, label) ->
                FilterChip(
                    selected = selected == value,
                    onClick = { onSelected(value) },
                    label = { Text(label) }
                )
            }
        }
    }
}

@Composable
private fun DishBrowserRow(
    meal: PredefinedMealEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${meal.calories} ккал • Б ${meal.protein} • Ж ${meal.fat} • У ${meal.carbs}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Открыть блюдо")
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
    allMeals: List<PredefinedMealEntity>,
    onMealClick: (PredefinedMealEntity) -> Unit,
    onReplaceMeal: (String, String, Long, PredefinedMealEntity) -> Unit
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
                    MealSlotSection(
                        dayLabel = dayLabel,
                        slot = slot,
                        allMeals = allMeals,
                        onMealClick = onMealClick,
                        onReplaceMeal = onReplaceMeal
                    )
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
    dayLabel: String,
    slot: PlannedMealSlot,
    allMeals: List<PredefinedMealEntity>,
    onMealClick: (PredefinedMealEntity) -> Unit,
    onReplaceMeal: (String, String, Long, PredefinedMealEntity) -> Unit
) {
    var mealForReplacement by remember(dayLabel, slot.title) { mutableStateOf<PredefinedMealEntity?>(null) }

    Column {
        Text(
            text = "${slot.title} • ${slot.totalCalories} ккал • Б ${slot.totalProtein} • Ж ${slot.totalFat} • У ${slot.totalCarbs}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        slot.dishes.forEach { meal ->
            DishLine(
                meal = meal,
                onClick = { onMealClick(meal) },
                onEditClick = { mealForReplacement = meal }
            )
        }

        mealForReplacement?.let { currentMeal ->
            MealReplacementDialog(
                currentMeal = currentMeal,
                allMeals = allMeals,
                onDismiss = { mealForReplacement = null },
                onReplace = { newMeal ->
                    onReplaceMeal(dayLabel, slot.title, currentMeal.id, newMeal)
                    mealForReplacement = null
                }
            )
        }
    }
}

@Composable
private fun DishLine(
    meal: PredefinedMealEntity,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onClick() }
                .padding(end = 8.dp)
        ) {
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
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Заменить блюдо")
            }
            IconButton(onClick = onClick) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Открыть блюдо")
            }
        }
    }
}

@Composable
private fun MealReplacementDialog(
    currentMeal: PredefinedMealEntity,
    allMeals: List<PredefinedMealEntity>,
    onDismiss: () -> Unit,
    onReplace: (PredefinedMealEntity) -> Unit
) {
    val alternatives = remember(currentMeal, allMeals) {
        val sameCategoryMeals = allMeals
            .filter { it.id != currentMeal.id }
            .filter { it.category == currentMeal.category }
            .sortedBy { replacementScore(it, currentMeal) }

        val similarMeals = sameCategoryMeals.filter { it.isSimilarForReplacement(currentMeal) }
        if (similarMeals.isNotEmpty()) similarMeals else sameCategoryMeals
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Заменить блюдо")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentMeal.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            if (alternatives.isEmpty()) {
                Text("Не нашлось других блюд в той же категории.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(alternatives) { alternative ->
                        ReplacementOptionRow(
                            meal = alternative,
                            onAddClick = { onReplace(alternative) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text("Закрыть")
            }
        }
    )
}

@Composable
private fun ReplacementOptionRow(
    meal: PredefinedMealEntity,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = meal.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${meal.calories} ккал • Б ${meal.protein} • Ж ${meal.fat} • У ${meal.carbs}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Выбрать блюдо")
            }
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
private fun TodayMenuButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Меню на сегодня",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = currentDayLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Открыть меню на сегодня",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun WeightInputCard(
    weight: String,
    onWeightChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Вес",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = weight,
                onValueChange = onWeightChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Текущий вес") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onSaveClick,
                enabled = weight.toFloatOrNull() != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить вес")
            }
        }
    }
}

@Composable
private fun TodayMenuDialog(
    todayPlan: com.example.lifeadvices11.data.models.DailyMealPlan?,
    onDismiss: () -> Unit,
    onAddMealSlot: (PlannedMealSlot) -> Unit,
    onMealClick: (PredefinedMealEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Меню на сегодня")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = todayPlan?.dayLabel ?: currentDayLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            if (todayPlan == null) {
                Text("Рацион на сегодня пока не найден.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(todayPlan.slots) { slot ->
                        TodayMenuSlotCard(
                            slot = slot,
                            onAddClick = { onAddMealSlot(slot) },
                            onMealClick = onMealClick
                        )
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.size(6.dp))
                Text("Закрыть")
            }
        }
    )
}

@Composable
private fun TodayMenuSlotCard(
    slot: PlannedMealSlot,
    onAddClick: () -> Unit,
    onMealClick: (PredefinedMealEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = slot.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${slot.totalCalories} ккал • Б ${slot.totalProtein} • Ж ${slot.totalFat} • У ${slot.totalCarbs}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить прием пищи")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            slot.dishes.forEach { meal ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMealClick(meal) }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = meal.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "${meal.calories} ккал • Б ${meal.protein} • Ж ${meal.fat} • У ${meal.carbs}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = "Открыть блюдо")
                }
            }
        }
    }
}

@Composable
private fun MealTypeSection(
    title: String,
    meals: List<MealEntryEntity>,
    onRemoveMealClick: (Long) -> Unit
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
            MealCard(meal = meal, onRemoveClick = { onRemoveMealClick(meal.id) })
        }
    }
}

@Composable
private fun MealCard(
    meal: MealEntryEntity,
    onRemoveClick: () -> Unit
) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${meal.calories} ккал",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onRemoveClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить блюдо")
                }
            }
        }
    }
}

private fun PredefinedMealEntity.isSimilarForReplacement(reference: PredefinedMealEntity): Boolean {
    val calorieDelta = kotlin.math.abs(calories - reference.calories)
    val proteinDelta = kotlin.math.abs(protein - reference.protein)
    val fatDelta = kotlin.math.abs(fat - reference.fat)
    val carbsDelta = kotlin.math.abs(carbs - reference.carbs)

    val allowedCalories = maxOf(80, (reference.calories * 0.3f).toInt())
    val allowedProtein = maxOf(8, (reference.protein * 0.45f).toInt())
    val allowedFat = maxOf(6, (reference.fat * 0.5f).toInt())
    val allowedCarbs = maxOf(10, (reference.carbs * 0.45f).toInt())

    return calorieDelta <= allowedCalories &&
        proteinDelta <= allowedProtein &&
        fatDelta <= allowedFat &&
        carbsDelta <= allowedCarbs
}

private fun replacementScore(candidate: PredefinedMealEntity, reference: PredefinedMealEntity): Int {
    return kotlin.math.abs(candidate.calories - reference.calories) * 3 +
        kotlin.math.abs(candidate.protein - reference.protein) * 4 +
        kotlin.math.abs(candidate.fat - reference.fat) * 2 +
        kotlin.math.abs(candidate.carbs - reference.carbs) * 2
}

private fun mealSupportsType(
    meal: PredefinedMealEntity,
    mealType: String
): Boolean {
    return meal.mealTypes
        .split(",")
        .map { it.trim() }
        .any { it.equals(mealType, ignoreCase = true) }
}

private fun normalizeWeightInput(input: String): String {
    val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }
    val normalized = filtered.replace(',', '.')
    val firstDotIndex = normalized.indexOf('.')
    return if (firstDotIndex >= 0) {
        normalized.substring(0, firstDotIndex + 1) +
            normalized.substring(firstDotIndex + 1).replace(".", "")
    } else {
        normalized
    }
}

private fun trimWeight(weight: Float): String {
    return if (weight % 1f == 0f) {
        weight.toInt().toString()
    } else {
        weight.toString()
    }
}

private fun currentDayLabel(): String {
    return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Понедельник"
        Calendar.TUESDAY -> "Вторник"
        Calendar.WEDNESDAY -> "Среда"
        Calendar.THURSDAY -> "Четверг"
        Calendar.FRIDAY -> "Пятница"
        Calendar.SATURDAY -> "Суббота"
        Calendar.SUNDAY -> "Воскресенье"
        else -> "Понедельник"
    }
}


