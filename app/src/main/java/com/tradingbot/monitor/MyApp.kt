package com.tradingbot.monitor

import android.app.Application

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Punto de inicialización global: DataStore, WorkManager, crash reporting, etc.
    }
}
