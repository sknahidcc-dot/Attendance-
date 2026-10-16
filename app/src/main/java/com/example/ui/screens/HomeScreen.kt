package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AttendanceRecord
import com.example.data.model.Department
import com.example.data.preferences.StudentPreferences
import com.example.data.repository.AttendanceRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: AttendanceRepository,
    prefs: StudentPreferences,
    onSelectDepartment: (Department) -> Unit,
    onOpenAdminLogin: () -> Unit,
    onOpenDeveloperProfile: () -> Unit,
    onOpenStudentSetup: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentStudent by remember { mutableStateOf(prefs.getStudent()) }
    val allRecords by repository.attendanceRecords.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }

    val todayDateBengali = remember {
        val cal = Calendar.getInstance()
        val bengaliMonths = listOf(
            "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
            "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
        )
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = bengaliMonths[cal.get(Calendar.MONTH)]
        val year = cal.get(Calendar.YEAR)
        "$day $month $year"
    }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    val todayRecords = remember(allRecords, todayStr) {
        allRecords.filter { it.date == todayStr }
    }

    val todayTotal = todayRecords.size
    val todayLibrary = todayRecords.count { it.department == Department.LIBRARY }
    val todayCoaching = todayRecords.count { it.department == Department.COACHING }

    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo_art),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Job Express",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(RubyRedBg, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = RubyRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "Attendance Management",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                },
                actions = {
                    // Refresh Button
                    IconButton(
                        onClick = {
                            isRefreshing = true
                            coroutineScope.launch {
                                repository.refreshData()
                                isRefreshing = false
                                Toast.makeText(context, "সর্বশেষ ডেটা সফলভাবে আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = RoyalPurple,
                            modifier = if (isRefreshing) Modifier.rotate(rotation) else Modifier
                        )
                    }

                    // Discreet top-right corner Developer Info Button
                    IconButton(
                        onClick = onOpenDeveloperProfile,
                        modifier = Modifier
                            .size(34.dp)
                            .background(RoyalPurpleBg, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About Developer",
                            tint = RoyalPurpleDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Discreet top-right corner Admin Button
                    IconButton(
                        onClick = onOpenAdminLogin,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(34.dp)
                            .background(SurfaceLight, CircleShape)
                            .border(1.dp, BorderMedium, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Login",
                            tint = TextDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        },
        containerColor = SurfaceLight
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
                // Active Student Profile Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x10000000))
                        .clickable { onOpenStudentSetup() },
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
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
                                .background(RoyalPurpleBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = RoyalPurple,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentStudent?.name ?: "শিক্ষার্থী পরিচয় সেট করুন",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentStudent != null)
                                    "${currentStudent!!.type.labelBangla} • ID: ${currentStudent!!.lid ?: currentStudent!!.cid ?: "N/A"}"
                                else "QR স্ক্যানের পূর্বে আপনার নাম ও আইডি সিলেক্ট করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(RoyalPurpleBg, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "পরিবর্তন",
                                color = RoyalPurpleDark,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                // Today Date Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "আজকের উপস্থিতি",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Text(
                            text = todayDateBengali,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextDark,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(SuccessGreenBg, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "● Google Sheets Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Two Main Action Cards: Red Gradient (Library) and Purple Gradient (Coaching)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Library Attendance Card (Red Gradient)
                    ModernGradientCard(
                        title = "লাইব্রেরি",
                        subtitle = "উপস্থিতি স্ক্যান",
                        icon = Icons.Default.LocalLibrary,
                        gradient = RedGradient,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectDepartment(Department.LIBRARY) }
                    )

                    // Coaching Attendance Card (Purple Gradient)
                    ModernGradientCard(
                        title = "কোচিং",
                        subtitle = "ব্যাচ ও স্ক্যান",
                        icon = Icons.Default.School,
                        gradient = PurpleGradient,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectDepartment(Department.COACHING) }
                    )
                }
            }

            // Today's Stats Overview (Clean White Card with Red and Purple Accents)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x10000000)),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "আজকের উপস্থিতির সংক্ষিপ্ত তথ্য",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextDark,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = RoyalPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            WhiteStatTile(
                                title = "মোট উপস্থিতি",
                                count = "$todayTotal",
                                accentColor = TextDark,
                                bgColor = SurfaceLight,
                                modifier = Modifier.weight(1f)
                            )
                            WhiteStatTile(
                                title = "লাইব্রেরি",
                                count = "$todayLibrary",
                                accentColor = RubyRed,
                                bgColor = RubyRedBg,
                                modifier = Modifier.weight(1f)
                            )
                            WhiteStatTile(
                                title = "কোচিং",
                                count = "$todayCoaching",
                                accentColor = RoyalPurple,
                                bgColor = RoyalPurpleBg,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Recent Attendance List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "আজকের সর্বশেষ উপস্থিতি",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextDark,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${todayRecords.size} টি রেকর্ড",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            if (todayRecords.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = TextLightMuted,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "আজকে এখনো কোনো উপস্থিতি নেওয়া হয়নি",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextDarkSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "উপরের লাইব্রেরি বা কোচিং বাটনে ক্লিক করে স্ক্যান করুন",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            } else {
                items(todayRecords.take(10)) { record ->
                    WhiteAttendanceRecordRow(record = record)
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ModernGradientCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(145.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x20000000))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = PureWhite, modifier = Modifier.size(26.dp))
                    }
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = PureWhite
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WhiteStatTile(
    title: String,
    count: String,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextMuted)
        Spacer(modifier = Modifier.height(4.dp))
        Text(count, style = MaterialTheme.typography.titleLarge, color = accentColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WhiteAttendanceRecordRow(record: AttendanceRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (record.department == Department.LIBRARY) RubyRedBg else RoyalPurpleBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (record.department == Department.LIBRARY) Icons.Default.LocalLibrary else Icons.Default.School,
                    contentDescription = null,
                    tint = if (record.department == Department.LIBRARY) RubyRed else RoyalPurple,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.studentName,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextDark,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${record.department.labelBangla}${if (record.batch != null) " (${record.batch})" else ""} • ${record.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = record.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDark,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .background(SuccessGreenBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = record.status,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
