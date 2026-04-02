package com.example.lifeadvices11.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_profile")
data class SleepProfileEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1, // всегда 1, так как профиль один
    // Данные онбординга сна
    val targetSleepHours: Double = 0.0,      // целевое количество часов сна
    val typicalBedTime: String = "",         // обычное время отхода ко сну (например, "23:00")
    val typicalWakeTime: String = "",        // обычное время пробуждения (например, "07:00")
    val sleepQuality: String = "",           // качество сна: "good", "normal", "poor"
    val sleepIssues: String = "",            // проблемы со сном: "insomnia", "snoring", "night_wakeups", "none"
    val preferredWakeUpTime: String = "",    // желаемое время пробуждения

    // Флаг прохождения онбординга сна
    val hasCompletedSleepOnboarding: Boolean = false
)

// Сущность для ежедневного трекинга сна
@Entity(tableName = "daily_sleep")
data class DailySleepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val sleepHours: Float = 0f,
    val bedTime: String = "",        // время отхода ко сну
    val wakeTime: String = "",       // время пробуждения
    val quality: Int = 3,            // качество сна от 1 до 5
    val notes: String = ""           // заметки о сне
)