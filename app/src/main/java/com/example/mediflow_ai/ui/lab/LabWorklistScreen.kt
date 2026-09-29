package com.example.mediflow_ai.ui.lab

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mediflow_ai.data.model.LabOrder
import com.example.mediflow_ai.data.model.LabTestStatus
import com.example.mediflow_ai.data.model.StandardLabCatalog
import com.example.mediflow_ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabWorklistScreen(
    viewModel: LabWorklistViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNewOrderDialog by remember { mutableStateOf(false) }

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
                                    imageVector = Icons.Default.Science,
                                    contentDescription = "Diagnostics Lab",
                                    tint = TealPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pathology & Lab Desk",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            Text(
                                text = "Diagnostic Orders & Real-time Reports",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewOrderDialog = true },
                containerColor = TealPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Order Lab Test", fontWeight = FontWeight.SemiBold) }
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
            // 1. Lab Overview Header
            item {
                LabHeaderBanner()
            }

            // 2. Metric Counters Row
            item {
                LabMetricsRow(
                    requested = uiState.requestedCount,
                    processing = uiState.processingCount,
                    completed = uiState.completedCount
                )
            }

            // 3. Filter Chips
            item {
                LabFilterRow(
                    selectedFilter = uiState.selectedFilter,
                    totalCount = uiState.totalCount,
                    requestedCount = uiState.requestedCount,
                    processingCount = uiState.processingCount,
                    completedCount = uiState.completedCount,
                    onSelectFilter = { viewModel.setFilter(it) }
                )
            }

            // 4. Lab Orders List
            if (uiState.filteredOrders.isEmpty()) {
                item {
                    EmptyLabOrdersCard(filter = uiState.selectedFilter)
                }
            } else {
                items(uiState.filteredOrders, key = { it.orderId }) { order ->
                    LabOrderCard(
                        order = order,
                        onCollectSample = { viewModel.collectSample(order.orderId) },
                        onEnterResults = { viewModel.openResultDialog(order) }
                    )
                }
            }

            // Bottom spacer for FloatingActionButton
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Result Entry Dialog
    if (uiState.showResultDialog && uiState.selectedOrderForResults != null) {
        ResultEntryDialog(
            order = uiState.selectedOrderForResults!!,
            resultText = uiState.resultInput,
            onResultChange = { viewModel.onResultInputChange(it) },
            onDismiss = { viewModel.closeResultDialog() },
            onSubmit = { viewModel.submitResults() }
        )
    }

    // New Order Dialog (Doctor test ordering simulation)
    if (showNewOrderDialog) {
        NewLabOrderDialog(
            patients = uiState.registeredPatients,
            onDismiss = { showNewOrderDialog = false },
            onSubmit = { patientId, patientName, token, testName, category ->
                viewModel.orderNewTest(patientId, patientName, token, testName, category)
                showNewOrderDialog = false
            }
        )
    }
}

@Composable
fun LabHeaderBanner() {
    Surface(
        color = SoftCyan,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Central Pathology & Testing",
                    style = MaterialTheme.typography.titleMedium,
                    color = TealPrimaryDark,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Real-time test handoff: Doctor orders → Sample collection → Results notification.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.Biotech,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun LabMetricsRow(
    requested: Int,
    processing: Int,
    completed: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LabMetricBox(
            label = "Requested",
            count = requested,
            accentColor = WarningAmber,
            containerColor = LightAmberBadge,
            modifier = Modifier.weight(1f)
        )
        LabMetricBox(
            label = "Processing",
            count = processing,
            accentColor = MedicalBlue,
            containerColor = SoftCyan,
            modifier = Modifier.weight(1f)
        )
        LabMetricBox(
            label = "Completed",
            count = completed,
            accentColor = HospitalGreen,
            containerColor = LightGreenBadge,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun LabMetricBox(
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
fun LabFilterRow(
    selectedFilter: LabFilter,
    totalCount: Int,
    requestedCount: Int,
    processingCount: Int,
    completedCount: Int,
    onSelectFilter: (LabFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            FilterChip(
                selected = selectedFilter == LabFilter.ALL,
                onClick = { onSelectFilter(LabFilter.ALL) },
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
                selected = selectedFilter == LabFilter.REQUESTED,
                onClick = { onSelectFilter(LabFilter.REQUESTED) },
                label = { Text("Requested ($requestedCount)") },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WarningAmber,
                    selectedLabelColor = Color.White
                )
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == LabFilter.PROCESSING,
                onClick = { onSelectFilter(LabFilter.PROCESSING) },
                label = { Text("Processing ($processingCount)") },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MedicalBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == LabFilter.COMPLETED,
                onClick = { onSelectFilter(LabFilter.COMPLETED) },
                label = { Text("Completed ($completedCount)") },
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
fun LabOrderCard(
    order: LabOrder,
    onCollectSample: () -> Unit,
    onEnterResults: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Patient & Order ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = SoftCyan,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "#${order.tokenNumber}",
                                fontWeight = FontWeight.Bold,
                                color = TealPrimaryDark,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = order.patientName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${order.patientId} • ${order.orderId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Status Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (order.status) {
                        LabTestStatus.REQUESTED -> LightAmberBadge
                        LabTestStatus.PROCESSING -> SoftCyan
                        LabTestStatus.COMPLETED -> LightGreenBadge
                    }
                ) {
                    Text(
                        text = order.status.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (order.status) {
                            LabTestStatus.REQUESTED -> WarningAmber
                            LabTestStatus.PROCESSING -> MedicalBlue
                            LabTestStatus.COMPLETED -> HospitalGreen
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Test Name & Category
            Surface(
                color = MedicalBackground,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = order.testName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TealPrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = order.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = TealPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ordered by: ${order.prescribedByDoctor} at ${order.orderedAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Results Display if Completed
            if (order.status == LabTestStatus.COMPLETED && order.resultSummary != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = LightGreenBadge.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HospitalGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AssignmentTurnedIn,
                                contentDescription = null,
                                tint = HospitalGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Diagnostic Findings (Ready at ${order.completedAt}):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HospitalGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = order.resultSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = HospitalGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Doctor & Patient Notified",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HospitalGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (order.sampleCollected) "Sample: Collected" else "Sample: Pending",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (order.sampleCollected) HospitalGreen else WarningAmber,
                    fontWeight = FontWeight.SemiBold
                )

                when (order.status) {
                    LabTestStatus.REQUESTED -> {
                        Button(
                            onClick = onCollectSample,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.Checklist,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Collect Sample", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    LabTestStatus.PROCESSING -> {
                        Button(
                            onClick = onEnterResults,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Enter Results & Finalize", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    LabTestStatus.COMPLETED -> {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = LightGreenBadge
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = HospitalGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Report Finalized",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HospitalGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultEntryDialog(
    order: LabOrder,
    resultText: String,
    onResultChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Input Diagnostic Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${order.testName} • ${order.patientName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Enter test values or diagnostic remarks. Finalizing will notify doctor and patient.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                OutlinedTextField(
                    value = resultText,
                    onValueChange = onResultChange,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    label = { Text("Result / Findings") },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = HospitalGreen,
                        unfocusedBorderColor = Color(0xFFB0BEC5),
                        focusedLabelColor = HospitalGreen,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = HospitalGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = HospitalGreen)
            ) {
                Text("Finalize & Notify")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLabOrderDialog(
    patients: List<com.example.mediflow_ai.data.model.Patient>,
    onDismiss: () -> Unit,
    onSubmit: (patientId: String, patientName: String, token: Int, testName: String, category: String) -> Unit
) {
    var selectedPatientIndex by remember { mutableStateOf(0) }
    var selectedTestIndex by remember { mutableStateOf(0) }

    val safePatients = if (patients.isNotEmpty()) patients else listOf(
        com.example.mediflow_ai.data.model.Patient(
            patientId = "MF-2026-1011",
            fullName = "Sunil Joshi",
            age = 45,
            gender = "Male",
            phoneNumber = "9822011234",
            bloodGroup = "B+",
            chiefComplaints = "High blood pressure",
            departmentNeeded = "General Medicine",
            tokenNumber = 101,
            registrationTime = "09:15 AM"
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Doctor: Order Laboratory Test",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Select Patient:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                // Patient chips
                safePatients.forEachIndexed { index, patient ->
                    FilterChip(
                        selected = selectedPatientIndex == index,
                        onClick = { selectedPatientIndex = index },
                        label = { Text("#${patient.tokenNumber} - ${patient.fullName}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Select Test from Catalog:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                StandardLabCatalog.items.take(4).forEachIndexed { index, test ->
                    FilterChip(
                        selected = selectedTestIndex == index,
                        onClick = { selectedTestIndex = index },
                        label = { Text("${test.name} (${test.category})") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = safePatients[selectedPatientIndex]
                    val t = StandardLabCatalog.items[selectedTestIndex]
                    onSubmit(p.patientId, p.fullName, p.tokenNumber, t.name, t.category)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Send Order to Lab")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EmptyLabOrdersCard(filter: LabFilter) {
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
                imageVector = Icons.Default.Science,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No lab orders in ${filter.name.lowercase()}",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )
        }
    }
}
