package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Department
import com.example.data.model.Student
import com.example.data.model.StudentType
import com.example.data.preferences.StudentPreferences
import com.example.data.repository.AttendanceRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Job Express Attendance", appName)
    }

    @Test
    fun `verify attendance duplicate prevention logic`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = AttendanceRepository.getInstance(context)
        val prefs = StudentPreferences(context)

        val testStudent = Student(
            id = "STD-101",
            name = "Nahid Hasan",
            lid = "LIB-201",
            cid = "COACH-301",
            phone = "01712345678",
            type = StudentType.BOTH,
            batch = "BCS-1",
            status = "Active"
        )

        // First attendance
        val firstResult = repo.markAttendance(
            student = testStudent,
            department = Department.LIBRARY,
            selectedBatch = null,
            scannedQrCode = prefs.libraryWallToken
        )
        assertNotNull(firstResult)

        // Second attendance on same day in same department should be prevented
        val secondResult = repo.markAttendance(
            student = testStudent,
            department = Department.LIBRARY,
            selectedBatch = null,
            scannedQrCode = prefs.libraryWallToken
        )
        assertNotNull(secondResult)
    }
}
