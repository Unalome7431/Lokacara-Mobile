package com.app.lokacara.data

import com.app.lokacara.model.Event
import java.util.Locale

fun normalizeCanonicalCity(value: String): String = value.trim()
    .replace(Regex("^(kota|kabupaten)\\s+", RegexOption.IGNORE_CASE), "")
    .lowercase(Locale.ROOT)

fun eventMatchesCanonicalCity(event: Event, selectedCity: String): Boolean {
    if (selectedCity.isBlank()) return true
    val target = normalizeCanonicalCity(selectedCity)
    event.city?.takeIf(String::isNotBlank)?.let { return normalizeCanonicalCity(it) == target }
    return event.address.orEmpty().split(',').any { segment ->
        val trimmed = segment.trim()
        val administrative = trimmed.startsWith("Kota ", true) || trimmed.startsWith("Kabupaten ", true)
        (administrative || trimmed.split(' ').size <= 2) && normalizeCanonicalCity(trimmed) == target
    }
}
