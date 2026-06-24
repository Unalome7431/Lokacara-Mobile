package com.app.lokacara.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

data class MediaConstraints(
    val maxBytes: Long = 10_000_000L,
    val maxDimensionPx: Float = 1600f,
    val jpegQuality: Int = 80,
    val outputFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    val outputExtension: String = "jpg",
    val outputMimeType: String = "image/jpeg"
)

data class PreparedMedia(
    val bytes: ByteArray,
    val formDataName: String,
    val fileName: String
)

fun PreparedMedia.toMultipartBodyPart(): MultipartBody.Part {
    return MultipartBody.Part.createFormData(
        formDataName,
        fileName,
        bytes.toRequestBody(outputMimeType().toMediaTypeOrNull())
    )
}

private fun PreparedMedia.outputMimeType(): String {
    return when {
        fileName.endsWith(".png", ignoreCase = true) -> "image/png"
        fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
        else -> "image/jpeg"
    }
}

fun prepareMediaFromUri(
    context: Context,
    uri: Uri,
    formDataName: String,
    constraints: MediaConstraints = MediaConstraints()
): Result<PreparedMedia> {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return Result.failure(IllegalArgumentException("File tidak dapat dibuka"))
        var bytes = inputStream.use { it.readBytes() }

        if (bytes.size > constraints.maxBytes) {
            return Result.failure(IllegalArgumentException("Ukuran file maksimal ${constraints.maxBytes / 1_000_000} MB"))
        }

        if (bytes.size > 300_000) {
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: return Result.failure(IllegalArgumentException("Format gambar tidak didukung"))
            val scale = minOf(
                constraints.maxDimensionPx / bitmap.width,
                constraints.maxDimensionPx / bitmap.height,
                1f
            )
            val scaled = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true
                )
            } else bitmap
            val out = ByteArrayOutputStream()
            scaled.compress(constraints.outputFormat, constraints.jpegQuality, out)
            bytes = out.toByteArray()
            if (scaled !== bitmap) scaled.recycle()
            bitmap.recycle()
        }

        val originalType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val extension = inferExtension(originalType)
        val fileName = "${formDataName}_${System.currentTimeMillis()}.$extension"

        Result.success(
            PreparedMedia(
                bytes = bytes,
                formDataName = formDataName,
                fileName = fileName
            )
        )
    } catch (e: IllegalArgumentException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(IllegalArgumentException("Gagal memproses gambar: ${e.message}"))
    }
}

fun isRemoteUri(uri: Uri): Boolean {
    val scheme = uri.scheme
    return scheme == "http" || scheme == "https"
}

private fun inferExtension(mimeType: String): String {
    return when {
        mimeType.contains("png", ignoreCase = true) -> "png"
        mimeType.contains("webp", ignoreCase = true) -> "webp"
        else -> "jpg"
    }
}

fun validateFileSize(context: Context, uri: Uri, maxBytes: Long = 10_000_000L): Result<Unit> {
    return try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        val size = cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (sizeIndex >= 0) it.getLong(sizeIndex) else -1L
            } else -1L
        } ?: -1L
        if (size > maxBytes) {
            Result.failure(IllegalArgumentException("Ukuran file maksimal ${maxBytes / 1_000_000} MB"))
        } else {
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(IllegalArgumentException("Gagal membaca informasi file"))
    }
}
