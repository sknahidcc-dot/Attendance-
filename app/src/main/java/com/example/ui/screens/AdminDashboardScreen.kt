package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.data.model.*
import com.example.data.preferences.StudentPreferences
import com.example.data.repository.AttendanceRepository
import com.example.ui.components.HourlyHeatmapChart
import com.example.ui.components.QrCodeGenerator
import com.example.ui.components.SimpleBarChart
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    repository: AttendanceRepository,
    prefs: StudentPreferences,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("ওভারভিউ রিপোর্ট", "শিক্ষার্থীভিত্তিক", "লাইব্রেরি বিশ্লেষণ", "দেয়াল QR তৈরি", "সেটিংস")

    val allStudents by repository.students.collectAsState()
    val allRecords by repository.attendanceRecords.collectAsState()

    var selectedStudentForDetail by remember { mutableStateOf<Student?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("টিচার ও অ্যাডমিন ড্যাশবোর্ড", style = MaterialTheme.typography.titleLarge, color = TextDark, fontWeight = FontWeight.Bold)
                        Text("Job Express Attendance Management", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        prefs.isAdminLoggedIn = false
                        onLogout()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = RubyRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        },
        containerColor = SurfaceLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = PureWhite,
                contentColor = RoyalPurple,
                edgePadding = 12.dp
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selectedTab == index) RoyalPurple else TextMuted,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTab) {
                    0 -> OverallReportsTab(repository = repository, allRecords = allRecords)
                    1 -> StudentWiseReportsTab(
                        repository = repository,
                        students = allStudents,
                        onStudentClick = { selectedStudentForDetail = it }
                    )
                    2 -> LibraryTimeAnalysisTab(repository = repository, records = allRecords)
                    3 -> WallQrManagerTab(prefs = prefs)
                    4 -> AdminSettingsTab(prefs = prefs, repository = repository)
                }
            }
        }
    }

    // Detail Dialog for Student Personal History
    selectedStudentForDetail?.let { st ->
        StudentPersonalHistoryDialog(
            student = st,
            repository = repository,
            onDismiss = { selectedStudentForDetail = null }
        )
    }
}

@Composable
private fun OverallReportsTab(
    repository: AttendanceRepository,
    allRecords: List<AttendanceRecord>
) {
    var selectedRange by remember { mutableStateOf(ReportRange.WEEK) }
    var selectedDept by remember { mutableStateOf<Department?>(null) }

    val filteredRecords = remember(selectedRange, selectedDept, allRecords) {
        repository.getRecordsForRange(selectedRange, selectedDept)
    }

    val totalCount = filteredRecords.size
    val libraryCount = filteredRecords.count { it.department == Department.LIBRARY }
    val coachingCount = filteredRecords.count { it.department == Department.COACHING }

    val chartData = remember(filteredRecords) {
        val grouped = filteredRecords.groupBy { it.date }
        if (grouped.isEmpty()) {
            listOf("ডেটা নেই" to 0)
        } else {
            grouped.entries
                .sortedBy { it.key }
                .takeLast(7)
                .map { (dateStr, list) ->
                    val shortDate = try {
                        val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr)
                        SimpleDateFormat("dd MMM", Locale.US).format(d ?: Date())
                    } catch (e: Exception) {
                        dateStr.takeLast(5)
                    }
                    shortDate to list.size
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("সময়সীমা অনুযায়ী রিপোর্ট:", style = MaterialTheme.typography.titleMedium, color = TextDark, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ReportRange.values().take(3).forEach { range ->
                FilterChip(
                    selected = selectedRange == range,
                    onClick = { selectedRange = range },
                    label = { Text(range.labelBangla, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RoyalPurple,
                        selectedLabelColor = PureWhite
                    )
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ReportRange.values().drop(3).forEach { range ->
                FilterChip(
                    selected = selectedRange == range,
                    onClick = { selectedRange = range },
                    label = { Text(range.labelBangla, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RoyalPurple,
                        selectedLabelColor = PureWhite
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedDept == null,
                onClick = { selectedDept = null },
                label = { Text("উভয় বিভাগ (সকল)") },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RoyalPurple, selectedLabelColor = PureWhite)
            )
            FilterChip(
                selected = selectedDept == Department.LIBRARY,
                onClick = { selectedDept = Department.LIBRARY },
                label = { Text("শুধু লাইব্রেরি") },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RubyRed, selectedLabelColor = PureWhite)
            )
            FilterChip(
                selected = selectedDept == Department.COACHING,
                onClick = { selectedDept = Department.COACHING },
                label = { Text("শুধু কোচিং") },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RoyalPurple, selectedLabelColor = PureWhite)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary KPI Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WhiteStatCard(
                title = "মোট উপস্থিতি",
                value = "$totalCount জন",
                accentColor = TextDark,
                bgColor = PureWhite,
                modifier = Modifier.weight(1f)
            )
            WhiteStatCard(
                title = "লাইব্রেরি",
                value = "$libraryCount জন",
                accentColor = RubyRed,
                bgColor = RubyRedBg,
                modifier = Modifier.weight(1f)
            )
            WhiteStatCard(
                title = "কোচিং",
                value = "$coachingCount জন",
                accentColor = RoyalPurple,
                bgColor = RoyalPurpleBg,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "উপস্থিতি ট্রেন্ড গ্রাফ (${selectedRange.labelBangla}):",
            style = MaterialTheme.typography.titleMedium,
            color = TextDark,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        SimpleBarChart(data = chartData)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "উপস্থিতির তালিকা (${filteredRecords.size} টি রেকর্ড):",
            style = MaterialTheme.typography.titleMedium,
            color = TextDark,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        filteredRecords.take(15).forEach { rec ->
            WhiteAttendanceRecordRow(record = rec)
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun StudentWiseReportsTab(
    repository: AttendanceRepository,
    students: List<Student>,
    onStudentClick: (Student) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(searchQuery, students) {
        if (searchQuery.isBlank()) students
        else {
            students.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                (it.lid?.contains(searchQuery, ignoreCase = true) == true) ||
                (it.cid?.contains(searchQuery, ignoreCase = true) == true) ||
                it.phone.contains(searchQuery)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("নাম, LID, CID বা মোবাইল নম্বর দিয়ে খুঁজুন...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = RoyalPurple) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RoyalPurple,
                unfocusedBorderColor = BorderMedium
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "শিক্ষার্থীর ব্যক্তিগত রিপোর্ট দেখতে নামের ওপর ক্লিক করুন:",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredList) { st ->
                val summary = remember(st.id) { repository.getStudentSummary(st.id) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStudentClick(st) },
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (st.type == StudentType.LIBRARY) RubyRedBg else RoyalPurpleBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = st.name.take(1),
                                style = MaterialTheme.typography.titleLarge,
                                color = if (st.type == StudentType.LIBRARY) RubyRed else RoyalPurple,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = st.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${st.type.labelBangla} • LID: ${st.lid ?: "-"} • CID: ${st.cid ?: "-"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${summary?.totalAttendance ?: 0} দিন",
                                style = MaterialTheme.typography.titleMedium,
                                color = RoyalPurpleDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "বিস্তারিত >",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextLightMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryTimeAnalysisTab(
    repository: AttendanceRepository,
    records: List<AttendanceRecord>
) {
    val stats = remember(records) { repository.getLibraryHourlyStats(records) }
    val peakSlot = stats.maxByOrNull { it.count }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(RoyalPurpleBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = RoyalPurple, modifier = Modifier.size(28.dp))
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text("সর্বোচ্চ উপস্থিতির সময় (Peak Hour)", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Text(
                        text = "${peakSlot?.label ?: "সকাল 09 AM"} (${peakSlot?.period ?: "সকাল"})",
                        style = MaterialTheme.typography.titleLarge,
                        color = RoyalPurpleDark,
                        fontWeight = FontWeight.Bold
                    )
                    Text("এই সময়ে সর্বাধিক শিক্ষার্থী লাইব্রেরিতে প্রবেশ করে।", style = MaterialTheme.typography.bodySmall, color = TextDarkSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        HourlyHeatmapChart(stats = stats)
    }
}

@Composable
private fun WallQrManagerTab(prefs: StudentPreferences) {
    val context = LocalContext.current
    var libToken by remember { mutableStateOf(prefs.libraryWallToken) }
    var coachToken by remember { mutableStateOf(prefs.coachingWallToken) }
    var selectedQrType by remember { mutableStateOf("LIBRARY") }

    val currentContent = if (selectedQrType == "LIBRARY") libToken else coachToken
    val currentTitle = if (selectedQrType == "LIBRARY") "লাইব্রেরি দেয়াল QR কোড" else "কোচিং দেয়াল QR কোড"

    val qrBitmap = remember(currentContent) {
        QrCodeGenerator.generateQrBitmap(
            content = currentContent,
            size = 512,
            foregroundColor = android.graphics.Color.BLACK,
            backgroundColor = android.graphics.Color.WHITE
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("দেয়ালে প্রদর্শনের জন্য QR কোড পরিচালনা", style = MaterialTheme.typography.titleLarge, color = TextDark, fontWeight = FontWeight.Bold)
        Text("এই QR কোডগুলো প্রিন্ট করে লাইব্রেরি ও কোচিংয়ের দেয়ালে টানিয়ে দিন।", style = MaterialTheme.typography.bodySmall, color = TextMuted, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PureWhite, RoundedCornerShape(12.dp))
                .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (selectedQrType == "LIBRARY") RubyRed else PureWhite, RoundedCornerShape(10.dp))
                    .clickable { selectedQrType = "LIBRARY" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("লাইব্রেরি QR", style = MaterialTheme.typography.titleSmall, color = if (selectedQrType == "LIBRARY") PureWhite else TextDark, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (selectedQrType == "COACHING") RoyalPurple else PureWhite, RoundedCornerShape(10.dp))
                    .clickable { selectedQrType = "COACHING" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("কোচিং QR", style = MaterialTheme.typography.titleSmall, color = if (selectedQrType == "COACHING") PureWhite else TextDark, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Printable QR Card with Clean White
        Card(
            modifier = Modifier
                .size(280.dp)
                .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x15000000))
                .border(2.dp, if (selectedQrType == "LIBRARY") RubyRed else RoyalPurple, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Job Express",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextDark,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selectedQrType == "LIBRARY") RubyRed else RoyalPurpleDark,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                qrBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Wall QR Code",
                        modifier = Modifier
                            .size(170.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "স্ক্যান করে উপস্থিতি দিন",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    qrBitmap?.let { bmp ->
                        shareOrSaveQr(context, bmp, currentTitle)
                    }
                },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = PureWhite)
                Spacer(modifier = Modifier.width(6.dp))
                Text("শেয়ার / প্রিন্ট করুন", color = PureWhite, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    val newToken = "JOBEXPRESS_${selectedQrType}_" + UUID.randomUUID().toString().take(8).uppercase()
                    if (selectedQrType == "LIBRARY") {
                        prefs.libraryWallToken = newToken
                        libToken = newToken
                    } else {
                        prefs.coachingWallToken = newToken
                        coachToken = newToken
                    }
                },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RubyRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, RubyRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = RubyRed)
                Spacer(modifier = Modifier.width(6.dp))
                Text("নতুন সিকিউর QR", color = RubyRed, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AdminSettingsTab(
    prefs: StudentPreferences,
    repository: AttendanceRepository
) {
    val coroutineScope = rememberCoroutineScope()
    var scriptUrl by remember { mutableStateOf(prefs.googleScriptUrl) }
    var saveStatus by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Google Sheets ও Apps Script ব্যাকএন্ড", style = MaterialTheme.typography.titleLarge, color = TextDark, fontWeight = FontWeight.Bold)
                Text("Google Apps Script Web App URL টি এখানে পেস্ট করে সরাসরি স্প্রেডশিটের সাথে যুক্ত করুন।", style = MaterialTheme.typography.bodySmall, color = TextMuted)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = scriptUrl,
                    onValueChange = {
                        scriptUrl = it
                        saveStatus = null
                    },
                    label = { Text("Google Apps Script Web App URL") },
                    placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalPurple,
                        unfocusedBorderColor = BorderMedium
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            prefs.googleScriptUrl = scriptUrl
                            saveStatus = "URL সফলভাবে সংরক্ষিত হয়েছে!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            isTestingConnection = true
                            coroutineScope.launch {
                                val success = repository.refreshData()
                                isTestingConnection = false
                                saveStatus = if (success) "কানেকশন সফল! গুগল শিট লাইভ যুক্ত হয়েছে।" else "সার্ভার রেসপন্স চেক করা হয়েছে।"
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RubyRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RubyRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(color = RubyRed, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("কানেকশন টেস্ট", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (saveStatus != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(saveStatus!!, color = SuccessGreen, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StudentPersonalHistoryDialog(
    student: Student,
    repository: AttendanceRepository,
    onDismiss: () -> Unit
) {
    val records = remember(student.id) {
        repository.attendanceRecords.value.filter { it.studentId == student.id }
    }
    val summary = remember(student.id) { repository.getStudentSummary(student.id) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderMedium),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(student.name, style = MaterialTheme.typography.titleLarge, color = TextDark, fontWeight = FontWeight.Bold)
                        Text(
                            "${student.type.labelBangla} • ${student.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = RoyalPurpleDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WhiteStatCard("মোট উপস্থিতি", "${summary?.totalAttendance ?: 0} দিন", TextDark, SurfaceLight, Modifier.weight(1f))
                    WhiteStatCard("লাইব্রেরি", "${summary?.libraryAttendance ?: 0} দিন", RubyRed, RubyRedBg, Modifier.weight(1f))
                    WhiteStatCard("কোচিং", "${summary?.coachingAttendance ?: 0} দিন", RoyalPurple, RoyalPurpleBg, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("উপস্থিতির তারিখ ও সময় বিস্তারিত:", style = MaterialTheme.typography.titleMedium, color = TextDark, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (records.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("এখনো কোনো উপস্থিতি পাওয়া যায়নি", color = TextMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(records) { rec ->
                            WhiteAttendanceRecordRow(record = rec)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = TextDark)
                ) {
                    Text("বন্ধ করুন", color = PureWhite)
                }
            }
        }
    }
}

@Composable
private fun WhiteStatCard(
    title: String,
    value: String,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = accentColor, fontWeight = FontWeight.Bold)
    }
}

private fun shareOrSaveQr(context: Context, bitmap: Bitmap, title: String) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "wall_qr_${System.currentTimeMillis()}.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "QR কোড প্রিন্ট বা সেভ করুন"))
    } catch (e: Exception) {
        // fallback
    }
}
