package com.example.lifeadvices11.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_practices")
data class SleepPracticeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val shortDescription: String = "",
    val fullDescription: String = "",
    val steps: String = "",
    val duration: Int = 0,
    val category: String = "",
    val benefits: String = "",
    val contraindications: String = "",
    val isCompleted: Boolean = false
)