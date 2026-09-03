package com.kcalulo.vale

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ValeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ValeCrashHandler.install(this)
    }
}
