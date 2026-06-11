package com.app.lokacara.data.remote

import com.squareup.moshi.Moshi
import retrofit2.HttpException
import java.io.IOException

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
}

suspend fun <T> safeApiCall(call: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(call())
    } catch (e: HttpException) {
        val code = e.code()
        val errorBody = e.response()?.errorBody()?.string()
        val message = if (errorBody != null) {
            try {
                val map = Moshi.Builder().build()
                    .adapter(Map::class.java)
                    .fromJson(errorBody)
                val msg = map?.get("message")?.toString()
                if (!msg.isNullOrBlank()) msg else "Error $code"
            } catch (_: Exception) {
                "Error $code"
            }
        } else {
            "Error $code"
        }
        ApiResult.Error(message, code)
    } catch (e: IOException) {
        ApiResult.Error("Gagal terhubung ke server. Periksa koneksi internet Anda.")
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Terjadi kesalahan")
    }
}
