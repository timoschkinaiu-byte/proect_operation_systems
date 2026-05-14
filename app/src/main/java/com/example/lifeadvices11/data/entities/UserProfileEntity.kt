package com.example.lifeadvices11.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1,
    val height: Int = 0,
    val weight: Float = 0f,
    val age: Int = 0,
    val gender: String = "",
    val activityLevel: String = "",
    val goal: String = "",
    val sportGoal: String = "",
    val bodyFocus: String = "",
    val fitnessLevel: String = "",
    val trainingDays: String = "",
    val trainingCount: Int = 0,
    val dailyCalories: Int = 0,
    val workoutLevel: Int = 1,
    val workoutCompletions: Int = 0,
    val lastWorkoutDate: Long? = null,
    val hasCompletedNutritionOnboarding: Boolean = false,
    val hasCompletedSportOnboarding: Boolean = false,
    val hasCompletedPsychologyOnboarding: Boolean = false,
    val hasCompletedSleepOnboarding: Boolean = false,
    val hasCompletedStudyOnboarding: Boolean = false
)
