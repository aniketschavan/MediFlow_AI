package com.example.mediflow_ai.ui.billing

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
import com.example.mediflow_ai.data.model.PatientInvoice
import com.example.mediflow_ai.data.model.PaymentMode
import com.example.mediflow_ai.data.model.PaymentStatus
import com.example.mediflow_ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    viewModel: BillingViewModel = viewModel(),
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
                                    imageVector = Icons.Default.Payment,
                                    contentDescription = "Billing Desk",
                                    tint = TealPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Hospital Billing Desk",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            Text(
                                text = "Cross-Department Invoicing & Dues",
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
            // 1. Revenue Metrics Header
            item {
                RevenueMetricsRow(
                    collected = uiState.totalRevenueCollected,
                    pending = uiState.totalPendingDues,
                    paidCount = uiState.paidCount,
                    pendingCount = uiState.pendingCount
                )
            }

            // 2. Filter Chips
            item {
                BillingFilterChips(
                    selectedFilter = uiState.selectedFilter,
                    totalCount = uiState.allInvoices.size,
                    pendingCount = uiState.pendingCount,
                    paidCount = uiState.paidCount,
                    onSelectFilter = { viewModel.setFilter(it) }
                )
            }

            // 3. Invoice Cards
            if (uiState.filteredInvoices.isEmpty()) {
                item {
                    EmptyInvoicesCard(filter = uiState.selectedFilter)
                }
            } else {
                items(uiState.filteredInvoices, key = { it.invoiceId }) { invoice ->
                    PatientInvoiceCard(
                        invoice = invoice,
                        onCollectPayment = { viewModel.openPaymentDialog(invoice) },
                        onViewReceipt = { viewModel.openReceiptDialog(invoice) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Payment Collection Dialog
    if (uiState.showPaymentDialog && uiState.selectedInvoiceForPayment != null) {
        PaymentCollectionDialog(
            invoice = uiState.selectedInvoiceForPayment!!,
            amountInput = uiState.paymentAmountInput,
            selectedMode = uiState.selectedPaymentMode,
            onAmountChange = { viewModel.onAmountChange(it) },
            onModeChange = { viewModel.onPaymentModeChange(it) },
            onDismiss = { viewModel.closePaymentDialog() },
            onConfirm = { viewModel.submitPayment() }
        )
    }

    // Official Receipt Modal Sheet
    if (uiState.showReceiptDialog && uiState.selectedInvoiceForReceipt != null) {
        ReceiptPreviewDialog(
            invoice = uiState.selectedInvoiceForReceipt!!,
            onDismiss = { viewModel.closeReceiptDialog() }
        )
    }
}

@Composable
fun RevenueMetricsRow(
    collected: Double,
    pending: Double,
    paidCount: Int,
    pendingCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = LightGreenBadge,
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "₹${String.format("%.0f", collected)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = HospitalGreen
                )
                Text(
                    text = "Collected ($paidCount Paid)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFFFEBEE),
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "₹${String.format("%.0f", pending)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFD32F2F)
                )
                Text(
                    text = "Pending ($pendingCount Dues)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BillingFilterChips(
    selectedFilter: BillingFilter,
    totalCount: Int,
    pendingCount: Int,
    paidCount: Int,
    onSelectFilter: (BillingFilter) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = selectedFilter == BillingFilter.ALL,
                onClick = { onSelectFilter(BillingFilter.ALL) },
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
                selected = selectedFilter == BillingFilter.PENDING,
                onClick = { onSelectFilter(BillingFilter.PENDING) },
                label = { Text("Payment Due ($pendingCount)") },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WarningAmber,
                    selectedLabelColor = Color.White
                )
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == BillingFilter.PAID,
                onClick = { onSelectFilter(BillingFilter.PAID) },
                label = { Text("Settled & Paid ($paidCount)") },
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
fun PatientInvoiceCard(
    invoice: PatientInvoice,
    onCollectPayment: () -> Unit,
    onViewReceipt: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Invoice ID & Status
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
                                text = "#${invoice.tokenNumber}",
                                fontWeight = FontWeight.Bold,
                                color = TealPrimaryDark,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = invoice.patientName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${invoice.patientId} • ${invoice.invoiceId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (invoice.status) {
                        PaymentStatus.PAID -> LightGreenBadge
                        PaymentStatus.PARTIALLY_PAID -> SoftCyan
                        PaymentStatus.PENDING -> Color(0xFFFFEBEE)
                    }
                ) {
                    Text(
                        text = invoice.status.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (invoice.status) {
                            PaymentStatus.PAID -> HospitalGreen
                            PaymentStatus.PARTIALLY_PAID -> MedicalBlue
                            PaymentStatus.PENDING -> Color(0xFFD32F2F)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Itemized Services Box
            Surface(
                color = MedicalBackground,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Consolidated Services (${invoice.lineItems.size} items):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    invoice.lineItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "• ${item.serviceName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "₹${String.format("%.2f", item.amount)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Totals Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total: ₹${String.format("%.2f", invoice.totalPayable)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "Due: ₹${String.format("%.2f", invoice.balanceDue)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (invoice.balanceDue > 0) Color(0xFFD32F2F) else HospitalGreen
                    )
                }

                if (invoice.status != PaymentStatus.PAID) {
                    Button(
                        onClick = onCollectPayment,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HospitalGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Collect Payment", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    OutlinedButton(
                        onClick = onViewReceipt,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Receipt", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentCollectionDialog(
    invoice: PatientInvoice,
    amountInput: String,
    selectedMode: PaymentMode,
    onAmountChange: (String) -> Unit,
    onModeChange: (PaymentMode) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Process Hospital Payment", fontWeight = FontWeight.Bold)
                Text(
                    text = "${invoice.invoiceId} • ${invoice.patientName} (Due: ₹${String.format("%.2f", invoice.balanceDue)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Select Payment Channel:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                PaymentMode.values().forEach { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = { onModeChange(mode) },
                        label = { Text(mode.displayName) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = onAmountChange,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    label = { Text("Amount to Settle (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = Color(0xFFB0BEC5),
                        focusedLabelColor = TealPrimary,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = TealPrimary
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
                Text("Confirm Payment")
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
fun ReceiptPreviewDialog(
    invoice: PatientInvoice,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "MediFlow AI Multi-Specialty Hospital",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TealPrimary
                )
                Text(
                    text = "OFFICIAL TAX INVOICE & RECEIPT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = MedicalBackground,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Invoice #: ${invoice.invoiceId}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Text("Patient: ${invoice.patientName} (${invoice.patientId})", style = MaterialTheme.typography.bodySmall)
                        Text("Settled at: ${invoice.paidAt ?: "Paid"}", style = MaterialTheme.typography.bodySmall)
                        Text("Payment Mode: ${invoice.paymentMode?.displayName ?: "Online"}", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = BorderLight)
                Spacer(modifier = Modifier.height(10.dp))

                invoice.lineItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.serviceName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text("₹${String.format("%.2f", item.amount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = BorderLight)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TOTAL AMOUNT PAID:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text("₹${String.format("%.2f", invoice.paidAmount)}", fontWeight = FontWeight.ExtraBold, color = HospitalGreen, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = LightGreenBadge,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = HospitalGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PAID & DIGITALLY VERIFIED", fontWeight = FontWeight.Bold, color = HospitalGreen, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)) {
                Text("Close Receipt")
            }
        }
    )
}

@Composable
fun EmptyInvoicesCard(filter: BillingFilter) {
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
                imageVector = Icons.Default.Payment,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No invoices in ${filter.name.lowercase()}",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )
        }
    }
}
