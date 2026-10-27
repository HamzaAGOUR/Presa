package com.presa.app

import android.app.Application
import com.presa.app.di.AppContainer

class PresaApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    override fun onTerminate() {
        container.hidManager.close()
        super.onTerminate()
    }
}
