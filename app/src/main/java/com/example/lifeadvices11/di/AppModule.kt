package com.example.lifeadvices11.di

import android.content.Context
import com.example.lifeadvices11.data.database.AppDatabase
import com.example.lifeadvices11.data.repositories.NutritionRepository
import com.example.lifeadvices11.data.repositories.UserRepository
import com.example.lifeadvices11.data.repositories.SleepRepository

object AppModule {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun provideUserRepository(): UserRepository {
        val database = AppDatabase.getInstance(appContext)
        return UserRepository(database.userProfileDao())
    }
    fun provideNutritionRepository(): NutritionRepository {
        val database = AppDatabase.getInstance(appContext)
        return NutritionRepository(
            nutritionDao = database.nutritionDao(),
            userDao = database.userProfileDao()
        )
    }
    fun provideSleepRepository(): SleepRepository {
        val database = AppDatabase.getInstance(appContext)
        return SleepRepository(database.sleepDao())
    }
}