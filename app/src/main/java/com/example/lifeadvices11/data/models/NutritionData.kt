package com.example.lifeadvices11.data.models

data class UserAnthroData(
    val height: Int = 0,
    val weight: Float = 0f,
    val age: Int = 0,
    val gender: String = "",
    val activityLevel: String = "",
    val goal: String = ""
)

data class NutritionNorm(
    val calories: Int = 0,
    val protein: Int = 0,
    val fat: Int = 0,
    val carbs: Int = 0
)