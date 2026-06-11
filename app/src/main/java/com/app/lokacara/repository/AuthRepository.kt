package com.app.lokacara.repository

import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.dto.AuthResponse
import com.app.lokacara.data.remote.dto.LoginRequest
import com.app.lokacara.data.remote.dto.MessageResponse
import com.app.lokacara.data.remote.dto.RegisterRequest
import com.app.lokacara.data.remote.safeApiCall
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun login(email: String, password: String): ApiResult<AuthResponse> {
        return safeApiCall {
            apiService.login(LoginRequest(email.trim(), password))
        }
    }

    suspend fun register(name: String, email: String, password: String): ApiResult<AuthResponse> {
        return safeApiCall {
            apiService.register(
                RegisterRequest(name.trim(), email.trim(), password, password)
            )
        }
    }

    suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
        newPasswordConfirmation: String
    ): ApiResult<MessageResponse> {
        return safeApiCall {
            apiService.changePassword(
                mapOf(
                    "old_password" to oldPassword,
                    "new_password" to newPassword,
                    "new_password_confirmation" to newPasswordConfirmation
                )
            )
        }
    }
}
