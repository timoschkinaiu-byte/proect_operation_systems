package com.example.lifeadvices11.utils

import com.example.lifeadvices11.data.models.ActivityLevel
import com.example.lifeadvices11.data.models.NutritionNorm
import com.example.lifeadvices11.data.models.UserAnthroData

object NutritionCalculator {

    private fun calculateBMR(data: UserAnthroData): Double {
        return when (data.gender.lowercase()) {
            "male" -> (10 * data.weight) + (6.25 * data.height) - (5 * data.age) + 5
            "female" -> (10 * data.weight) + (6.25 * data.height) - (5 * data.age) - 161
            else -> 0.0
        }
    }

    private fun getActivityMultiplier(levelKey: String): Double {
        return when (levelKey) {
            "sedentary" -> ActivityLevel.SEDENTARY.multiplier
            "light" -> ActivityLevel.LIGHT.multiplier
            "moderate" -> ActivityLevel.MODERATE.multiplier
            "active" -> ActivityLevel.ACTIVE.multiplier
            "extreme" -> ActivityLevel.EXTREME.multiplier
            else -> 1.2
        }
    }

    private fun adjustCaloriesForGoal(calories: Double, goal: String): Double {
        return when (goal.lowercase()) {
            "lose" -> calories * 0.85
            "gain" -> calories * 1.15
            else -> calories
        }
    }

    fun calculateNutritionNorm(data: UserAnthroData): NutritionNorm {
        val bmr = calculateBMR(data)
        val activityMultiplier = getActivityMultiplier(data.activityLevel)
        val tdee = bmr * activityMultiplier
        val adjustedCalories = adjustCaloriesForGoal(tdee, data.goal)

        val calories = adjustedCalories.toInt()

        return when (data.goal.lowercase()) {
            "lose" -> {
                val protein = (calories * 0.3 / 4).toInt()
                val fat = (calories * 0.25 / 9).toInt()
                val carbs = (calories * 0.45 / 4).toInt()
                NutritionNorm(calories, protein, fat, carbs)
            }
            "gain" -> {
                val protein = (calories * 0.25 / 4).toInt()
                val fat = (calories * 0.2 / 9).toInt()
                val carbs = (calories * 0.55 / 4).toInt()
                NutritionNorm(calories, protein, fat, carbs)
            }
            else -> {
                val protein = (calories * 0.25 / 4).toInt()
                val fat = (calories * 0.25 / 9).toInt()
                val carbs = (calories * 0.5 / 4).toInt()
                NutritionNorm(calories, protein, fat, carbs)
            }
        }
    }
}