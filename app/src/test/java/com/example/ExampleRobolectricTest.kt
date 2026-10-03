package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Student
import com.example.data.model.SubscriptionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LibAdmin", appName)
    }

    @Test
    fun `test student subscription status and expiry calculation`() {
        val now = System.currentTimeMillis()
        val dayMillis = TimeUnit.DAYS.toMillis(1)

        val activeStudent = Student(
            id = 1,
            name = "Alex Rivera",
            phone = "+1 555-234-8901",
            email = "alex@example.com",
            idProofNumber = "LIB-001",
            address = "452 Elm St",
            shift = "Full Day",
            planType = "1 Month",
            startDateMillis = now - (10 * dayMillis),
            endDateMillis = now + (20 * dayMillis)
        )
        assertEquals(SubscriptionStatus.ACTIVE, activeStudent.getStatus(now))
        assertTrue(activeStudent.daysRemaining(now) > 3)

        val expiringSoonStudent = activeStudent.copy(
            endDateMillis = now + (2 * dayMillis)
        )
        assertEquals(SubscriptionStatus.EXPIRING_SOON, expiringSoonStudent.getStatus(now))

        val expiredStudent = activeStudent.copy(
            endDateMillis = now - (1 * dayMillis)
        )
        assertEquals(SubscriptionStatus.EXPIRED, expiredStudent.getStatus(now))
    }

    @Test
    fun `test student pending fee calculation`() {
        val studentPaid = Student(
            id = 1,
            name = "Paid Student",
            phone = "12345",
            email = "paid@example.com",
            idProofNumber = "ID-1",
            address = "Street",
            shift = "Morning",
            planType = "1 Month",
            startDateMillis = 0,
            endDateMillis = 1000000,
            feeAmount = 1000.0,
            feePaid = 1000.0
        )
        assertFalse(studentPaid.isPendingFee)
        assertEquals(0.0, studentPaid.pendingFeeAmount, 0.01)

        val studentDue = studentPaid.copy(feeAmount = 1000.0, feePaid = 700.0)
        assertTrue(studentDue.isPendingFee)
        assertEquals(300.0, studentDue.pendingFeeAmount, 0.01)
    }

    @Test
    fun `test excel report generation produces valid spreadsheet file`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val student = Student(
            id = 101,
            name = "Rohan Sharma",
            phone = "+91 98765 43210",
            email = "rohan@example.com",
            idProofNumber = "LIB-2024-9988",
            address = "Civil Lines, Delhi",
            shift = "Morning (6 AM - 2 PM)",
            planType = "1 Month",
            startDateMillis = System.currentTimeMillis() - 100000,
            endDateMillis = System.currentTimeMillis() + 86400000,
            feeAmount = 1000.0,
            feePaid = 800.0
        )

        val file = com.example.util.ExcelReportGenerator.generateExcelFile(
            context,
            listOf(student),
            "Test Filter"
        )

        org.junit.Assert.assertNotNull(file)
        assertTrue(file!!.exists())
        assertTrue(file.length() > 0)

        val content = file.readText()
        assertTrue(content.contains("LIBADMIN - READING LIBRARY STUDENT & FEE STATUS ROSTER"))
        assertTrue(content.contains("Rohan Sharma"))
        assertTrue(content.contains("LIB-2024-9988"))
        assertTrue(content.contains("800.00"))
        assertTrue(content.contains("200.00"))
    }

    @Test
    fun `test batch delete students in dao`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.db.AppDatabase::class.java
        ).allowMainThreadQueries().build()
        val dao = db.studentDao()

        val s1 = com.example.data.db.StudentEntity(
            id = 10,
            name = "Student A",
            phone = "111",
            email = "a@test.com",
            idProofNumber = "ID1",
            address = "Addr",
            avatarKey = "avatar_1",
            shift = "Morning",
            planType = "1 Month",
            startDateMillis = 0,
            endDateMillis = 1000,
            feeAmount = 1000.0,
            feePaid = 1000.0,
            emergencyContact = "123",
            notes = "Test"
        )
        val s2 = s1.copy(id = 20, name = "Student B", idProofNumber = "ID2")
        val s3 = s1.copy(id = 30, name = "Student C", idProofNumber = "ID3")

        val id1 = dao.insertStudent(s1)
        val id2 = dao.insertStudent(s2)
        val id3 = dao.insertStudent(s3)

        org.junit.Assert.assertNotNull(dao.getStudentById(id1))
        org.junit.Assert.assertNotNull(dao.getStudentById(id2))
        org.junit.Assert.assertNotNull(dao.getStudentById(id3))

        // Delete students id1 and id3
        dao.deleteStudentsByIds(listOf(id1, id3))

        assertNull(dao.getStudentById(id1))
        org.junit.Assert.assertNotNull(dao.getStudentById(id2))
        assertNull(dao.getStudentById(id3))

        db.close()
    }

    @Test
    fun `test database handles 5k users batch insertion and query`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.db.AppDatabase::class.java
        ).allowMainThreadQueries().build()
        val dao = db.studentDao()

        val bulkStudents = (1..5000).map { i ->
            com.example.data.db.StudentEntity(
                id = i.toLong(),
                name = "Student #$i",
                phone = "+91 90000${String.format(java.util.Locale.US, "%05d", i)}",
                email = "student$i@lib.com",
                idProofNumber = "LIB-5K-$i",
                address = "Hall A, Seat $i",
                avatarKey = "avatar_1",
                shift = "Full Day",
                planType = "1 Month",
                startDateMillis = 1000L * i,
                endDateMillis = 2000L * i,
                feeAmount = 1000.0,
                feePaid = 1000.0,
                emergencyContact = "999",
                notes = "5k scale member"
            )
        }

        // Test chunked insertion of 5,000 members
        dao.insertStudentsChunked(bulkStudents)

        val totalCount = dao.getStudentCount()
        assertEquals(5000, totalCount)

        // Test indexed query of a single member out of 5,000
        val student2500 = dao.getStudentById(2500L)
        org.junit.Assert.assertNotNull(student2500)
        assertEquals("Student #2500", student2500?.name)
        assertEquals("LIB-5K-2500", student2500?.idProofNumber)

        db.close()
    }
}
