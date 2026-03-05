package com.example.lifeadvices11.ui.onboarding.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeadvices11.data.repositories.UserRepository
import com.example.lifeadvices11.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NutritionOnboardingViewModel : ViewModel() {

    private val repository: UserRepository = AppModule.provideUserRepository()

    private val _height = MutableStateFlow("")
    val height: StateFlow<String> = _height

    private val _weight = MutableStateFlow("")
    val weight: StateFlow<String> = _weight

    private val _age = MutableStateFlow("")
    val age: StateFlow<String> = _age

    private val _gender = MutableStateFlow("")
    val gender: StateFlow<String> = _gender

    private val _activityLevel = MutableStateFlow("")
    val activityLevel: StateFlow<String> = _activityLevel

    private val _goal = MutableStateFlow("")
    val goal: StateFlow<String> = _goal

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    fun updateHeight(value: String) {
        _height.value = value
    }

    fun updateWeight(value: String) {
        _weight.value = value
    }

    fun updateAge(value: String) {
        _age.value = value
    }

    fun updateGender(value: String) {
        _gender.value = value
    }

    fun updateActivityLevel(value: String) {
        _activityLevel.value = value
    }

    fun updateGoal(value: String) {
        _goal.value = value
    }

    fun saveOnboardingData(onComplete: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true

            repository.saveNutritionOnboardingData(
                height = _height.value.toIntOrNull() ?: 0,
                weight = _weight.value.toFloatOrNull() ?: 0f,
                age = _age.value.toIntOrNull() ?: 0,
                gender = _gender.value,
                activityLevel = _activityLevel.value,
                goal = _goal.value
            )

            _isSaving.value = false
            onComplete()
        }
    }

    suspend fun hasCompletedOnboarding(): Boolean {
        return repository.hasCompletedNutritionOnboarding()
    }
}