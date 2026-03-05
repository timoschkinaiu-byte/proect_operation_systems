package com.example.lifeadvices11.data.repositories

import com.example.lifeadvices11.data.dao.UserProfileDao
import com.example.lifeadvices11.data.entities.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import com.example.lifeadvices11.data.models.UserAnthroData

class UserRepository(
    private val dao: UserProfileDao
) {

    fun getProfile(): Flow<UserProfileEntity?> = dao.getProfile()

    suspend fun getProfileSync(): UserProfileEntity? = dao.getProfileSync()

    suspend fun createProfileIfNotExists() {
        val existingProfile = dao.getProfileSync()
        if (existingProfile == null) {
            dao.insertProfile(UserProfileEntity(id = 1))
        }
    }

    suspend fun saveNutritionOnboardingData(
        height: Int,
        weight: Float,
        age: Int,
        gender: String,
        activityLevel: String,
        goal: String
    ) {
        dao.updatePersonalInfo(height, weight, age, gender)
        dao.updateActivityLevel(activityLevel)
        dao.updateGoal(goal)
        dao.markNutritionOnboardingComplete()
    }

    suspend fun hasCompletedNutritionOnboarding(): Boolean {
        return dao.hasCompletedNutritionOnboarding() ?: false
    }

    suspend fun getNutritionData(): UserAnthroData? {
        val profile = dao.getProfileSync() ?: return null
        return UserAnthroData(
            height = profile.height,
            weight = profile.weight,
            age = profile.age,
            gender = profile.gender,
            activityLevel = profile.activityLevel,
            goal = profile.goal
        )
    }
}