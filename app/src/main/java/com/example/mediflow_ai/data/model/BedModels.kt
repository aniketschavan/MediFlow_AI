package com.example.mediflow_ai.data.model

enum class BedStatus(val label: String) {
    AVAILABLE("Available"),
    OCCUPIED("Occupied"),
    CLEANING("Sanitizing / Cleaning")
}

enum class WardType(val displayName: String, val dailyRate: Double) {
    ICU("Intensive Care Unit (ICU)", 4500.0),
    EMERGENCY("Emergency & Trauma", 2500.0),
    GENERAL_MALE("General Ward - Male", 1200.0),
    GENERAL_FEMALE("General Ward - Female", 1200.0)
}

data class HospitalBed(
    val bedId: String,
    val ward: WardType,
    val bedNumber: Int,
    val status: BedStatus = BedStatus.AVAILABLE,
    val occupiedByPatientId: String? = null,
    val occupiedByPatientName: String? = null,
    val tokenNumber: Int? = null,
    val admittedAt: String? = null,
    val chargePerDay: Double = ward.dailyRate
)
