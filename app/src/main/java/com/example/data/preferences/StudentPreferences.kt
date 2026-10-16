package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Student
import com.example.data.model.StudentType

class StudentPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("job_express_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_STUDENT_ID = "student_id"
        private const val KEY_STUDENT_NAME = "student_name"
        private const val KEY_STUDENT_LID = "student_lid"
        private const val KEY_STUDENT_CID = "student_cid"
        private const val KEY_STUDENT_PHONE = "student_phone"
        private const val KEY_STUDENT_TYPE = "student_type"
        private const val KEY_STUDENT_BATCH = "student_batch"
        private const val KEY_GOOGLE_SCRIPT_URL = "google_script_url"
        private const val KEY_LIBRARY_WALL_TOKEN = "library_wall_token"
        private const val KEY_COACHING_WALL_TOKEN = "coaching_wall_token"
        private const val KEY_IS_ADMIN_LOGGED_IN = "is_admin_logged_in"
        
        // Default secure tokens for wall QRs
        const val DEFAULT_LIBRARY_TOKEN = "JOBEXPRESS_LIB_WALL_2026_TOKEN"
        const val DEFAULT_COACHING_TOKEN = "JOBEXPRESS_COACH_WALL_2026_TOKEN"
    }

    fun saveStudent(student: Student) {
        prefs.edit()
            .putString(KEY_STUDENT_ID, student.id)
            .putString(KEY_STUDENT_NAME, student.name)
            .putString(KEY_STUDENT_LID, student.lid)
            .putString(KEY_STUDENT_CID, student.cid)
            .putString(KEY_STUDENT_PHONE, student.phone)
            .putString(KEY_STUDENT_TYPE, student.type.name)
            .putString(KEY_STUDENT_BATCH, student.batch)
            .apply()
    }

    fun getStudent(): Student? {
        val id = prefs.getString(KEY_STUDENT_ID, null) ?: return null
        val name = prefs.getString(KEY_STUDENT_NAME, "") ?: ""
        val lid = prefs.getString(KEY_STUDENT_LID, null)
        val cid = prefs.getString(KEY_STUDENT_CID, null)
        val phone = prefs.getString(KEY_STUDENT_PHONE, "") ?: ""
        val typeStr = prefs.getString(KEY_STUDENT_TYPE, StudentType.BOTH.name) ?: StudentType.BOTH.name
        val type = try {
            StudentType.valueOf(typeStr)
        } catch (e: Exception) {
            StudentType.BOTH
        }
        val batch = prefs.getString(KEY_STUDENT_BATCH, null)

        return Student(
            id = id,
            name = name,
            lid = lid,
            cid = cid,
            phone = phone,
            type = type,
            batch = batch
        )
    }

    fun clearStudent() {
        prefs.edit()
            .remove(KEY_STUDENT_ID)
            .remove(KEY_STUDENT_NAME)
            .remove(KEY_STUDENT_LID)
            .remove(KEY_STUDENT_CID)
            .remove(KEY_STUDENT_PHONE)
            .remove(KEY_STUDENT_TYPE)
            .remove(KEY_STUDENT_BATCH)
            .apply()
    }

    var googleScriptUrl: String
        get() = prefs.getString(KEY_GOOGLE_SCRIPT_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_GOOGLE_SCRIPT_URL, value.trim()).apply()

    var libraryWallToken: String
        get() = prefs.getString(KEY_LIBRARY_WALL_TOKEN, DEFAULT_LIBRARY_TOKEN) ?: DEFAULT_LIBRARY_TOKEN
        set(value) = prefs.edit().putString(KEY_LIBRARY_WALL_TOKEN, value).apply()

    var coachingWallToken: String
        get() = prefs.getString(KEY_COACHING_WALL_TOKEN, DEFAULT_COACHING_TOKEN) ?: DEFAULT_COACHING_TOKEN
        set(value) = prefs.edit().putString(KEY_COACHING_WALL_TOKEN, value).apply()

    var isAdminLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_ADMIN_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_ADMIN_LOGGED_IN, value).apply()
}
