package com.example.lifeadvices11.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lifeadvices11.data.entities.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfileSync(): UserProfileEntity?

    @Query("UPDATE user_profile SET height = :height, weight = :weight, age = :age, gender = :gender WHERE id = 1")
    suspend fun updatePersonalInfo(height: Int, weight: Float, age: Int, gender: String)

    @Query("UPDATE user_profile SET activityLevel = :level WHERE id = 1")
    suspend fun updateActivityLevel(level: String)

    @Query("UPDATE user_profile SET goal = :goal WHERE id = 1")
    suspend fun updateGoal(goal: String)

    @Query("UPDATE user_profile SET sportGoal = :goal WHERE id = 1")
    suspend fun updateSportGoal(goal: String)

    @Query("UPDATE user_profile SET bodyFocus = :focus WHERE id = 1")
    suspend fun updateBodyFocus(focus: String)

    @Query("UPDATE user_profile SET fitnessLevel = :level WHERE id = 1")
    suspend fun updateFitnessLevel(level: String)

    @Query("UPDATE user_profile SET trainingDays = :days, trainingCount = :count WHERE id = 1")
    suspend fun updateTrainingDays(days: String, count: Int)

    @Query("UPDATE user_profile SET hasCompletedNutritionOnboarding = 1 WHERE id = 1")
    suspend fun markNutritionOnboardingComplete()

    @Query("SELECT hasCompletedNutritionOnboarding FROM user_profile WHERE id = 1")
    suspend fun hasCompletedNutritionOnboarding(): Boolean?

    @Query("UPDATE user_profile SET hasCompletedSportOnboarding = 1 WHERE id = 1")
    suspend fun markSportOnboardingComplete()

    @Query("SELECT hasCompletedSportOnboarding FROM user_profile WHERE id = 1")
    suspend fun hasCompletedSportOnboarding(): Boolean?

    @Query("UPDATE user_profile SET dailyCalories = dailyCalories + :calories WHERE id = 1")
    suspend fun addCaloriesToDaily(calories: Int)

    @Query("UPDATE user_profile SET dailyCalories = MAX(dailyCalories - :calories, 0) WHERE id = 1")
    suspend fun subtractCaloriesFromDaily(calories: Int)

    @Query("UPDATE user_profile SET dailyCalories = 0 WHERE id = 1")
    suspend fun resetDailyCalories()

    @Query("SELECT dailyCalories FROM user_profile WHERE id = 1")
    suspend fun getDailyCalories(): Int
}
