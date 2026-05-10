package com.app.lokacara.repository

import com.app.lokacara.R
import com.app.lokacara.model.HistoryEvent
import com.app.lokacara.model.UpcomingEvent

class TicketsRepository {

    fun getUpcomingEvents(): List<UpcomingEvent> {
        return listOf(
            UpcomingEvent("Workshop Ui/Ux : Future of Ai", "Rabu, 3 Desember", "14.00", "Pura Mangkunegaran", "Workshop", imageRes = R.drawable.seminar_2)
        )
    }

    fun getHistoryEvents(): List<HistoryEvent> {
        return listOf(
            HistoryEvent("Konser Musik di Surakarta", "25 April 2026", "15:00", "Pura Mangkunegaran", "Musik", true, R.drawable.seminar),
            HistoryEvent("Konser Musik di Surakarta", "25 April 2026", "15:00", "Pura Mangkunegaran", "Musik", false, R.drawable.seminar_2),
            HistoryEvent("Konser Musik di Surakarta", "25 April 2026", "15:00", "Pura Mangkunegaran", "Musik", true, R.drawable.seminar_3)
        )
    }
}