package com.app.lokacara.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val EVENT_REMINDERS = "event_reminders"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            EVENT_REMINDERS,
            "Notifikasi event",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Pembaruan otomatis untuk event yang diikuti dan dikelola"
        }
        manager.createNotificationChannel(channel)
    }
}
