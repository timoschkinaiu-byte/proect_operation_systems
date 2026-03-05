package com.example.lifeadvices11.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.lifeadvices11.data.dao.NutritionDao
import com.example.lifeadvices11.data.dao.UserProfileDao
import com.example.lifeadvices11.data.entities.UserProfileEntity
import com.example.lifeadvices11.data.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        DailyNutritionEntity::class,
        MealEntryEntity::class,
        PredefinedMealEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun nutritionDao(): NutritionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "life_advices_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {  // ✅ Исправлено!
                        override fun onCreate(db: SupportSQLiteDatabase) {  // ✅ Правильный параметр!
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    prePopulateMeals(database.nutritionDao())
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prePopulateMeals(dao: NutritionDao) {
            val meals = listOf(
                // Завтраки
                PredefinedMealEntity(name = "Овсянка с ягодами", calories = 250, protein = 10, fat = 5, carbs = 40, category = "breakfast", tags = "пп,быстро"),
                PredefinedMealEntity(name = "Омлет с сыром", calories = 350, protein = 20, fat = 18, carbs = 8, category = "breakfast", tags = "сытно"),
                PredefinedMealEntity(name = "Гречка с молоком", calories = 300, protein = 12, fat = 8, carbs = 45, category = "breakfast", tags = "пп"),
                PredefinedMealEntity(name = "Творог с фруктами", calories = 280, protein = 25, fat = 5, carbs = 30, category = "breakfast", tags = "пп,белок"),

                // Обеды
                PredefinedMealEntity(name = "Куриная грудка с гречкой", calories = 450, protein = 35, fat = 8, carbs = 55, category = "lunch", tags = "пп,мясо"),
                PredefinedMealEntity(name = "Рыба с рисом", calories = 400, protein = 30, fat = 10, carbs = 50, category = "lunch", tags = "рыба"),
                PredefinedMealEntity(name = "Паста с курицей", calories = 500, protein = 28, fat = 15, carbs = 65, category = "lunch", tags = "сытно"),
                PredefinedMealEntity(name = "Суп куриный", calories = 250, protein = 20, fat = 8, carbs = 25, category = "lunch", tags = "лёгкий"),

                // Ужины
                PredefinedMealEntity(name = "Рыба на пару с овощами", calories = 320, protein = 28, fat = 12, carbs = 20, category = "dinner", tags = "пп,лёгкий"),
                PredefinedMealEntity(name = "Куриное филе с салатом", calories = 350, protein = 32, fat = 10, carbs = 25, category = "dinner", tags = "пп"),
                PredefinedMealEntity(name = "Овощное рагу", calories = 200, protein = 8, fat = 5, carbs = 30, category = "dinner", tags = "веган"),
                PredefinedMealEntity(name = "Творожная запеканка", calories = 280, protein = 22, fat = 8, carbs = 30, category = "dinner", tags = "пп"),

                // Перекусы
                PredefinedMealEntity(name = "Яблоко", calories = 80, protein = 0, fat = 0, carbs = 20, category = "snack", tags = "фрукты"),
                PredefinedMealEntity(name = "Йогурт", calories = 120, protein = 8, fat = 3, carbs = 15, category = "snack", tags = "молочка"),
                PredefinedMealEntity(name = "Орехи", calories = 150, protein = 5, fat = 12, carbs = 5, category = "snack", tags = "полезно"),
                PredefinedMealEntity(name = "Банан", calories = 105, protein = 1, fat = 0, carbs = 27, category = "snack", tags = "фрукты")
            )
            dao.insertPredefinedMeals(meals)
        }
    }
}