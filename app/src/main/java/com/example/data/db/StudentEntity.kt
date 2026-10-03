package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Student

@Entity(
    tableName = "students",
    indices = [
        Index(value = ["endDateMillis"]),
        Index(value = ["name"]),
        Index(value = ["phone"]),
        Index(value = ["idProofNumber"]),
        Index(value = ["shift"]),
        Index(value = ["createdAt"])
    ]
)
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String,
    val idProofNumber: String,
    val address: String,
    val avatarKey: String,
    val photoUri: String? = null,
    val shift: String,
    val planType: String,
    val startDateMillis: Long,
    val endDateMillis: Long,
    val feeAmount: Double,
    val feePaid: Double,
    val emergencyContact: String,
    val notes: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Student = Student(
        id = id,
        name = name,
        phone = phone,
        email = email,
        idProofNumber = idProofNumber,
        address = address,
        avatarKey = avatarKey,
        photoUri = photoUri,
        shift = shift,
        planType = planType,
        startDateMillis = startDateMillis,
        endDateMillis = endDateMillis,
        feeAmount = feeAmount,
        feePaid = feePaid,
        emergencyContact = emergencyContact,
        notes = notes,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(student: Student): StudentEntity = StudentEntity(
            id = student.id,
            name = student.name,
            phone = student.phone,
            email = student.email,
            idProofNumber = student.idProofNumber,
            address = student.address,
            avatarKey = student.avatarKey,
            photoUri = student.photoUri,
            shift = student.shift,
            planType = student.planType,
            startDateMillis = student.startDateMillis,
            endDateMillis = student.endDateMillis,
            feeAmount = student.feeAmount,
            feePaid = student.feePaid,
            emergencyContact = student.emergencyContact,
            notes = student.notes,
            createdAt = student.createdAt
        )
    }
}

@Entity(
    tableName = "reminder_logs",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["studentId"])
    ]
)
data class ReminderLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val channel: String, // WHATSAPP, SMS, EMAIL, PUSH
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SENT"
)
