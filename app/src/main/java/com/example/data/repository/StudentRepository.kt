package com.example.data.repository

import com.example.data.db.ReminderLogEntity
import com.example.data.db.StudentDao
import com.example.data.db.StudentEntity
import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StudentRepository(private val studentDao: StudentDao) {

    val allStudents: Flow<List<Student>> = studentDao.getAllStudentsFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    val allReminderLogs: Flow<List<ReminderLogEntity>> = studentDao.getAllReminderLogsFlow()

    suspend fun getStudentById(id: Long): Student? {
        return studentDao.getStudentById(id)?.toDomain()
    }

    suspend fun insertStudent(student: Student): Long {
        return studentDao.insertStudent(StudentEntity.fromDomain(student))
    }

    suspend fun updateStudent(student: Student) {
        studentDao.updateStudent(StudentEntity.fromDomain(student))
    }

    suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudentById(student.id)
    }

    suspend fun deleteStudentById(id: Long) {
        studentDao.deleteStudentById(id)
    }

    suspend fun deleteStudentsByIds(ids: List<Long>) {
        studentDao.deleteStudentsByIds(ids)
    }

    suspend fun insertStudentsChunked(students: List<Student>) {
        studentDao.insertStudentsChunked(students.map { StudentEntity.fromDomain(it) })
    }

    suspend fun getStudentCount(): Int = studentDao.getStudentCount()

    suspend fun clearBenchmarkStudents() {
        studentDao.clearBenchmarkStudents()
    }

    suspend fun logReminder(log: ReminderLogEntity): Long {
        return studentDao.insertReminderLog(log)
    }

    suspend fun clearReminderLogs() {
        studentDao.clearReminderLogs()
    }
}
