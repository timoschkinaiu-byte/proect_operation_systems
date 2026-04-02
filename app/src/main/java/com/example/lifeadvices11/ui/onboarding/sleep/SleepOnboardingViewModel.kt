package com.example.lifeadvices11.ui.onboarding.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeadvices11.data.repositories.SleepRepository

import com.example.lifeadvices11.data.entities.SleepProfileEntity
import com.example.lifeadvices11.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.lifeadvices11.data.entities.SleepPracticeEntity

class SleepOnboardingViewModel : ViewModel() {

    private val repository: SleepRepository = AppModule.provideSleepRepository()

    // Поля для онбординга
    private val _targetSleepHours = MutableStateFlow("")
    val targetSleepHours: StateFlow<String> = _targetSleepHours

    private val _bedTime = MutableStateFlow("")
    val bedTime: StateFlow<String> = _bedTime

    private val _wakeTime = MutableStateFlow("")
    val wakeTime: StateFlow<String> = _wakeTime

    private val _preferredWakeTime = MutableStateFlow("")
    val preferredWakeTime: StateFlow<String> = _preferredWakeTime

    private val _sleepQuality = MutableStateFlow("")
    val sleepQuality: StateFlow<String> = _sleepQuality

    private val _sleepIssues = MutableStateFlow("")
    val sleepIssues: StateFlow<String> = _sleepIssues

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    // Update functions
    fun updateTargetSleepHours(value: String) {
        _targetSleepHours.value = value
    }

    fun updateBedTime(value: String) {
        _bedTime.value = value
    }

    fun updateWakeTime(value: String) {
        _wakeTime.value = value
    }

    fun updatePreferredWakeTime(value: String) {
        _preferredWakeTime.value = value
    }

    fun updateSleepQuality(value: String) {
        _sleepQuality.value = value
    }

    fun updateSleepIssues(value: String) {
        _sleepIssues.value = value
    }

    fun saveOnboardingData(onComplete: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true

            repository.saveSleepOnboardingData(
                targetHours = _targetSleepHours.value.toDoubleOrNull() ?: 8.0,
                bedTime = _bedTime.value,
                wakeTime = _wakeTime.value,
                quality = _sleepQuality.value,
                issues = _sleepIssues.value,
                preferredWakeTime = _preferredWakeTime.value
            )
            val profile = SleepProfileEntity(
                id = 1,
                targetSleepHours = _targetSleepHours.value.toDoubleOrNull() ?: 8.0,
                typicalBedTime = _bedTime.value,
                typicalWakeTime = _wakeTime.value,
                sleepQuality = _sleepQuality.value,
                sleepIssues = _sleepIssues.value,
                preferredWakeUpTime = _preferredWakeTime.value,
                hasCompletedSleepOnboarding = true
            )
            val practices = repository.generatePersonalizedPractices(profile)
            repository.savePractices(practices)

            _isSaving.value = false
            onComplete()
        }
    }

    suspend fun hasCompletedOnboarding(): Boolean {
        return repository.hasCompletedSleepOnboarding()
    }
}