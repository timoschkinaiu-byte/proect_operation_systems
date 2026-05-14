package com.example.lifeadvices11.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.lifeadvices11.data.dao.NutritionDao
import com.example.lifeadvices11.data.dao.PsychologyDao
import com.example.lifeadvices11.data.dao.SleepDao
import com.example.lifeadvices11.data.dao.StudyDao
import com.example.lifeadvices11.data.dao.UserProfileDao
import com.example.lifeadvices11.data.dao.WorkoutDao
import com.example.lifeadvices11.data.entities.DailyEmotionEntity
import com.example.lifeadvices11.data.entities.DailySleepEntity
import com.example.lifeadvices11.data.entities.DailyStudyEntity
import com.example.lifeadvices11.data.entities.PsychologyPracticeEntity
import com.example.lifeadvices11.data.entities.PsychologyProfileEntity
import com.example.lifeadvices11.data.entities.SleepPracticeEntity
import com.example.lifeadvices11.data.entities.SleepProfileEntity
import com.example.lifeadvices11.data.entities.StudyCategoryEntity
import com.example.lifeadvices11.data.entities.StudyProfileEntity
import com.example.lifeadvices11.data.entities.UserProfileEntity
import com.example.lifeadvices11.data.entities.UserWorkoutEntity
import com.example.lifeadvices11.data.entities.WorkoutCompletionEntity
import com.example.lifeadvices11.data.models.DailyNutritionEntity
import com.example.lifeadvices11.data.models.MealEntryEntity
import com.example.lifeadvices11.data.models.PredefinedMealEntity
import com.example.lifeadvices11.data.models.WeightEntryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        UserWorkoutEntity::class,
        WorkoutCompletionEntity::class,
        DailyNutritionEntity::class,
        WeightEntryEntity::class,
        MealEntryEntity::class,
        SleepProfileEntity::class,
        DailySleepEntity::class,
        StudyProfileEntity::class,
        DailyStudyEntity::class,
        PsychologyProfileEntity::class,
        DailyEmotionEntity::class,
        PredefinedMealEntity::class,
        SleepPracticeEntity::class,
        StudyCategoryEntity::class,
        PsychologyPracticeEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun sleepDao(): SleepDao
    abstract fun studyDao(): StudyDao
    abstract fun psychologyDao(): PsychologyDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @Volatile
        private var instanceRef: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "life_advices_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            instanceRef?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    prePopulateMeals(database.nutritionDao())
                                    prePopulateSleepPractices(database.sleepDao())
                                    prePopulatePsychologyPractices(database.psychologyDao())
                                }
                            }
                        }
                    })
                    .build()
                instanceRef = instance
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prePopulateMeals(dao: NutritionDao) {
            dao.insertPredefinedMeals(NutritionSeedData.predefinedMeals())
        }

        private suspend fun prePopulateSleepPractices(dao: SleepDao) {
            val practices = listOf(
                SleepPracticeEntity(
                    title = "Р”С‹С…Р°С‚РµР»СЊРЅР°СЏ С‚РµС…РЅРёРєР° 4-7-8",
                    shortDescription = "РЈСЃРїРѕРєР°РёРІР°РµС‚ РЅРµСЂРІРЅСѓСЋ СЃРёСЃС‚РµРјСѓ Р·Р° 3 РјРёРЅСѓС‚С‹",
                    fullDescription = "Р­С‚Р° С‚РµС…РЅРёРєР° РґС‹С…Р°РЅРёСЏ Р±С‹Р»Р° СЂР°Р·СЂР°Р±РѕС‚Р°РЅР° РґРѕРєС‚РѕСЂРѕРј Р­РЅРґСЂСЋ Р’РµР№Р»РѕРј. РћРЅР° РѕСЃРЅРѕРІР°РЅР° РЅР° РґСЂРµРІРЅРµР№ РїСЂР°РЅР°СЏРјРµ Рё РїРѕРјРѕРіР°РµС‚ Р±С‹СЃС‚СЂРѕ СѓСЃРїРѕРєРѕРёС‚СЊСЃСЏ, СЃРЅРёР·РёС‚СЊ С‚СЂРµРІРѕР¶РЅРѕСЃС‚СЊ Рё РїРѕРґРіРѕС‚РѕРІРёС‚СЊСЃСЏ РєРѕ СЃРЅСѓ. РњРµС‚РѕРґРёРєР° СЂР°Р±РѕС‚Р°РµС‚ Р·Р° СЃС‡С‘С‚ СѓРґР»РёРЅРµРЅРёСЏ РІС‹РґРѕС…Р°, С‡С‚Рѕ Р°РєС‚РёРІРёСЂСѓРµС‚ РїР°СЂР°СЃРёРјРїР°С‚РёС‡РµСЃРєСѓСЋ РЅРµСЂРІРЅСѓСЋ СЃРёСЃС‚РµРјСѓ Рё Р·Р°РјРµРґР»СЏРµС‚ СЃРµСЂРґРµС‡РЅС‹Р№ СЂРёС‚Рј.",
                    steps = "1. РЎСЏРґСЊС‚Рµ РїСЂСЏРјРѕ РёР»Рё Р»СЏРіС‚Рµ РЅР° СЃРїРёРЅСѓ. РљРѕРЅС‡РёРє СЏР·С‹РєР° РґРѕР»Р¶РµРЅ РєР°СЃР°С‚СЊСЃСЏ РЅС‘Р±Р° Р·Р° РІРµСЂС…РЅРёРјРё Р·СѓР±Р°РјРё.\n\n2. РџРѕР»РЅРѕСЃС‚СЊСЋ РІС‹РґРѕС…РЅРёС‚Рµ С‡РµСЂРµР· СЂРѕС‚, РёР·РґР°РІР°СЏ Р»С‘РіРєРёР№ СЃРІРёСЃС‚СЏС‰РёР№ Р·РІСѓРє.\n\n3. Р—Р°РєСЂРѕР№С‚Рµ СЂРѕС‚ Рё РјРµРґР»РµРЅРЅРѕ РІРґРѕС…РЅРёС‚Рµ С‡РµСЂРµР· РЅРѕСЃ РЅР° 4 СЃРµРєСѓРЅРґС‹.\n\n4. Р—Р°РґРµСЂР¶РёС‚Рµ РґС‹С…Р°РЅРёРµ РЅР° 7 СЃРµРєСѓРЅРґ.\n\n5. Р’С‹РґРѕС…РЅРёС‚Рµ С‡РµСЂРµР· СЂРѕС‚ РЅР° 8 СЃРµРєСѓРЅРґ, СЃРЅРѕРІР° РёР·РґР°РІР°СЏ СЃРІРёСЃС‚СЏС‰РёР№ Р·РІСѓРє.\n\n6. РџРѕРІС‚РѕСЂРёС‚Рµ С†РёРєР» 4-8 СЂР°Р·.",
                    duration = 3,
                    category = "breathing",
                    benefits = "РЎРЅРёР¶Р°РµС‚ С‚СЂРµРІРѕР¶РЅРѕСЃС‚СЊ\nРџРѕРјРѕРіР°РµС‚ СѓСЃРЅСѓС‚СЊ",
                    contraindications = "РџСЂРё СЃРёР»СЊРЅРѕРј РіРѕР»РѕРІРѕРєСЂСѓР¶РµРЅРёРё"
                )
            )
            dao.insertPractices(practices)
        }

        private suspend fun prePopulatePsychologyPractices(dao: PsychologyDao) {
            val practices = listOf(
                PsychologyPracticeEntity(
                    title = "РљРІР°РґСЂР°С‚РЅРѕРµ РґС‹С…Р°РЅРёРµ",
                    shortDescription = "РЎРЅРёР¶Р°РµС‚ РѕСЃС‚СЂРѕРµ РЅР°РїСЂСЏР¶РµРЅРёРµ Р·Р° 3-5 РјРёРЅСѓС‚.",
                    fullDescription = "РџСЂР°РєС‚РёРєР° РїРѕРјРѕРіР°РµС‚ Р±С‹СЃС‚СЂРѕ Р·Р°РјРµРґР»РёС‚СЊСЃСЏ, РІС‹СЂРѕРІРЅСЏС‚СЊ РґС‹С…Р°РЅРёРµ Рё РІРµСЂРЅСѓС‚СЊ РІРЅРёРјР°РЅРёРµ РІ РЅР°СЃС‚РѕСЏС‰РёР№ РјРѕРјРµРЅС‚.",
                    category = "breathing",
                    durationMinutes = 5,
                    targetGoal = "reduce_anxiety",
                    isRecommended = true
                ),
                PsychologyPracticeEntity(
                    title = "Р”РЅРµРІРЅРёРє РјС‹СЃР»РµР№",
                    shortDescription = "РџРѕРјРѕРіР°РµС‚ РІС‹РЅРµСЃС‚Рё С‚СЂРµРІРѕР¶РЅС‹Рµ РјС‹СЃР»Рё РёР· РіРѕР»РѕРІС‹ РЅР° Р±СѓРјР°РіСѓ.",
                    fullDescription = "РљРѕСЂРѕС‚РєР°СЏ РїРёСЃСЊРјРµРЅРЅР°СЏ РїСЂР°РєС‚РёРєР° РґР»СЏ РѕСЃРјС‹СЃР»РµРЅРёСЏ СЃРѕСЃС‚РѕСЏРЅРёСЏ.",
                    category = "journaling",
                    durationMinutes = 10,
                    targetGoal = "understand_emotions",
                    isRecommended = true
                )
            )
            dao.insertPractices(practices)
        }
    }
}
