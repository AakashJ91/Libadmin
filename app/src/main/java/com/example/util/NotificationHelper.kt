package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.model.Student
import java.net.URLEncoder

object NotificationHelper {

    const val CHANNEL_ID = "student_expiry_alerts_channel"
    private const val CHANNEL_NAME = "Subscription Expiry Alerts"
    private const val CHANNEL_DESC = "Alerts library admin when student library access is near expiration"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Posts an Android system push notification on the mobile device alerting the admin.
     */
    fun notifyAdminOfExpiringMembers(
        context: Context,
        expiringStudents: List<Student>,
        expiredStudents: List<Student>
    ) {
        createNotificationChannel(context)

        val totalAlerts = expiringStudents.size + expiredStudents.size
        if (totalAlerts == 0) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "directory_expiring")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (expiredStudents.isNotEmpty()) {
            "⚠️ LibAdmin: $totalAlerts Students Expiring or Expired!"
        } else {
            "⏰ LibAdmin: ${expiringStudents.size} Students Expiring Soon"
        }

        val studentSummary = (expiringStudents + expiredStudents)
            .take(3)
            .joinToString(separator = ", ") { it.name }

        val bigText = buildString {
            if (expiringStudents.isNotEmpty()) {
                append("Expiring in ≤3 days:\n")
                expiringStudents.forEach {
                    val days = it.daysRemaining()
                    append("• ${it.name} (${if (days == 0) "Today!" else "$days days left"})\n")
                }
            }
            if (expiredStudents.isNotEmpty()) {
                append("Already expired:\n")
                expiredStudents.forEach {
                    append("• ${it.name} (Expired)\n")
                }
            }
            append("Tap to view students & dispatch WhatsApp/SMS/Email reminders.")
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText("Action required for: $studentSummary")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(101, builder.build())
        } catch (_: SecurityException) {
            // Handled
        }
    }

    /**
     * Posts a direct simulated push reminder to the user/student on mobile.
     */
    fun notifyStudentDirectPush(context: Context, student: Student) {
        createNotificationChannel(context)

        val days = student.daysRemaining()
        val daysText = if (days < 0) "expired ${-days} days ago" else if (days == 0) "expires TODAY" else "expires in $days days"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("LibAdmin Access Renewal")
            .setContentText("Hello ${student.name}, your library access $daysText (${student.formattedEndDate()}).")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Hello ${student.name},\nYour reading library access (${student.shift}) $daysText.\nPlease visit library administration desk or renew online for uninterrupted access."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify((200 + student.id).toInt(), builder.build())
        } catch (_: SecurityException) {
            // Handled
        }
    }

    /**
     * Formats WhatsApp message and launches WhatsApp intent.
     */
    fun openWhatsAppReminder(context: Context, student: Student) {
        val template = AppSettingsManager.getNotificationTemplate(context)
        val customMsg = AppSettingsManager.formatMessage(template, student)

        val message = """
            📚 *LibAdmin Reading Library - Notification*
            
            $customMsg
            
            Shift: ${student.shift}
            Plan: ${student.planType}
            Expiry: ${student.formattedEndDate()}
            ${if (student.isPendingFee) "⚠️ Pending Dues: ₹${String.format(java.util.Locale.US, "%.2f", student.pendingFeeAmount)}\n" else ""}
            Regards,
            Library Administration Team
        """.trimIndent()

        val cleanPhone = student.phone.replace(Regex("[^0-9+]"), "")
        val encodedMessage = try {
            URLEncoder.encode(message, "UTF-8")
        } catch (e: Exception) {
            message
        }

        val uriString = if (cleanPhone.isNotEmpty()) {
            "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage"
        } else {
            "https://api.whatsapp.com/send?text=$encodedMessage"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Launches SMS composer with pre-filled reminder text.
     */
    fun openSmsReminder(context: Context, student: Student) {
        val template = AppSettingsManager.getNotificationTemplate(context)
        val text = AppSettingsManager.formatMessage(template, student)

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${student.phone.replace(Regex("[^0-9+]"), "")}")
            putExtra("sms_body", text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open SMS app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Launches Email client with pre-filled subject and reminder body.
     */
    fun openEmailReminder(context: Context, student: Student) {
        val days = student.daysRemaining()
        val subject = "Library Access Expiry Alert - ${student.name}"
        val body = """
            Dear ${student.name},
            
            This is an automated notification regarding your reading library access duration.
            
            Student ID: ${student.idProofNumber}
            Shift: ${student.shift}
            Plan: ${student.planType}
            Expiry Date: ${student.formattedEndDate()} (${if (days < 0) "Expired ${-days} day(s) ago" else "$days days remaining"})
            ${if (student.isPendingFee) "Outstanding Balance: ₹" + String.format("%.2f", student.pendingFeeAmount) else "Fee Status: Paid in full"}
            
            If you wish to extend your access duration, please visit the administration desk or respond to this email with your renewal confirmation.
            
            Best regards,
            Library Administration Desk
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${student.email}")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open Email client: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openDialer(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneNumber.replace(Regex("[^0-9+]"), "")}")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open phone dialer", Toast.LENGTH_SHORT).show()
        }
    }
}
