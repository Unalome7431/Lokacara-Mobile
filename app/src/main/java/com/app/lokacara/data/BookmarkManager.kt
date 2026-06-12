package com.app.lokacara.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val Context.bookmarkDataStore by preferencesDataStore(name = "bookmarks")

class BookmarkManager(private val context: Context) {
    companion object {
        val BOOKMARKED_IDS = stringSetPreferencesKey("bookmarked_ids")
    }

    private val toggleLock = Mutex()

    val bookmarkedIds: Flow<Set<String>> = context.bookmarkDataStore.data.map { prefs ->
        prefs[BOOKMARKED_IDS] ?: emptySet()
    }

    suspend fun toggleBookmark(eventId: String) {
        toggleLock.withLock {
            context.bookmarkDataStore.edit { prefs ->
                val current = prefs[BOOKMARKED_IDS] ?: emptySet()
                prefs[BOOKMARKED_IDS] = if (eventId in current) {
                    current - eventId
                } else {
                    current + eventId
                }
            }
        }
    }

    suspend fun addBookmark(eventId: String) {
        context.bookmarkDataStore.edit { prefs ->
            val current = prefs[BOOKMARKED_IDS] ?: emptySet()
            prefs[BOOKMARKED_IDS] = current + eventId
        }
    }

    suspend fun removeBookmark(eventId: String) {
        context.bookmarkDataStore.edit { prefs ->
            val current = prefs[BOOKMARKED_IDS] ?: emptySet()
            prefs[BOOKMARKED_IDS] = current - eventId
        }
    }
}
