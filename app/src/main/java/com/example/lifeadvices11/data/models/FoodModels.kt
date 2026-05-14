package com.example.lifeadvices11.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

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

@Entity(tableName = "meal_entries")
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dailyNutritionId: Long = 0,
    val mealType: String = "",
    val foodName: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val fat: Int = 0,
    val carbs: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "meal_plans")
data class MealPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String = "",
    val name: String = "",
    val description: String = "",
    val totalCalories: Int = 0,
    val meals: String = ""
)

@Entity(tableName = "predefined_meals")
data class PredefinedMealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val fat: Int = 0,
    val carbs: Int = 0,
    val category: String = "",
    val mealTypes: String = "",
    val ingredients: String = "",
    val recipe: String = "",
    val tags: String = ""
)

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

data class MealPlanGroup(
    val id: Int,
    val title: String,
    val calorieRange: IntRange,
    val proteinRange: IntRange,
    val targetCalories: Int,
    val targetProtein: Int
)

data class PlannedMealSlot(
    val mealType: String,
    val title: String,
    val dishes: List<PredefinedMealEntity>,
    val totalCalories: Int,
    val totalProtein: Int,
    val totalFat: Int,
    val totalCarbs: Int
)

data class DailyMealPlan(
    val dayLabel: String,
    val slots: List<PlannedMealSlot>,
    val totalCalories: Int,
    val totalProtein: Int,
    val totalFat: Int,
    val totalCarbs: Int
)

data class WeeklyMealPlan(
    val group: MealPlanGroup,
    val days: List<DailyMealPlan>
)
