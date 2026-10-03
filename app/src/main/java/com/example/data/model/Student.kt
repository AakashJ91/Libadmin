package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class SubscriptionStatus {
    ACTIVE,
    EXPIRING_SOON,
    EXPIRED
}

enum class ReminderChannel {
    WHATSAPP,
    SMS,
    EMAIL,
    SYSTEM_NOTIFICATION
}

data class Student(
    val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String,
    val idProofNumber: String, // e.g. Student ID or National ID
    val address: String,
    val avatarKey: String = "avatar_1",
    val photoUri: String? = null,
    val shift: String, // "Morning (6 AM - 2 PM)", "Evening (2 PM - 10 PM)", "Full Day (6 AM - 10 PM)", "24 Hours Access"
    val planType: String, // "1 Week", "1 Month", "3 Months", "6 Months", "Custom"
    val startDateMillis: Long,
    val endDateMillis: Long,
    val feeAmount: Double = 0.0,
    val feePaid: Double = 0.0,
    val emergencyContact: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculates days remaining until expiration.
     */
    fun daysRemaining(currentTime: Long = System.currentTimeMillis()): Int {
        val diffMillis = endDateMillis - currentTime
        return TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
    }

    fun getStatus(currentTime: Long = System.currentTimeMillis()): SubscriptionStatus {
        val days = daysRemaining(currentTime)
        return when {
            days < 0 -> SubscriptionStatus.EXPIRED
            days <= 3 -> SubscriptionStatus.EXPIRING_SOON
            else -> SubscriptionStatus.ACTIVE
        }
    }

    fun formattedStartDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(startDateMillis))
    }

    fun formattedEndDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(endDateMillis))
    }

    val isPendingFee: Boolean
        get() = (feeAmount - feePaid) > 0.01

    val pendingFeeAmount: Double
        get() = maxOf(0.0, feeAmount - feePaid)
}
