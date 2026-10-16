package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.data.model.Department
import com.example.data.preferences.StudentPreferences
import com.example.data.repository.AttendanceRepository
import com.example.ui.screens.*
import com.example.ui.theme.JobExpressTheme
import com.example.ui.theme.PureWhite

enum class AppScreen {
    SPLASH,
    HOME,
    SCAN,
    ADMIN_DASHBOARD,
    DEVELOPER_PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = AttendanceRepository.getInstance(this)
        val prefs = StudentPreferences(this)

        setContent {
            JobExpressTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PureWhite
                ) {
                    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
                    var selectedDepartment by remember { mutableStateOf(Department.LIBRARY) }

                    var showStudentSetupDialog by remember { mutableStateOf(false) }
                    var showAdminLoginDialog by remember { mutableStateOf(false) }

                    var currentStudent by remember { mutableStateOf(prefs.getStudent()) }
                    val allStudents by repository.students.collectAsState()

                    var pendingDepartmentLaunch by remember { mutableStateOf<Department?>(null) }
                    val cameraPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            pendingDepartmentLaunch?.let { dept ->
                                selectedDepartment = dept
                                currentScreen = AppScreen.SCAN
                            }
                        } else {
                            Toast.makeText(
                                this,
                                "QR স্ক্যান করার জন্য ক্যামেরার অনুমতি প্রয়োজন।",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    fun launchScanner(dept: Department) {
                        val hasCameraPermission = ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasCameraPermission) {
                            selectedDepartment = dept
                            currentScreen = AppScreen.SCAN
                        } else {
                            pendingDepartmentLaunch = dept
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }

                    when (currentScreen) {
                        AppScreen.SPLASH -> {
                            SplashScreen(
                                onSplashFinished = {
                                    currentScreen = AppScreen.HOME
                                }
                            )
                        }

                        AppScreen.HOME -> {
                            HomeScreen(
                                repository = repository,
                                prefs = prefs,
                                onSelectDepartment = { dept ->
                                    launchScanner(dept)
                                },
                                onOpenAdminLogin = {
                                    if (prefs.isAdminLoggedIn) {
                                        currentScreen = AppScreen.ADMIN_DASHBOARD
                                    } else {
                                        showAdminLoginDialog = true
                                    }
                                },
                                onOpenDeveloperProfile = {
                                    currentScreen = AppScreen.DEVELOPER_PROFILE
                                },
                                onOpenStudentSetup = {
                                    showStudentSetupDialog = true
                                }
                            )
                        }

                        AppScreen.SCAN -> {
                            BackHandler {
                                currentScreen = AppScreen.HOME
                            }
                            AttendanceScanScreen(
                                department = selectedDepartment,
                                student = currentStudent,
                                repository = repository,
                                prefs = prefs,
                                onBack = { currentScreen = AppScreen.HOME },
                                onOpenStudentSetup = { showStudentSetupDialog = true }
                            )
                        }

                        AppScreen.ADMIN_DASHBOARD -> {
                            BackHandler {
                                currentScreen = AppScreen.HOME
                            }
                            AdminDashboardScreen(
                                repository = repository,
                                prefs = prefs,
                                onBack = { currentScreen = AppScreen.HOME },
                                onLogout = { currentScreen = AppScreen.HOME }
                            )
                        }

                        AppScreen.DEVELOPER_PROFILE -> {
                            BackHandler {
                                currentScreen = AppScreen.HOME
                            }
                            DeveloperProfileScreen(
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                        }
                    }

                    if (showStudentSetupDialog) {
                        StudentSetupDialog(
                            initialStudent = currentStudent,
                            registeredStudents = allStudents,
                            onDismiss = { showStudentSetupDialog = false },
                            onSaveStudent = { updatedStudent ->
                                prefs.saveStudent(updatedStudent)
                                currentStudent = updatedStudent
                                repository.addStudent(updatedStudent)
                            }
                        )
                    }

                    if (showAdminLoginDialog) {
                        AdminLoginDialog(
                            prefs = prefs,
                            onDismiss = { showAdminLoginDialog = false },
                            onLoginSuccess = {
                                showAdminLoginDialog = false
                                currentScreen = AppScreen.ADMIN_DASHBOARD
                            }
                        )
                    }
                }
            }
        }
    }
}
