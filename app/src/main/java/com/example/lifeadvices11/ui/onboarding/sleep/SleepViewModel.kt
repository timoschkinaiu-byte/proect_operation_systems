package com.example.lifeadvices11.ui.sections.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeadvices11.data.entities.DailySleepEntity
import com.example.lifeadvices11.data.entities.SleepPracticeEntity
import com.example.lifeadvices11.data.entities.SleepProfileEntity
import com.example.lifeadvices11.data.repositories.SleepRepository
import com.example.lifeadvices11.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SleepViewModel : ViewModel() {

    private val repository: SleepRepository = AppModule.provideSleepRepository()

    private val _sleepProfile = MutableStateFlow<SleepProfileEntity?>(null)
    val sleepProfile: StateFlow<SleepProfileEntity?> = _sleepProfile.asStateFlow()

    private val _todaySleep = MutableStateFlow<DailySleepEntity?>(null)
    val todaySleep: StateFlow<DailySleepEntity?> = _todaySleep.asStateFlow()

    private val _lastWeekSleep = MutableStateFlow<List<DailySleepEntity>>(emptyList())
    val lastWeekSleep: StateFlow<List<DailySleepEntity>> = _lastWeekSleep.asStateFlow()

    private val _practices = MutableStateFlow<List<SleepPracticeEntity>>(emptyList())
    val practices: StateFlow<List<SleepPracticeEntity>> = _practices.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
        loadPractices()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _sleepProfile.value = repository.getSleepProfileSync()
            _todaySleep.value = repository.getTodaySleep()
            _lastWeekSleep.value = repository.getLastWeekSleep()
            _isLoading.value = false
        }
    }

    fun loadPractices() {
        viewModelScope.launch {
            _practices.value = repository.getAllPractices()
        }
    }

    fun addSleepEntry(sleepHours: Float, bedTime: String, wakeTime: String, quality: Int, notes: String) {
        viewModelScope.launch {
            val entry = DailySleepEntity(
                sleepHours = sleepHours,
                bedTime = bedTime,
                wakeTime = wakeTime,
                quality = quality,
                notes = notes
            )
            repository.addSleepEntry(entry)
            loadData()
        }
    }

    fun togglePracticeCompletion(practice: SleepPracticeEntity) {
        viewModelScope.launch {
            val updated = practice.copy(isCompleted = !practice.isCompleted)
            repository.updatePractice(updated)
            loadPractices()
        }
    }

    fun refreshData() {
        loadData()
        loadPractices()
    }
    init {
        loadData()
        initializePractices()
    }

    private fun initializePractices() {
        viewModelScope.launch {
            repository.initializePracticesIfNeeded()
            loadPractices()
        }
    }
    fun regeneratePractices() {
        viewModelScope.launch {
            val profile = _sleepProfile.value
            if (profile != null) {
                repository.generateAndSavePractices(profile)
                loadPractices()
            }
        }
    }
}