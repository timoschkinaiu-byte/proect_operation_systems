package com.example.lifeadvices11.ui.sections.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeadvices11.data.models.*
import com.example.lifeadvices11.data.repositories.NutritionRepository
import com.example.lifeadvices11.data.repositories.UserRepository
import com.example.lifeadvices11.di.AppModule
import com.example.lifeadvices11.utils.NutritionCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NutritionViewModel : ViewModel() {

    private val userRepository: UserRepository = AppModule.provideUserRepository()
    private val nutritionRepository: NutritionRepository = AppModule.provideNutritionRepository()

    private val _todayNutrition = MutableStateFlow<DailyNutritionEntity?>(null)
    val todayNutrition: StateFlow<DailyNutritionEntity?> = _todayNutrition

    private val _userNorms = MutableStateFlow<NutritionNorm?>(null)
    val userNorms: StateFlow<NutritionNorm?> = _userNorms

    private val _todayMeals = MutableStateFlow<List<MealEntryEntity>>(emptyList())
    val todayMeals: StateFlow<List<MealEntryEntity>> = _todayMeals

    private val _recommendedMealPlans = MutableStateFlow<List<MealPlanCategory>>(emptyList())
    val recommendedMealPlans: StateFlow<List<MealPlanCategory>> = _recommendedMealPlans

    private val _caloriesProgress = MutableStateFlow(0f)
    val caloriesProgress: StateFlow<Float> = _caloriesProgress

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true

            val userProfile = userRepository.getProfileSync()

            if (userProfile != null) {
                val userData = UserAnthroData(
                    height = userProfile.height,
                    weight = userProfile.weight,
                    age = userProfile.age,
                    gender = userProfile.gender,
                    activityLevel = userProfile.activityLevel,
                    goal = userProfile.goal
                )

                val norms = NutritionCalculator.calculateNutritionNorm(userData)
                _userNorms.value = norms

                val today = nutritionRepository.createTodayNutritionIfNotExists(userData)
                _todayNutrition.value = today

                val meals = nutritionRepository.getTodayMeals()
                _todayMeals.value = meals

                _caloriesProgress.value = if (norms.calories > 0) {
                    today.totalCalories.toFloat() / norms.calories.toFloat()
                } else 0f

                val plans = nutritionRepository.getRecommendedMealPlans()
                _recommendedMealPlans.value = plans
            }

            _isLoading.value = false
        }
    }

    fun addMeal(
        mealType: String,
        foodName: String,
        calories: Int,
        protein: Int,
        fat: Int,
        carbs: Int
    ) {
        viewModelScope.launch {
            nutritionRepository.addMeal(mealType, foodName, calories, protein, fat, carbs)
            loadData()
        }
    }

    fun refreshData() {
        loadData()
    }
}