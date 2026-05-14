package com.example.lifeadvices11.ui.sections.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeadvices11.data.models.DailyNutritionEntity
import com.example.lifeadvices11.data.models.MealEntryEntity
import com.example.lifeadvices11.data.models.NutritionNorm
import com.example.lifeadvices11.data.models.PlannedMealSlot
import com.example.lifeadvices11.data.models.PredefinedMealEntity
import com.example.lifeadvices11.data.models.UserAnthroData
import com.example.lifeadvices11.data.models.WeeklyMealPlan
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

    private val _selectedWeeklyPlan = MutableStateFlow<WeeklyMealPlan?>(null)
    val selectedWeeklyPlan: StateFlow<WeeklyMealPlan?> = _selectedWeeklyPlan

    private val _allPredefinedMeals = MutableStateFlow<List<PredefinedMealEntity>>(emptyList())
    val allPredefinedMeals: StateFlow<List<PredefinedMealEntity>> = _allPredefinedMeals

    private val _latestWeight = MutableStateFlow<Float?>(null)
    val latestWeight: StateFlow<Float?> = _latestWeight

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
                } else {
                    0f
                }

                _selectedWeeklyPlan.value = nutritionRepository.getCurrentWeeklyMealPlan()
                _allPredefinedMeals.value = nutritionRepository.getPredefinedMeals()
                _latestWeight.value = nutritionRepository.getLatestWeight()
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

    fun addPlannedMealSlot(slot: PlannedMealSlot) {
        viewModelScope.launch {
            nutritionRepository.addPlannedMealSlot(slot)
            loadData()
        }
    }

    fun removeTodayMeal(mealEntryId: Long) {
        viewModelScope.launch {
            nutritionRepository.removeMeal(mealEntryId)
            loadData()
        }
    }

    fun refreshData() {
        loadData()
    }

    fun saveWeight(weight: Float) {
        viewModelScope.launch {
            nutritionRepository.saveWeight(weight)
            loadData()
        }
    }

    fun replaceMealInWeeklyPlan(
        dayLabel: String,
        slotTitle: String,
        oldMealId: Long,
        newMeal: PredefinedMealEntity
    ) {
        val currentPlan = _selectedWeeklyPlan.value ?: return

        val updatedDays = currentPlan.days.map dayMap@{ day ->
            if (day.dayLabel != dayLabel) return@dayMap day

            val updatedSlots = day.slots.map slotMap@{ slot ->
                if (slot.title != slotTitle) return@slotMap slot

                val updatedDishes = slot.dishes.map { meal ->
                    if (meal.id == oldMealId) newMeal else meal
                }

                recalculateSlot(slot, updatedDishes)
            }

            day.copy(
                slots = updatedSlots,
                totalCalories = updatedSlots.sumOf { it.totalCalories },
                totalProtein = updatedSlots.sumOf { it.totalProtein },
                totalFat = updatedSlots.sumOf { it.totalFat },
                totalCarbs = updatedSlots.sumOf { it.totalCarbs }
            )
        }

        _selectedWeeklyPlan.value = currentPlan.copy(days = updatedDays)
    }

    private fun recalculateSlot(
        slot: PlannedMealSlot,
        dishes: List<PredefinedMealEntity>
    ): PlannedMealSlot {
        return slot.copy(
            dishes = dishes,
            totalCalories = dishes.sumOf { it.calories },
            totalProtein = dishes.sumOf { it.protein },
            totalFat = dishes.sumOf { it.fat },
            totalCarbs = dishes.sumOf { it.carbs }
        )
    }
}
