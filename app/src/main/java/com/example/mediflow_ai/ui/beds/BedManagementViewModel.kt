package com.example.mediflow_ai.ui.beds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediflow_ai.data.model.BedStatus
import com.example.mediflow_ai.data.model.HospitalBed
import com.example.mediflow_ai.data.model.Patient
import com.example.mediflow_ai.data.model.WardType
import com.example.mediflow_ai.data.repository.HospitalRepository
import kotlinx.coroutines.flow.*

data class BedManagementUiState(
    val allBeds: List<HospitalBed> = emptyList(),
    val filteredBeds: List<HospitalBed> = emptyList(),
    val selectedWard: WardType? = null,
    val totalBeds: Int = 0,
    val availableCount: Int = 0,
    val occupiedCount: Int = 0,
    val cleaningCount: Int = 0,
    val occupancyPercentage: Int = 0,
    val selectedBedForAdmission: HospitalBed? = null,
    val showAdmissionDialog: Boolean = false,
    val registeredPatients: List<Patient> = emptyList()
)

class BedManagementViewModel : ViewModel() {

    private val selectedWard = MutableStateFlow<WardType?>(null)
    private val selectedBedForAdmission = MutableStateFlow<HospitalBed?>(null)
    private val showAdmissionDialog = MutableStateFlow(false)

    val uiState: StateFlow<BedManagementUiState> = combine(
        HospitalRepository.hospitalBeds,
        HospitalRepository.queueItems,
        selectedWard
    ) { beds, queueItems, wardFilter ->
        val available = beds.count { it.status == BedStatus.AVAILABLE }
        val occupied = beds.count { it.status == BedStatus.OCCUPIED }
        val cleaning = beds.count { it.status == BedStatus.CLEANING }
        val total = beds.size
        val percentage = if (total > 0) (occupied * 100) / total else 0

        val filtered = if (wardFilter != null) {
            beds.filter { it.ward == wardFilter }
        } else {
            beds
        }

        val patients = queueItems.map { it.patient }

        BedManagementUiState(
            allBeds = beds,
            filteredBeds = filtered,
            selectedWard = wardFilter,
            totalBeds = total,
            availableCount = available,
            occupiedCount = occupied,
            cleaningCount = cleaning,
            occupancyPercentage = percentage,
            selectedBedForAdmission = selectedBedForAdmission.value,
            showAdmissionDialog = showAdmissionDialog.value,
            registeredPatients = patients
        )
    }.combine(showAdmissionDialog) { state, isDialogOpen ->
        state.copy(showAdmissionDialog = isDialogOpen)
    }.combine(selectedBedForAdmission) { state, bed ->
        state.copy(selectedBedForAdmission = bed)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BedManagementUiState()
    )

    fun setWardFilter(ward: WardType?) {
        selectedWard.value = ward
    }

    fun openAdmissionDialog(bed: HospitalBed) {
        selectedBedForAdmission.value = bed
        showAdmissionDialog.value = true
    }

    fun closeAdmissionDialog() {
        showAdmissionDialog.value = false
        selectedBedForAdmission.value = null
    }

    fun admitPatient(bedId: String, patientId: String, patientName: String, tokenNumber: Int) {
        HospitalRepository.admitPatientToBed(bedId, patientId, patientName, tokenNumber)
        closeAdmissionDialog()
    }

    fun dischargePatient(bedId: String) {
        HospitalRepository.dischargePatientFromBed(bedId)
    }

    fun markSanitized(bedId: String) {
        HospitalRepository.markBedCleaned(bedId)
    }
}
