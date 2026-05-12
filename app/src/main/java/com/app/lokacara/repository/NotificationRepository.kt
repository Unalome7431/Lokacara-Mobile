package com.app.lokacara.repository

import com.app.lokacara.model.NotificationItem
import com.app.lokacara.model.NotificationType

class NotificationRepository {
    fun getNotifications(): List<NotificationItem> {
        return listOf(
            NotificationItem("1", "Velengio", "mengikuti Event mu", "Jumat, 15:00", "Hari ini", NotificationType.SOCIAL, false),
            NotificationItem("2", "Velengio", "menyimpan Event mu", "Jumat, 17:00", "Hari ini", NotificationType.SOCIAL, false),
            NotificationItem("3", "Daffa", "mengikuti Event mu", "Kamis", "Minggu Ini", NotificationType.SOCIAL, true),

            NotificationItem("4", "", "Pendaftaran Berhasil! Tiket Workshop AI kamu sudah terbit.", "Jumat, 10:00", "Hari ini", NotificationType.SYSTEM, false),
            NotificationItem("5", "", "H-1 Event: Siapkan dirimu! Fullstack Workshop dimulai besok pagi.", "Rabu", "Minggu Ini", NotificationType.SYSTEM, true)
        )
    }
}