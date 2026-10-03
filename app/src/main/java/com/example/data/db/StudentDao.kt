package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY endDateMillis ASC")
    fun getAllStudentsFlow(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    fun getStudentByIdFlow(id: Long): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): StudentEntity?

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getStudentCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<StudentEntity>)

    @Transaction
    suspend fun insertStudentsChunked(students: List<StudentEntity>) {
        students.chunked(500).forEach { chunk ->
            insertAll(chunk)
        }
    }

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Long)

    @Transaction
    @Query("DELETE FROM students WHERE id IN (:ids)")
    suspend fun deleteStudentsByIds(ids: List<Long>)

    @Query("DELETE FROM students WHERE id > 7")
    suspend fun clearBenchmarkStudents()

    @Query("DELETE FROM students")
    suspend fun clearAllStudents()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminderLog(log: ReminderLogEntity): Long

    @Query("SELECT * FROM reminder_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllReminderLogsFlow(): Flow<List<ReminderLogEntity>>

    @Query("DELETE FROM reminder_logs")
    suspend fun clearReminderLogs()
}
