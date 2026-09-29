package com.example.mediflow_ai.data.model

enum class PharmacyOrderStatus(val label: String) {
    PENDING("Pending Dispensing"),
    DISPENSED("Dispensed & Fulfilled"),
    OUT_OF_STOCK("Out of Stock")
}

data class MedicineItem(
    val medicineId: String,
    val name: String,
    val category: String,
    val stockUnits: Int,
    val minThreshold: Int = 20,
    val unitType: String = "Tablets",
    val pricePerUnit: Double = 5.0
) {
    val isLowStock: Boolean
        get() = stockUnits <= minThreshold

    val isOutOfStock: Boolean
        get() = stockUnits <= 0
}

data class PrescriptionItem(
    val medicineName: String,
    val dosage: String, // e.g. "1-0-1 after food"
    val days: Int,
    val quantity: Int
)

data class PharmacyOrder(
    val orderId: String,
    val patientId: String,
    val patientName: String,
    val tokenNumber: Int,
    val prescribedByDoctor: String = "Dr. Rajesh Patil",
    val items: List<PrescriptionItem>,
    val status: PharmacyOrderStatus = PharmacyOrderStatus.PENDING,
    val orderedAt: String,
    val dispensedAt: String? = null,
    val totalAmount: Double = 0.0
)
