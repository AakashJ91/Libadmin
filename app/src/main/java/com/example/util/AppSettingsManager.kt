package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Student

object AppSettingsManager {

    private const val PREFS_NAME = "libadmin_app_settings"

    // Notification Message Template
    private const val KEY_NOTIFICATION_TEMPLATE = "key_notification_template"
    const val DEFAULT_NOTIFICATION_TEMPLATE =
        "Dear {name}, your library access ({plan}) expires on {date} ({days}). Please renew promptly to retain your study seat. Pending dues: ₹{dues}. - LibAdmin Library"

    // Default Fees
    const val KEY_FEE_1_WEEK = "key_fee_1_week"
    const val KEY_FEE_1_MONTH = "key_fee_1_month"
    const val KEY_FEE_3_MONTHS = "key_fee_3_months"
    const val KEY_FEE_6_MONTHS = "key_fee_6_months"

    const val DEFAULT_FEE_1_WEEK = 300.0
    const val DEFAULT_FEE_1_MONTH = 1000.0
    const val DEFAULT_FEE_3_MONTHS = 2700.0
    const val DEFAULT_FEE_6_MONTHS = 5000.0

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getNotificationTemplate(context: Context): String {
        return getPrefs(context).getString(KEY_NOTIFICATION_TEMPLATE, DEFAULT_NOTIFICATION_TEMPLATE)
            ?: DEFAULT_NOTIFICATION_TEMPLATE
    }

    fun saveNotificationTemplate(context: Context, template: String) {
        getPrefs(context).edit().putString(KEY_NOTIFICATION_TEMPLATE, template.trim()).apply()
    }

    fun getDefaultFee(context: Context, plan: String): Double {
        val prefs = getPrefs(context)
        return when {
            plan.contains("Week", ignoreCase = true) ->
                prefs.getFloat(KEY_FEE_1_WEEK, DEFAULT_FEE_1_WEEK.toFloat()).toDouble()
            plan.contains("1 Month", ignoreCase = true) ->
                prefs.getFloat(KEY_FEE_1_MONTH, DEFAULT_FEE_1_MONTH.toFloat()).toDouble()
            plan.contains("3 Month", ignoreCase = true) ->
                prefs.getFloat(KEY_FEE_3_MONTHS, DEFAULT_FEE_3_MONTHS.toFloat()).toDouble()
            plan.contains("6 Month", ignoreCase = true) ->
                prefs.getFloat(KEY_FEE_6_MONTHS, DEFAULT_FEE_6_MONTHS.toFloat()).toDouble()
            else -> prefs.getFloat(KEY_FEE_1_MONTH, DEFAULT_FEE_1_MONTH.toFloat()).toDouble()
        }
    }

    fun getAllDefaultFees(context: Context): Map<String, Double> {
        val prefs = getPrefs(context)
        return mapOf(
            "1 Week" to prefs.getFloat(KEY_FEE_1_WEEK, DEFAULT_FEE_1_WEEK.toFloat()).toDouble(),
            "1 Month" to prefs.getFloat(KEY_FEE_1_MONTH, DEFAULT_FEE_1_MONTH.toFloat()).toDouble(),
            "3 Months" to prefs.getFloat(KEY_FEE_3_MONTHS, DEFAULT_FEE_3_MONTHS.toFloat()).toDouble(),
            "6 Months" to prefs.getFloat(KEY_FEE_6_MONTHS, DEFAULT_FEE_6_MONTHS.toFloat()).toDouble()
        )
    }

    fun saveDefaultFees(
        context: Context,
        feeWeek: Double,
        fee1Month: Double,
        fee3Months: Double,
        fee6Months: Double
    ) {
        getPrefs(context).edit()
            .putFloat(KEY_FEE_1_WEEK, feeWeek.toFloat())
            .putFloat(KEY_FEE_1_MONTH, fee1Month.toFloat())
            .putFloat(KEY_FEE_3_MONTHS, fee3Months.toFloat())
            .putFloat(KEY_FEE_6_MONTHS, fee6Months.toFloat())
            .apply()
    }

    /**
     * Replaces standard placeholders in the template with student's actual values.
     */
    fun formatMessage(template: String, student: Student, now: Long = System.currentTimeMillis()): String {
        val days = student.daysRemaining(now)
        val daysString = when {
            days < 0 -> "Expired ${-days}d ago"
            days == 0 -> "Expires Today"
            else -> "$days days left"
        }

        return template
            .replace("{name}", student.name)
            .replace("{plan}", student.planType)
            .replace("{shift}", student.shift)
            .replace("{date}", student.formattedEndDate())
            .replace("{days}", daysString)
            .replace("{id}", student.idProofNumber)
            .replace("{fee}", student.feeAmount.toInt().toString())
            .replace("{paid}", student.feePaid.toInt().toString())
            .replace("{dues}", student.pendingFeeAmount.toInt().toString())
    }
}
