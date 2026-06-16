package com.app.lokacara

import android.app.Application
import com.app.lokacara.notifications.NotificationChannels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LokacaraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureCreated(this)
    }
}
