package com.app.lokacara.notifications

import com.app.lokacara.data.PushTokenManager
import com.app.lokacara.data.SettingsManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class LokacaraMessagingService : FirebaseMessagingService() {
    @Inject lateinit var settingsManager: SettingsManager
    @Inject lateinit var pushTokenManager: PushTokenManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        val payload = AppNotificationPayloadParser.parse(message.data) ?: return
        val notificationsEnabled = runCatching {
            runBlocking { settingsManager.notificationsEnabled.first() }
        }.getOrDefault(true)

        if (notificationsEnabled) {
            AppNotificationNotifier.show(this, payload)
        }
    }

    override fun onNewToken(token: String) {
        serviceScope.launch {
            pushTokenManager.registerTokenIfAuthenticated(token)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
