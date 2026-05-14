package com.app.lokacara.repository

import com.app.lokacara.model.UserProfile

class AuthRepository {

    fun login(email: String, password: String): Result<UserProfile> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                Result.failure(Exception("Email dan password harus diisi"))
            } else {
                Result.success(
                    UserProfile(
                        name = "Daffa Arrivo",
                        email = email,
                        phone = "+628788133233145",
                        location = "Surakarta, Jawa Tengah"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun register(email: String, password: String): Result<UserProfile> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                Result.failure(Exception("Email dan password harus diisi"))
            } else if (password.length < 6) {
                Result.failure(Exception("Kata sandi minimal 6 karakter"))
            } else {
                Result.success(
                    UserProfile(
                        name = "Pengguna Baru",
                        email = email,
                        phone = "",
                        location = "Indonesia"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
