package com.app.lokacara.data

import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.model.Event
import com.app.lokacara.ui.components.SnackbarManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class BookmarkSyncHelper @Inject constructor(
    private val bookmarkManager: BookmarkManager,
    private val apiService: ApiService,
    private val analytics: AnalyticsTracker
) {
    private var bookmarkJob: Job? = null

    fun syncBookmarks(
        scope: CoroutineScope,
        vararg states: MutableStateFlow<List<Event>>
    ) {
        bookmarkJob?.cancel()
        bookmarkJob = scope.launch {
            bookmarkManager.bookmarkedIds.collect { bookmarkedIds ->
                states.forEach { state ->
                    state.value = state.value.map { event ->
                        val bookmarked = event.id.toString() in bookmarkedIds
                        if (event.isBookmarked != bookmarked) event.copy(isBookmarked = bookmarked) else event
                    }
                }
            }
        }
    }

    fun syncBookmark(
        scope: CoroutineScope,
        state: MutableStateFlow<Event>
    ) {
        bookmarkJob?.cancel()
        bookmarkJob = scope.launch {
            bookmarkManager.bookmarkedIds.collect { bookmarkedIds ->
                val event = state.value
                val bookmarked = event.id.toString() in bookmarkedIds
                if (event.id != 0L && event.isBookmarked != bookmarked) {
                    state.value = event.copy(isBookmarked = bookmarked)
                }
            }
        }
    }

    fun toggleBookmark(
        scope: CoroutineScope,
        eventId: String
    ) {
        scope.launch {
            val bookmarkedIds = bookmarkManager.bookmarkedIds.first()
            val wasBookmarked = bookmarkedIds.contains(eventId)
            bookmarkManager.toggleBookmark(eventId)
            val idLong = eventId.toLongOrNull()
            if (idLong != null) {
                try {
                    if (wasBookmarked) apiService.removeBookmark(idLong)
                    else apiService.addBookmark(idLong)
                } catch (_: Exception) {
                    bookmarkManager.toggleBookmark(eventId)
                    SnackbarManager.showError("Gagal menyimpan bookmark")
                    return@launch
                }
            }
            if (wasBookmarked) {
                SnackbarManager.show("Event dihapus dari bookmark")
                analytics.logEvent("bookmark_removed", mapOf("event_id" to eventId))
            } else {
                SnackbarManager.show("Event disimpan")
                analytics.logEvent("bookmark_added", mapOf("event_id" to eventId))
            }
        }
    }

    fun cancel() {
        bookmarkJob?.cancel()
    }
}
