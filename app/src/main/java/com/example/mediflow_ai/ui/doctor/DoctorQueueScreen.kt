package com.example.mediflow_ai.ui.doctor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mediflow_ai.data.model.ConsultationStatus
import com.example.mediflow_ai.data.model.QueueItem
import com.example.mediflow_ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorQueueScreen(
    viewModel: DoctorQueueViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = TealPrimaryLight.copy(alpha = 0.3f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = "Doctor OPD",
                                    tint = TealPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Doctor OPD & Queue",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            Text(
                                text = "Real-time Patient Flow Management",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MedicalBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // 1. Doctor Profile & Availability Header
            item {
                DoctorProfileCard(
                    name = uiState.doctor.name,
                    specialty = uiState.doctor.specialty,
                    roomNumber = uiState.doctor.roomNumber,
                    isAvailable = uiState.doctor.isAvailable,
                    onToggleAvailability = { viewModel.toggleAvailability() }
                )
            }

            // 2. Queue KPI Metrics
            item {
                QueueMetricsRow(
                    waiting = uiState.waitingCount,
                    inConsultation = uiState.inConsultationCount,
                    completed = uiState.completedCount
                )
            }

            // 3. Active Consultation Spotlight Card
            val active = uiState.activeConsultation
            if (active != null) {
                item {
                    ActiveConsultationCard(
                        queueItem = active,
                        onComplete = { viewModel.completeConsultation(active.patient.patientId) }
                    )
                }
            }

            // 4. Filter Chips Row
            item {
                QueueFilterChips(
                    selectedFilter = uiState.selectedFilter,
                    totalCount = uiState.totalPatients,
                    waitingCount = uiState.waitingCount,
                    inConsultationCount = uiState.inConsultationCount,
                    completedCount = uiState.completedCount,
                    onSelectFilter = { viewModel.setFilter(it) }
                )
            }

            // 5. Patient Queue Items
            if (uiState.filteredQueueItems.isEmpty()) {
                item {
                    EmptyQueueCard(selectedFilter = uiState.selectedFilter)
                }
            } else {
                items(uiState.filteredQueueItems, key = { it.patient.patientId }) { item ->
                    PatientQueueCard(
                        queueItem = item,
                        isDoctorAvailable = uiState.doctor.isAvailable,
                        onStartConsultation = { viewModel.startConsultation(item.patient.patientId) },
                        onCompleteConsultation = { viewModel.completeConsultation(item.patient.patientId) },
                        onReturnToWaiting = { viewModel.returnToWaiting(item.patient.patientId) }
                    )
                }
            }
        }
    }
}

@Composable
fun DoctorProfileCard(
    name: String,
    specialty: String,
    roomNumber: String,
    isAvailable: Boolean,
    onToggleAvailability: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = SoftCyan,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "$specialty • $roomNumber",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Availability Switch
                Switch(
                    checked = isAvailable,
                    onCheckedChange = { onToggleAvailability() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = HospitalGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = WarningAmber
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Doctor Availability Status:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isAvailable) LightGreenBadge else LightAmberBadge
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isAvailable) HospitalGreen else WarningAmber)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAvailable) "Available for Consultation" else "On Break / Busy",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isAvailable) HospitalGreen else WarningAmber
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QueueMetricsRow(
    waiting: Int,
    inConsultation: Int,
    completed: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricCard(
            label = "Waiting",
            count = waiting,
            accentColor = WarningAmber,
            containerColor = LightAmberBadge,
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            label = "In Session",
            count = inConsultation,
            accentColor = MedicalBlue,
            containerColor = SoftCyan,
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            label = "Completed",
            count = completed,
            accentColor = HospitalGreen,
            containerColor = LightGreenBadge,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MetricCard(
    label: String,
    count: Int,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun ActiveConsultationCard(
    queueItem: QueueItem,
    onComplete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MedicalBlue.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SoftCyan
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MedicalBlue)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NOW IN CONSULTATION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MedicalBlue
                        )
                    }
                }

                Text(
                    text = "Started at ${queueItem.consultationStartTime ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MedicalBlue,
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#${queueItem.patient.tokenNumber}",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = queueItem.patient.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${queueItem.patient.patientId} • ${queueItem.patient.age} Yrs • ${queueItem.patient.gender} • ${queueItem.patient.bloodGroup}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MedicalBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Chief Complaints:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Text(
                        text = queueItem.patient.chiefComplaints,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HospitalGreen)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete Consultation", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun QueueFilterChips(
    selectedFilter: QueueFilter,
    totalCount: Int,
    waitingCount: Int,
    inConsultationCount: Int,
    completedCount: Int,
    onSelectFilter: (QueueFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            FilterChip(
                selected = selectedFilter == QueueFilter.ALL,
                onClick = { onSelectFilter(QueueFilter.ALL) },
                label = { Text("All ($totalCount)") },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TealPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == QueueFilter.WAITING,
                onClick = { onSelectFilter(QueueFilter.WAITING) },
                label = { Text("Waiting ($waitingCount)") },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WarningAmber,
                    selectedLabelColor = Color.White
                )
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == QueueFilter.IN_CONSULTATION,
                onClick = { onSelectFilter(QueueFilter.IN_CONSULTATION) },
                label = { Text("In Session ($inConsultationCount)") },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MedicalBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == QueueFilter.COMPLETED,
                onClick = { onSelectFilter(QueueFilter.COMPLETED) },
                label = { Text("Done ($completedCount)") },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = HospitalGreen,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun PatientQueueCard(
    queueItem: QueueItem,
    isDoctorAvailable: Boolean,
    onStartConsultation: () -> Unit,
    onCompleteConsultation: () -> Unit,
    onReturnToWaiting: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = when (queueItem.consultationStatus) {
                            ConsultationStatus.WAITING -> LightAmberBadge
                            ConsultationStatus.IN_CONSULTATION -> SoftCyan
                            ConsultationStatus.COMPLETED -> LightGreenBadge
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "#${queueItem.patient.tokenNumber}",
                                fontWeight = FontWeight.Bold,
                                color = when (queueItem.consultationStatus) {
                                    ConsultationStatus.WAITING -> WarningAmber
                                    ConsultationStatus.IN_CONSULTATION -> MedicalBlue
                                    ConsultationStatus.COMPLETED -> HospitalGreen
                                },
                                fontSize = 15.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = queueItem.patient.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${queueItem.patient.patientId} • ${queueItem.patient.age}y, ${queueItem.patient.gender}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (queueItem.consultationStatus) {
                        ConsultationStatus.WAITING -> LightAmberBadge
                        ConsultationStatus.IN_CONSULTATION -> SoftCyan
                        ConsultationStatus.COMPLETED -> LightGreenBadge
                    }
                ) {
                    Text(
                        text = queueItem.consultationStatus.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (queueItem.consultationStatus) {
                            ConsultationStatus.WAITING -> WarningAmber
                            ConsultationStatus.IN_CONSULTATION -> MedicalBlue
                            ConsultationStatus.COMPLETED -> HospitalGreen
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Complaints text
            Text(
                text = "Symptoms: ${queueItem.patient.chiefComplaints}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(10.dp))

            // Action row based on status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reg: ${queueItem.patient.registrationTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                when (queueItem.consultationStatus) {
                    ConsultationStatus.WAITING -> {
                        Button(
                            onClick = onStartConsultation,
                            enabled = isDoctorAvailable,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Consult", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    ConsultationStatus.IN_CONSULTATION -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onReturnToWaiting,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Hold", style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = onCompleteConsultation,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HospitalGreen),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Complete", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    ConsultationStatus.COMPLETED -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = HospitalGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Done at ${queueItem.consultationEndTime ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = HospitalGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyQueueCard(selectedFilter: QueueFilter) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No patients found in ${selectedFilter.name.lowercase().replace('_', ' ')}",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )
        }
    }
}
