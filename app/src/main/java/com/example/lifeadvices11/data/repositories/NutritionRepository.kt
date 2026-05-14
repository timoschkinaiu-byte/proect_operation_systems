package com.example.lifeadvices11.data.repositories

import com.example.lifeadvices11.data.dao.NutritionDao
import com.example.lifeadvices11.data.dao.UserProfileDao
import com.example.lifeadvices11.data.database.NutritionSeedData
import com.example.lifeadvices11.data.models.DailyMealPlan
import com.example.lifeadvices11.data.models.DailyNutritionEntity
import com.example.lifeadvices11.data.models.MealEntryEntity
import com.example.lifeadvices11.data.models.MealPlanCategory
import com.example.lifeadvices11.data.models.MealPlanGroup
import com.example.lifeadvices11.data.models.MealSuggestion
import com.example.lifeadvices11.data.models.NutritionProgressMetric
import com.example.lifeadvices11.data.models.NutritionProgressPoint
import com.example.lifeadvices11.data.models.PlannedMealSlot
import com.example.lifeadvices11.data.models.PredefinedMealEntity
import com.example.lifeadvices11.data.models.UserAnthroData
import com.example.lifeadvices11.data.models.WeeklyMealPlan
import com.example.lifeadvices11.data.models.WeightEntryEntity
import com.example.lifeadvices11.utils.NutritionCalculator
import java.util.Calendar
import kotlin.math.abs

class NutritionRepository(
    private val nutritionDao: NutritionDao,
    private val userDao: UserProfileDao
) {

    companion object {
        private const val MAX_RANGE = 9_999

        private val calorieRanges = listOf(
            1_300..1_699,
            1_700..2_099,
            2_100..2_600
        )

        private val proteinRanges = listOf(
            60..89,
            90..119,
            120..170
        )

        private val breakfastDayPatterns = listOf(
            listOf("закуски", "напитки"),
            listOf("десерты", "напитки"),
            listOf("закуски", "десерты"),
            listOf("салаты", "напитки"),
            listOf("закуски", "напитки"),
            listOf("десерты", "закуски"),
            listOf("салаты", "десерты")
        )

        private val lunchDayPatterns = listOf(
            listOf("основные блюда", "салаты"),
            listOf("супы", "салаты"),
            listOf("основные блюда", "напитки"),
            listOf("супы", "основные блюда"),
            listOf("основные блюда", "салаты"),
            listOf("салаты", "супы"),
            listOf("основные блюда", "супы")
        )

        private val snackDayPatterns = listOf(
            listOf("закуски", "напитки"),
            listOf("десерты", "напитки"),
            listOf("десерты", "закуски"),
            listOf("напитки", "закуски"),
            listOf("десерты", "напитки"),
            listOf("закуски", "десерты"),
            listOf("напитки", "десерты")
        )

        private val dinnerDayPatterns = listOf(
            listOf("салаты", "основные блюда"),
            listOf("супы", "закуски"),
            listOf("основные блюда", "салаты"),
            listOf("супы", "салаты"),
            listOf("основные блюда", "закуски"),
            listOf("салаты", "супы"),
            listOf("основные блюда", "напитки")
        )

        private val dayLabels = listOf(
            "Понедельник",
            "Вторник",
            "Среда",
            "Четверг",
            "Пятница",
            "Суббота",
            "Воскресенье"
        )
    }

    suspend fun getTodayNutrition(): DailyNutritionEntity? {
        val todayStart = getStartOfDay()
        return nutritionDao.getDailyNutritionByDate(todayStart)
    }

    suspend fun getTodayMeals(): List<MealEntryEntity> {
        val todayStart = getStartOfDay()
        val today = nutritionDao.getDailyNutritionByDate(todayStart)
        return if (today != null) {
            nutritionDao.getMealsForDaySync(today.id)
        } else {
            emptyList()
        }
    }

    suspend fun createTodayNutritionIfNotExists(userData: UserAnthroData): DailyNutritionEntity {
        val todayStart = getStartOfDay()
        var today = nutritionDao.getDailyNutritionByDate(todayStart)

        if (today == null) {
            val norms = NutritionCalculator.calculateNutritionNorm(userData)
            today = DailyNutritionEntity(
                date = todayStart,
                goalCalories = norms.calories,
                goalProtein = norms.protein,
                goalFat = norms.fat,
                goalCarbs = norms.carbs
            )
            nutritionDao.insertDailyNutrition(today)
            today = nutritionDao.getDailyNutritionByDate(todayStart)!!
        }
        return today
    }

    suspend fun addMeal(
        mealType: String,
        foodName: String,
        calories: Int,
        protein: Int,
        fat: Int,
        carbs: Int
    ) {
        val userData = getUserAnthroData() ?: return
        val today = createTodayNutritionIfNotExists(userData)

        val mealEntry = MealEntryEntity(
            dailyNutritionId = today.id,
            mealType = mealType,
            foodName = foodName,
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs
        )
        nutritionDao.insertMealEntry(mealEntry)

        val updatedDaily = today.copy(
            totalCalories = today.totalCalories + calories,
            totalProtein = today.totalProtein + protein,
            totalFat = today.totalFat + fat,
            totalCarbs = today.totalCarbs + carbs
        )
        nutritionDao.insertDailyNutrition(updatedDaily)
    }

    suspend fun addPlannedMealSlot(slot: PlannedMealSlot) {
        slot.dishes.forEach { meal ->
            addMeal(
                mealType = slot.mealType,
                foodName = meal.name,
                calories = meal.calories,
                protein = meal.protein,
                fat = meal.fat,
                carbs = meal.carbs
            )
        }
    }

    suspend fun removeMeal(mealEntryId: Long) {
        val mealEntry = nutritionDao.getMealEntryById(mealEntryId) ?: return
        nutritionDao.deleteMealEntryById(mealEntryId)

        val remainingMeals = nutritionDao.getMealsForDaySync(mealEntry.dailyNutritionId)
        val dailyNutrition = nutritionDao.getDailyNutritionByDate(getStartOfDay()) ?: return

        val updatedDaily = dailyNutrition.copy(
            totalCalories = remainingMeals.sumOf { it.calories },
            totalProtein = remainingMeals.sumOf { it.protein },
            totalFat = remainingMeals.sumOf { it.fat },
            totalCarbs = remainingMeals.sumOf { it.carbs }
        )
        nutritionDao.insertDailyNutrition(updatedDaily)
    }

    suspend fun getPredefinedMeals(): List<PredefinedMealEntity> {
        val existingMeals = nutritionDao.getAllPredefinedMeals()
        if (existingMeals.isNotEmpty()) return existingMeals

        nutritionDao.insertPredefinedMeals(NutritionSeedData.predefinedMeals())
        return nutritionDao.getAllPredefinedMeals()
    }

    suspend fun getCustomMeals(): List<PredefinedMealEntity> {
        ensureMealsSeeded()
        return nutritionDao.getCustomMeals()
    }

    suspend fun getPredefinedMealById(mealId: Long): PredefinedMealEntity? {
        ensureMealsSeeded()
        return nutritionDao.getPredefinedMealById(mealId)
    }

    suspend fun getMealsByCategory(category: String): List<PredefinedMealEntity> {
        ensureMealsSeeded()
        return nutritionDao.getMealsByCategory(category)
    }

    suspend fun getRecommendedMealPlans(): List<MealPlanCategory> = emptyList()

    suspend fun addCustomMeal(
        name: String,
        category: String,
        mealTypes: List<String>,
        calories: Int?,
        protein: Int?,
        fat: Int?,
        carbs: Int?,
        ingredients: String,
        recipe: String
    ) {
        val customMeal = PredefinedMealEntity(
            name = name.trim(),
            calories = calories ?: 0,
            protein = protein ?: 0,
            fat = fat ?: 0,
            carbs = carbs ?: 0,
            category = category,
            mealTypes = mealTypes.joinToString(","),
            ingredients = ingredients.trim(),
            recipe = recipe.trim(),
            tags = "",
            isCustom = true
        )
        nutritionDao.insertPredefinedMeal(customMeal)
    }

    suspend fun getCurrentWeeklyMealPlan(): WeeklyMealPlan? {
        val userData = getUserAnthroData() ?: return null
        val norms = NutritionCalculator.calculateNutritionNorm(userData)
        val meals = getPredefinedMeals()
        val planGroup = resolvePlanGroup(norms.calories, norms.protein)
        return buildWeeklyMealPlan(meals, planGroup)
    }

    suspend fun getAllWeeklyMealPlans(): List<WeeklyMealPlan> {
        val meals = getPredefinedMeals()
        return buildPlanGroups().map { buildWeeklyMealPlan(meals, it) }
    }

    suspend fun saveWeight(weight: Float) {
        nutritionDao.insertWeightEntry(
            WeightEntryEntity(
                weight = weight,
                date = System.currentTimeMillis()
            )
        )
    }

    suspend fun getLatestWeight(): Float? {
        return nutritionDao.getLatestWeightEntry()?.weight ?: userDao.getProfileSync()?.weight?.toFloat()
    }

    suspend fun getProgressPoints(metric: NutritionProgressMetric): List<NutritionProgressPoint> {
        val fromDate = getStartOfDayOffset(daysBack = 13)
        val userData = getUserAnthroData()
        val norms = userData?.let { NutritionCalculator.calculateNutritionNorm(it) }

        return when (metric) {
            NutritionProgressMetric.WEIGHT -> {
                val baselineWeight = nutritionDao.getLatestWeightEntry()?.weight
                    ?: userDao.getProfileSync()?.weight?.toFloat()
                    ?: 0f
                nutritionDao.getWeightEntriesFromDate(fromDate).map { entry ->
                    NutritionProgressPoint(
                        label = formatDayLabel(entry.date),
                        value = entry.weight,
                        goal = baselineWeight,
                        isWithinGoal = entry.weight in (baselineWeight * 0.95f)..(baselineWeight * 1.05f)
                    )
                }
            }

            else -> {
                nutritionDao.getDailyNutritionFromDate(fromDate).map { entry ->
                    val goalValue = when (metric) {
                        NutritionProgressMetric.CALORIES -> (norms?.calories ?: entry.goalCalories).toFloat()
                        NutritionProgressMetric.PROTEIN -> (norms?.protein ?: entry.goalProtein).toFloat()
                        NutritionProgressMetric.FAT -> (norms?.fat ?: entry.goalFat).toFloat()
                        NutritionProgressMetric.CARBS -> (norms?.carbs ?: entry.goalCarbs).toFloat()
                        NutritionProgressMetric.WEIGHT -> 0f
                    }
                    val actualValue = when (metric) {
                        NutritionProgressMetric.CALORIES -> entry.totalCalories.toFloat()
                        NutritionProgressMetric.PROTEIN -> entry.totalProtein.toFloat()
                        NutritionProgressMetric.FAT -> entry.totalFat.toFloat()
                        NutritionProgressMetric.CARBS -> entry.totalCarbs.toFloat()
                        NutritionProgressMetric.WEIGHT -> 0f
                    }

                    NutritionProgressPoint(
                        label = formatDayLabel(entry.date),
                        value = actualValue,
                        goal = goalValue,
                        isWithinGoal = isWithinNutritionGoal(actualValue, goalValue)
                    )
                }
            }
        }
    }

    private suspend fun getUserAnthroData(): UserAnthroData? {
        val profile = userDao.getProfileSync() ?: return null
        return UserAnthroData(
            height = profile.height,
            weight = profile.weight,
            age = profile.age,
            gender = profile.gender,
            activityLevel = profile.activityLevel,
            goal = profile.goal
        )
    }

    private suspend fun ensureMealsSeeded() {
        if (nutritionDao.getAllPredefinedMeals().isEmpty()) {
            nutritionDao.insertPredefinedMeals(NutritionSeedData.predefinedMeals())
        }
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfDayOffset(daysBack: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, -daysBack)
        return calendar.timeInMillis
    }

    private fun formatDayLabel(timestamp: Long): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        return "%02d.%02d".format(day, month)
    }

    private fun isWithinNutritionGoal(actual: Float, goal: Float): Boolean {
        if (goal <= 0f) return false
        val lowerBound = goal * 0.9f
        val upperBound = goal * 1.1f
        return actual in lowerBound..upperBound
    }

    private fun buildPlanGroups(): List<MealPlanGroup> {
        val calorieNames = listOf("Легкий", "Сбалансированный", "Энергичный")
        val proteinNames = listOf("базовый белок", "умеренный белок", "высокий белок")
        val groups = mutableListOf<MealPlanGroup>()
        var nextId = 1

        calorieRanges.forEachIndexed { calorieIndex, calorieRange ->
            proteinRanges.forEachIndexed { proteinIndex, proteinRange ->
                groups += MealPlanGroup(
                    id = nextId++,
                    title = "${calorieNames[calorieIndex]} рацион, ${proteinNames[proteinIndex]}",
                    calorieRange = calorieRange,
                    proteinRange = proteinRange,
                    targetCalories = (calorieRange.first + calorieRange.last) / 2,
                    targetProtein = (proteinRange.first + proteinRange.last) / 2
                )
            }
        }

        return groups
    }

    private fun resolvePlanGroup(calories: Int, protein: Int): MealPlanGroup {
        val groups = buildPlanGroups()
        return groups.minByOrNull { group ->
            distanceToRange(calories, group.calorieRange) * 10 + distanceToRange(protein, group.proteinRange)
        } ?: groups.first()
    }

    private fun buildWeeklyMealPlan(
        meals: List<PredefinedMealEntity>,
        group: MealPlanGroup
    ): WeeklyMealPlan {
        val usageCount = mutableMapOf<Long, Int>()
        val dayPlans = dayLabels.mapIndexed { dayIndex, dayLabel ->
            val slots = listOf(
                buildMealSlot(
                    dayIndex = dayIndex,
                    mealType = "breakfast",
                    title = "Завтрак",
                    targetCalories = (group.targetCalories * 0.26f).toInt(),
                    targetProtein = (group.targetProtein * 0.24f).toInt(),
                    meals = meals,
                    categoryPattern = breakfastDayPatterns[dayIndex],
                    usageCount = usageCount,
                    group = group
                ),
                buildMealSlot(
                    dayIndex = dayIndex,
                    mealType = "lunch",
                    title = "Обед",
                    targetCalories = (group.targetCalories * 0.34f).toInt(),
                    targetProtein = (group.targetProtein * 0.32f).toInt(),
                    meals = meals,
                    categoryPattern = lunchDayPatterns[dayIndex],
                    usageCount = usageCount,
                    group = group
                ),
                buildMealSlot(
                    dayIndex = dayIndex,
                    mealType = "snack",
                    title = "Перекус",
                    targetCalories = (group.targetCalories * 0.14f).toInt(),
                    targetProtein = (group.targetProtein * 0.12f).toInt(),
                    meals = meals,
                    categoryPattern = snackDayPatterns[dayIndex],
                    usageCount = usageCount,
                    group = group
                ),
                buildMealSlot(
                    dayIndex = dayIndex,
                    mealType = "dinner",
                    title = "Ужин",
                    targetCalories = (group.targetCalories * 0.26f).toInt(),
                    targetProtein = (group.targetProtein * 0.32f).toInt(),
                    meals = meals,
                    categoryPattern = dinnerDayPatterns[dayIndex],
                    usageCount = usageCount,
                    group = group
                )
            )

            DailyMealPlan(
                dayLabel = dayLabel,
                slots = slots,
                totalCalories = slots.sumOf { it.totalCalories },
                totalProtein = slots.sumOf { it.totalProtein },
                totalFat = slots.sumOf { it.totalFat },
                totalCarbs = slots.sumOf { it.totalCarbs }
            )
        }

        return WeeklyMealPlan(group = group, days = dayPlans)
    }

    private fun buildMealSlot(
        dayIndex: Int,
        mealType: String,
        title: String,
        targetCalories: Int,
        targetProtein: Int,
        meals: List<PredefinedMealEntity>,
        categoryPattern: List<String>,
        usageCount: MutableMap<Long, Int>,
        group: MealPlanGroup
    ): PlannedMealSlot {
        val selectedDishes = mutableListOf<PredefinedMealEntity>()
        val preferredCategories = categoryPattern + listOf("закуски", "салаты", "основные блюда", "супы", "десерты", "напитки")
        val maxItems = when {
            mealType == "snack" && group.targetCalories < 1_700 -> 1
            mealType == "breakfast" && group.targetCalories < 1_700 -> 2
            mealType == "dinner" && group.targetCalories >= 2_100 -> 3
            mealType == "lunch" && group.targetCalories >= 1_700 -> 3
            else -> 2
        }

        var remainingCalories = targetCalories
        var remainingProtein = targetProtein

        preferredCategories.forEachIndexed { index, category ->
            if (selectedDishes.size >= maxItems) return@forEachIndexed

            val candidate = selectBestMeal(
                meals = meals,
                mealType = mealType,
                preferredCategory = category,
                remainingCalories = remainingCalories,
                remainingProtein = remainingProtein,
                usageCount = usageCount,
                alreadySelected = selectedDishes,
                variationSeed = dayIndex + index + group.id
            ) ?: return@forEachIndexed

            selectedDishes += candidate
            usageCount[candidate.id] = usageCount.getOrDefault(candidate.id, 0) + 1
            remainingCalories -= candidate.calories
            remainingProtein -= candidate.protein

            val enoughCalories = selectedDishes.sumOf { it.calories } >= (targetCalories * 0.82f).toInt()
            val enoughProtein = selectedDishes.sumOf { it.protein } >= (targetProtein * 0.8f).toInt()
            if (enoughCalories && enoughProtein && selectedDishes.isNotEmpty()) return@forEachIndexed
        }

        if (selectedDishes.isEmpty()) {
            selectBestMeal(
                meals = meals,
                mealType = mealType,
                preferredCategory = null,
                remainingCalories = targetCalories,
                remainingProtein = targetProtein,
                usageCount = usageCount,
                alreadySelected = emptyList(),
                variationSeed = dayIndex + group.id
            )?.let { fallback ->
                selectedDishes += fallback
                usageCount[fallback.id] = usageCount.getOrDefault(fallback.id, 0) + 1
            }
        }

        return PlannedMealSlot(
            mealType = mealType,
            title = title,
            dishes = selectedDishes,
            totalCalories = selectedDishes.sumOf { it.calories },
            totalProtein = selectedDishes.sumOf { it.protein },
            totalFat = selectedDishes.sumOf { it.fat },
            totalCarbs = selectedDishes.sumOf { it.carbs }
        )
    }

    private fun selectBestMeal(
        meals: List<PredefinedMealEntity>,
        mealType: String,
        preferredCategory: String?,
        remainingCalories: Int,
        remainingProtein: Int,
        usageCount: Map<Long, Int>,
        alreadySelected: List<PredefinedMealEntity>,
        variationSeed: Int
    ): PredefinedMealEntity? {
        val candidates = meals
            .asSequence()
            .filter { it.hasMealType(mealType) }
            .filter { preferredCategory == null || it.category == preferredCategory }
            .filter { candidate -> alreadySelected.none { it.id == candidate.id } }
            .sortedBy { candidate ->
                mealScore(
                    meal = candidate,
                    remainingCalories = remainingCalories,
                    remainingProtein = remainingProtein,
                    usageCount = usageCount.getOrDefault(candidate.id, 0),
                    variationSeed = variationSeed,
                    preferredCategory = preferredCategory
                )
            }
            .toList()

        return candidates.firstOrNull()
            ?: meals
                .asSequence()
                .filter { it.hasMealType(mealType) }
                .filter { candidate -> alreadySelected.none { it.id == candidate.id } }
                .minByOrNull { candidate ->
                    mealScore(
                        meal = candidate,
                        remainingCalories = remainingCalories,
                        remainingProtein = remainingProtein,
                        usageCount = usageCount.getOrDefault(candidate.id, 0),
                        variationSeed = variationSeed,
                        preferredCategory = null
                    )
                }
    }

    private fun mealScore(
        meal: PredefinedMealEntity,
        remainingCalories: Int,
        remainingProtein: Int,
        usageCount: Int,
        variationSeed: Int,
        preferredCategory: String?
    ): Int {
        val caloriePenalty = abs(meal.calories - remainingCalories.coerceAtLeast(0))
        val proteinPenalty = abs(meal.protein - remainingProtein.coerceAtLeast(0)) * 7
        val usagePenalty = usageCount * 180
        val overshootPenalty = if (meal.calories > remainingCalories + 120) (meal.calories - remainingCalories) * 2 else 0
        val categoryPenalty = if (preferredCategory != null && meal.category != preferredCategory) 250 else 0
        val variationPenalty = ((meal.id.toInt() + variationSeed) % 7) * 3
        return caloriePenalty + proteinPenalty + usagePenalty + overshootPenalty + categoryPenalty + variationPenalty
    }

    private fun distanceToRange(value: Int, range: IntRange): Int {
        return when {
            value < range.first -> range.first - value
            value > range.last -> value - range.last
            else -> 0
        }
    }

    private fun PredefinedMealEntity.hasMealType(mealType: String): Boolean =
        mealTypes.split(",").map { it.trim() }.any { it.equals(mealType, ignoreCase = true) }

    private fun PredefinedMealEntity.hasTag(tag: String): Boolean =
        tags.split(",").map { it.trim() }.any { it.equals(tag, ignoreCase = true) }

    private fun PredefinedMealEntity.primaryMealType(): String {
        val parsedMealTypes = mealTypes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return parsedMealTypes.firstOrNull() ?: "snack"
    }

    private fun PredefinedMealEntity.toSuggestion(preferredMealType: String? = null): MealSuggestion {
        val mealType = when {
            preferredMealType != null && hasMealType(preferredMealType) -> preferredMealType
            else -> primaryMealType()
        }

        return MealSuggestion(
            mealType = mealType,
            name = name,
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs
        )
    }
}
