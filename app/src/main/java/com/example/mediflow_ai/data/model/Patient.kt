package com.example.mediflow_ai.data.model

data class Patient(
    val patientId: String,
    val fullName: String,
    val age: Int,
    val gender: String,
    val phoneNumber: String,
    val bloodGroup: String,
    val chiefComplaints: String,
    val departmentNeeded: String,
    val tokenNumber: Int,
    val status: String = "Registered • Waiting for Triage",
    val registrationTime: String,
    val isStaffNotified: Boolean = true
)
