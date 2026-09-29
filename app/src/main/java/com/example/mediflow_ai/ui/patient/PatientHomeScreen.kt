package com.example.mediflow_ai.ui.patient

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediflow_ai.data.model.ConsultationStatus
import com.example.mediflow_ai.data.repository.HospitalRepository
import com.example.mediflow_ai.ui.theme.HospitalGreen
import com.example.mediflow_ai.ui.theme.LightAmberBadge
import com.example.mediflow_ai.ui.theme.LightGreenBadge
import com.example.mediflow_ai.ui.theme.MedicalBackground
import com.example.mediflow_ai.ui.theme.MedicalBlue
import com.example.mediflow_ai.ui.theme.TealPrimary
import com.example.mediflow_ai.ui.theme.TextPrimary
import com.example.mediflow_ai.ui.theme.TextSecondary
import com.example.mediflow_ai.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHomeScreen(
    patientId: String,
    patientName: String,
    tokenNumber: Int?,
    onTokenGenerated: (Int) -> Unit = {}
) {
    val queueItems by HospitalRepository.queueItems.collectAsState()
    val doctorProfile by HospitalRepository.doctorProfile.collectAsState()

    var showTokenGenerationDialog by remember { mutableStateOf(false) }
    var selectedDepartment by remember { mutableStateOf("General Medicine") }
    var chiefComplaintsInput by remember { mutableStateOf("") }
    var isDeptDropdownExpanded by remember { mutableStateOf(false) }

    val departments = listOf(
        "General Medicine",
        "Cardiology",
        "Orthopedics",
        "Pediatrics",
        "ENT",
        "Dermatology",
        "Neurology"
    )

    // Resolve active queue item only if an active token is assigned
    val activeQueueItem = if (tokenNumber != null) {
        queueItems.find {
            it.patient.tokenNumber == tokenNumber && it.consultationStatus != ConsultationStatus.COMPLETED
        } ?: queueItems.find { it.patient.tokenNumber == tokenNumber }
    } else {
        null
    }
    val registeredPatient = HospitalRepository.getPatientByIdOrPhone(patientId)
    val patient = activeQueueItem?.patient ?: registeredPatient
    val consultStatus = activeQueueItem?.consultationStatus ?: ConsultationStatus.WAITING

    // Calculate current queue serving token and ahead count
    val currentlyServingItem = queueItems.find { it.consultationStatus == ConsultationStatus.IN_CONSULTATION }
    val currentServingToken = currentlyServingItem?.patient?.tokenNumber ?: 100

    val effectiveToken = tokenNumber
    val patientsAhead = if (effectiveToken != null) {
        queueItems.count {
            it.consultationStatus == ConsultationStatus.WAITING && it.patient.tokenNumber < effectiveToken
        }
    } else 0

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MedicalBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            if (effectiveToken != null) {
                // ==============================================================
                // 1. ACTIVE LIVE OPD TOKEN CARD (Patient has active check-in)
                // ==============================================================
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(1.5.dp, TealPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Your OPD Token",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "#$effectiveToken",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TealPrimary
                                )
                            }

                            // Status Pill Badge
                            val (statusText, badgeColor, textColor) = when (consultStatus) {
                                ConsultationStatus.WAITING -> Triple("In Queue", LightAmberBadge, WarningAmber)
                                ConsultationStatus.IN_CONSULTATION -> Triple("With Doctor", LightGreenBadge, HospitalGreen)
                                ConsultationStatus.COMPLETED -> Triple("Consulted", Color(0xFFE3F2FD), MedicalBlue)
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = badgeColor
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = when (consultStatus) {
                                            ConsultationStatus.WAITING -> Icons.Default.Schedule
                                            ConsultationStatus.IN_CONSULTATION -> Icons.Default.MedicalServices
                                            ConsultationStatus.COMPLETED -> Icons.Default.CheckCircle
                                        },
                                        contentDescription = null,
                                        tint = textColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = statusText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = textColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Queue Position & ETA
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Currently Serving",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Token #$currentServingToken",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Queue Position",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = if (consultStatus == ConsultationStatus.IN_CONSULTATION) {
                                        "Your Turn Now!"
                                    } else if (consultStatus == ConsultationStatus.COMPLETED) {
                                        "Finished"
                                    } else {
                                        "$patientsAhead patients ahead"
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (consultStatus == ConsultationStatus.IN_CONSULTATION) HospitalGreen else TealPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Queue Progress Bar
                        val progress = when (consultStatus) {
                            ConsultationStatus.WAITING -> if (patientsAhead == 0) 0.75f else 0.40f
                            ConsultationStatus.IN_CONSULTATION -> 0.88f
                            ConsultationStatus.COMPLETED -> 1.0f
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = TealPrimary,
                            trackColor = Color(0xFFE2F1F1)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Doctor Details
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF7FBFB), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${doctorProfile.name} • ${doctorProfile.specialty}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${doctorProfile.roomNumber} • ${patient?.departmentNeeded ?: "General Medicine"} OPD",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Registered Vitals & Medical Information Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Patient Clinical Intake Profile",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            VitalsItem(label = "Patient ID", value = patient?.patientId ?: patientId)
                            VitalsItem(label = "Age / Gender", value = "${patient?.age ?: 32} Y / ${patient?.gender ?: "Patient"}")
                            VitalsItem(label = "Blood Group", value = patient?.bloodGroup ?: "O+")
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Chief Complaints Recorded for Today's Visit:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Text(
                            text = patient?.chiefComplaints ?: "General OPD consultation & symptom checkup",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // OPD Waiting Area Notice
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF8E1), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Please remain seated in the OPD Waiting Area. The digital chime and nurse display will call Token #$effectiveToken when ready.",
                        fontSize = 13.sp,
                        color = Color(0xFF795548),
                        lineHeight = 18.sp
                    )
                }

            } else {
                // ==============================================================
                // 2. NO ACTIVE OPD TOKEN CARD (Account logged in without visit)
                // ==============================================================
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE0F2F1),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EventBusy,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "No Active OPD Visit Today",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Welcome back, $patientName. You are not currently registered in any doctor's queue. If you are visiting MediFlow Hospital today, generate an OPD token below to join the live clinical queue.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 19.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { showTokenGenerationDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generate OPD Token / Check In",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Patient Health Records Summary Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Hospital Records & Services",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Access your verified digital health records anytime using the bottom tabs.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        QuickServiceRow(
                            icon = Icons.Default.Description,
                            title = "Pharmacy & Prescriptions",
                            subtitle = "View doctor recommendations, dosage schedules, and dispensing status"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        QuickServiceRow(
                            icon = Icons.Default.Science,
                            title = "Diagnostic Laboratory Reports",
                            subtitle = "Check pathology, blood test, and imaging diagnostic findings"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        QuickServiceRow(
                            icon = Icons.Default.Payment,
                            title = "Billing & Payment Receipts",
                            subtitle = "Review settled OPD consultations, invoices, and payment receipts"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hospital Hours Notice
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "MediFlow OPD operates daily 08:00 AM – 08:00 PM. Emergency & Trauma Care is accessible 24x7 at Gate 1.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }

    // ==============================================================
    // 3. OPD TOKEN GENERATION MODAL DIALOG
    // ==============================================================
    if (showTokenGenerationDialog) {
        AlertDialog(
            onDismissRequest = { showTokenGenerationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Generate OPD Queue Token",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Select consultation specialty and provide your symptoms to receive an OPD queue token.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    // Department Dropdown
                    ExposedDropdownMenuBox(
                        expanded = isDeptDropdownExpanded,
                        onExpandedChange = { isDeptDropdownExpanded = !isDeptDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedDepartment,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Clinical Department") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDeptDropdownExpanded) },
                            shape = RoundedCornerShape(10.dp),
                            textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = TealPrimary,
                                unfocusedBorderColor = Color(0xFFB0BEC5)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        )
                        ExposedDropdownMenu(
                            expanded = isDeptDropdownExpanded,
                            onDismissRequest = { isDeptDropdownExpanded = false }
                        ) {
                            departments.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text(dept, fontSize = 14.sp) },
                                    onClick = {
                                        selectedDepartment = dept
                                        isDeptDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Chief Complaints Input
                    OutlinedTextField(
                        value = chiefComplaintsInput,
                        onValueChange = { chiefComplaintsInput = it },
                        label = { Text("Symptoms / Reason for Visit *") },
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = Color(0xFFB0BEC5)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Duty Doctor Banner
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE0F2F1),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Attending: ${doctorProfile.name}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                                Text(
                                    text = "${doctorProfile.roomNumber} • Live Queue Active",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val complaints = chiefComplaintsInput.trim().ifEmpty { "General consultation & clinical checkup" }
                        val generatedItem = HospitalRepository.generateOpdTokenForPatient(
                            patientId = patientId,
                            department = selectedDepartment,
                            symptoms = complaints
                        )
                        showTokenGenerationDialog = false
                        onTokenGenerated(generatedItem.patient.tokenNumber)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Issue Token",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showTokenGenerationDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

@Composable
private fun QuickServiceRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TealPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun VitalsItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}
