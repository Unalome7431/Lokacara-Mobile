package com.app.lokacara.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageUrlProviderTest {

    private val provider = ImageUrlProvider()

    @Test
    fun avatarUrl_relativePath_usesProfileAvatarEndpoint() {
        val result = provider.avatarUrl("avatars/user_12.jpg")

        assertEquals("https://lokacara.my.id/api/profile/avatar/user_12.jpg", result)
    }

    @Test
    fun avatarUrl_httpsUrl_isPreserved() {
        val result = provider.avatarUrl("https://cdn.example.com/avatar.jpg")

        assertEquals("https://cdn.example.com/avatar.jpg", result)
    }

    @Test
    fun avatarUrl_httpUrl_isUpgradedToHttps() {
        val result = provider.avatarUrl("http://cdn.example.com/avatar.jpg")

        assertEquals("https://cdn.example.com/avatar.jpg", result)
    }
}
