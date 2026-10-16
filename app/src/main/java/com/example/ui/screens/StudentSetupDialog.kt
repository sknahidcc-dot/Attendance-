package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CoachingBatches
import com.example.data.model.Student
import com.example.data.model.StudentType
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSetupDialog(
    initialStudent: Student?,
    registeredStudents: List<Student>,
    onDismiss: () -> Unit,
    onSaveStudent: (Student) -> Unit
) {
    var selectedStudentMode by remember { mutableStateOf(if (registeredStudents.isNotEmpty()) "SELECT" else "CUSTOM") }
    var selectedStudentId by remember { mutableStateOf(initialStudent?.id ?: registeredStudents.firstOrNull()?.id ?: "") }

    var customName by remember { mutableStateOf(initialStudent?.name ?: "") }
    var customLid by remember { mutableStateOf(initialStudent?.lid ?: "") }
    var customCid by remember { mutableStateOf(initialStudent?.cid ?: "") }
    var customPhone by remember { mutableStateOf(initialStudent?.phone ?: "") }
    var customType by remember { mutableStateOf(initialStudent?.type ?: StudentType.BOTH) }
    var customBatch by remember { mutableStateOf(initialStudent?.batch ?: CoachingBatches.list[0]) }

    var batchMenuExpanded by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PureWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderMedium),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "আমার শিক্ষার্থী পরিচয়",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextDark,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Text(
                    text = "একবার সেটআপ করলে পরবর্তীতে QR স্ক্যানের সময় পুনরায় টাইপ করতে হবে না।",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceLight, RoundedCornerShape(10.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(10.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedStudentMode == "SELECT") RoyalPurple else SurfaceLight,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedStudentMode = "SELECT" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "তালিকা থেকে নির্বাচন",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedStudentMode == "SELECT") PureWhite else TextDarkSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedStudentMode == "CUSTOM") RoyalPurple else SurfaceLight,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedStudentMode = "CUSTOM" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "নতুন তথ্য দিন",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedStudentMode == "CUSTOM") PureWhite else TextDarkSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedStudentMode == "SELECT") {
                    Text(
                        text = "নিবন্ধিত শিক্ষার্থী তালিকা:",
                        style = MaterialTheme.typography.titleSmall,
                        color = RoyalPurpleDark,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    registeredStudents.forEach { student ->
                        val isSelected = student.id == selectedStudentId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(
                                    if (isSelected) RoyalPurpleBg else SurfaceLight,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) RoyalPurple else BorderLight,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedStudentId = student.id
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedStudentId = student.id },
                                colors = RadioButtonDefaults.colors(selectedColor = RoyalPurple)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = student.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextDark,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${student.type.labelBangla} • LID: ${student.lid ?: "নাই"} • CID: ${student.cid ?: "নাই"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("শিক্ষার্থীর নাম") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalPurple,
                            unfocusedBorderColor = BorderMedium
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customPhone,
                        onValueChange = { customPhone = it },
                        label = { Text("মোবাইল নম্বর") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalPurple,
                            unfocusedBorderColor = BorderMedium
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = typeMenuExpanded,
                        onExpandedChange = { typeMenuExpanded = !typeMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = customType.labelBangla,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("সদস্যপদের ধরন") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalPurple,
                                unfocusedBorderColor = BorderMedium
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = typeMenuExpanded,
                            onDismissRequest = { typeMenuExpanded = false }
                        ) {
                            StudentType.values().forEach { stType ->
                                DropdownMenuItem(
                                    text = { Text(stType.labelBangla) },
                                    onClick = {
                                        customType = stType
                                        typeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (customType == StudentType.LIBRARY || customType == StudentType.BOTH) {
                        OutlinedTextField(
                            value = customLid,
                            onValueChange = { customLid = it },
                            label = { Text("লাইব্রেরি আইডি (LID)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalPurple,
                                unfocusedBorderColor = BorderMedium
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (customType == StudentType.COACHING || customType == StudentType.BOTH) {
                        OutlinedTextField(
                            value = customCid,
                            onValueChange = { customCid = it },
                            label = { Text("কোচিং আইডি (CID)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalPurple,
                                unfocusedBorderColor = BorderMedium
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        ExposedDropdownMenuBox(
                            expanded = batchMenuExpanded,
                            onExpandedChange = { batchMenuExpanded = !batchMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = customBatch,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("কোচিং ব্যাচ") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchMenuExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RoyalPurple,
                                    unfocusedBorderColor = BorderMedium
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = batchMenuExpanded,
                                onDismissRequest = { batchMenuExpanded = false }
                            ) {
                                CoachingBatches.list.forEach { batchName ->
                                    DropdownMenuItem(
                                        text = { Text(batchName) },
                                        onClick = {
                                            customBatch = batchName
                                            batchMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (selectedStudentMode == "SELECT") {
                            val st = registeredStudents.find { it.id == selectedStudentId }
                            if (st != null) onSaveStudent(st)
                        } else {
                            val newSt = Student(
                                id = "STD-" + System.currentTimeMillis().toString().takeLast(4),
                                name = customName.ifBlank { "শিক্ষার্থী" },
                                lid = customLid.ifBlank { null },
                                cid = customCid.ifBlank { null },
                                phone = customPhone,
                                type = customType,
                                batch = customBatch,
                                status = "Active"
                            )
                            onSaveStudent(newSt)
                        }
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("সংরক্ষণ ও নিশ্চিত করুন", style = MaterialTheme.typography.titleMedium, color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
