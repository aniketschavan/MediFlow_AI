package com.example.mediflow_ai.ui.auth

enum class LoginTab {
    PATIENT,
    STAFF
}

sealed class AuthState {
    data object Splash : AuthState()
    
    data class Login(
        val initialTab: LoginTab = LoginTab.PATIENT
    ) : AuthState()
    
    data object PatientRegistration : AuthState()
    
    data class PatientSession(
        val patientId: String = "MF-2026-1012",
        val patientName: String = "Sneha Kulkarni",
        val tokenNumber: Int? = null
    ) : AuthState()
    
    data class StaffSession(
        val roleName: String = "Doctor / Physician",
        val staffName: String = "Dr. Rajesh Patil"
    ) : AuthState()
}
