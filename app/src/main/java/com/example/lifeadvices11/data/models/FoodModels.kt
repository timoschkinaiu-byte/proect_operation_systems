package com.example.lifeadvices11.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

// Сущность для ежедневного трекинга
@Entity(tableName = "daily_nutrition")
data class DailyNutritionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val totalCalories: Int = 0,
    val totalProtein: Int = 0,
    val totalFat: Int = 0,
    val totalCarbs: Int = 0,
    val goalCalories: Int = 0,
    val goalProtein: Int = 0,
    val goalFat: Int = 0,
    val goalCarbs: Int = 0
)

// Сущность для приемов пищи
@Entity(tableName = "meal_entries")
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dailyNutritionId: Long = 0,
    val mealType: String = "", // "breakfast", "lunch", "dinner", "snack"
    val foodName: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val fat: Int = 0,
    val carbs: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

// Сущность для готовых рационов (встроенные в приложение)
@Entity(tableName = "meal_plans")
data class MealPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String = "", // "lose_weight", "gain_mass", "maintain"
    val name: String = "",
    val description: String = "",
    val totalCalories: Int = 0,
    val meals: String = "" // JSON строка с приемами пищи
)

// Сущность для готовых блюд
@Entity(tableName = "predefined_meals")
data class PredefinedMealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val fat: Int = 0,
    val carbs: Int = 0,
    val category: String = "", // "breakfast", "lunch", "dinner", "snack"
    val tags: String = "" // "популярное,быстрое,пп"
)

// Модель для UI
data class MealSuggestion(
    val mealType: String,
    val name: String,
    val calories: Int,
    val protein: Int,
    val fat: Int,
    val carbs: Int
)

data class MealPlanCategory(
    val title: String,
    val meals: List<MealSuggestion>
)