package com.example.mediflow_ai.ui.beds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mediflow_ai.data.model.BedStatus
import com.example.mediflow_ai.data.model.HospitalBed
import com.example.mediflow_ai.data.model.Patient
import com.example.mediflow_ai.data.model.WardType
import com.example.mediflow_ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BedManagementScreen(
    viewModel: BedManagementViewModel = viewModel(),
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
                                    imageVector = Icons.Default.Hotel,
                                    contentDescription = "Hospital Beds",
                                    tint = TealPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Hospital Bed Desk",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            Text(
                                text = "Live Ward Occupancy & Allocations",
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // 1. KPI Occupancy Counters (Spans full width)
            item(span = { GridItemSpan(2) }) {
                BedMetricsRow(
                    total = uiState.totalBeds,
                    available = uiState.availableCount,
                    occupied = uiState.occupiedCount,
                    cleaning = uiState.cleaningCount
                )
            }

            // 2. Capacity Progress Gauge (Spans full width)
            item(span = { GridItemSpan(2) }) {
                OccupancyCapacityGauge(
                    percentage = uiState.occupancyPercentage,
                    available = uiState.availableCount
                )
            }

            // 3. Ward Filter Chips (Spans full width)
            item(span = { GridItemSpan(2) }) {
                WardFilterChips(
                    selectedWard = uiState.selectedWard,
                    onSelectWard = { viewModel.setWardFilter(it) }
                )
            }

            // 4. Bed Grid Items
            items(uiState.filteredBeds, key = { it.bedId }) { bed ->
                BedGridCard(
                    bed = bed,
                    onAdmitClick = { viewModel.openAdmissionDialog(bed) },
                    onDischargeClick = { viewModel.dischargePatient(bed.bedId) },
                    onSanitizeClick = { viewModel.markSanitized(bed.bedId) }
                )
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Admission Modal Dialog
    if (uiState.showAdmissionDialog && uiState.selectedBedForAdmission != null) {
        BedAdmissionDialog(
            bed = uiState.selectedBedForAdmission!!,
            patients = uiState.registeredPatients,
            onDismiss = { viewModel.closeAdmissionDialog() },
            onConfirmAdmission = { patientId, patientName, token ->
                viewModel.admitPatient(
                    bedId = uiState.selectedBedForAdmission!!.bedId,
                    patientId = patientId,
                    patientName = patientName,
                    tokenNumber = token
                )
            }
        )
    }
}

@Composable
fun BedMetricsRow(
    total: Int,
    available: Int,
    occupied: Int,
    cleaning: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BedStatBox("Total Beds", total, TealPrimaryDark, CardElevationLight, Modifier.weight(1f))
        BedStatBox("Available", available, HospitalGreen, LightGreenBadge, Modifier.weight(1f))
        BedStatBox("Occupied", occupied, Color(0xFFD32F2F), Color(0xFFFFEBEE), Modifier.weight(1f))
        BedStatBox("Cleaning", cleaning, WarningAmber, LightAmberBadge, Modifier.weight(1f))
    }
}

@Composable
fun BedStatBox(
    label: String,
    count: Int,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun OccupancyCapacityGauge(
    percentage: Int,
    available: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hospital Ward Capacity",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "$percentage% Occupied",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (percentage > 80) Color(0xFFD32F2F) else TealPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { percentage / 100f },
                color = if (percentage > 80) Color(0xFFD32F2F) else TealPrimary,
                trackColor = BorderLight,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$available vacant beds ready for immediate admission",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun WardFilterChips(
    selectedWard: WardType?,
    onSelectWard: (WardType?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = selectedWard == null,
                onClick = { onSelectWard(null) },
                label = { Text("All Wards") },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TealPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }

        WardType.values().forEach { ward ->
            item {
                FilterChip(
                    selected = selectedWard == ward,
                    onClick = { onSelectWard(ward) },
                    label = { Text(ward.name.replace('_', ' ')) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TealPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun BedGridCard(
    bed: HospitalBed,
    onAdmitClick: () -> Unit,
    onDischargeClick: () -> Unit,
    onSanitizeClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = when (bed.status) {
                BedStatus.AVAILABLE -> HospitalGreen.copy(alpha = 0.4f)
                BedStatus.OCCUPIED -> Color(0xFFEF9A9A)
                BedStatus.CLEANING -> WarningAmber.copy(alpha = 0.4f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Bed ID & Status Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bed.bedId,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Surface(
                    shape = CircleShape,
                    color = when (bed.status) {
                        BedStatus.AVAILABLE -> LightGreenBadge
                        BedStatus.OCCUPIED -> Color(0xFFFFEBEE)
                        BedStatus.CLEANING -> LightAmberBadge
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (bed.status) {
                                BedStatus.AVAILABLE -> Icons.Default.Check
                                BedStatus.OCCUPIED -> Icons.Default.Person
                                BedStatus.CLEANING -> Icons.Default.CleaningServices
                            },
                            contentDescription = null,
                            tint = when (bed.status) {
                                BedStatus.AVAILABLE -> HospitalGreen
                                BedStatus.OCCUPIED -> Color(0xFFD32F2F)
                                BedStatus.CLEANING -> WarningAmber
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = bed.ward.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Body info based on status
            when (bed.status) {
                BedStatus.AVAILABLE -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LightGreenBadge,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "READY FOR ADMISSION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HospitalGreen,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "₹${bed.chargePerDay.toInt()} / Day",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onAdmitClick,
                        colors = ButtonDefaults.buttonColors(containerColor = HospitalGreen),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Admit Patient", style = MaterialTheme.typography.labelMedium)
                    }
                }

                BedStatus.OCCUPIED -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFEBEE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = bed.occupiedByPatientName ?: "Admitted",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                            Text(
                                text = "Token #${bed.tokenNumber} • ${bed.admittedAt}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onDischargeClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Discharge", style = MaterialTheme.typography.labelMedium)
                    }
                }

                BedStatus.CLEANING -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LightAmberBadge,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "SANITIZATION IN PROGRESS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Bed sterilizing...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onSanitizeClick,
                        colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark Sanitized", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun BedAdmissionDialog(
    bed: HospitalBed,
    patients: List<Patient>,
    onDismiss: () -> Unit,
    onConfirmAdmission: (patientId: String, patientName: String, token: Int) -> Unit
) {
    var selectedPatientIndex by remember { mutableStateOf(0) }
    val safePatients = if (patients.isNotEmpty()) patients else listOf(
        Patient(
            patientId = "MF-2026-1013",
            fullName = "Amit Shinde",
            age = 52,
            gender = "Male",
            phoneNumber = "9822099887",
            bloodGroup = "A+",
            chiefComplaints = "Under Observation",
            departmentNeeded = "General Medicine",
            tokenNumber = 103,
            registrationTime = "09:45 AM"
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Admit Patient to Bed", fontWeight = FontWeight.Bold)
                Text(
                    text = "${bed.bedId} • ${bed.ward.displayName} (₹${bed.chargePerDay.toInt()}/day)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Select Patient to Admit:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                safePatients.forEachIndexed { index, p ->
                    FilterChip(
                        selected = selectedPatientIndex == index,
                        onClick = { selectedPatientIndex = index },
                        label = { Text("#${p.tokenNumber} - ${p.fullName} (${p.patientId})") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = safePatients[selectedPatientIndex]
                    onConfirmAdmission(p.patientId, p.fullName, p.tokenNumber)
                },
                colors = ButtonDefaults.buttonColors(containerColor = HospitalGreen)
            ) {
                Text("Confirm Admission")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
