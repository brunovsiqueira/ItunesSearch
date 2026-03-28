package com.bruno.itunessearch

import android.app.Application
import com.bruno.itunessearch.di.AppContainer

class App : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
