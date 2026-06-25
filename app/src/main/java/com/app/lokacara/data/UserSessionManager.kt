package com.app.lokacara.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64

private val Context.userDataStore by preferencesDataStore(name = "user_session")

data class UserSession(
    val isLoggedIn: Boolean = false,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val profileImagePath: String = "",
    val accessToken: String = "",
    val userId: Long = 0L,
    val userRole: String = "",
    val authProvider: String = "email",
    val hasLocalPassword: Boolean = true
)

class UserSessionManager(private val context: Context) {
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TOKEN_KEY_ALIAS = "lokacara_session_token"
        private const val TOKEN_PREFIX = "v1:"

        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val NAME = stringPreferencesKey("name")
        val EMAIL = stringPreferencesKey("email")
        val PHONE = stringPreferencesKey("phone")
        val LOCATION = stringPreferencesKey("location")
        val PROFILE_IMAGE_PATH = stringPreferencesKey("profile_image_path")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val USER_ID = longPreferencesKey("user_id")
        val USER_ROLE = stringPreferencesKey("user_role")
        val AUTH_PROVIDER = stringPreferencesKey("auth_provider")
        val HAS_LOCAL_PASSWORD = booleanPreferencesKey("has_local_password")
    }

    val userSession: Flow<UserSession> = context.userDataStore.data.map { prefs ->
        UserSession(
            isLoggedIn = prefs[IS_LOGGED_IN] ?: false,
            name = prefs[NAME] ?: "",
            email = prefs[EMAIL] ?: "",
            phone = prefs[PHONE] ?: "",
            location = prefs[LOCATION] ?: "",
            profileImagePath = prefs[PROFILE_IMAGE_PATH] ?: "",
            accessToken = decryptToken(prefs[ACCESS_TOKEN] ?: ""),
            userId = prefs[USER_ID] ?: 0L,
            userRole = prefs[USER_ROLE] ?: "",
            authProvider = prefs[AUTH_PROVIDER] ?: "email",
            hasLocalPassword = prefs[HAS_LOCAL_PASSWORD] ?: true
        )
    }

    suspend fun saveUserSession(name: String, email: String, phone: String, location: String) {
        context.userDataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = true
            prefs[NAME] = name
            prefs[EMAIL] = email
            prefs[PHONE] = phone
            prefs[LOCATION] = location
        }
    }

    suspend fun saveAuth(
        token: String,
        userId: Long,
        name: String,
        email: String,
        role: String,
        provider: String = "email",
        hasLocalPassword: Boolean = provider != "google"
    ) {
        context.userDataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = true
            prefs[ACCESS_TOKEN] = encryptToken(token)
            prefs[USER_ID] = userId
            prefs[NAME] = name
            prefs[EMAIL] = email
            prefs[USER_ROLE] = role
            prefs[AUTH_PROVIDER] = provider
            prefs[HAS_LOCAL_PASSWORD] = hasLocalPassword
        }
    }

    suspend fun updateAuthState(provider: String? = null, hasLocalPassword: Boolean? = null) {
        context.userDataStore.edit { prefs ->
            provider?.let { prefs[AUTH_PROVIDER] = it }
            hasLocalPassword?.let { prefs[HAS_LOCAL_PASSWORD] = it }
        }
    }

    suspend fun getAccessToken(): String {
        return decryptToken(context.userDataStore.data.first()[ACCESS_TOKEN] ?: "")
    }

    enum class Field { NAME, EMAIL, PHONE, LOCATION }

    suspend fun updateField(field: Field, value: String) {
        context.userDataStore.edit { prefs ->
            when (field) {
                Field.NAME -> prefs[NAME] = value
                Field.EMAIL -> prefs[EMAIL] = value
                Field.PHONE -> prefs[PHONE] = value
                Field.LOCATION -> prefs[LOCATION] = value
            }
        }
    }

    suspend fun updateProfileImagePath(path: String) {
        context.userDataStore.edit { prefs ->
            prefs[PROFILE_IMAGE_PATH] = path
        }
    }

    suspend fun getProfileImagePath(): String {
        return context.userDataStore.data.first()[PROFILE_IMAGE_PATH] ?: ""
    }

    suspend fun logout() {
        context.userDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    private fun encryptToken(token: String): String {
        if (token.isBlank()) return ""
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
            val encrypted = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
            val payload = cipher.iv + encrypted
            TOKEN_PREFIX + Base64.encodeToString(payload, Base64.NO_WRAP)
        } catch (_: Exception) {
            ""
        }
    }

    private fun decryptToken(storedToken: String): String {
        if (storedToken.isBlank()) return ""
        if (!storedToken.startsWith(TOKEN_PREFIX)) return storedToken
        return try {
            val payload = Base64.decode(storedToken.removePrefix(TOKEN_PREFIX), Base64.NO_WRAP)
            if (payload.size <= 12) return ""
            val iv = payload.copyOfRange(0, 12)
            val encrypted = payload.copyOfRange(12, payload.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (_: Exception) {
            ""
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(TOKEN_KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keySpec = KeyGenParameterSpec.Builder(
            TOKEN_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }
}
