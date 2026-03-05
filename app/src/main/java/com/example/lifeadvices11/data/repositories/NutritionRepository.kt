package com.example.lifeadvices11.data.repositories

import com.example.lifeadvices11.data.dao.NutritionDao
import com.example.lifeadvices11.data.dao.UserProfileDao
import com.example.lifeadvices11.data.models.*
import com.example.lifeadvices11.utils.NutritionCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

class NutritionRepository(
    private val nutritionDao: NutritionDao,
    private val userDao: UserProfileDao
) {

    // Получить сегодняшние данные
    suspend fun getTodayNutrition(): DailyNutritionEntity? {
        val todayStart = getStartOfDay()
        return nutritionDao.getDailyNutritionByDate(todayStart)
    }

    // Получить приемы пищи за сегодня
    suspend fun getTodayMeals(): List<MealEntryEntity> {
        val todayStart = getStartOfDay()
        val today = nutritionDao.getDailyNutritionByDate(todayStart)
        return if (today != null) {
            nutritionDao.getMealsForDaySync(today.id)
        } else {
            emptyList()
        }
    }
    // Создать запись на сегодня
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

            // Получаем с ID
            today = nutritionDao.getDailyNutritionByDate(todayStart)!!
        }
        return today
    }

    // Добавить прием пищи
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

        // Добавляем запись о приеме пищи
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

        // Обновляем суммарные показатели за день
        val updatedDaily = today.copy(
            totalCalories = today.totalCalories + calories,
            totalProtein = today.totalProtein + protein,
            totalFat = today.totalFat + fat,
            totalCarbs = today.totalCarbs + carbs
        )
        nutritionDao.insertDailyNutrition(updatedDaily)
    }

    // Получить готовые блюда для рационов
    suspend fun getPredefinedMeals(): List<PredefinedMealEntity> {
        return nutritionDao.getAllPredefinedMeals()
    }

    // Получить блюда по категории
    suspend fun getMealsByCategory(category: String): List<PredefinedMealEntity> {
        return nutritionDao.getMealsByCategory(category)
    }

    // Получить рекомендованные блюда на основе цели пользователя
    suspend fun getRecommendedMealPlans(): List<MealPlanCategory> {
        val userData = getUserAnthroData() ?: return emptyList()
        val allMeals = getPredefinedMeals()

        return when (userData.goal.lowercase()) {
            "lose" -> listOf(
                MealPlanCategory(
                    title = "🔥 Для похудения",
                    meals = allMeals.filter { it.tags.contains("пп") || it.calories < 300 }
                        .take(4)
                        .map { it.toSuggestion() }
                ),
                MealPlanCategory(
                    title = "🍽️ Легкие ужины",
                    meals = allMeals.filter { it.category == "dinner" && it.calories < 350 }
                        .take(3)
                        .map { it.toSuggestion() }
                )
            )
            "gain" -> listOf(
                MealPlanCategory(
                    title = "💪 Для набора массы",
                    meals = allMeals.filter { it.calories > 400 || it.tags.contains("сытно") }
                        .take(4)
                        .map { it.toSuggestion() }
                ),
                MealPlanCategory(
                    title = "🍗 С высоким содержанием белка",
                    meals = allMeals.filter { it.protein > 25 }
                        .take(3)
                        .map { it.toSuggestion() }
                )
            )
            else -> listOf(
                MealPlanCategory(
                    title = "⚖️ Сбалансированное питание",
                    meals = allMeals.filter { it.protein in 15..30 && it.fat < 15 }
                        .take(4)
                        .map { it.toSuggestion() }
                ),
                MealPlanCategory(
                    title = "🍏 Популярное",
                    meals = allMeals.filter { it.tags.contains("популярное") || it.tags.contains("пп") }
                        .take(3)
                        .map { it.toSuggestion() }
                )
            )
        }
    }

    // Вспомогательные функции
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

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun PredefinedMealEntity.toSuggestion(): MealSuggestion {
        return MealSuggestion(
            mealType = this.category,
            name = this.name,
            calories = this.calories,
            protein = this.protein,
            fat = this.fat,
            carbs = this.carbs
        )
    }
}