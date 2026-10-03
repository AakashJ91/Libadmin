package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.Student
import com.example.data.model.SubscriptionStatus
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    /**
     * Generates a multi-page PDF report of the students list and launches the system share/view intent.
     */
    fun generateAndSharePdf(
        context: Context,
        students: List<Student>,
        filterTitle: String = "All Members"
    ): File? {
        val pdfDocument = PdfDocument()

        val pageWidth = 612 // Standard US Letter width in points
        val pageHeight = 792 // Standard US Letter height in points

        val titlePaint = Paint().apply {
            color = Color.rgb(14, 96, 122)
            textSize = 17f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 9.5f
            isAntiAlias = true
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(14, 96, 122)
            style = Paint.Style.FILL
        }

        val headerTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 8f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val rowTextPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 7.5f
            isAntiAlias = true
        }

        val rowAltBgPaint = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }

        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 0.8f
        }

        val activeBadgePaint = Paint().apply {
            color = Color.rgb(22, 163, 74)
            textSize = 7f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val expiringBadgePaint = Paint().apply {
            color = Color.rgb(217, 119, 6)
            textSize = 7f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val expiredBadgePaint = Paint().apply {
            color = Color.rgb(220, 38, 38)
            textSize = 7f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val now = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(now))

        val maxPdfStudents = 250
        val studentsForPdf = if (students.size > maxPdfStudents) students.take(maxPdfStudents) else students

        val rowsPerPage = 18
        val totalPages = maxOf(1, ((studentsForPdf.size + rowsPerPage - 1) / rowsPerPage))

        return try {
            for (pageIndex in 0 until totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            var currentY = 38f

            // --- Header Banner ---
            canvas.drawText("READING LIBRARY ADMINISTRATION ROSTER", 36f, currentY, titlePaint)
            currentY += 14f
            canvas.drawText("Student Enrollments, Access Durations & Expiry Audit Report", 36f, currentY, subtitlePaint)
            currentY += 13f

            val metaPaint = Paint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = 8.5f
                isAntiAlias = true
            }
            canvas.drawText("Filter: $filterTitle   |   Total Listed: ${students.size}   |   Generated: $dateStr", 36f, currentY, metaPaint)
            currentY += 13f

            canvas.drawLine(36f, currentY, (pageWidth - 36).toFloat(), currentY, linePaint)
            currentY += 14f

            // KPI Stat summary boxes on Page 1
            if (pageIndex == 0) {
                val activeCount = students.count { it.getStatus(now) == SubscriptionStatus.ACTIVE }
                val expiringCount = students.count { it.getStatus(now) == SubscriptionStatus.EXPIRING_SOON }
                val expiredCount = students.count { it.getStatus(now) == SubscriptionStatus.EXPIRED }
                val totalCollected = students.sumOf { it.feePaid }
                val totalDue = students.sumOf { it.pendingFeeAmount }

                val boxWidth = (pageWidth - 72 - 32) / 5f
                val boxHeight = 34f

                drawStatBox(canvas, 36f, currentY, boxWidth, boxHeight, "ACTIVE ACCESS", "$activeCount", Color.rgb(220, 252, 231), Color.rgb(22, 163, 74))
                drawStatBox(canvas, 36f + (boxWidth + 8f), currentY, boxWidth, boxHeight, "EXPIRING (≤3D)", "$expiringCount", Color.rgb(254, 243, 199), Color.rgb(217, 119, 6))
                drawStatBox(canvas, 36f + (boxWidth + 8f) * 2, currentY, boxWidth, boxHeight, "EXPIRED", "$expiredCount", Color.rgb(254, 226, 226), Color.rgb(220, 38, 38))
                drawStatBox(canvas, 36f + (boxWidth + 8f) * 3, currentY, boxWidth, boxHeight, "COLLECTED", "Rs. ${String.format(Locale.US, "%.0f", totalCollected)}", Color.rgb(224, 242, 254), Color.rgb(3, 105, 161))
                drawStatBox(canvas, 36f + (boxWidth + 8f) * 4, currentY, boxWidth, boxHeight, "PENDING DUES", "Rs. ${String.format(Locale.US, "%.0f", totalDue)}", Color.rgb(241, 245, 249), Color.rgb(15, 23, 42))

                currentY += boxHeight + 16f
            }

            // --- Table Header ---
            val tableLeft = 32f
            val tableRight = (pageWidth - 32).toFloat()
            val headerHeight = 22f

            val headerRect = RectF(tableLeft, currentY, tableRight, currentY + headerHeight)
            canvas.drawRoundRect(headerRect, 4f, 4f, headerBgPaint)

            val colXNo = tableLeft + 5f
            val colXName = tableLeft + 24f
            val colXId = tableLeft + 140f
            val colXShift = tableLeft + 215f
            val colXPhone = tableLeft + 290f
            val colXPlan = tableLeft + 365f
            val colXExpiry = tableLeft + 420f
            val colXDays = tableLeft + 475f
            val colXStatus = tableLeft + 512f

            val textBaseline = currentY + 14.5f
            canvas.drawText("#", colXNo, textBaseline, headerTextPaint)
            canvas.drawText("STUDENT NAME", colXName, textBaseline, headerTextPaint)
            canvas.drawText("STUDENT ID", colXId, textBaseline, headerTextPaint)
            canvas.drawText("SHIFT", colXShift, textBaseline, headerTextPaint)
            canvas.drawText("PHONE", colXPhone, textBaseline, headerTextPaint)
            canvas.drawText("PLAN", colXPlan, textBaseline, headerTextPaint)
            canvas.drawText("EXPIRY", colXExpiry, textBaseline, headerTextPaint)
            canvas.drawText("DAYS", colXDays, textBaseline, headerTextPaint)
            canvas.drawText("STATUS", colXStatus, textBaseline, headerTextPaint)

            currentY += headerHeight + 2f

            // Table Rows
            val startIndex = pageIndex * rowsPerPage
            val endIndex = minOf(studentsForPdf.size, startIndex + rowsPerPage)
            val rowHeight = 24f

            for (i in startIndex until endIndex) {
                val student = studentsForPdf[i]
                val rowRect = RectF(tableLeft, currentY, tableRight, currentY + rowHeight)

                if (i % 2 == 1) {
                    canvas.drawRect(rowRect, rowAltBgPaint)
                }

                canvas.drawLine(tableLeft, currentY + rowHeight, tableRight, currentY + rowHeight, linePaint)

                val rowBaseline = currentY + 16f
                val status = student.getStatus(now)
                val daysRemaining = student.daysRemaining(now)

                canvas.drawText("${i + 1}", colXNo, rowBaseline, rowTextPaint)

                val nameTruncated = if (student.name.length > 20) student.name.take(18) + ".." else student.name
                canvas.drawText(nameTruncated, colXName, rowBaseline, rowTextPaint)

                canvas.drawText(student.idProofNumber, colXId, rowBaseline, rowTextPaint)

                val shiftTruncated = student.shift.split(" ").firstOrNull() ?: student.shift
                canvas.drawText(shiftTruncated, colXShift, rowBaseline, rowTextPaint)

                canvas.drawText(student.phone, colXPhone, rowBaseline, rowTextPaint)
                canvas.drawText(student.planType, colXPlan, rowBaseline, rowTextPaint)
                canvas.drawText(student.formattedEndDate(), colXExpiry, rowBaseline, rowTextPaint)

                val daysStr = when {
                    daysRemaining < 0 -> "${-daysRemaining}d ago"
                    daysRemaining == 0 -> "Today"
                    else -> "${daysRemaining}d"
                }
                canvas.drawText(daysStr, colXDays, rowBaseline, rowTextPaint)

                val (statusStr, statusPaint) = when (status) {
                    SubscriptionStatus.ACTIVE -> "ACTIVE" to activeBadgePaint
                    SubscriptionStatus.EXPIRING_SOON -> "EXPIRING" to expiringBadgePaint
                    SubscriptionStatus.EXPIRED -> "EXPIRED" to expiredBadgePaint
                }
                canvas.drawText(statusStr, colXStatus, rowBaseline, statusPaint)

                currentY += rowHeight
            }

            // Signatures (on last page)
            if (pageIndex == totalPages - 1) {
                currentY += 28f
                val signPaint = Paint().apply {
                    color = Color.rgb(100, 116, 139)
                    textSize = 8.5f
                    isAntiAlias = true
                }
                canvas.drawLine(36f, currentY + 28f, 200f, currentY + 28f, linePaint)
                canvas.drawText("Library Administrator Signature", 36f, currentY + 41f, signPaint)

                canvas.drawLine((pageWidth - 200).toFloat(), currentY + 28f, (pageWidth - 36).toFloat(), currentY + 28f, linePaint)
                canvas.drawText("Seal & Approval Date", (pageWidth - 200).toFloat(), currentY + 41f, signPaint)
            }

            // Footer
            val footerY = (pageHeight - 22).toFloat()
            val footerPaint = Paint().apply {
                color = Color.rgb(148, 163, 184)
                textSize = 8f
                isAntiAlias = true
            }
            canvas.drawText("LibAdmin Study Library • Student Enrollment Records", 36f, footerY, footerPaint)
            val pageNumStr = "Page ${pageIndex + 1} of $totalPages"
            canvas.drawText(pageNumStr, (pageWidth - 36 - footerPaint.measureText(pageNumStr)), footerY, footerPaint)

            pdfDocument.finishPage(page)
        }

            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val fileName = "LibAdmin_Report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.pdf"
            val outputFile = File(reportsDir, fileName)

            FileOutputStream(outputFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
                outputStream.flush()
            }
            pdfDocument.close()
            outputFile
        } catch (e: Throwable) {
            e.printStackTrace()
            try {
                pdfDocument.close()
            } catch (_: Exception) {}
            null
        }
    }

    private fun drawStatBox(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        label: String,
        value: String,
        bgColor: Int,
        textColor: Int
    ) {
        val bgPaint = Paint().apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        val labelPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 6.5f
            isAntiAlias = true
        }
        val valPaint = Paint().apply {
            color = textColor
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }

        canvas.drawRoundRect(RectF(x, y, x + width, y + height), 4f, 4f, bgPaint)
        canvas.drawText(label, x + 6f, y + 12f, labelPaint)
        canvas.drawText(value, x + 6f, y + 26f, valPaint)
    }

    fun sharePdfFile(context: Context, pdfFile: File): Boolean {
        return try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(
                context,
                authority,
                pdfFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = android.content.ClipData.newRawUri("LibAdmin PDF", uri)
                putExtra(Intent.EXTRA_SUBJECT, "LibAdmin Student & Expiry Report")
                putExtra(Intent.EXTRA_TEXT, "Attached is the latest Library Student Access & Expiry Report.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Share Library PDF Report").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun viewPdfFile(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            sharePdfFile(context, pdfFile)
        }
    }
}
