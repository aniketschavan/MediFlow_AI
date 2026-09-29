package com.example.mediflow_ai.data.model

enum class ConsultationStatus(val label: String) {
    WAITING("Waiting"),
    IN_CONSULTATION("In Consultation"),
    COMPLETED("Completed")
}

data class QueueItem(
    val patient: Patient,
    val consultationStatus: ConsultationStatus = ConsultationStatus.WAITING,
    val consultationStartTime: String? = null,
    val consultationEndTime: String? = null
)

data class DoctorProfile(
    val doctorId: String = "DOC-104",
    val name: String = "Dr. Rajesh Patil",
    val specialty: String = "General Medicine / Physician",
    val roomNumber: String = "OPD Cabin 104",
    val isAvailable: Boolean = true
)
