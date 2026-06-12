package com.app.lokacara.data.remote

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageUrlProvider @Inject constructor() {

    companion object {
        const val BASE = "https://lokacara.my.id"
    }

    fun posterUrl(posterPath: String?): String? {
        if (posterPath.isNullOrBlank()) return "$BASE/images/default_cover.jpg"
        if (posterPath.startsWith("http")) return posterPath
        val filename = posterPath.substringAfterLast("/")
        return "$BASE/api/posters/$filename"
    }

    fun avatarUrl(avatarUrl: String?): String? {
        if (avatarUrl.isNullOrBlank()) return "$BASE/images/default_avatar.png"
        if (avatarUrl.startsWith("http")) return avatarUrl
        val filename = avatarUrl.substringAfterLast("/")
        return "$BASE/api/avatars/$filename"
    }

    fun certificateUrl(fileUrl: String?): String? {
        if (fileUrl.isNullOrBlank()) return "$BASE/images/default_certificate.png"
        if (fileUrl.startsWith("http")) return fileUrl
        return "$BASE/$fileUrl"
    }
}
