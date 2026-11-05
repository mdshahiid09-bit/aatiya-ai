package com.aatiya.voiceassistant

import android.app.Application
import com.aatiya.voiceassistant.di.AppContainer

class AatiyaApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
