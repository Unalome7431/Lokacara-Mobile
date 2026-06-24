package com.app.lokacara.data.result

import com.app.lokacara.data.remote.ApiResult

object AppErrorMessages {
    const val NETWORK = "Gagal terhubung ke server. Periksa koneksi internet Anda."
    const val SERVER_400 = "Permintaan tidak valid"
    const val SERVER_401 = "Sesi berakhir, silakan login kembali"
    const val SERVER_403 = "Anda tidak memiliki akses"
    const val SERVER_404 = "Data tidak ditemukan"
    const val SERVER_413 = "Ukuran file terlalu besar"
    const val SERVER_422 = "Data yang dikirim tidak valid"
    const val SERVER_429 = "Terlalu banyak permintaan, coba lagi nanti"
    const val SERVER_500 = "Terjadi kesalahan pada server"
    const val SERVER_UNKNOWN = "Terjadi kesalahan"
}

fun httpCodeToMessage(code: Int): String {
    return when (code) {
        400 -> AppErrorMessages.SERVER_400
        401 -> AppErrorMessages.SERVER_401
        403 -> AppErrorMessages.SERVER_403
        404 -> AppErrorMessages.SERVER_404
        413 -> AppErrorMessages.SERVER_413
        422 -> AppErrorMessages.SERVER_422
        429 -> AppErrorMessages.SERVER_429
        in 500..599 -> AppErrorMessages.SERVER_500
        else -> AppErrorMessages.SERVER_UNKNOWN
    }
}

fun ApiResult.Error.toUserMessage(default: String = AppErrorMessages.SERVER_UNKNOWN): String {
    return message.takeIf { it.isNotBlank() } ?: code?.let { httpCodeToMessage(it) } ?: default
}

fun ApiResult.Error.toMessageWithFallback(fallback: String): String {
    val codeMessage = code?.let { httpCodeToMessage(it) }
    return message.ifBlank { codeMessage ?: fallback }
}
