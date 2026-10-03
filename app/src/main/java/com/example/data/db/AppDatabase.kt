package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Database(
    entities = [StudentEntity::class, ReminderLogEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "library_admin_db"
                )
                    .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database.studentDao())
                    }
                }
            }
        }

        private suspend fun seedInitialData(dao: StudentDao) {
            val now = System.currentTimeMillis()
            val dayMillis = TimeUnit.DAYS.toMillis(1)

            val seedStudents = listOf(
                StudentEntity(
                    id = 1,
                    name = "Alex Rivera",
                    phone = "+1 (555) 234-8901",
                    email = "alex.rivera@example.com",
                    idProofNumber = "LIB-2024-8841",
                    address = "452 Elm Street, Apt 3B, Downtown",
                    avatarKey = "avatar_1",
                    shift = "Full Day (6 AM - 10 PM)",
                    planType = "1 Month",
                    startDateMillis = now - (29 * dayMillis),
                    endDateMillis = now + (1 * dayMillis), // Expiring in 1 day!
                    feeAmount = 1200.0,
                    feePaid = 1200.0,
                    emergencyContact = "+1 (555) 991-0021 (Father)",
                    notes = "Medical College Entrance exam preparation."
                ),
                StudentEntity(
                    id = 2,
                    name = "Sophia Chen",
                    phone = "+1 (555) 872-3312",
                    email = "sophia.chen@example.com",
                    idProofNumber = "LIB-2024-9023",
                    address = "18 Pine Ridge Way, North Hills",
                    avatarKey = "avatar_2",
                    shift = "Morning (6 AM - 2 PM)",
                    planType = "3 Months",
                    startDateMillis = now - (87 * dayMillis),
                    endDateMillis = now + (3 * dayMillis), // Expiring in 3 days!
                    feeAmount = 3000.0,
                    feePaid = 3000.0,
                    emergencyContact = "+1 (555) 321-4490 (Mother)",
                    notes = "Law Bar Exam preparation."
                ),
                StudentEntity(
                    id = 3,
                    name = "Marcus Johnson",
                    phone = "+1 (555) 431-7788",
                    email = "marcus.j@example.com",
                    idProofNumber = "LIB-2024-7611",
                    address = "742 Evergreen Terrace, Westside",
                    avatarKey = "avatar_3",
                    shift = "Evening (2 PM - 10 PM)",
                    planType = "1 Week",
                    startDateMillis = now - (9 * dayMillis),
                    endDateMillis = now - (2 * dayMillis), // Expired 2 days ago!
                    feeAmount = 400.0,
                    feePaid = 400.0,
                    emergencyContact = "+1 (555) 765-1122 (Sister)",
                    notes = "Renewal extension pending."
                ),
                StudentEntity(
                    id = 4,
                    name = "Emily Davis",
                    phone = "+1 (555) 654-9988",
                    email = "emily.davis@example.com",
                    idProofNumber = "LIB-2024-9104",
                    address = "1204 Oak Meadow Drive, Lakeview",
                    avatarKey = "avatar_4",
                    shift = "24 Hours Access",
                    planType = "6 Months",
                    startDateMillis = now - (30 * dayMillis),
                    endDateMillis = now + (150 * dayMillis), // Active (5 months left)
                    feeAmount = 5500.0,
                    feePaid = 5500.0,
                    emergencyContact = "+1 (555) 887-2233 (Spouse)",
                    notes = "Civil Services aspirant."
                ),
                StudentEntity(
                    id = 5,
                    name = "Ryan Patel",
                    phone = "+1 (555) 345-6677",
                    email = "ryan.patel@example.com",
                    idProofNumber = "LIB-2024-9331",
                    address = "89 Maple Court, Metro City",
                    avatarKey = "avatar_5",
                    shift = "Morning (6 AM - 2 PM)",
                    planType = "1 Month",
                    startDateMillis = now - (10 * dayMillis),
                    endDateMillis = now + (20 * dayMillis), // Active (20 days left)
                    feeAmount = 1000.0,
                    feePaid = 700.0, // Partial payment
                    emergencyContact = "+1 (555) 993-4411 (Brother)",
                    notes = "Balance ₹300 due next week."
                ),
                StudentEntity(
                    id = 6,
                    name = "Olivia Wilson",
                    phone = "+1 (555) 789-0123",
                    email = "olivia.w@example.com",
                    idProofNumber = "LIB-2024-9442",
                    address = "550 University Blvd, Campus District",
                    avatarKey = "avatar_6",
                    shift = "24 Hours Access",
                    planType = "3 Months",
                    startDateMillis = now - (45 * dayMillis),
                    endDateMillis = now + (45 * dayMillis), // Active
                    feeAmount = 3200.0,
                    feePaid = 3200.0,
                    emergencyContact = "+1 (555) 123-9900 (Mother)",
                    notes = "Data Science certification studies."
                ),
                StudentEntity(
                    id = 7,
                    name = "David Kim",
                    phone = "+1 (555) 555-8821",
                    email = "david.kim@example.com",
                    idProofNumber = "LIB-2024-9550",
                    address = "312 Sunset Plaza, Suite 9",
                    avatarKey = "avatar_7",
                    shift = "Evening (2 PM - 10 PM)",
                    planType = "1 Month",
                    startDateMillis = now - (28 * dayMillis),
                    endDateMillis = now + (2 * dayMillis), // Expiring in 2 days!
                    feeAmount = 1000.0,
                    feePaid = 1000.0,
                    emergencyContact = "+1 (555) 234-9988 (Father)",
                    notes = "GRE prep student."
                )
            )

            dao.insertAll(seedStudents)

            dao.insertReminderLog(
                ReminderLogEntity(
                    studentId = 3,
                    studentName = "Marcus Johnson",
                    channel = "WHATSAPP",
                    message = "Reminder sent: Reading library access expired on overdue date.",
                    timestamp = now - (1 * dayMillis),
                    status = "SENT"
                )
            )
        }
    }
}
