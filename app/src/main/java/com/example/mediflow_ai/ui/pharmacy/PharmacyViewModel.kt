package com.example.mediflow_ai.ui.pharmacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediflow_ai.data.model.MedicineItem
import com.example.mediflow_ai.data.model.Patient
import com.example.mediflow_ai.data.model.PharmacyOrder
import com.example.mediflow_ai.data.model.PharmacyOrderStatus
import com.example.mediflow_ai.data.model.PrescriptionItem
import com.example.mediflow_ai.data.repository.HospitalRepository
import kotlinx.coroutines.flow.*

enum class PharmacyTab {
    PRESCRIPTIONS,
    INVENTORY
}

enum class PharmacyOrderFilter {
    ALL,
    PENDING,
    DISPENSED
}

data class PharmacyUiState(
    val activeTab: PharmacyTab = PharmacyTab.PRESCRIPTIONS,
    val selectedFilter: PharmacyOrderFilter = PharmacyOrderFilter.ALL,
    val allOrders: List<PharmacyOrder> = emptyList(),
    val filteredOrders: List<PharmacyOrder> = emptyList(),
    val inventory: List<MedicineItem> = emptyList(),
    val lowStockMedicines: List<MedicineItem> = emptyList(),
    val pendingCount: Int = 0,
    val dispensedCount: Int = 0,
    val lowStockAlertCount: Int = 0,
    val selectedMedicineForRestock: MedicineItem? = null,
    val restockUnitsInput: String = "50",
    val showRestockDialog: Boolean = false,
    val registeredPatients: List<Patient> = emptyList()
)

class PharmacyViewModel : ViewModel() {

    private val activeTab = MutableStateFlow(PharmacyTab.PRESCRIPTIONS)
    private val selectedFilter = MutableStateFlow(PharmacyOrderFilter.ALL)
    private val selectedMedicineForRestock = MutableStateFlow<MedicineItem?>(null)
    private val restockUnitsInput = MutableStateFlow("50")
    private val showRestockDialog = MutableStateFlow(false)

    val uiState: StateFlow<PharmacyUiState> = combine(
        HospitalRepository.pharmacyOrders,
        HospitalRepository.medicineInventory,
        HospitalRepository.queueItems
    ) { orders, inventory, queueItems ->
        val pending = orders.count { it.status == PharmacyOrderStatus.PENDING }
        val dispensed = orders.count { it.status == PharmacyOrderStatus.DISPENSED }
        val lowStock = inventory.filter { it.isLowStock }
        val patients = queueItems.map { it.patient }

        Triple(orders, inventory, Triple(pending, dispensed, Pair(lowStock, patients)))
    }.combine(activeTab) { base, tab ->
        Pair(base, tab)
    }.combine(selectedFilter) { (base, tab), filter ->
        val orders = base.first
        val inventory = base.second
        val pending = base.third.first
        val dispensed = base.third.second
        val lowStock = base.third.third.first
        val patients = base.third.third.second

        val filtered = when (filter) {
            PharmacyOrderFilter.ALL -> orders
            PharmacyOrderFilter.PENDING -> orders.filter { it.status == PharmacyOrderStatus.PENDING }
            PharmacyOrderFilter.DISPENSED -> orders.filter { it.status == PharmacyOrderStatus.DISPENSED }
        }

        PharmacyUiState(
            activeTab = tab,
            selectedFilter = filter,
            allOrders = orders,
            filteredOrders = filtered,
            inventory = inventory,
            lowStockMedicines = lowStock,
            pendingCount = pending,
            dispensedCount = dispensed,
            lowStockAlertCount = lowStock.size,
            selectedMedicineForRestock = selectedMedicineForRestock.value,
            restockUnitsInput = restockUnitsInput.value,
            showRestockDialog = showRestockDialog.value,
            registeredPatients = patients
        )
    }.combine(showRestockDialog) { state, isOpen ->
        state.copy(showRestockDialog = isOpen)
    }.combine(selectedMedicineForRestock) { state, med ->
        state.copy(selectedMedicineForRestock = med)
    }.combine(restockUnitsInput) { state, input ->
        state.copy(restockUnitsInput = input)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PharmacyUiState()
    )

    fun selectTab(tab: PharmacyTab) {
        activeTab.value = tab
    }

    fun setFilter(filter: PharmacyOrderFilter) {
        selectedFilter.value = filter
    }

    fun dispenseOrder(orderId: String) {
        HospitalRepository.dispensePharmacyOrder(orderId)
    }

    fun openRestockDialog(medicine: MedicineItem) {
        selectedMedicineForRestock.value = medicine
        restockUnitsInput.value = "50"
        showRestockDialog.value = true
    }

    fun closeRestockDialog() {
        showRestockDialog.value = false
        selectedMedicineForRestock.value = null
    }

    fun onRestockUnitsChange(input: String) {
        if (input.all { it.isDigit() } && input.length <= 4) {
            restockUnitsInput.value = input
        }
    }

    fun submitRestock() {
        val med = selectedMedicineForRestock.value ?: return
        val units = restockUnitsInput.value.toIntOrNull() ?: 50
        if (units > 0) {
            HospitalRepository.restockMedicine(med.medicineId, units)
            closeRestockDialog()
        }
    }

    fun createPrescription(
        patientId: String,
        patientName: String,
        token: Int,
        items: List<PrescriptionItem>
    ) {
        HospitalRepository.createPharmacyOrder(
            patientId = patientId,
            patientName = patientName,
            tokenNumber = token,
            items = items
        )
    }
}
