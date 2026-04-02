package com.example.lifeadvices11.ui.sections.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeadvices11.data.entities.SleepProfileEntity
import com.example.lifeadvices11.data.repositories.SleepRepository
import com.example.lifeadvices11.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SleepViewModel : ViewModel() {

    private val repository: SleepRepository = AppModule.provideSleepRepository()

    private val _sleepProfile = MutableStateFlow<SleepProfileEntity?>(null)
    val sleepProfile: StateFlow<SleepProfileEntity?> = _sleepProfile

    private val _sleepGoal = MutableStateFlow(8.0)
    val sleepGoal: StateFlow<Double> = _sleepGoal

    init {
        loadSleepProfile()
    }

    private fun loadSleepProfile() {
        viewModelScope.launch {
            val profile = repository.getSleepProfileSync()
            _sleepProfile.value = profile
            _sleepGoal.value = profile?.targetSleepHours ?: 8.0
        }
    }

    fun refreshData() {
        loadSleepProfile()
    }
}