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

    @Query("UPDATE user_profile SET hasCompletedNutritionOnboarding = 1 WHERE id = 1")
    suspend fun markNutritionOnboardingComplete()

    @Query("SELECT hasCompletedNutritionOnboarding FROM user_profile WHERE id = 1")
    suspend fun hasCompletedNutritionOnboarding(): Boolean?
}