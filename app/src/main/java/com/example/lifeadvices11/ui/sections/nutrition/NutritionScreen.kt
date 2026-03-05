package com.example.lifeadvices11.ui.sections.nutrition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifeadvices11.data.models.*
import com.example.lifeadvices11.ui.navigation.Screen
import com.example.lifeadvices11.ui.onboarding.nutrition.NutritionOnboardingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(navController: NavController) {
    val viewModel: NutritionViewModel = viewModel()
    val onboardingViewModel: NutritionOnboardingViewModel = viewModel()

    var isLoading by remember { mutableStateOf(true) }
    var needsOnboarding by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    val todayNutrition by viewModel.todayNutrition.collectAsState()
    val userNorms by viewModel.userNorms.collectAsState()
    val caloriesProgress by viewModel.caloriesProgress.collectAsState()
    val todayMeals by viewModel.todayMeals.collectAsState()
    val recommendedPlans by viewModel.recommendedMealPlans.collectAsState()
    LaunchedEffect(Unit) {
        val hasOnboarding = withContext(Dispatchers.IO) {
            onboardingViewModel.hasCompletedOnboarding()
        }
        needsOnboarding = !hasOnboarding
        isLoading = false

        if (needsOnboarding) {
            navController.navigate(Screen.NutritionOnboarding.route) {
                popUpTo(Screen.Nutrition.route) { inclusive = true }
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (!needsOnboarding) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text("🍎 Питание") },
                        actions = {
                            IconButton(onClick = { /* статистика */ }) {
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
                    onAddMealClick = { /* TODO */ }
                )
                1 -> MealPlansTab(
                    paddingValues = paddingValues,
                    mealPlans = recommendedPlans,
                    onSelectMeal = { mealType, name, calories, protein, fat, carbs ->
                        viewModel.addMeal(mealType, name, calories, protein, fat, carbs)
                    }
                )
            }
        }
    }
}

// ============== ВКЛАДКА 1: ОСНОВНАЯ ==============
@Composable
fun MainTab(
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
            goal = userNorms?.calories ?: 2000,
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        )

        BJUSection(
            protein = Pair(
                todayNutrition?.totalProtein ?: 0,
                userNorms?.protein ?: 0
            ),
            fat = Pair(
                todayNutrition?.totalFat ?: 0,
                userNorms?.fat ?: 0
            ),
            carbs = Pair(
                todayNutrition?.totalCarbs ?: 0,
                userNorms?.carbs ?: 0
            )
        )
        QuickAddButton(onAddClick = onAddMealClick)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Сегодня",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        MealTypeSection("breakfast", "🍳 Завтрак", mealsByType["breakfast"] ?: emptyList())
        MealTypeSection("lunch", "🍲 Обед", mealsByType["lunch"] ?: emptyList())
        MealTypeSection("dinner", "🍽️ Ужин", mealsByType["dinner"] ?: emptyList())
        MealTypeSection("snack", "🍪 Перекусы", mealsByType["snack"] ?: emptyList())
    }
}

// ============== ВКЛАДКА 2: РАЦИОНЫ ==============
@Composable
fun MealPlansTab(
    paddingValues: PaddingValues,
    mealPlans: List<MealPlanCategory>,
    onSelectMeal: (String, String, Int, Int, Int, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(mealPlans) { category ->
            MealPlanCategoryCard(
                category = category,
                onSelectMeal = onSelectMeal
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MealPlanCategoryCard(
    category: MealPlanCategory,
    onSelectMeal: (String, String, Int, Int, Int, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            category.meals.forEach { meal ->
                MealSuggestionItem(
                    meal = meal,
                    onClick = {
                        onSelectMeal(
                            meal.mealType,
                            meal.name,
                            meal.calories,
                            meal.protein,
                            meal.fat,
                            meal.carbs
                        )
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
fun MealSuggestionItem(
    meal: MealSuggestion,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = meal.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "Б:${meal.protein} Ж:${meal.fat} У:${meal.carbs}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${meal.calories} ккал",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
            Icon(
                Icons.Default.AddCircle,
                contentDescription = "Добавить",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ============== ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ ==============
@Composable
fun CalorieRingProgress(
    current: Int,
    goal: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (goal > 0) current.toFloat() / goal.toFloat() else 0f

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 12.dp,
            color = when {
                progress < 0.8f -> Color.Green
                progress < 1.0f -> Color(0xFFFFA500)
                else -> Color.Red
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
fun BJUSection(
    protein: Pair<Int, Int>,
    fat: Pair<Int, Int>,
    carbs: Pair<Int, Int>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Баланс нутриентов",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Передаем правильные пары (текущее, цель)
            NutrientBar("Белки", protein.first, protein.second, Color(0xFF42A5F5))
            NutrientBar("Жиры", fat.first, fat.second, Color(0xFFFFA726))
            NutrientBar("Углеводы", carbs.first, carbs.second, Color(0xFF66BB6A))
        }
    }
}

@Composable
fun NutrientBar(
    name: String,
    current: Int,
    goal: Int,
    color: Color
) {
    val progress = if (goal > 0) current.toFloat() / goal.toFloat() else 0f

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
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
            progress = { progress },
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
fun QuickAddButton(
    onAddClick: () -> Unit
) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
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
fun MealTypeSection(
    type: String,
    title: String,
    meals: List<MealEntryEntity>
) {
    if (meals.isNotEmpty()) {
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
}

@Composable
fun MealCard(meal: MealEntryEntity) {
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
                Text(
                    text = meal.foodName,
                    style = MaterialTheme.typography.titleMedium
                )
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