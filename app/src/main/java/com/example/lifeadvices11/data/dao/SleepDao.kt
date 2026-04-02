package com.example.lifeadvices11.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lifeadvices11.data.entities.SleepProfileEntity
import com.example.lifeadvices11.data.entities.DailySleepEntity
import com.example.lifeadvices11.data.entities.SleepPracticeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    // Sleep Profile
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepProfile(profile: SleepProfileEntity)

    @Update
    suspend fun updateSleepProfile(profile: SleepProfileEntity)

    @Query("SELECT * FROM sleep_profile WHERE id = 1")
    fun getSleepProfile(): Flow<SleepProfileEntity?>

    @Query("SELECT * FROM sleep_profile WHERE id = 1")
    suspend fun getSleepProfileSync(): SleepProfileEntity?

    @Query("UPDATE sleep_profile SET targetSleepHours = :targetHours, typicalBedTime = :bedTime, typicalWakeTime = :wakeTime, sleepQuality = :quality, sleepIssues = :issues, preferredWakeUpTime = :preferredWakeTime, hasCompletedSleepOnboarding = 1 WHERE id = 1")
    suspend fun saveSleepOnboardingData(
        targetHours: Double,
        bedTime: String,
        wakeTime: String,
        quality: String,
        issues: String,
        preferredWakeTime: String
    )
    @Query("SELECT hasCompletedSleepOnboarding FROM sleep_profile WHERE id = 1")
    suspend fun hasCompletedSleepOnboarding(): Boolean?
    // Daily Sleep Tracking
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySleep(entry: DailySleepEntity)
    @Query("SELECT * FROM daily_sleep WHERE date = :date")
    suspend fun getDailySleepByDate(date: Long): DailySleepEntity?
    @Query("SELECT * FROM daily_sleep WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getSleepHistory(startDate: Long, endDate: Long): Flow<List<DailySleepEntity>>
    @Query("SELECT * FROM daily_sleep ORDER BY date DESC LIMIT 7")
    suspend fun getLastWeekSleep(): List<DailySleepEntity>



    // Sleep practices
    @Query("SELECT * FROM sleep_practices")
    suspend fun getAllPractices(): List<SleepPracticeEntity>

    @Query("SELECT * FROM sleep_practices WHERE category = :category")
    suspend fun getPracticesByCategory(category: String): List<SleepPracticeEntity>

    @Update
    suspend fun updatePractice(practice: SleepPracticeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPractices(practices: List<SleepPracticeEntity>)
}