package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.ReminderLogEntity
import com.example.data.model.ReminderChannel
import com.example.data.model.Student
import com.example.data.model.SubscriptionStatus
import com.example.data.repository.StudentRepository
import com.example.ui.theme.AppAccent
import com.example.ui.theme.ThemeMode
import com.example.util.ExcelReportGenerator
import com.example.util.NotificationHelper
import com.example.util.PdfReportGenerator
import com.example.util.DataBackupHelper
import com.example.util.ImportResult
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class FilterType(val displayName: String) {
    ALL("All Students"),
    PENDING_FEES("Pending Dues"),
    PAID_FEES("Fees Fully Paid"),
    EXPIRING_3_DAYS("Expiring ≤ 3 Days"),
    EXPIRING_7_DAYS("Expiring ≤ 7 Days"),
    ACTIVE("Active Access"),
    EXPIRED("Expired Access"),
    MORNING("Morning Shift"),
    EVENING("Evening Shift"),
    FULL_DAY("Full Day")
}

enum class SortOrder(val displayName: String) {
    EXPIRY_SOONEST("Expiry: Soonest First"),
    EXPIRY_LATEST("Expiry: Latest First"),
    DUE_HIGHEST("Pending Dues: Highest First"),
    NAME_AZ("Name: A to Z")
}

data class LibraryMetrics(
    val totalStudents: Int = 0,
    val activeCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val expiredCount: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalPendingDues: Double = 0.0
)

sealed class AppScreen {
    data object Dashboard : AppScreen()
    data object Directory : AppScreen()
    data object ReminderHub : AppScreen()
    data class StudentDetail(val studentId: Long) : AppScreen()
    data class AddEditStudent(val studentId: Long? = null) : AppScreen()
    data object Settings : AppScreen()
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudentRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = StudentRepository(db.studentDao())
    }

    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminderLogs: StateFlow<List<ReminderLogEntity>> = repository.allReminderLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow(FilterType.ALL)
    val sortOrder = MutableStateFlow(SortOrder.EXPIRY_SOONEST)

    fun setFilter(filter: FilterType) {
        selectedFilter.value = filter
    }

    fun setSort(order: SortOrder) {
        sortOrder.value = order
    }

    // Current navigation screen
    val currentScreen = MutableStateFlow<AppScreen>(AppScreen.Dashboard)
    val navigationStack = mutableListOf<AppScreen>()

    private val themePrefs = application.getSharedPreferences("app_theme_preferences", Context.MODE_PRIVATE)

    val themeMode = MutableStateFlow(
        when (themePrefs.getString("theme_mode", "SYSTEM")) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    )

    val accentColor = MutableStateFlow(
        try {
            AppAccent.valueOf(themePrefs.getString("accent_color", "OCEAN") ?: "OCEAN")
        } catch (e: Exception) {
            AppAccent.OCEAN
        }
    )

    val showThemeSheet = MutableStateFlow(false)

    // Dark mode state: null = system default, true = dark, false = light
    val darkModeOverride = MutableStateFlow<Boolean?>(
        when (themePrefs.getString("theme_mode", "SYSTEM")) {
            "LIGHT" -> false
            "DARK" -> true
            else -> null
        }
    )

    fun setThemeMode(mode: ThemeMode) {
        themeMode.value = mode
        themePrefs.edit().putString("theme_mode", mode.name).apply()
        darkModeOverride.value = when (mode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> null
        }
    }

    fun setAccentColor(accent: AppAccent) {
        accentColor.value = accent
        themePrefs.edit().putString("accent_color", accent.name).apply()
    }

    fun openThemeSheet() {
        showThemeSheet.value = true
    }

    fun closeThemeSheet() {
        showThemeSheet.value = false
    }

    // Filtered and Sorted Students list
    val filteredStudents: StateFlow<List<Student>> = combine(
        allStudents,
        searchQuery,
        selectedFilter,
        sortOrder
    ) { students, query, filter, sort ->
        val now = System.currentTimeMillis()
        var result = students

        // Search text matching
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter {
                it.name.lowercase().contains(q) ||
                        it.phone.lowercase().contains(q) ||
                        it.idProofNumber.lowercase().contains(q) ||
                        it.email.lowercase().contains(q) ||
                        it.shift.lowercase().contains(q)
            }
        }

        // Quick filter
        result = when (filter) {
            FilterType.ALL -> result
            FilterType.PENDING_FEES -> result.filter { it.isPendingFee }
            FilterType.PAID_FEES -> result.filter { !it.isPendingFee }
            FilterType.EXPIRING_3_DAYS -> result.filter {
                val days = it.daysRemaining(now)
                days in 0..3
            }
            FilterType.EXPIRING_7_DAYS -> result.filter {
                val days = it.daysRemaining(now)
                days in 0..7
            }
            FilterType.ACTIVE -> result.filter { it.getStatus(now) == SubscriptionStatus.ACTIVE }
            FilterType.EXPIRED -> result.filter { it.getStatus(now) == SubscriptionStatus.EXPIRED }
            FilterType.MORNING -> result.filter { it.shift.contains("Morning", ignoreCase = true) }
            FilterType.EVENING -> result.filter { it.shift.contains("Evening", ignoreCase = true) }
            FilterType.FULL_DAY -> result.filter { it.shift.contains("Full Day", ignoreCase = true) }
        }

        // Sorting
        when (sort) {
            SortOrder.EXPIRY_SOONEST -> result.sortedBy { it.endDateMillis }
            SortOrder.EXPIRY_LATEST -> result.sortedByDescending { it.endDateMillis }
            SortOrder.DUE_HIGHEST -> result.sortedByDescending { it.pendingFeeAmount }
            SortOrder.NAME_AZ -> result.sortedBy { it.name.lowercase() }
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Computed metrics
    val metrics: StateFlow<LibraryMetrics> = allStudents.combine(MutableStateFlow(Unit)) { students, _ ->
        val now = System.currentTimeMillis()
        val total = students.size
        val active = students.count { it.getStatus(now) == SubscriptionStatus.ACTIVE }
        val expiring = students.count { it.getStatus(now) == SubscriptionStatus.EXPIRING_SOON }
        val expired = students.count { it.getStatus(now) == SubscriptionStatus.EXPIRED }
        val rev = students.sumOf { it.feePaid }
        val dues = students.sumOf { it.pendingFeeAmount }

        LibraryMetrics(
            totalStudents = total,
            activeCount = active,
            expiringSoonCount = expiring,
            expiredCount = expired,
            totalRevenue = rev,
            totalPendingDues = dues
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryMetrics())

    fun navigateTo(screen: AppScreen) {
        val current = currentScreen.value
        if (current != screen) {
            navigationStack.add(current)
            currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (navigationStack.isNotEmpty()) {
            currentScreen.value = navigationStack.removeAt(navigationStack.size - 1)
            return true
        }
        if (currentScreen.value != AppScreen.Dashboard) {
            currentScreen.value = AppScreen.Dashboard
            return true
        }
        return false
    }

    fun toggleDarkMode() {
        darkModeOverride.value = when (darkModeOverride.value) {
            null -> true
            true -> false
            false -> null
        }
    }

    fun saveStudent(student: Student) {
        viewModelScope.launch {
            if (student.id == 0L) {
                repository.insertStudent(student)
            } else {
                repository.updateStudent(student)
            }
        }
    }

    fun deleteStudent(studentId: Long) {
        viewModelScope.launch {
            repository.deleteStudentById(studentId)
            if (currentScreen.value is AppScreen.StudentDetail) {
                navigateBack()
            }
        }
    }

    /**
     * Renews student access for a given duration.
     */
    fun renewStudent(student: Student, durationDays: Int, planTitle: String, addedFee: Double) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val baseStart = if (student.endDateMillis < now) now else student.endDateMillis
            val newEnd = baseStart + TimeUnit.DAYS.toMillis(durationDays.toLong())

            val updated = student.copy(
                planType = planTitle,
                startDateMillis = if (student.endDateMillis < now) now else student.startDateMillis,
                endDateMillis = newEnd,
                feeAmount = student.feeAmount + addedFee,
                feePaid = student.feePaid + addedFee
            )
            repository.updateStudent(updated)

            // Log renewal
            repository.logReminder(
                ReminderLogEntity(
                    studentId = student.id,
                    studentName = student.name,
                    channel = "RENEWAL",
                    message = "Renewed for $planTitle until ${updated.formattedEndDate()}",
                    status = "COMPLETED"
                )
            )
        }
    }

    fun deleteMultipleStudents(ids: Set<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.deleteStudentsByIds(ids.toList())
            repository.logReminder(
                ReminderLogEntity(
                    studentId = 0,
                    studentName = "Admin System",
                    channel = "DELETION",
                    message = "Batch deleted ${ids.size} student records from library registry.",
                    status = "DELETED"
                )
            )
        }
    }

    /**
     * Executes automated expiry scan, sends Android admin push notification,
     * and returns the count of alerted students.
     */
    fun runAutomatedExpiryScan(context: Context): Pair<Int, Int> {
        val now = System.currentTimeMillis()
        val students = allStudents.value
        val expiring = students.filter { it.getStatus(now) == SubscriptionStatus.EXPIRING_SOON }
        val expired = students.filter { it.getStatus(now) == SubscriptionStatus.EXPIRED }

        // Send Android push notification to the mobile device itself!
        NotificationHelper.notifyAdminOfExpiringMembers(context, expiring, expired)

        // Log the audit event in Room
        viewModelScope.launch {
            repository.logReminder(
                ReminderLogEntity(
                    studentId = 0,
                    studentName = "Admin System",
                    channel = "PUSH_ALERT",
                    message = "Automated audit: ${expiring.size} expiring soon, ${expired.size} expired members notified on mobile.",
                    status = "DISPATCHED"
                )
            )
        }

        return Pair(expiring.size, expired.size)
    }

    /**
     * Dispatches a student reminder via WhatsApp, SMS, Email, or Direct Push.
     */
    fun dispatchReminder(context: Context, student: Student, channel: ReminderChannel) {
        when (channel) {
            ReminderChannel.WHATSAPP -> NotificationHelper.openWhatsAppReminder(context, student)
            ReminderChannel.SMS -> NotificationHelper.openSmsReminder(context, student)
            ReminderChannel.EMAIL -> NotificationHelper.openEmailReminder(context, student)
            ReminderChannel.SYSTEM_NOTIFICATION -> NotificationHelper.notifyStudentDirectPush(context, student)
        }

        // Record in audit log
        viewModelScope.launch {
            repository.logReminder(
                ReminderLogEntity(
                    studentId = student.id,
                    studentName = student.name,
                    channel = channel.name,
                    message = "Reminder sent: Reading library access expiring on ${student.formattedEndDate()}",
                    status = "SENT"
                )
            )
        }
    }

    val isExporting = MutableStateFlow(false)

    fun exportPdfReport(context: Context, onResult: ((File?, Boolean) -> Unit)? = null): File? {
        if (onResult != null) {
            isExporting.value = true
            viewModelScope.launch(Dispatchers.IO) {
                val listToExport = filteredStudents.value
                val filterName = selectedFilter.value.displayName
                val file = PdfReportGenerator.generateAndSharePdf(context, listToExport, filterName)
                var shared = false
                if (file != null) {
                    shared = PdfReportGenerator.sharePdfFile(context, file)
                }
                withContext(Dispatchers.Main) {
                    isExporting.value = false
                    onResult(file, shared)
                }
            }
            return null
        } else {
            val listToExport = filteredStudents.value
            val filterName = selectedFilter.value.displayName
            val file = PdfReportGenerator.generateAndSharePdf(context, listToExport, filterName)
            if (file != null) {
                PdfReportGenerator.sharePdfFile(context, file)
            }
            return file
        }
    }

    fun exportExcelReport(context: Context, onResult: ((File?, Boolean) -> Unit)? = null): File? {
        if (onResult != null) {
            isExporting.value = true
            viewModelScope.launch(Dispatchers.IO) {
                val listToExport = filteredStudents.value
                val filterName = selectedFilter.value.displayName
                val file = ExcelReportGenerator.generateExcelFile(context, listToExport, filterName)
                var shared = false
                if (file != null) {
                    shared = ExcelReportGenerator.shareExcelFile(context, file)
                }
                withContext(Dispatchers.Main) {
                    isExporting.value = false
                    onResult(file, shared)
                }
            }
            return null
        } else {
            val listToExport = filteredStudents.value
            val filterName = selectedFilter.value.displayName
            val file = ExcelReportGenerator.generateExcelFile(context, listToExport, filterName)
            if (file != null) {
                ExcelReportGenerator.shareExcelFile(context, file)
            }
            return file
        }
    }

    /**
     * Exports all user data into a single ZIP archive containing database Excel CSV and photos folder.
     */
    fun exportFullBackup(context: Context, onResult: (File?, Boolean) -> Unit) {
        if (isExporting.value) return
        isExporting.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val students = allStudents.value
            val (zipFile, shared) = DataBackupHelper.exportAllUserDataToZip(context, students)
            withContext(Dispatchers.Main) {
                isExporting.value = false
                onResult(zipFile, shared)
            }
        }
    }

    val isImporting = MutableStateFlow(false)

    /**
     * Imports student records and photos from a user-selected ZIP or CSV archive.
     */
    fun importUserData(context: Context, sourceUri: Uri, onResult: (ImportResult) -> Unit) {
        if (isImporting.value) return
        isImporting.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = DataBackupHelper.importUserData(context, sourceUri, repository)
            withContext(Dispatchers.Main) {
                isImporting.value = false
                onResult(result)
            }
        }
    }

    val isSeeding5k = MutableStateFlow(false)
    val seedingProgress = MutableStateFlow(0f)

    /**
     * Seeds 5,000 realistic members in batched chunks to validate 5k database scaling.
     */
    fun populate5kUsersBenchmark(onFinished: () -> Unit = {}) {
        if (isSeeding5k.value) return
        isSeeding5k.value = true
        seedingProgress.value = 0f

        viewModelScope.launch(Dispatchers.IO) {
            val firstNames = listOf(
                "Aarav", "Vivaan", "Aditya", "Vihaan", "Arjun", "Sai", "Reyansh", "Ayaan", "Krishna", "Ishaan",
                "Shaurya", "Atharv", "Advik", "Pranav", "Advaith", "Aaryan", "Dhruv", "Kabir", "Rudra", "Ananya",
                "Diya", "Ira", "Myra", "Saanvi", "Aanya", "Pari", "Navya", "Riya", "Aadhya", "Kiara",
                "Rohan", "Vikram", "Rahul", "Pooja", "Neha", "Amit", "Kavya", "Deepak", "Sneha", "Tanvi"
            )
            val lastNames = listOf(
                "Sharma", "Verma", "Gupta", "Malhotra", "Bhatia", "Saxena", "Mehta", "Chopra", "Kapoor", "Joshi",
                "Patel", "Reddy", "Nair", "Iyer", "Rao", "Kumar", "Singh", "Yadav", "Mishra", "Pandey",
                "Deshmukh", "Kulkarni", "Patil", "Chavan", "Bose", "Banerjee", "Mukherjee", "Chatterjee", "Ghosh", "Das"
            )
            val shifts = listOf(
                "Morning (6 AM - 2 PM)",
                "Evening (2 PM - 10 PM)",
                "Full Day (6 AM - 10 PM)",
                "24 Hours Access"
            )
            val plans = listOf(
                Pair("1 Week", 7),
                Pair("1 Month", 30),
                Pair("3 Months", 90),
                Pair("6 Months", 180),
                Pair("1 Year", 365)
            )

            val now = System.currentTimeMillis()
            val dayMillis = TimeUnit.DAYS.toMillis(1)
            val totalToGenerate = 5000
            val chunkSize = 500

            val currentMaxCount = repository.getStudentCount()

            for (chunkStart in 0 until totalToGenerate step chunkSize) {
                val chunk = mutableListOf<Student>()
                val end = minOf(chunkStart + chunkSize, totalToGenerate)

                for (i in chunkStart until end) {
                    val memberNumber = currentMaxCount + i + 1
                    val fn = firstNames[i % firstNames.size]
                    val ln = lastNames[(i / firstNames.size) % lastNames.size]
                    val fullName = "$fn $ln"
                    val phone = "+91 9${String.format(Locale.US, "%09d", kotlin.math.abs((memberNumber * 73931L) % 1000000000L))}"
                    val email = "${fn.lowercase()}.${ln.lowercase()}$memberNumber@gmail.com"
                    val idProof = "LIB-${2024 + (i % 3)}-${String.format(Locale.US, "%05d", memberNumber)}"
                    val shift = shifts[i % shifts.size]
                    val (planTitle, durationDays) = plans[i % plans.size]

                    // Distribute statuses: 70% active, 15% expiring soon, 15% expired
                    val statusBucket = i % 100
                    val (startDaysAgo, endDaysAhead) = when {
                        statusBucket < 15 -> Pair(durationDays - 2, 2)
                        statusBucket < 30 -> Pair(durationDays + 5, -5)
                        else -> Pair(durationDays / 3, (durationDays * 2) / 3)
                    }

                    val feeTotal = when (durationDays) {
                        7 -> 400.0
                        30 -> 1000.0
                        90 -> 2800.0
                        180 -> 5200.0
                        else -> 9800.0
                    }

                    // 20% students have pending dues
                    val feePaid = if (i % 5 == 0) feeTotal * 0.7 else feeTotal

                    chunk.add(
                        Student(
                            id = 0,
                            name = fullName,
                            phone = phone,
                            email = email,
                            idProofNumber = idProof,
                            address = "Seat #${(i % 120) + 1}, Reading Hall ${(i % 3) + 1}",
                            avatarKey = "avatar_${(i % 8) + 1}",
                            shift = shift,
                            planType = planTitle,
                            startDateMillis = now - (startDaysAgo.toLong() * dayMillis),
                            endDateMillis = now + (endDaysAhead.toLong() * dayMillis),
                            feeAmount = feeTotal,
                            feePaid = feePaid,
                            emergencyContact = "+91 98${String.format(Locale.US, "%08d", kotlin.math.abs((memberNumber * 41113L) % 100000000L))} (Guardian)",
                            notes = "Member record generated for 5k high-scale capacity validation."
                        )
                    )
                }

                repository.insertStudentsChunked(chunk)
                seedingProgress.value = (end.toFloat() / totalToGenerate.toFloat())
            }

            isSeeding5k.value = false
            withContext(Dispatchers.Main) {
                onFinished()
            }
        }
    }

    fun resetToDefaultMembers(onFinished: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearBenchmarkStudents()
            withContext(Dispatchers.Main) {
                onFinished()
            }
        }
    }
}
