package com.app.lokacara.data

import com.app.lokacara.model.Event
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CanonicalCityMatcherTest {
    @Test
    fun `canonical Surakarta matches exact city`() {
        assertTrue(eventMatchesCanonicalCity(event(city = "Kota Surakarta"), "Surakarta"))
    }

    @Test
    fun `venue text does not create a city match`() {
        assertFalse(eventMatchesCanonicalCity(
            event(location = "Surakarta Convention Hall", city = "Semarang"),
            "Surakarta"
        ))
    }

    @Test
    fun `unknown administrative city is excluded`() {
        assertFalse(eventMatchesCanonicalCity(event(location = "Surakarta Hall"), "Surakarta"))
    }

    @Test
    fun `administrative address segment is a strict fallback`() {
        assertTrue(eventMatchesCanonicalCity(
            event(address = "Jl. Slamet Riyadi, Kota Surakarta, Jawa Tengah"),
            "Surakarta"
        ))
    }

    private fun event(
        location: String = "Venue",
        address: String? = null,
        city: String? = null
    ) = Event(
        id = 1, title = "Event", description = "", date = "", location = location,
        price = "Gratis", category = "", address = address, city = city
    )
}
