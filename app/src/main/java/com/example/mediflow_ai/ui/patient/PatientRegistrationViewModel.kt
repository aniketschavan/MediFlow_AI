package com.example.mediflow_ai.ui.patient

import androidx.lifecycle.ViewModel
import com.example.mediflow_ai.data.model.Patient
import com.example.mediflow_ai.data.repository.HospitalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class PatientRegistrationUiState(
    val fullName: String = "",
    val age: String = "",
    val gender: String = "Male",
    val phoneNumber: String = "",
    val bloodGroup: String = "O+",
    val chiefComplaints: String = "",
    val department: String = "General Medicine",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registeredPatient: Patient? = null,
    val recentRegistrations: List<Patient> = emptyList()
)

class PatientRegistrationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PatientRegistrationUiState())
    val uiState: StateFlow<PatientRegistrationUiState> = _uiState.asStateFlow()

    private var tokenCounter = 101

    fun onFullNameChange(name: String) {
        _uiState.update { it.copy(fullName = name, errorMessage = null) }
    }

    fun onAgeChange(age: String) {
        if (age.all { it.isDigit() } && age.length <= 3) {
            _uiState.update { it.copy(age = age, errorMessage = null) }
        }
    }

    fun onGenderChange(gender: String) {
        _uiState.update { it.copy(gender = gender) }
    }

    fun onPhoneChange(phone: String) {
        if (phone.all { it.isDigit() } && phone.length <= 10) {
            _uiState.update { it.copy(phoneNumber = phone, errorMessage = null) }
        }
    }

    fun onBloodGroupChange(bloodGroup: String) {
        _uiState.update { it.copy(bloodGroup = bloodGroup) }
    }

    fun onComplaintsChange(complaints: String) {
        _uiState.update { it.copy(chiefComplaints = complaints, errorMessage = null) }
    }

    fun onDepartmentChange(department: String) {
        _uiState.update { it.copy(department = department) }
    }

    fun registerPatient() {
        val state = _uiState.value
        if (state.fullName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter patient full name") }
            return
        }
        val ageInt = state.age.toIntOrNull()
        if (ageInt == null || ageInt <= 0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid age") }
            return
        }
        if (state.phoneNumber.length < 10) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid 10-digit mobile number") }
            return
        }
        if (state.chiefComplaints.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter primary symptoms / chief complaints") }
            return
        }

        val currentTime = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
        val generatedId = "MF-2026-${Random.nextInt(1000, 9999)}"
        val assignedToken = tokenCounter++

        val newPatient = Patient(
            patientId = generatedId,
            fullName = state.fullName.trim(),
            age = ageInt,
            gender = state.gender,
            phoneNumber = state.phoneNumber.trim(),
            bloodGroup = state.bloodGroup,
            chiefComplaints = state.chiefComplaints.trim(),
            departmentNeeded = state.department,
            tokenNumber = assignedToken,
            registrationTime = currentTime,
            status = "Registered • Waiting for Doctor",
            isStaffNotified = true
        )

        // Sync with shared Hospital Repository for Doctor Queue
        HospitalRepository.addPatient(newPatient)

        _uiState.update { current ->
            current.copy(
                registeredPatient = newPatient,
                recentRegistrations = listOf(newPatient) + current.recentRegistrations,
                errorMessage = null
            )
        }
    }

    fun registerAnotherPatient() {
        _uiState.update {
            it.copy(
                fullName = "",
                age = "",
                gender = "Male",
                phoneNumber = "",
                bloodGroup = "O+",
                chiefComplaints = "",
                department = "General Medicine",
                registeredPatient = null,
                errorMessage = null
            )
        }
    }
}
