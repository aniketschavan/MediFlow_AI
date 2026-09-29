package com.example.mediflow_ai.ui.doctor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediflow_ai.data.model.ConsultationStatus
import com.example.mediflow_ai.data.model.DoctorProfile
import com.example.mediflow_ai.data.model.QueueItem
import com.example.mediflow_ai.data.repository.HospitalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class QueueFilter {
    ALL,
    WAITING,
    IN_CONSULTATION,
    COMPLETED
}

data class DoctorQueueUiState(
    val doctor: DoctorProfile = DoctorProfile(),
    val allQueueItems: List<QueueItem> = emptyList(),
    val filteredQueueItems: List<QueueItem> = emptyList(),
    val activeConsultation: QueueItem? = null,
    val selectedFilter: QueueFilter = QueueFilter.ALL,
    val totalPatients: Int = 0,
    val waitingCount: Int = 0,
    val inConsultationCount: Int = 0,
    val completedCount: Int = 0
)

class DoctorQueueViewModel : ViewModel() {

    private val selectedFilter = MutableStateFlow(QueueFilter.ALL)

    val uiState: StateFlow<DoctorQueueUiState> = combine(
        HospitalRepository.doctorProfile,
        HospitalRepository.queueItems,
        selectedFilter
    ) { doctor, items, filter ->
        val waiting = items.count { it.consultationStatus == ConsultationStatus.WAITING }
        val inConsultation = items.count { it.consultationStatus == ConsultationStatus.IN_CONSULTATION }
        val completed = items.count { it.consultationStatus == ConsultationStatus.COMPLETED }
        val activeItem = items.firstOrNull { it.consultationStatus == ConsultationStatus.IN_CONSULTATION }

        val filtered = when (filter) {
            QueueFilter.ALL -> items
            QueueFilter.WAITING -> items.filter { it.consultationStatus == ConsultationStatus.WAITING }
            QueueFilter.IN_CONSULTATION -> items.filter { it.consultationStatus == ConsultationStatus.IN_CONSULTATION }
            QueueFilter.COMPLETED -> items.filter { it.consultationStatus == ConsultationStatus.COMPLETED }
        }

        DoctorQueueUiState(
            doctor = doctor,
            allQueueItems = items,
            filteredQueueItems = filtered,
            activeConsultation = activeItem,
            selectedFilter = filter,
            totalPatients = items.size,
            waitingCount = waiting,
            inConsultationCount = inConsultation,
            completedCount = completed
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DoctorQueueUiState()
    )

    fun toggleAvailability() {
        HospitalRepository.toggleDoctorAvailability()
    }

    fun setFilter(filter: QueueFilter) {
        selectedFilter.value = filter
    }

    fun startConsultation(patientId: String) {
        HospitalRepository.updateConsultationStatus(patientId, ConsultationStatus.IN_CONSULTATION)
    }

    fun completeConsultation(patientId: String) {
        HospitalRepository.updateConsultationStatus(patientId, ConsultationStatus.COMPLETED)
    }

    fun returnToWaiting(patientId: String) {
        HospitalRepository.updateConsultationStatus(patientId, ConsultationStatus.WAITING)
    }
}
