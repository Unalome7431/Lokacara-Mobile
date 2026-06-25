package com.app.lokacara.data.validation

object Validators {

    private val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()

    fun isValidEmail(value: String): Boolean {
        return emailRegex.matches(value.trim())
    }

    fun validateRequired(value: String, fieldLabel: String): String? {
        return if (value.isBlank()) "$fieldLabel harus diisi" else null
    }

    fun validateEmail(value: String): String? {
        val trimmed = value.trim()
        return when {
            trimmed.isBlank() -> "Email harus diisi"
            !isValidEmail(trimmed) -> "Format email tidak valid"
            else -> null
        }
    }

    fun validateName(value: String): String? {
        return if (value.trim().isBlank()) "Nama lengkap harus diisi" else null
    }

    fun validatePassword(value: String, minLength: Int = 6): String? {
        return if (value.length < minLength) "Kata sandi minimal $minLength karakter" else null
    }

    fun validatePasswordConfirmation(password: String, confirmation: String): String? {
        return when {
            confirmation.isBlank() -> "Konfirmasi kata sandi harus diisi"
            password != confirmation -> "Konfirmasi kata sandi tidak sama"
            else -> null
        }
    }

    fun validateTextLength(value: String, maxLength: Int, fieldLabel: String): String? {
        return if (value.length > maxLength) "$fieldLabel maksimal $maxLength karakter" else null
    }

    fun validateCapacity(value: Int, min: Int = 1, max: Int = 100_000): String? {
        return if (value < min || value > max) "Kuota peserta harus di antara $min sampai $max" else null
    }

    fun validatePrice(value: Int, min: Int = 0): String? {
        return if (value < min) "Harga event tidak valid" else null
    }

    fun validateSchedule(startStr: String, endStr: String): String? {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
            val start = sdf.parse(startStr)?.time ?: return "Format tanggal mulai tidak valid"
            val end = sdf.parse(endStr)?.time ?: return "Format tanggal selesai tidak valid"
            if (end <= start) "Waktu selesai harus setelah waktu mulai" else null
        } catch (_: Exception) {
            "Format tanggal tidak valid"
        }
    }

    fun validateFileSize(bytes: Long, maxBytes: Long = 10_000_000L): String? {
        return if (bytes > maxBytes) "Ukuran file maksimal ${maxBytes / 1_000_000} MB" else null
    }

    fun validateLocation(latitude: String, longitude: String): String? {
        val lat = latitude.trim()
        val lng = longitude.trim()
        return when {
            lat.isBlank() && lng.isBlank() -> "Pilih lokasi dari peta atau gunakan lokasi saat ini"
            lat.isBlank() || lng.isBlank() -> "Latitude dan longitude harus diisi keduanya"
            else -> null
        }
    }

    fun validateOnlineEventDetails(platform: String, link: String): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (platform.isBlank()) errors["platform"] = "Aplikasi/platform harus diisi"
        if (link.isBlank()) errors["link"] = "Link event harus diisi"
        return errors
    }
}

fun String.isSyntheticEmail(): Boolean {
    return trim().endsWith("@placeholder.local", ignoreCase = true)
}

fun String.isValidEmail(): Boolean {
    return Validators.isValidEmail(this)
}

fun String.isDisplayableEmail(): Boolean {
    val value = trim()
    return value.isNotBlank() && !value.isSyntheticEmail()
}

fun String.toDisplayEmail(): String {
    return if (isDisplayableEmail()) trim() else ""
}
