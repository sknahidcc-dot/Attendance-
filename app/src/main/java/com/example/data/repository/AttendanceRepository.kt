package com.example.data.repository

import android.content.Context
import com.example.data.model.*
import com.example.data.preferences.StudentPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AttendanceRepository private constructor(private val context: Context) {

    private val prefs = StudentPreferences(context)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.US)
    private val displayDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.US)

    // In-memory / persisted local stores
    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _attendanceRecords = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val attendanceRecords: StateFlow<List<AttendanceRecord>> = _attendanceRecords.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val initialStudents = listOf(
            Student(
                id = "STD-101",
                name = "Nahid Hasan",
                lid = "LIB-201",
                cid = "COACH-301",
                phone = "01712345678",
                type = StudentType.BOTH,
                batch = "BCS-1",
                status = "Active",
                registrationDate = "2026-01-10"
            ),
            Student(
                id = "STD-102",
                name = "Ariful Islam",
                lid = "LIB-202",
                cid = null,
                phone = "01823456789",
                type = StudentType.LIBRARY,
                batch = null,
                status = "Active",
                registrationDate = "2026-01-15"
            ),
            Student(
                id = "STD-103",
                name = "Sadia Sultana",
                lid = null,
                cid = "COACH-302",
                phone = "01934567890",
                type = StudentType.COACHING,
                batch = "BANK",
                status = "Active",
                registrationDate = "2026-02-01"
            ),
            Student(
                id = "STD-104",
                name = "Tanvir Ahmed",
                lid = "LIB-203",
                cid = "COACH-303",
                phone = "01745678901",
                type = StudentType.BOTH,
                batch = "NTRCA",
                status = "Active",
                registrationDate = "2026-02-10"
            ),
            Student(
                id = "STD-105",
                name = "Nusrat Jahan",
                lid = "LIB-204",
                cid = null,
                phone = "01656789012",
                type = StudentType.LIBRARY,
                batch = null,
                status = "Active",
                registrationDate = "2026-02-15"
            ),
            Student(
                id = "STD-106",
                name = "Mahmudur Rahman",
                lid = null,
                cid = "COACH-304",
                phone = "01567890123",
                type = StudentType.COACHING,
                batch = "GOVERNMENT 12-20",
                status = "Active",
                registrationDate = "2026-03-01"
            ),
            Student(
                id = "STD-107",
                name = "Farzana Akter",
                lid = "LIB-205",
                cid = "COACH-305",
                phone = "01378901234",
                type = StudentType.BOTH,
                batch = "EXAM BATCH",
                status = "Active",
                registrationDate = "2026-03-10"
            ),
            Student(
                id = "STD-108",
                name = "Mehedi Hasan",
                lid = null,
                cid = "COACH-306",
                phone = "01889012345",
                type = StudentType.COACHING,
                batch = "BCS-2",
                status = "Active",
                registrationDate = "2026-03-20"
            )
        )
        _students.value = initialStudents

        // If no student is saved on this device yet, save the first one as default
        if (prefs.getStudent() == null) {
            prefs.saveStudent(initialStudents[0])
        }

        // Seed some realistic historical records for reports
        val seedRecords = mutableListOf<AttendanceRecord>()
        val cal = Calendar.getInstance()
        val todayStr = dateFormat.format(cal.time)

        // Today's records
        seedRecords.add(
            AttendanceRecord(
                id = UUID.randomUUID().toString(),
                studentId = "STD-102",
                studentName = "Ariful Islam",
                lid = "LIB-202",
                cid = null,
                department = Department.LIBRARY,
                batch = null,
                date = todayStr,
                time = "08:30:15 AM",
                timestamp = System.currentTimeMillis() - 7200000,
                status = "উপস্থিত"
            )
        )
        seedRecords.add(
            AttendanceRecord(
                id = UUID.randomUUID().toString(),
                studentId = "STD-103",
                studentName = "Sadia Sultana",
                lid = null,
                cid = "COACH-302",
                department = Department.COACHING,
                batch = "BANK",
                date = todayStr,
                time = "09:45:20 AM",
                timestamp = System.currentTimeMillis() - 3600000,
                status = "উপস্থিত"
            )
        )
        seedRecords.add(
            AttendanceRecord(
                id = UUID.randomUUID().toString(),
                studentId = "STD-104",
                studentName = "Tanvir Ahmed",
                lid = "LIB-203",
                cid = "COACH-303",
                department = Department.LIBRARY,
                batch = null,
                date = todayStr,
                time = "10:15:00 AM",
                timestamp = System.currentTimeMillis() - 1800000,
                status = "উপস্থিত"
            )
        )

        // Historical sample data over the past 30 days
        val sampleHours = listOf("08:15:00 AM", "09:30:00 AM", "11:20:00 AM", "02:45:00 PM", "04:10:00 PM", "06:30:00 PM")
        for (i in 1..25) {
            val histCal = Calendar.getInstance()
            histCal.add(Calendar.DAY_OF_YEAR, -i)
            val histDate = dateFormat.format(histCal.time)
            
            // Random students attendance
            for (st in initialStudents.shuffled().take(4)) {
                if (st.type == StudentType.LIBRARY || st.type == StudentType.BOTH) {
                    seedRecords.add(
                        AttendanceRecord(
                            id = UUID.randomUUID().toString(),
                            studentId = st.id,
                            studentName = st.name,
                            lid = st.lid,
                            cid = st.cid,
                            department = Department.LIBRARY,
                            batch = null,
                            date = histDate,
                            time = sampleHours.random(),
                            timestamp = histCal.timeInMillis,
                            status = "উপস্থিত"
                        )
                    )
                }
                if (st.type == StudentType.COACHING || st.type == StudentType.BOTH) {
                    seedRecords.add(
                        AttendanceRecord(
                            id = UUID.randomUUID().toString(),
                            studentId = st.id,
                            studentName = st.name,
                            lid = st.lid,
                            cid = st.cid,
                            department = Department.COACHING,
                            batch = st.batch ?: "BCS-1",
                            date = histDate,
                            time = sampleHours.random(),
                            timestamp = histCal.timeInMillis,
                            status = "উপস্থিত"
                        )
                    )
                }
            }
        }
        _attendanceRecords.value = seedRecords
    }

    suspend fun markAttendance(
        student: Student,
        department: Department,
        selectedBatch: String?,
        scannedQrCode: String
    ): AttendanceResult = withContext(Dispatchers.IO) {
        val scriptUrl = prefs.googleScriptUrl

        // 1. Verify QR code token
        val expectedToken = if (department == Department.LIBRARY) {
            prefs.libraryWallToken
        } else {
            prefs.coachingWallToken
        }

        val isValidWallQr = scannedQrCode.trim() == expectedToken.trim() ||
                scannedQrCode.contains(if (department == Department.LIBRARY) "LIBRARY" else "COACHING", ignoreCase = true) ||
                scannedQrCode.startsWith("JOBEXPRESS_")

        if (!isValidWallQr) {
            return@withContext AttendanceResult.InvalidQr(
                "স্ক্যান করা QR কোডটি সঠিক নয়। অনুগ্রহ করে দেয়ালে স্থাপিত সঠিক ${department.labelBangla} QR কোড স্ক্যান করুন।"
            )
        }

        // 2. Verify Student Registration
        val registeredStudent = _students.value.find { 
            it.id.equals(student.id, ignoreCase = true) || 
            (it.lid != null && it.lid.equals(student.lid, ignoreCase = true)) ||
            (it.cid != null && it.cid.equals(student.cid, ignoreCase = true)) ||
            (it.phone.isNotBlank() && it.phone == student.phone)
        }

        if (registeredStudent == null || registeredStudent.status != "Active") {
            return@withContext AttendanceResult.NotRegistered(
                "আপনি এই প্রতিষ্ঠানের নিবন্ধিত সদস্য নন। ভর্তি হতে যোগাযোগ করুন: 01351835060"
            )
        }

        // 3. Department Membership Check
        when (department) {
            Department.LIBRARY -> {
                if (registeredStudent.type != StudentType.LIBRARY && registeredStudent.type != StudentType.BOTH) {
                    return@withContext AttendanceResult.DepartmentMismatch(
                        "আপনি লাইব্রেরির নিবন্ধিত শিক্ষার্থী নন। আপনি শুধুমাত্র ${registeredStudent.type.labelBangla} হিসেবে নিবন্ধিত।"
                    )
                }
            }
            Department.COACHING -> {
                if (registeredStudent.type != StudentType.COACHING && registeredStudent.type != StudentType.BOTH) {
                    return@withContext AttendanceResult.DepartmentMismatch(
                        "আপনি কোচিংয়ের নিবন্ধিত শিক্ষার্থী নন। আপনি শুধুমাত্র ${registeredStudent.type.labelBangla} হিসেবে নিবন্ধিত।"
                    )
                }
            }
        }

        // 4. Duplicate Check for same department on same day
        val now = Calendar.getInstance()
        val todayStr = dateFormat.format(now.time)
        val existingToday = _attendanceRecords.value.find {
            it.studentId == registeredStudent.id &&
            it.department == department &&
            it.date == todayStr
        }

        if (existingToday != null) {
            return@withContext AttendanceResult.AlreadyAttended(
                "আপনি আজকের দিনে ইতিমধ্যে ${department.labelBangla} বিভাগে উপস্থিতি দিয়েছেন!",
                existingToday.time
            )
        }

        val currentTimeStr = timeFormat.format(now.time)
        val finalBatch = if (department == Department.COACHING) {
            selectedBatch ?: registeredStudent.batch ?: "BCS-1"
        } else null

        val newRecord = AttendanceRecord(
            id = UUID.randomUUID().toString(),
            studentId = registeredStudent.id,
            studentName = registeredStudent.name,
            lid = registeredStudent.lid,
            cid = registeredStudent.cid,
            department = department,
            batch = finalBatch,
            date = todayStr,
            time = currentTimeStr,
            timestamp = now.timeInMillis,
            status = "উপস্থিত"
        )

        // Try syncing with Google Apps Script if URL provided
        if (scriptUrl.isNotBlank()) {
            try {
                val jsonPayload = JSONObject().apply {
                    put("action", "markAttendance")
                    put("studentId", registeredStudent.id)
                    put("studentName", registeredStudent.name)
                    put("lid", registeredStudent.lid ?: "")
                    put("cid", registeredStudent.cid ?: "")
                    put("department", department.name)
                    put("batch", finalBatch ?: "")
                    put("date", todayStr)
                    put("time", currentTimeStr)
                    put("token", scannedQrCode)
                }

                val body = jsonPayload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(scriptUrl)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                val resJson = try { JSONObject(responseBody) } catch (e: Exception) { null }
                
                if (resJson != null && resJson.optBoolean("success", false) == false) {
                    val errMsg = resJson.optString("message", "সার্ভার এরর")
                    if (errMsg.contains("already", ignoreCase = true) || errMsg.contains("উপস্থিতি দিয়েছেন", ignoreCase = true)) {
                        return@withContext AttendanceResult.AlreadyAttended(errMsg, currentTimeStr)
                    }
                    if (errMsg.contains("নিবন্ধিত", ignoreCase = true) || errMsg.contains("not registered", ignoreCase = true)) {
                        return@withContext AttendanceResult.NotRegistered(errMsg)
                    }
                }
            } catch (e: Exception) {
                // If offline or network error, locally keep record
            }
        }

        // Save locally in list
        val updated = mutableListOf<AttendanceRecord>()
        updated.add(0, newRecord)
        updated.addAll(_attendanceRecords.value)
        _attendanceRecords.value = updated

        AttendanceResult.Success(
            record = newRecord,
            message = "${department.labelBangla} বিভাগে আপনার উপস্থিতি সফলভাবে গ্রহণ করা হয়েছে!"
        )
    }

    suspend fun refreshData(): Boolean = withContext(Dispatchers.IO) {
        val scriptUrl = prefs.googleScriptUrl
        if (scriptUrl.isBlank()) return@withContext true

        try {
            val request = Request.Builder()
                .url("$scriptUrl?action=getAllData")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: ""
                val json = JSONObject(bodyStr)
                if (json.optBoolean("success", false)) {
                    // Parse records if returned
                    val recordsArray = json.optJSONArray("records")
                    if (recordsArray != null) {
                        val parsed = mutableListOf<AttendanceRecord>()
                        for (i in 0 until recordsArray.length()) {
                            val obj = recordsArray.getJSONObject(i)
                            parsed.add(
                                AttendanceRecord(
                                    id = obj.optString("id", UUID.randomUUID().toString()),
                                    studentId = obj.optString("studentId"),
                                    studentName = obj.optString("studentName"),
                                    lid = obj.optString("lid").takeIf { it.isNotBlank() },
                                    cid = obj.optString("cid").takeIf { it.isNotBlank() },
                                    department = try {
                                        Department.valueOf(obj.optString("department"))
                                    } catch (e: Exception) {
                                        Department.LIBRARY
                                    },
                                    batch = obj.optString("batch").takeIf { it.isNotBlank() },
                                    date = obj.optString("date"),
                                    time = obj.optString("time"),
                                    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                                    status = obj.optString("status", "উপস্থিত")
                                )
                            )
                        }
                        _attendanceRecords.value = parsed
                    }
                    return@withContext true
                }
            }
        } catch (e: Exception) {
            // Error handling
        }
        return@withContext false
    }

    fun addStudent(student: Student) {
        val list = _students.value.toMutableList()
        val index = list.indexOfFirst { it.id == student.id }
        if (index >= 0) {
            list[index] = student
        } else {
            list.add(student)
        }
        _students.value = list
    }

    fun getRecordsForRange(range: ReportRange, department: Department?): List<AttendanceRecord> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -range.days)
        val cutoff = cal.timeInMillis

        return _attendanceRecords.value.filter { record ->
            val matchesDept = department == null || record.department == department
            val matchesTime = record.timestamp >= cutoff
            matchesDept && matchesTime
        }
    }

    fun getStudentSummary(studentId: String): StudentAttendanceSummary? {
        val student = _students.value.find { it.id == studentId } ?: return null
        val records = _attendanceRecords.value.filter { it.studentId == studentId }
        val libCount = records.count { it.department == Department.LIBRARY }
        val coachCount = records.count { it.department == Department.COACHING }
        val last = records.maxByOrNull { it.timestamp }
        val rate = if (student.type == StudentType.BOTH) {
            (records.size.toFloat() / 60f).coerceAtMost(1f) * 100
        } else {
            (records.size.toFloat() / 30f).coerceAtMost(1f) * 100
        }

        return StudentAttendanceSummary(
            student = student,
            totalAttendance = records.size,
            libraryAttendance = libCount,
            coachingAttendance = coachCount,
            lastAttendance = last,
            attendanceRate = rate
        )
    }

    fun getLibraryHourlyStats(records: List<AttendanceRecord> = _attendanceRecords.value): List<TimeSlotStat> {
        val libRecords = records.filter { it.department == Department.LIBRARY }
        val hourMap = mutableMapOf<Int, Int>()
        for (h in 6..22) {
            hourMap[h] = 0
        }

        val parser12 = SimpleDateFormat("hh:mm:ss a", Locale.US)
        for (r in libRecords) {
            try {
                val date = parser12.parse(r.time)
                if (date != null) {
                    val cal = Calendar.getInstance().apply { time = date }
                    val h = cal.get(Calendar.HOUR_OF_DAY)
                    hourMap[h] = (hourMap[h] ?: 0) + 1
                }
            } catch (e: Exception) {
                // fallback hour based on timestamp
                val cal = Calendar.getInstance().apply { timeInMillis = r.timestamp }
                val h = cal.get(Calendar.HOUR_OF_DAY)
                hourMap[h] = (hourMap[h] ?: 0) + 1
            }
        }

        return hourMap.map { (h, count) ->
            val period = when {
                h in 6..11 -> "সকাল"
                h in 12..16 -> "দুপুর"
                else -> "সন্ধ্যা"
            }
            val label = when {
                h == 12 -> "12 PM"
                h > 12 -> "${h - 12} PM"
                else -> "$h AM"
            }
            TimeSlotStat(h, label, count, period)
        }.sortedBy { it.hour }
    }

    companion object {
        @Volatile
        private var instance: AttendanceRepository? = null

        fun getInstance(context: Context): AttendanceRepository {
            return instance ?: synchronized(this) {
                instance ?: AttendanceRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
