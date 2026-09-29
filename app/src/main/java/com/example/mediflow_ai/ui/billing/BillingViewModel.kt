package com.example.mediflow_ai.ui.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediflow_ai.data.model.PatientInvoice
import com.example.mediflow_ai.data.model.PaymentMode
import com.example.mediflow_ai.data.model.PaymentStatus
import com.example.mediflow_ai.data.repository.HospitalRepository
import kotlinx.coroutines.flow.*

enum class BillingFilter {
    ALL,
    PENDING,
    PAID
}

data class BillingUiState(
    val allInvoices: List<PatientInvoice> = emptyList(),
    val filteredInvoices: List<PatientInvoice> = emptyList(),
    val selectedFilter: BillingFilter = BillingFilter.ALL,
    val totalRevenueCollected: Double = 0.0,
    val totalPendingDues: Double = 0.0,
    val paidCount: Int = 0,
    val pendingCount: Int = 0,
    val selectedInvoiceForPayment: PatientInvoice? = null,
    val paymentAmountInput: String = "",
    val selectedPaymentMode: PaymentMode = PaymentMode.UPI_ONLINE,
    val showPaymentDialog: Boolean = false,
    val selectedInvoiceForReceipt: PatientInvoice? = null,
    val showReceiptDialog: Boolean = false
)

class BillingViewModel : ViewModel() {

    private val selectedFilter = MutableStateFlow(BillingFilter.ALL)
    private val selectedInvoiceForPayment = MutableStateFlow<PatientInvoice?>(null)
    private val paymentAmountInput = MutableStateFlow("")
    private val selectedPaymentMode = MutableStateFlow(PaymentMode.UPI_ONLINE)
    private val showPaymentDialog = MutableStateFlow(false)
    private val selectedInvoiceForReceipt = MutableStateFlow<PatientInvoice?>(null)
    private val showReceiptDialog = MutableStateFlow(false)

    val uiState: StateFlow<BillingUiState> = combine(
        HospitalRepository.invoices,
        selectedFilter
    ) { invoices, filter ->
        val totalRevenue = invoices.sumOf { it.paidAmount }
        val totalPending = invoices.sumOf { it.balanceDue }
        val paidCount = invoices.count { it.status == PaymentStatus.PAID }
        val pendingCount = invoices.count { it.status != PaymentStatus.PAID }

        val filtered = when (filter) {
            BillingFilter.ALL -> invoices
            BillingFilter.PENDING -> invoices.filter { it.status != PaymentStatus.PAID }
            BillingFilter.PAID -> invoices.filter { it.status == PaymentStatus.PAID }
        }

        BillingUiState(
            allInvoices = invoices,
            filteredInvoices = filtered,
            selectedFilter = filter,
            totalRevenueCollected = totalRevenue,
            totalPendingDues = totalPending,
            paidCount = paidCount,
            pendingCount = pendingCount,
            selectedInvoiceForPayment = selectedInvoiceForPayment.value,
            paymentAmountInput = paymentAmountInput.value,
            selectedPaymentMode = selectedPaymentMode.value,
            showPaymentDialog = showPaymentDialog.value,
            selectedInvoiceForReceipt = selectedInvoiceForReceipt.value,
            showReceiptDialog = showReceiptDialog.value
        )
    }.combine(showPaymentDialog) { state, isOpen ->
        state.copy(showPaymentDialog = isOpen)
    }.combine(selectedInvoiceForPayment) { state, invoice ->
        state.copy(selectedInvoiceForPayment = invoice)
    }.combine(paymentAmountInput) { state, amount ->
        state.copy(paymentAmountInput = amount)
    }.combine(selectedPaymentMode) { state, mode ->
        state.copy(selectedPaymentMode = mode)
    }.combine(showReceiptDialog) { state, isReceiptOpen ->
        state.copy(showReceiptDialog = isReceiptOpen)
    }.combine(selectedInvoiceForReceipt) { state, receiptInvoice ->
        state.copy(selectedInvoiceForReceipt = receiptInvoice)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BillingUiState()
    )

    fun setFilter(filter: BillingFilter) {
        selectedFilter.value = filter
    }

    fun openPaymentDialog(invoice: PatientInvoice) {
        selectedInvoiceForPayment.value = invoice
        paymentAmountInput.value = String.format("%.2f", invoice.balanceDue)
        selectedPaymentMode.value = PaymentMode.UPI_ONLINE
        showPaymentDialog.value = true
    }

    fun closePaymentDialog() {
        showPaymentDialog.value = false
        selectedInvoiceForPayment.value = null
    }

    fun onAmountChange(amount: String) {
        paymentAmountInput.value = amount
    }

    fun onPaymentModeChange(mode: PaymentMode) {
        selectedPaymentMode.value = mode
    }

    fun submitPayment() {
        val invoice = selectedInvoiceForPayment.value ?: return
        val amount = paymentAmountInput.value.toDoubleOrNull() ?: invoice.balanceDue
        if (amount > 0) {
            HospitalRepository.recordPayment(invoice.invoiceId, amount, selectedPaymentMode.value)
            closePaymentDialog()
        }
    }

    fun openReceiptDialog(invoice: PatientInvoice) {
        selectedInvoiceForReceipt.value = invoice
        showReceiptDialog.value = true
    }

    fun closeReceiptDialog() {
        showReceiptDialog.value = false
        selectedInvoiceForReceipt.value = null
    }
}
