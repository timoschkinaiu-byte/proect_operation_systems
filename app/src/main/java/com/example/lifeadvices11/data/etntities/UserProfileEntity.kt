package com.example.lifeadvices11.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1,  // всегда 1, так как профиль один

    // Антропометрические данные
    val height: Int = 0,
    val weight: Float = 0f,
    val age: Int = 0,
    val gender: String = "",  // "male" или "female"
    val activityLevel: String = "",  // "sedentary", "light", "moderate", "active", "extreme"
    val goal: String = "",  // "lose", "maintain", "gain"

    // Флаги прохождения онбординга
    val hasCompletedNutritionOnboarding: Boolean = false,
    val hasCompletedSportOnboarding: Boolean = false,
    val hasCompletedPsychologyOnboarding: Boolean = false,
    val hasCompletedSleepOnboarding: Boolean = false,
    val hasCompletedStudyOnboarding: Boolean = false
)