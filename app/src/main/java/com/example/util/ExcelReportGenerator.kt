package com.example.util

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.Student
import com.example.data.model.SubscriptionStatus
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelReportGenerator {

    private const val TAG = "ExcelReportGenerator"

    /**
     * Generates a Microsoft Excel-compatible Spreadsheet (.csv with UTF-8 BOM)
     * containing registered students, fee statuses, expiry dates, and dues.
     */
    fun generateExcelFile(
        context: Context,
        students: List<Student>,
        filterTitle: String = "All Students"
    ): File? {
        val now = System.currentTimeMillis()
        val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val displayDateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        val timestamp = fileDateFormat.format(Date(now))
        val displayDate = displayDateFormat.format(Date(now))

        val fileName = "LibAdmin_Students_Roster_$timestamp.csv"
        val reportsDir = File(context.cacheDir, "reports").apply {
            if (!exists()) mkdirs()
        }
        val file = File(reportsDir, fileName)

        try {
            FileOutputStream(file).use { fos ->
                // Write UTF-8 BOM so Microsoft Excel immediately recognizes UTF-8 formatting and symbols
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    // 1. Report Header Section
                    writer.write(csvRow("LIBADMIN - READING LIBRARY STUDENT & FEE STATUS ROSTER"))
                    writer.write(csvRow("Generated On", displayDate))
                    writer.write(csvRow("Filter Applied", filterTitle))
                    writer.write(csvRow("Total Records", students.size.toString()))
                    writer.write("\n")

                    // 2. Financial & Expiry Summary Block
                    val totalRevenue = students.sumOf { it.feePaid }
                    val totalPending = students.sumOf { it.pendingFeeAmount }
                    val activeCount = students.count { it.getStatus(now) == SubscriptionStatus.ACTIVE }
                    val expiringCount = students.count { it.getStatus(now) == SubscriptionStatus.EXPIRING_SOON }
                    val expiredCount = students.count { it.getStatus(now) == SubscriptionStatus.EXPIRED }
                    val paidCount = students.count { !it.isPendingFee }
                    val dueCount = students.count { it.isPendingFee }

                    writer.write(csvRow("--- SUMMARY AUDIT ---", ""))
                    writer.write(csvRow("Total Active Access", activeCount.toString()))
                    writer.write(csvRow("Expiring Soon (<=3 Days)", expiringCount.toString()))
                    writer.write(csvRow("Expired Access", expiredCount.toString()))
                    writer.write(csvRow("Fees Fully Paid", paidCount.toString()))
                    writer.write(csvRow("Students With Pending Dues", dueCount.toString()))
                    writer.write(csvRow("Total Fees Collected (INR)", String.format(Locale.US, "%.2f", totalRevenue)))
                    writer.write(csvRow("Total Outstanding Dues (INR)", String.format(Locale.US, "%.2f", totalPending)))
                    writer.write("\n")

                    // 3. Table Column Headers
                    writer.write(
                        csvRow(
                            "S.No",
                            "Student ID",
                            "Full Name",
                            "Phone Number",
                            "Email Address",
                            "Study Shift",
                            "Duration Plan",
                            "Start Date",
                            "Expiry Date",
                            "Days Remaining",
                            "Access Expiry Status",
                            "Total Fee (INR)",
                            "Fee Paid (INR)",
                            "Pending Due (INR)",
                            "Current Fee Status",
                            "Emergency Contact",
                            "Residential Address",
                            "Admin Notes"
                        )
                    )

                    // 4. Student Data Rows
                    students.forEachIndexed { index, student ->
                        val status = student.getStatus(now)
                        val days = student.daysRemaining(now)
                        val statusLabel = when (status) {
                            SubscriptionStatus.ACTIVE -> "Active Access"
                            SubscriptionStatus.EXPIRING_SOON -> "Expiring Soon ($days days left)"
                            SubscriptionStatus.EXPIRED -> if (days == 0) "Expired Today" else "Expired (${-days}d ago)"
                        }

                        val feeStatusLabel = if (student.isPendingFee) {
                            "PENDING (Due: Rs. ${String.format(Locale.US, "%.2f", student.pendingFeeAmount)})"
                        } else {
                            "PAID IN FULL"
                        }

                        writer.write(
                            csvRow(
                                (index + 1).toString(),
                                student.idProofNumber,
                                student.name,
                                student.phone,
                                student.email,
                                student.shift,
                                student.planType,
                                student.formattedStartDate(),
                                student.formattedEndDate(),
                                days.toString(),
                                statusLabel,
                                String.format(Locale.US, "%.2f", student.feeAmount),
                                String.format(Locale.US, "%.2f", student.feePaid),
                                String.format(Locale.US, "%.2f", student.pendingFeeAmount),
                                feeStatusLabel,
                                student.emergencyContact,
                                student.address,
                                student.notes
                            )
                        )
                    }

                    writer.flush()
                }
            }

            Log.i(TAG, "Excel spreadsheet exported successfully: ${file.absolutePath} (${file.length()} bytes)")
            return file
        } catch (e: Exception) {
            Log.e(TAG, "Error generating Excel spreadsheet", e)
            return null
        }
    }

    /**
     * Shares or opens the exported Excel spreadsheet via Android Intent chooser.
     */
    fun shareExcelFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "LibAdmin - Students & Fee Roster")
            putExtra(
                Intent.EXTRA_TEXT,
                "Please find attached the exported LibAdmin registered students roster with current fee status and expiry dates."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Open / Share Excel Sheet").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
    }

    /**
     * Formats elements into a CSV row with proper quote escaping.
     */
    private fun csvRow(vararg fields: String): String {
        return fields.joinToString(separator = ",") { field ->
            val escaped = field.replace("\"", "\"\"")
            "\"$escaped\""
        } + "\r\n"
    }
}
