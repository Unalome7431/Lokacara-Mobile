package com.app.lokacara.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import java.io.File

class FileStorageManager(private val context: Context) {

    private val postersDir: File
        get() = File(context.filesDir, "posters").also { it.mkdirs() }

    private val profilePicsDir: File
        get() = File(context.filesDir, "profile_pics").also { it.mkdirs() }

    private val certificatesDir: File
        get() = File(context.filesDir, "certificates").also { it.mkdirs() }

    fun saveUriToFile(uri: Uri, subDir: String, fileName: String): String? {
        return try {
            val dir = File(context.filesDir, subDir).also { it.mkdirs() }
            val file = File(dir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun saveEventPoster(uri: Uri, eventId: String): String? {
        return saveUriToFile(uri, "posters", "poster_$eventId.jpg")
    }

    fun getEventPoster(eventId: String): File? {
        val file = File(postersDir, "poster_$eventId.jpg")
        return if (file.exists()) file else null
    }

    fun saveProfilePhoto(uri: Uri): String? {
        return saveUriToFile(uri, "profile_pics", "profile_photo.jpg")
    }

    fun getProfilePhoto(): File? {
        val file = File(profilePicsDir, "profile_photo.jpg")
        return if (file.exists()) file else null
    }

    fun saveCertificate(drawableResId: Int, fileName: String): String? {
        return try {
            val resolver = context.contentResolver
            val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Lokacara")
                }
            }

            val uri = resolver.insert(imageCollection, contentValues)
            
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    // Ambil data mentah dari resource alih-alih konversi ke bitmap agar tidak pecah/blank
                    context.resources.openRawResource(drawableResId).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                it.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveCertificateFromUri(uri: Uri, fileName: String): String? {
        return saveUriToFile(uri, "certificates", fileName)
    }

    fun getCertificate(fileName: String): File? {
        val file = File(certificatesDir, fileName)
        return if (file.exists()) file else null
    }

    fun deleteCertificate(fileName: String): Boolean {
        return File(certificatesDir, fileName).delete()
    }

    fun deleteEventPoster(eventId: String): Boolean {
        return File(postersDir, "poster_$eventId.jpg").delete()
    }

    fun deleteProfilePhoto(): Boolean {
        return File(profilePicsDir, "profile_photo.jpg").delete()
    }
}
