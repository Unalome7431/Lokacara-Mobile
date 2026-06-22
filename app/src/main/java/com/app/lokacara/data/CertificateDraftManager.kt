package com.app.lokacara.data

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private val Context.certificateDraftDataStore by preferencesDataStore(name = "certificate_drafts")

data class CertificateDraft(
    val templateFilePath: String? = null,
    val fontFamily: String = "Roboto",
    val fontSize: String = "Medium",
    val fontColor: String = "#000000",
    val xPosition: Float = 50f,
    val isXCentered: Boolean = true,
    val yPosition: Float = 50f,
    val isYCentered: Boolean = true,
    val distributionStatus: String? = null,
    val lastDistributedAt: Long? = null
)

class CertificateDraftManager(private val context: Context) {
    suspend fun load(userId: Long, eventId: Long): CertificateDraft? {
        val prefix = certificateDraftKey(userId, eventId)
        val prefs = context.certificateDraftDataStore.data.first()
        val fontFamily = prefs[stringPreferencesKey("${prefix}_font_family")] ?: return null
        return CertificateDraft(
            templateFilePath = prefs[stringPreferencesKey("${prefix}_template_file")],
            fontFamily = fontFamily,
            fontSize = prefs[stringPreferencesKey("${prefix}_font_size")] ?: "Medium",
            fontColor = prefs[stringPreferencesKey("${prefix}_font_color")] ?: "#000000",
            xPosition = prefs[floatPreferencesKey("${prefix}_x_position")] ?: 50f,
            isXCentered = prefs[booleanPreferencesKey("${prefix}_x_centered")] ?: true,
            yPosition = prefs[floatPreferencesKey("${prefix}_y_position")] ?: 50f,
            isYCentered = prefs[booleanPreferencesKey("${prefix}_y_centered")] ?: true,
            distributionStatus = prefs[stringPreferencesKey("${prefix}_distribution_status")],
            lastDistributedAt = prefs[longPreferencesKey("${prefix}_last_distributed_at")]
        )
    }

    suspend fun save(userId: Long, eventId: Long, draft: CertificateDraft) {
        val prefix = certificateDraftKey(userId, eventId)
        context.certificateDraftDataStore.edit { prefs ->
            fun string(name: String) = stringPreferencesKey("${prefix}_$name")
            draft.templateFilePath?.let { prefs[string("template_file")] = it } ?: prefs.remove(string("template_file"))
            prefs[string("font_family")] = draft.fontFamily
            prefs[string("font_size")] = draft.fontSize
            prefs[string("font_color")] = draft.fontColor
            prefs[floatPreferencesKey("${prefix}_x_position")] = draft.xPosition
            prefs[booleanPreferencesKey("${prefix}_x_centered")] = draft.isXCentered
            prefs[floatPreferencesKey("${prefix}_y_position")] = draft.yPosition
            prefs[booleanPreferencesKey("${prefix}_y_centered")] = draft.isYCentered
            draft.distributionStatus?.let { prefs[string("distribution_status")] = it }
                ?: prefs.remove(string("distribution_status"))
            draft.lastDistributedAt?.let { prefs[longPreferencesKey("${prefix}_last_distributed_at")] = it }
                ?: prefs.remove(longPreferencesKey("${prefix}_last_distributed_at"))
        }
    }

    suspend fun copyTemplate(userId: Long, eventId: Long, uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val directory = File(context.filesDir, "certificate_templates").also(File::mkdirs)
            val target = File(directory, "${certificateDraftKey(userId, eventId)}.img")
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use(input::copyTo)
            } ?: return@runCatching null
            target.absolutePath
        }.getOrNull()
    }
}

fun certificateDraftKey(userId: Long, eventId: Long): String = "u${userId}_e${eventId}"
