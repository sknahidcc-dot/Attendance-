package com.example.data.model

enum class StudentType(val labelBangla: String) {
    LIBRARY("লাইব্রেরি স্টুডেন্ট"),
    COACHING("কোচিং স্টুডেন্ট"),
    BOTH("লাইব্রেরি ও কোচিং স্টুডেন্ট")
}

enum class Department(val labelBangla: String) {
    LIBRARY("লাইব্রেরি"),
    COACHING("কোচিং")
}

data class Student(
    val id: String,
    val name: String,
    val lid: String? = null,
    val cid: String? = null,
    val phone: String,
    val type: StudentType,
    val batch: String? = null,
    val status: String = "Active", // "Active", "Inactive"
    val registrationDate: String = ""
)

data class AttendanceRecord(
    val id: String,
    val studentId: String,
    val studentName: String,
    val lid: String? = null,
    val cid: String? = null,
    val department: Department,
    val batch: String? = null,
    val date: String,      // YYYY-MM-DD
    val time: String,      // HH:mm:ss
    val timestamp: Long,
    val status: String = "উপস্থিত"
)

sealed class AttendanceResult {
    data class Success(val record: AttendanceRecord, val message: String) : AttendanceResult()
    data class AlreadyAttended(val message: String, val lastTime: String) : AttendanceResult()
    data class NotRegistered(val message: String, val contactPhone: String = "01351835060") : AttendanceResult()
    data class DepartmentMismatch(val message: String) : AttendanceResult()
    data class InvalidQr(val message: String) : AttendanceResult()
    data class Error(val message: String) : AttendanceResult()
}

object CoachingBatches {
    val list = listOf(
        "BCS-1",
        "BCS-2",
        "NTRCA",
        "BANK",
        "GOVERNMENT 12-20",
        "EXAM BATCH"
    )
}

enum class ReportRange(val labelBangla: String, val days: Int) {
    TODAY("প্রতিদিনের উপস্থিতি", 1),
    WEEK("সাপ্তাহিক উপস্থিতি", 7),
    MONTH("মাসিক উপস্থিতি", 30),
    THREE_MONTHS("৩ মাসের উপস্থিতি", 90),
    SIX_MONTHS("৬ মাসের উপস্থিতি", 180),
    YEAR("১ বছরের উপস্থিতি", 365)
}

data class TimeSlotStat(
    val hour: Int,
    val label: String,
    val count: Int,
    val period: String // "সকাল", "দুপুর", "সন্ধ্যা"
)

data class StudentAttendanceSummary(
    val student: Student,
    val totalAttendance: Int,
    val libraryAttendance: Int,
    val coachingAttendance: Int,
    val lastAttendance: AttendanceRecord?,
    val attendanceRate: Float
)
