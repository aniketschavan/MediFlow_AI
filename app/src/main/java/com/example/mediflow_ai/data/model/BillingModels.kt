package com.example.mediflow_ai.data.model

enum class PaymentStatus(val label: String) {
    PENDING("Payment Due"),
    PARTIALLY_PAID("Partially Paid"),
    PAID("Settled & Paid")
}

enum class PaymentMode(val displayName: String) {
    UPI_ONLINE("UPI / QR Code"),
    CASH("Cash Counter"),
    CARD("Debit / Credit Card"),
    INSURANCE_TPA("Health Insurance / TPA")
}

data class BillLineItem(
    val serviceName: String,
    val category: String, // "Consultation", "Laboratory", "Pharmacy", "Bed & Room"
    val amount: Double
)

data class PatientInvoice(
    val invoiceId: String,
    val patientId: String,
    val patientName: String,
    val tokenNumber: Int,
    val lineItems: List<BillLineItem>,
    val subtotal: Double,
    val discountAmount: Double = 0.0,
    val totalPayable: Double,
    val paidAmount: Double = 0.0,
    val balanceDue: Double = totalPayable - paidAmount,
    val status: PaymentStatus = if (paidAmount >= totalPayable) PaymentStatus.PAID else PaymentStatus.PENDING,
    val paymentMode: PaymentMode? = null,
    val generatedAt: String,
    val paidAt: String? = null
)
