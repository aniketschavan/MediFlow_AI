package com.example.mediflow_ai.ui.lab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediflow_ai.data.model.LabOrder
import com.example.mediflow_ai.data.model.LabTestStatus
import com.example.mediflow_ai.data.model.Patient
import com.example.mediflow_ai.data.repository.HospitalRepository
import kotlinx.coroutines.flow.*

enum class LabFilter {
    ALL,
    REQUESTED,
    PROCESSING,
    COMPLETED
}

data class LabWorklistUiState(
    val allOrders: List<LabOrder> = emptyList(),
    val filteredOrders: List<LabOrder> = emptyList(),
    val selectedFilter: LabFilter = LabFilter.ALL,
    val totalCount: Int = 0,
    val requestedCount: Int = 0,
    val processingCount: Int = 0,
    val completedCount: Int = 0,
    val showResultDialog: Boolean = false,
    val selectedOrderForResults: LabOrder? = null,
    val resultInput: String = "",
    val registeredPatients: List<Patient> = emptyList()
)

class LabWorklistViewModel : ViewModel() {

    private val selectedFilter = MutableStateFlow(LabFilter.ALL)
    private val showResultDialog = MutableStateFlow(false)
    private val selectedOrder = MutableStateFlow<LabOrder?>(null)
    private val resultText = MutableStateFlow("")

    val uiState: StateFlow<LabWorklistUiState> = combine(
        HospitalRepository.labOrders,
        HospitalRepository.queueItems,
        selectedFilter
    ) { orders, queueItems, filter ->
        val requested = orders.count { it.status == LabTestStatus.REQUESTED }
        val processing = orders.count { it.status == LabTestStatus.PROCESSING }
        val completed = orders.count { it.status == LabTestStatus.COMPLETED }

        val filtered = when (filter) {
            LabFilter.ALL -> orders
            LabFilter.REQUESTED -> orders.filter { it.status == LabTestStatus.REQUESTED }
            LabFilter.PROCESSING -> orders.filter { it.status == LabTestStatus.PROCESSING }
            LabFilter.COMPLETED -> orders.filter { it.status == LabTestStatus.COMPLETED }
        }

        val patients = queueItems.map { it.patient }

        LabWorklistUiState(
            allOrders = orders,
            filteredOrders = filtered,
            selectedFilter = filter,
            totalCount = orders.size,
            requestedCount = requested,
            processingCount = processing,
            completedCount = completed,
            showResultDialog = showResultDialog.value,
            selectedOrderForResults = selectedOrder.value,
            resultInput = resultText.value,
            registeredPatients = patients
        )
    }.combine(showResultDialog) { state, isDialogOpen ->
        state.copy(showResultDialog = isDialogOpen)
    }.combine(selectedOrder) { state, activeOrder ->
        state.copy(selectedOrderForResults = activeOrder)
    }.combine(resultText) { state, text ->
        state.copy(resultInput = text)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LabWorklistUiState()
    )

    fun setFilter(filter: LabFilter) {
        selectedFilter.value = filter
    }

    fun collectSample(orderId: String) {
        HospitalRepository.collectLabSample(orderId)
    }

    fun openResultDialog(order: LabOrder) {
        selectedOrder.value = order
        resultText.value = when (order.testName) {
            "Complete Blood Count (CBC)" -> "Hemoglobin: 13.5 g/dL, WBC: 7,400 /mcL, Platelets: 2.4 Lakh. Normal range."
            "Fasting Blood Glucose (FBG)" -> "Fasting Blood Sugar: 112 mg/dL (Pre-diabetic threshold: 100-125 mg/dL)."
            "Lipid Profile" -> "Total Cholesterol: 185 mg/dL, HDL: 48, LDL: 110. Good lipid balance."
            else -> "All parameters evaluated within reference ranges. No abnormal pathology observed."
        }
        showResultDialog.value = true
    }

    fun closeResultDialog() {
        showResultDialog.value = false
        selectedOrder.value = null
        resultText.value = ""
    }

    fun onResultInputChange(text: String) {
        resultText.value = text
    }

    fun submitResults() {
        val order = selectedOrder.value ?: return
        if (resultText.value.isNotBlank()) {
            HospitalRepository.completeLabReport(order.orderId, resultText.value.trim())
            closeResultDialog()
        }
    }

    fun orderNewTest(
        patientId: String,
        patientName: String,
        tokenNumber: Int,
        testName: String,
        category: String
    ) {
        HospitalRepository.createLabOrder(
            patientId = patientId,
            patientName = patientName,
            tokenNumber = tokenNumber,
            testName = testName,
            category = category
        )
    }
}
