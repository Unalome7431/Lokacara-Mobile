package com.app.lokacara.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.repository.PushTokenRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

private val Context.pushTokenDataStore by preferencesDataStore(name = "push_token")

@Singleton
class PushTokenManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val userSessionManager: UserSessionManager,
    private val repository: PushTokenRepository
) {
    private object Keys {
        val LAST_SYNCED_TOKEN = stringPreferencesKey("last_synced_token")
    }

    suspend fun syncCurrentTokenIfAuthenticated() {
        val token = fetchCurrentToken().orEmpty()
        if (token.isBlank()) return
        registerTokenIfAuthenticated(token)
    }

    suspend fun registerTokenIfAuthenticated(token: String) {
        if (token.isBlank()) return
        val session = userSessionManager.userSession.first()
        if (!session.isLoggedIn || session.accessToken.isBlank()) return

        val lastSynced = context.pushTokenDataStore.data.first()[Keys.LAST_SYNCED_TOKEN]
        if (lastSynced == token) return

        when (repository.register(token)) {
            is ApiResult.Success -> saveLastSyncedToken(token)
            is ApiResult.Error -> Unit
        }
    }

    suspend fun unregisterLastSyncedToken() {
        val token = context.pushTokenDataStore.data.first()[Keys.LAST_SYNCED_TOKEN].orEmpty()
        if (token.isBlank()) return

        repository.unregister(token)
        context.pushTokenDataStore.edit { prefs ->
            prefs.remove(Keys.LAST_SYNCED_TOKEN)
        }
    }

    private suspend fun saveLastSyncedToken(token: String) {
        context.pushTokenDataStore.edit { prefs ->
            prefs[Keys.LAST_SYNCED_TOKEN] = token
        }
    }

    private suspend fun fetchCurrentToken(): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!continuation.isActive) return@addOnCompleteListener
                    continuation.resume(if (task.isSuccessful) task.result else null)
                }
            } catch (_: Exception) {
                if (continuation.isActive) continuation.resume(null)
            }
        }
    }
}
