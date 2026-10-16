package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AttendanceResult
import com.example.data.model.CoachingBatches
import com.example.data.model.Department
import com.example.data.model.Student
import com.example.data.preferences.StudentPreferences
import com.example.data.repository.AttendanceRepository
import com.example.ui.camera.CameraQrScanner
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScanScreen(
    department: Department,
    student: Student?,
    repository: AttendanceRepository,
    prefs: StudentPreferences,
    onBack: () -> Unit,
    onOpenStudentSetup: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedBatch by remember {
        mutableStateOf(student?.batch ?: CoachingBatches.list[0])
    }
    var batchDropdownExpanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var attendanceResult by remember { mutableStateOf<AttendanceResult?>(null) }

    fun processAttendance(qrContent: String) {
        if (student == null) {
            onOpenStudentSetup()
            return
        }
        isLoading = true
        coroutineScope.launch {
            val result = repository.markAttendance(
                student = student,
                department = department,
                selectedBatch = if (department == Department.COACHING) selectedBatch else null,
                scannedQrCode = qrContent
            )
            isLoading = false
            attendanceResult = result
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${department.labelBangla} উপস্থিতি",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextDark,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (department == Department.LIBRARY) "দেয়ালে স্থাপিত লাইব্রেরি QR স্ক্যান করুন" else "ব্যাচ নির্বাচন করে কোচিং QR স্ক্যান করুন",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenStudentSetup) {
                        Icon(Icons.Default.Person, contentDescription = "Student Profile", tint = RoyalPurple)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        },
        containerColor = SurfaceLight
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // If Coaching, display Batch Dropdown
                if (department == Department.COACHING) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "কোচিং ব্যাচ নির্বাচন করুন:",
                                style = MaterialTheme.typography.titleSmall,
                                color = RoyalPurpleDark,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            ExposedDropdownMenuBox(
                                expanded = batchDropdownExpanded,
                                onExpandedChange = { batchDropdownExpanded = !batchDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedBatch,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchDropdownExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = RoyalPurple,
                                        unfocusedBorderColor = BorderMedium
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = batchDropdownExpanded,
                                    onDismissRequest = { batchDropdownExpanded = false }
                                ) {
                                    CoachingBatches.list.forEach { batchName ->
                                        DropdownMenuItem(
                                            text = { Text(batchName, fontWeight = FontWeight.Medium) },
                                            onClick = {
                                                selectedBatch = batchName
                                                batchDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Camera Scanner Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    CameraQrScanner(
                        onQrCodeDetected = { scannedCode ->
                            processAttendance(scannedCode)
                        }
                    )
                }

                // Bottom Panel with Student Identity Info and Instant Simulation Button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PureWhite)
                        .border(1.dp, BorderLight, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (student != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceLight, RoundedCornerShape(12.dp))
                                .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = student.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextDark,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ID: ${if (department == Department.LIBRARY) student.lid ?: student.id else student.cid ?: student.id}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                            TextButton(onClick = onOpenStudentSetup) {
                                Text("পরিবর্তন", color = RoyalPurpleDark, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = onOpenStudentSetup,
                            colors = ButtonDefaults.buttonColors(containerColor = RubyRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("প্রথমে আপনার শিক্ষার্থী আইডি সেটআপ করুন")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            val testToken = if (department == Department.LIBRARY) prefs.libraryWallToken else prefs.coachingWallToken
                            processAttendance(testToken)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (department == Department.LIBRARY) RubyRed else RoyalPurple),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (department == Department.LIBRARY) RubyRed else RoyalPurple),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("দেয়াল QR টেস্ট স্ক্যান (সিমুলেটর)", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Loading Overlay
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = RubyRed, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "সার্ভারে উপস্থিতি যাচাই হচ্ছে...",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Google Sheets-এ রেকর্ড সংরক্ষিত হচ্ছে",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Result Dialogs (Crisp White with High Contrast)
            attendanceResult?.let { result ->
                when (result) {
                    is AttendanceResult.Success -> {
                        Dialog(onDismissRequest = {
                            attendanceResult = null
                            onBack()
                        }) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = PureWhite,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, SuccessGreen),
                                modifier = Modifier.fillMaxWidth().padding(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(SuccessGreenBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Success",
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "উপস্থিতি সফল হয়েছে!",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = TextDark,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = result.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextDarkSecondary,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(SurfaceLight, RoundedCornerShape(12.dp))
                                            .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                                            .padding(14.dp)
                                    ) {
                                        Text(
                                            text = "শিক্ষার্থী: ${result.record.studentName}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextDark,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "বিভাগ: ${result.record.department.labelBangla}${if (result.record.batch != null) " (${result.record.batch})" else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = RoyalPurpleDark,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "তারিখ: ${result.record.date}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = "সার্ভার সময়: ${result.record.time}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextDark,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Button(
                                        onClick = {
                                            attendanceResult = null
                                            onBack()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) {
                                        Text("ঠিক আছে", color = PureWhite, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    is AttendanceResult.NotRegistered -> {
                        Dialog(onDismissRequest = { attendanceResult = null }) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = PureWhite,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, RubyRed),
                                modifier = Modifier.fillMaxWidth().padding(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(RubyRedBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.ErrorOutline,
                                            contentDescription = "Error",
                                            tint = RubyRed,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "অননুমোদিত শিক্ষার্থী!",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = RubyRed,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = result.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextDark,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:${result.contactPhone}")
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {}
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RubyRed),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = null, tint = PureWhite)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("ভর্তি হতে কল করুন: ${result.contactPhone}", color = PureWhite, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    TextButton(onClick = { attendanceResult = null }) {
                                        Text("বন্ধ করুন", color = TextMuted)
                                    }
                                }
                            }
                        }
                    }

                    is AttendanceResult.AlreadyAttended -> {
                        Dialog(onDismissRequest = { attendanceResult = null }) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = PureWhite,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, WarningAmber),
                                modifier = Modifier.fillMaxWidth().padding(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(WarningAmberBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = "Already Attended",
                                            tint = WarningAmber,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "ডুপ্লিকেট উপস্থিতি!",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = TextDark,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = result.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextDarkSecondary,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "আজকের উপস্থিতি গৃহীত হয়েছিল: ${result.lastTime}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Button(
                                        onClick = { attendanceResult = null },
                                        colors = ButtonDefaults.buttonColors(containerColor = TextDark),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) {
                                        Text("ঠিক আছে", color = PureWhite, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    is AttendanceResult.DepartmentMismatch -> {
                        Dialog(onDismissRequest = { attendanceResult = null }) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = PureWhite,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, RubyRed),
                                modifier = Modifier.fillMaxWidth().padding(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Block,
                                        contentDescription = "Mismatch",
                                        tint = RubyRed,
                                        modifier = Modifier.size(48.dp)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "বিভাগ অমিল!",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = RubyRed,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = result.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextDark,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Button(
                                        onClick = { attendanceResult = null },
                                        colors = ButtonDefaults.buttonColors(containerColor = TextDark),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("বন্ধ করুন", color = PureWhite)
                                    }
                                }
                            }
                        }
                    }

                    is AttendanceResult.InvalidQr, is AttendanceResult.Error -> {
                        val message = (result as? AttendanceResult.InvalidQr)?.message
                            ?: (result as? AttendanceResult.Error)?.message ?: "একটি ত্রুটি ঘটেছে"
                        Dialog(onDismissRequest = { attendanceResult = null }) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = PureWhite,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, RubyRed),
                                modifier = Modifier.fillMaxWidth().padding(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = RubyRed, modifier = Modifier.size(44.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("অবৈধ QR কোড", style = MaterialTheme.typography.titleMedium, color = RubyRed, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(message, style = MaterialTheme.typography.bodyMedium, color = TextDarkSecondary, textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { attendanceResult = null },
                                        colors = ButtonDefaults.buttonColors(containerColor = TextDark),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("পুনরায় চেষ্টা করুন", color = PureWhite)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
