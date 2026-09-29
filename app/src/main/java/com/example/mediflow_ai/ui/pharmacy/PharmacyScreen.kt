package com.example.mediflow_ai.ui.pharmacy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mediflow_ai.data.model.MedicineItem
import com.example.mediflow_ai.data.model.PharmacyOrder
import com.example.mediflow_ai.data.model.PharmacyOrderStatus
import com.example.mediflow_ai.data.model.PrescriptionItem
import com.example.mediflow_ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyScreen(
    viewModel: PharmacyViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNewPrescriptionDialog by remember { mutableStateOf(false) }

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
                                    imageVector = Icons.Default.LocalPharmacy,
                                    contentDescription = "Hospital Pharmacy",
                                    tint = TealPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Hospital Pharmacy Desk",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            Text(
                                text = "Dispensing & Automated Stock Control",
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
                onClick = { showNewPrescriptionDialog = true },
                containerColor = TealPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Prescription", fontWeight = FontWeight.SemiBold) }
            )
        },
        containerColor = MedicalBackground
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. KPI Counters Row
            PharmacyMetricsRow(
                pending = uiState.pendingCount,
                dispensed = uiState.dispensedCount,
                lowStock = uiState.lowStockAlertCount,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // 2. Low Stock Alert Banner (Visible when medicines fall <= minThreshold)
            if (uiState.lowStockMedicines.isNotEmpty()) {
                LowStockAlertBanner(
                    lowStockMeds = uiState.lowStockMedicines,
                    onRestockClick = { med -> viewModel.openRestockDialog(med) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // 3. Tab Row: Prescriptions vs Inventory
            TabRow(
                selectedTabIndex = uiState.activeTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = TealPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = uiState.activeTab == PharmacyTab.PRESCRIPTIONS,
                    onClick = { viewModel.selectTab(PharmacyTab.PRESCRIPTIONS) },
                    text = {
                        Text(
                            text = "Prescriptions (${uiState.allOrders.size})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = uiState.activeTab == PharmacyTab.INVENTORY,
                    onClick = { viewModel.selectTab(PharmacyTab.INVENTORY) },
                    text = {
                        Text(
                            text = "Stock Inventory (${uiState.inventory.size})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }

            // 4. Tab Content
            when (uiState.activeTab) {
                PharmacyTab.PRESCRIPTIONS -> {
                    PrescriptionsTabContent(
                        orders = uiState.filteredOrders,
                        selectedFilter = uiState.selectedFilter,
                        totalCount = uiState.allOrders.size,
                        pendingCount = uiState.pendingCount,
                        dispensedCount = uiState.dispensedCount,
                        onSelectFilter = { viewModel.setFilter(it) },
                        onDispenseOrder = { viewModel.dispenseOrder(it) }
                    )
                }

                PharmacyTab.INVENTORY -> {
                    InventoryTabContent(
                        inventory = uiState.inventory,
                        onRestock = { viewModel.openRestockDialog(it) }
                    )
                }
            }
        }
    }

    // Restock Dialog
    if (uiState.showRestockDialog && uiState.selectedMedicineForRestock != null) {
        RestockDialog(
            medicine = uiState.selectedMedicineForRestock!!,
            unitsInput = uiState.restockUnitsInput,
            onUnitsChange = { viewModel.onRestockUnitsChange(it) },
            onDismiss = { viewModel.closeRestockDialog() },
            onConfirm = { viewModel.submitRestock() }
        )
    }

    // New Prescription Dialog (Doctor ordering simulation)
    if (showNewPrescriptionDialog) {
        NewPrescriptionDialog(
            patients = uiState.registeredPatients,
            inventory = uiState.inventory,
            onDismiss = { showNewPrescriptionDialog = false },
            onSubmit = { patientId, patientName, token, items ->
                viewModel.createPrescription(patientId, patientName, token, items)
                showNewPrescriptionDialog = false
            }
        )
    }
}

@Composable
fun PharmacyMetricsRow(
    pending: Int,
    dispensed: Int,
    lowStock: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = LightAmberBadge,
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$pending",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarningAmber
                )
                Text(
                    text = "Pending Orders",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = LightGreenBadge,
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$dispensed",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = HospitalGreen
                )
                Text(
                    text = "Dispensed Today",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (lowStock > 0) Color(0xFFFFEBEE) else CardElevationLight,
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$lowStock",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (lowStock > 0) Color(0xFFD32F2F) else TextSecondary
                )
                Text(
                    text = "Low Stock Alert",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (lowStock > 0) Color(0xFFD32F2F) else TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LowStockAlertBanner(
    lowStockMeds: List<MedicineItem>,
    onRestockClick: (MedicineItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alert",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LOW STOCK WARNING (${lowStockMeds.size} Items)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD32F2F)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White
                ) {
                    Text(
                        text = "Admin & Pharmacist Alerted",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Show low medicines
            lowStockMeds.forEach { med ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• ${med.name}: only ${med.stockUnits} left (Min: ${med.minThreshold})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFC62828)
                    )
                    TextButton(
                        onClick = { onRestockClick(med) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Restock +", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}

@Composable
fun PrescriptionsTabContent(
    orders: List<PharmacyOrder>,
    selectedFilter: PharmacyOrderFilter,
    totalCount: Int,
    pendingCount: Int,
    dispensedCount: Int,
    onSelectFilter: (PharmacyOrderFilter) -> Unit,
    onDispenseOrder: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedFilter == PharmacyOrderFilter.ALL,
                        onClick = { onSelectFilter(PharmacyOrderFilter.ALL) },
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
                        selected = selectedFilter == PharmacyOrderFilter.PENDING,
                        onClick = { onSelectFilter(PharmacyOrderFilter.PENDING) },
                        label = { Text("Pending ($pendingCount)") },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WarningAmber,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == PharmacyOrderFilter.DISPENSED,
                        onClick = { onSelectFilter(PharmacyOrderFilter.DISPENSED) },
                        label = { Text("Dispensed ($dispensedCount)") },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HospitalGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Orders List
        if (orders.isEmpty()) {
            item {
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
                            text = "No prescriptions in ${selectedFilter.name.lowercase()}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(orders, key = { it.orderId }) { order ->
                PharmacyOrderCard(
                    order = order,
                    onDispense = { onDispenseOrder(order.orderId) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun PharmacyOrderCard(
    order: PharmacyOrder,
    onDispense: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Patient & Order Header
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

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (order.status == PharmacyOrderStatus.PENDING) LightAmberBadge else LightGreenBadge
                ) {
                    Text(
                        text = order.status.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (order.status == PharmacyOrderStatus.PENDING) WarningAmber else HospitalGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prescribed Medicines List
            Surface(
                color = MedicalBackground,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Prescribed Medicines (${order.items.size}):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.medicineName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Dosage: ${item.dosage} (${item.days} Days)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = TealPrimary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "${item.quantity} Units",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Doctor, Amount, and Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Doctor: ${order.prescribedByDoctor}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "Amount: ₹${String.format("%.2f", order.totalAmount)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimaryDark
                    )
                }

                if (order.status == PharmacyOrderStatus.PENDING) {
                    Button(
                        onClick = onDispense,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HospitalGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dispense Medicines", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = HospitalGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Fulfilled at ${order.dispensedAt ?: ""}",
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

@Composable
fun InventoryTabContent(
    inventory: List<MedicineItem>,
    onRestock: (MedicineItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(inventory, key = { it.medicineId }) { med ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = med.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${med.category} • ₹${med.pricePerUnit} per ${med.unitType.dropLast(1)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Min threshold: ${med.minThreshold} units",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (med.isLowStock) Color(0xFFFFEBEE) else LightGreenBadge
                        ) {
                            Text(
                                text = "${med.stockUnits} ${med.unitType}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (med.isLowStock) Color(0xFFD32F2F) else HospitalGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedButton(
                            onClick = { onRestock(med) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text("Restock +", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun RestockDialog(
    medicine: MedicineItem,
    unitsInput: String,
    onUnitsChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Restock Medication", fontWeight = FontWeight.Bold)
                Text(
                    text = "${medicine.name} (Current: ${medicine.stockUnits} units)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Enter quantity to add to pharmacy inventory:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                OutlinedTextField(
                    value = unitsInput,
                    onValueChange = onUnitsChange,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    label = { Text("Units to Add") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
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
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = HospitalGreen)
            ) {
                Text("Confirm Restock")
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
fun NewPrescriptionDialog(
    patients: List<com.example.mediflow_ai.data.model.Patient>,
    inventory: List<MedicineItem>,
    onDismiss: () -> Unit,
    onSubmit: (patientId: String, patientName: String, token: Int, items: List<PrescriptionItem>) -> Unit
) {
    var selectedPatientIndex by remember { mutableStateOf(0) }
    val safePatients = if (patients.isNotEmpty()) patients else listOf(
        com.example.mediflow_ai.data.model.Patient(
            patientId = "MF-2026-1011",
            fullName = "Sunil Joshi",
            age = 45,
            gender = "Male",
            phoneNumber = "9822011234",
            bloodGroup = "B+",
            chiefComplaints = "Fever",
            departmentNeeded = "General Medicine",
            tokenNumber = 101,
            registrationTime = "09:15 AM"
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Doctor: Prescribe Medication",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Select Patient:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                safePatients.forEachIndexed { index, patient ->
                    FilterChip(
                        selected = selectedPatientIndex == index,
                        onClick = { selectedPatientIndex = index },
                        label = { Text("#${patient.tokenNumber} - ${patient.fullName}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Prescription Package (Standard):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = MedicalBackground,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("• Paracetamol 650mg (1-0-1, 5 days, 10 tabs)", style = MaterialTheme.typography.bodySmall)
                        Text("• Pantoprazole 40mg (1-0-0, 5 days, 5 tabs)", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = safePatients[selectedPatientIndex]
                    val items = listOf(
                        PrescriptionItem("Paracetamol 650mg", "1-0-1 after food", 5, 10),
                        PrescriptionItem("Pantoprazole 40mg", "1-0-0 empty stomach", 5, 5)
                    )
                    onSubmit(p.patientId, p.fullName, p.tokenNumber, items)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Send to Pharmacy")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
