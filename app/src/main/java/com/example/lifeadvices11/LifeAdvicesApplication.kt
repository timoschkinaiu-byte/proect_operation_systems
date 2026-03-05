package com.example.lifeadvices11

import android.app.Application
import com.example.lifeadvices11.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LifeAdvicesApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        AppModule.init(this)

        CoroutineScope(Dispatchers.IO).launch {
            val repository = AppModule.provideUserRepository()
            repository.createProfileIfNotExists()
        }
    }
}