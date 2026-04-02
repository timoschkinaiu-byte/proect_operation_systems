package com.example.lifeadvices11.data.repositories

import com.example.lifeadvices11.data.dao.SleepDao
import com.example.lifeadvices11.data.entities.SleepProfileEntity
import com.example.lifeadvices11.data.entities.DailySleepEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.*

class SleepRepository(
    private val sleepDao: SleepDao
) {

    // Profile operations
    fun getSleepProfile(): Flow<SleepProfileEntity?> = sleepDao.getSleepProfile()

    suspend fun getSleepProfileSync(): SleepProfileEntity? = sleepDao.getSleepProfileSync()

    suspend fun createSleepProfileIfNotExists() {
        val existingProfile = sleepDao.getSleepProfileSync()
        if (existingProfile == null) {
            sleepDao.insertSleepProfile(SleepProfileEntity(id = 1))
        }
    }

    suspend fun saveSleepOnboardingData(
        targetHours: Double,
        bedTime: String,
        wakeTime: String,
        quality: String,
        issues: String,
        preferredWakeTime: String
    ) {
        sleepDao.saveSleepOnboardingData(
            targetHours, bedTime, wakeTime, quality, issues, preferredWakeTime
        )
    }

    suspend fun hasCompletedSleepOnboarding(): Boolean {
        return sleepDao.hasCompletedSleepOnboarding() ?: false
    }

    suspend fun getSleepGoal(): Double {
        val profile = sleepDao.getSleepProfileSync()
        return profile?.targetSleepHours ?: 8.0
    }

    // Daily tracking operations
    suspend fun addSleepEntry(
        sleepHours: Float,
        bedTime: String,
        wakeTime: String,
        quality: Int,
        notes: String = ""
    ) {
        val todayStart = getStartOfDay()
        val existingEntry = sleepDao.getDailySleepByDate(todayStart)

        val entry = DailySleepEntity(
            id = existingEntry?.id ?: 0,
            date = todayStart,
            sleepHours = sleepHours,
            bedTime = bedTime,
            wakeTime = wakeTime,
            quality = quality,
            notes = notes
        )
        sleepDao.insertDailySleep(entry)
    }

    suspend fun getTodaySleep(): DailySleepEntity? {
        val todayStart = getStartOfDay()
        return sleepDao.getDailySleepByDate(todayStart)
    }

    suspend fun getLastWeekSleep(): List<DailySleepEntity> {
        return sleepDao.getLastWeekSleep()
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}