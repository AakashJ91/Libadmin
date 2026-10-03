package com.example.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.Student
import com.example.data.repository.StudentRepository
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class ImportResult(
    val success: Boolean,
    val importedCount: Int = 0,
    val photosCount: Int = 0,
    val message: String
)

object DataBackupHelper {

    private const val TAG = "DataBackupHelper"

    /**
     * Exports all user data into a single ZIP archive containing:
     * 1. students_database.csv (Full database exported as Excel-compatible CSV)
     * 2. photos/ folder (All student profile pictures)
     */
    fun exportAllUserDataToZip(context: Context, students: List<Student>): Pair<File?, Boolean> {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val backupDir = File(context.cacheDir, "backups").apply {
            if (!exists()) mkdirs()
        }
        val zipFile = File(backupDir, "LibAdmin_FullBackup_$timestamp.zip")

        return try {
            FileOutputStream(zipFile).use { fos ->
                ZipOutputStream(fos).use { zos ->

                    // 1. Add students_database.csv
                    val csvEntry = ZipEntry("students_database.csv")
                    zos.putNextEntry(csvEntry)

                    // Write UTF-8 BOM so Excel opens with proper accents and formatting
                    zos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

                    val header = listOf(
                        "id", "name", "phone", "email", "idProofNumber",
                        "address", "emergencyContact", "shift", "planType",
                        "startDateMillis", "endDateMillis", "feeAmount", "feePaid",
                        "avatarKey", "photoFileName", "notes", "createdAt"
                    ).joinToString(separator = ",") { "\"$it\"" } + "\r\n"
                    zos.write(header.toByteArray(StandardCharsets.UTF_8))

                    val photosDir = File(context.filesDir, "student_photos")
                    val photosToZip = mutableSetOf<File>()

                    students.forEach { s ->
                        var photoFileName = ""
                        if (!s.photoUri.isNullOrBlank()) {
                            val pFile = File(s.photoUri)
                            if (pFile.exists()) {
                                photoFileName = pFile.name
                                photosToZip.add(pFile)
                            }
                        }

                        val row = listOf(
                            s.id.toString(),
                            escapeCsv(s.name),
                            escapeCsv(s.phone),
                            escapeCsv(s.email),
                            escapeCsv(s.idProofNumber),
                            escapeCsv(s.address),
                            escapeCsv(s.emergencyContact),
                            escapeCsv(s.shift),
                            escapeCsv(s.planType),
                            s.startDateMillis.toString(),
                            s.endDateMillis.toString(),
                            s.feeAmount.toString(),
                            s.feePaid.toString(),
                            escapeCsv(s.avatarKey),
                            escapeCsv(photoFileName),
                            escapeCsv(s.notes),
                            s.createdAt.toString()
                        ).joinToString(separator = ",") + "\r\n"

                        zos.write(row.toByteArray(StandardCharsets.UTF_8))
                    }
                    zos.closeEntry()

                    // Also include all files in student_photos if not already added
                    if (photosDir.exists() && photosDir.isDirectory) {
                        photosDir.listFiles()?.filter { it.isFile }?.forEach { photosToZip.add(it) }
                    }

                    // 2. Add photos into photos/ folder
                    val buffer = ByteArray(8192)
                    for (photo in photosToZip) {
                        try {
                            val photoEntry = ZipEntry("photos/${photo.name}")
                            zos.putNextEntry(photoEntry)
                            FileInputStream(photo).use { fis ->
                                var len: Int
                                while (fis.read(buffer).also { len = it } > 0) {
                                    zos.write(buffer, 0, len)
                                }
                            }
                            zos.closeEntry()
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to zip photo: ${photo.name}", e)
                        }
                    }

                    zos.finish()
                }
            }

            // Share ZIP archive via Android Intent Chooser
            val shared = shareZipFile(context, zipFile)
            Pair(zipFile, shared)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create full backup ZIP", e)
            Pair(null, false)
        }
    }

    private fun shareZipFile(context: Context, zipFile: File): Boolean {
        return try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, zipFile)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri("LibAdmin Full Backup", uri)
                putExtra(Intent.EXTRA_SUBJECT, "LibAdmin Full Database & Photos Backup")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is the complete LibAdmin student database backup (Excel CSV + student photos archive)."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Save / Share Full Backup ZIP").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing backup zip", e)
            false
        }
    }

    /**
     * Imports student data and photos from a user-selected ZIP or CSV file URI.
     */
    suspend fun importUserData(
        context: Context,
        sourceUri: Uri,
        repository: StudentRepository
    ): ImportResult {
        return try {
            val photosDir = File(context.filesDir, "student_photos").apply {
                if (!exists()) mkdirs()
            }

            var isZip = false
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                val magic = ByteArray(4)
                val read = input.read(magic)
                if (read >= 4 && magic[0] == 0x50.toByte() && magic[1] == 0x4B.toByte() &&
                    magic[2] == 0x03.toByte() && magic[3] == 0x04.toByte()
                ) {
                    isZip = true
                }
            }

            if (isZip) {
                importFromZip(context, sourceUri, photosDir, repository)
            } else {
                importFromCsvStream(context, sourceUri, repository)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import user data", e)
            ImportResult(false, 0, 0, "Import failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private suspend fun importFromZip(
        context: Context,
        sourceUri: Uri,
        photosDir: File,
        repository: StudentRepository
    ): ImportResult {
        var photoCount = 0
        var csvContent: String? = null
        val buffer = ByteArray(8192)

        context.contentResolver.openInputStream(sourceUri)?.use { rawIn ->
            ZipInputStream(rawIn).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val name = entry.name
                    if (!entry.isDirectory) {
                        if (name.endsWith(".csv", ignoreCase = true) || name.contains("database", ignoreCase = true)) {
                            val baos = ByteArrayOutputStream()
                            var len: Int
                            while (zis.read(buffer).also { len = it } > 0) {
                                baos.write(buffer, 0, len)
                            }
                            csvContent = baos.toString(StandardCharsets.UTF_8.name())
                        } else if (name.startsWith("photos/") || name.endsWith(".jpg", ignoreCase = true) || name.endsWith(".png", ignoreCase = true)) {
                            val fileName = File(name).name
                            val dest = File(photosDir, fileName)
                            FileOutputStream(dest).use { fos ->
                                var len: Int
                                while (zis.read(buffer).also { len = it } > 0) {
                                    fos.write(buffer, 0, len)
                                }
                            }
                            photoCount++
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }

        if (csvContent.isNullOrBlank()) {
            return ImportResult(false, 0, photoCount, "No student database CSV found inside the ZIP archive.")
        }

        val parsedStudents = parseCsvToStudents(csvContent!!, photosDir)
        if (parsedStudents.isNotEmpty()) {
            repository.insertStudentsChunked(parsedStudents)
        }

        return ImportResult(
            success = true,
            importedCount = parsedStudents.size,
            photosCount = photoCount,
            message = "Successfully imported ${parsedStudents.size} students and $photoCount photos."
        )
    }

    private suspend fun importFromCsvStream(
        context: Context,
        sourceUri: Uri,
        repository: StudentRepository
    ): ImportResult {
        val photosDir = File(context.filesDir, "student_photos").apply {
            if (!exists()) mkdirs()
        }

        val content = context.contentResolver.openInputStream(sourceUri)?.use { stream ->
            BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).readText()
        } ?: return ImportResult(false, 0, 0, "Could not read the selected CSV file.")

        val parsedStudents = parseCsvToStudents(content, photosDir)
        if (parsedStudents.isEmpty()) {
            return ImportResult(false, 0, 0, "No valid student records found in CSV file.")
        }

        repository.insertStudentsChunked(parsedStudents)
        return ImportResult(
            success = true,
            importedCount = parsedStudents.size,
            photosCount = 0,
            message = "Successfully imported ${parsedStudents.size} student records."
        )
    }

    private fun parseCsvToStudents(content: String, photosDir: File): List<Student> {
        val lines = content.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val students = mutableListOf<Student>()
        val header = parseCsvLine(lines[0])
        val colMap = header.mapIndexed { index, s -> s.trim().lowercase().replace("_", "") to index }.toMap()

        val now = System.currentTimeMillis()

        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.isEmpty() || cols.all { it.isBlank() }) continue

            fun get(key: String, fallback: String = ""): String {
                val idx = colMap[key.lowercase().replace("_", "")] ?: return fallback
                return if (idx < cols.size) cols[idx].trim() else fallback
            }

            val name = get("name", get("fullname", "Student $i"))
            if (name.isBlank()) continue

            val photoFileName = get("photofilename", get("photo", ""))
            var photoUri: String? = null
            if (photoFileName.isNotBlank()) {
                val candidate = File(photosDir, photoFileName)
                if (candidate.exists()) {
                    photoUri = candidate.absolutePath
                }
            }

            val student = Student(
                id = 0L, // Auto-generate IDs on restore
                name = name,
                phone = get("phone", get("phonenumber", "")),
                email = get("email", ""),
                idProofNumber = get("idproofnumber", get("studentid", "LIB-${(1000..9999).random()}")),
                address = get("address", ""),
                emergencyContact = get("emergencycontact", ""),
                shift = get("shift", "Full Day (6 AM - 10 PM)"),
                planType = get("plantype", "1 Month"),
                startDateMillis = get("startdatemillis", get("startdate", "$now")).toLongOrNull() ?: now,
                endDateMillis = get("enddatemillis", get("enddate", "${now + 30L * 86400000L}")).toLongOrNull() ?: (now + 30L * 86400000L),
                feeAmount = get("feeamount", "1000").toDoubleOrNull() ?: 1000.0,
                feePaid = get("feepaid", "1000").toDoubleOrNull() ?: 1000.0,
                avatarKey = get("avatarkey", "avatar_1"),
                photoUri = photoUri,
                notes = get("notes", "Imported record"),
                createdAt = get("createdat", "$now").toLongOrNull() ?: now
            )
            students.add(student)
        }

        return students
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '\"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                        sb.append('\"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    tokens.add(sb.toString().trim())
                    sb.clear()
                }
                else -> {
                    sb.append(c)
                }
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
