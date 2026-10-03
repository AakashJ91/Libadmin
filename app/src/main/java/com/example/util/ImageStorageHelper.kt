package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageStorageHelper {
    /**
     * Safely copies an image from an incoming Uri (e.g. from Android Photo Picker)
     * into the app's private files directory and returns the absolute file path.
     */
    fun savePhotoFromUri(context: Context, sourceUri: Uri): String? {
        return try {
            val photosDir = File(context.filesDir, "student_photos").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "student_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg"
            val destFile = File(photosDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Safely saves a captured camera Bitmap into private storage.
     */
    fun saveBitmap(context: Context, bitmap: android.graphics.Bitmap): String? {
        return try {
            val photosDir = File(context.filesDir, "student_photos").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "student_cam_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg"
            val destFile = File(photosDir, fileName)
            FileOutputStream(destFile).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, out)
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
