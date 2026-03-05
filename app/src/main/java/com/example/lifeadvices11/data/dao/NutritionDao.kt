package com.example.lifeadvices11.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lifeadvices11.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionDao {

    // Daily Nutrition
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyNutrition(entry: DailyNutritionEntity)

    @Query("SELECT * FROM daily_nutrition WHERE date = :date")
    suspend fun getDailyNutritionByDate(date: Long): DailyNutritionEntity?

    @Query("SELECT * FROM daily_nutrition ORDER BY date DESC LIMIT 1")
    suspend fun getLatestDailyNutrition(): DailyNutritionEntity?

    // Meal Entries
    @Insert
    suspend fun insertMealEntry(entry: MealEntryEntity)

    @Query("SELECT * FROM meal_entries WHERE dailyNutritionId = :dailyNutritionId")
    fun getMealsForDay(dailyNutritionId: Long): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM meal_entries WHERE dailyNutritionId = :dailyNutritionId")
    suspend fun getMealsForDaySync(dailyNutritionId: Long): List<MealEntryEntity>

    // Predefined Meals (для рационов)
    @Query("SELECT * FROM predefined_meals")
    suspend fun getAllPredefinedMeals(): List<PredefinedMealEntity>

    @Query("SELECT * FROM predefined_meals WHERE category = :category")
    suspend fun getMealsByCategory(category: String): List<PredefinedMealEntity>

    // Для инициализации базы готовыми блюдами
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPredefinedMeals(meals: List<PredefinedMealEntity>)
}