package com.app.lokacara.data.remote

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageUrlProvider @Inject constructor() {

    companion object {
        private const val BASE = "https://lokacara.my.id"
    }

    fun posterUrl(posterPath: String?): String? {
        if (posterPath == null) return null
        val filename = posterPath.substringAfterLast("/")
        return "$BASE/api/posters/$filename"
    }

    fun avatarUrl(avatarUrl: String?): String? {
        if (avatarUrl == null) return null
        if (avatarUrl.startsWith("http")) return avatarUrl
        val filename = avatarUrl.substringAfterLast("/")
        return "$BASE/api/profile/avatar/$filename"
    }
}
