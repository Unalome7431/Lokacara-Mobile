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
        if (posterPath.startsWith("https://")) return posterPath
        if (posterPath.startsWith("http://")) return posterPath.replaceFirst("http://", "https://")
        val filename = posterPath.substringAfterLast("/")
        return "$BASE/api/posters/$filename"
    }

    fun avatarUrl(avatarUrl: String?): String? {
        if (avatarUrl.isNullOrBlank()) return "$BASE/images/default_avatar.png"
        if (avatarUrl.startsWith("https://")) return avatarUrl
        if (avatarUrl.startsWith("http://")) return avatarUrl.replaceFirst("http://", "https://")
        val filename = avatarUrl.substringAfterLast("/")
        return "$BASE/api/profile/avatar/$filename"
    }

    fun certificateUrl(fileUrl: String?): String? {
        if (fileUrl.isNullOrBlank()) return "$BASE/images/default_certificate.png"
        if (fileUrl.startsWith("https://")) return fileUrl
        if (fileUrl.startsWith("http://")) return fileUrl.replaceFirst("http://", "https://")
        return "$BASE/$fileUrl"
    }
}
