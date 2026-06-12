package com.app.lokacara.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

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
    val userRole: String = ""
)

class UserSessionManager(private val context: Context) {
    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val NAME = stringPreferencesKey("name")
        val EMAIL = stringPreferencesKey("email")
        val PHONE = stringPreferencesKey("phone")
        val LOCATION = stringPreferencesKey("location")
        val PROFILE_IMAGE_PATH = stringPreferencesKey("profile_image_path")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val USER_ID = longPreferencesKey("user_id")
        val USER_ROLE = stringPreferencesKey("user_role")
    }

    val userSession: Flow<UserSession> = context.userDataStore.data.map { prefs ->
        UserSession(
            isLoggedIn = prefs[IS_LOGGED_IN] ?: false,
            name = prefs[NAME] ?: "",
            email = prefs[EMAIL] ?: "",
            phone = prefs[PHONE] ?: "",
            location = prefs[LOCATION] ?: "",
            profileImagePath = prefs[PROFILE_IMAGE_PATH] ?: "",
            accessToken = prefs[ACCESS_TOKEN] ?: "",
            userId = prefs[USER_ID] ?: 0L,
            userRole = prefs[USER_ROLE] ?: ""
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

    suspend fun saveAuth(token: String, userId: Long, name: String, email: String, role: String) {
        context.userDataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = true
            prefs[ACCESS_TOKEN] = token
            prefs[USER_ID] = userId
            prefs[NAME] = name
            prefs[EMAIL] = email
            prefs[USER_ROLE] = role
        }
    }

    suspend fun getAccessToken(): String {
        return context.userDataStore.data.first()[ACCESS_TOKEN] ?: ""
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
}
