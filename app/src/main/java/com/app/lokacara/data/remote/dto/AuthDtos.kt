package com.app.lokacara.data.remote.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val password_confirmation: String
)

data class AuthResponse(
    val message: String,
    val user: UserDto? = null,
    val token: String? = null
)

data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val role: String = "user",
    val phone: String? = null,
    val location: String? = null,
    val avatar_url: String? = null,
    val suspended_at: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

data class MessageResponse(
    val message: String
)

data class ProfileResponse(
    val user: UserDto? = null
)

data class RefreshTokenResponse(
    val token: String
)

data class GoogleLoginRequest(
    val token: String
)
