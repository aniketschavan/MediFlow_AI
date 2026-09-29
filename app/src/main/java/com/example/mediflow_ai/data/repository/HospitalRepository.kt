package com.example.mediflow_ai.data.repository

import com.example.mediflow_ai.data.model.ConsultationStatus
import com.example.mediflow_ai.data.model.DoctorProfile
import com.example.mediflow_ai.data.model.Patient
import com.example.mediflow_ai.data.model.QueueItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HospitalRepository {

    private val _doctorProfile = MutableStateFlow(DoctorProfile())
    val doctorProfile: StateFlow<DoctorProfile> = _doctorProfile.asStateFlow()

    private val initialQueue = listOf(
        QueueItem(
            patient = Patient(
                patientId = "MF-2026-1011",
                fullName = "Sunil Joshi",
                age = 45,
                gender = "Male",
                phoneNumber = "9822011234",
                bloodGroup = "B+",
                chiefComplaints = "High blood pressure & persistent morning headache",
                departmentNeeded = "General Medicine",
                tokenNumber = 101,
                status = "In Consultation",
                registrationTime = "09:15 AM",
                isStaffNotified = true
            ),
            consultationStatus = ConsultationStatus.IN_CONSULTATION,
            consultationStartTime = "09:30 AM"
        ),
        QueueItem(
            patient = Patient(
                patientId = "MF-2026-1012",
                fullName = "Sneha Kulkarni",
                age = 29,
                gender = "Female",
                phoneNumber = "9822055678",
                bloodGroup = "O+",
                chiefComplaints = "Severe throat infection, fever & body ache for 2 days",
                departmentNeeded = "General Medicine",
                tokenNumber = 102,
                status = "Waiting for Doctor",
                registrationTime = "09:30 AM",
                isStaffNotified = true
            ),
            consultationStatus = ConsultationStatus.WAITING
        ),
        QueueItem(
            patient = Patient(
                patientId = "MF-2026-1013",
                fullName = "Amit Shinde",
                age = 52,
                gender = "Male",
                phoneNumber = "9822099887",
                bloodGroup = "A+",
                chiefComplaints = "Routine diabetic checkup, fasting sugar report evaluation",
                departmentNeeded = "General Medicine",
                tokenNumber = 103,
                status = "Waiting for Doctor",
                registrationTime = "09:45 AM",
                isStaffNotified = true
            ),
            consultationStatus = ConsultationStatus.WAITING
        ),
        QueueItem(
            patient = Patient(
                patientId = "MF-2026-1010",
                fullName = "Ramesh Gaikwad",
                age = 38,
                gender = "Male",
                phoneNumber = "9822077441",
                bloodGroup = "AB+",
                chiefComplaints = "Mild seasonal cold & seasonal allergies",
                departmentNeeded = "General Medicine",
                tokenNumber = 100,
                status = "Consultation Finished",
                registrationTime = "09:00 AM",
                isStaffNotified = true
            ),
            consultationStatus = ConsultationStatus.COMPLETED,
            consultationStartTime = "09:05 AM",
            consultationEndTime = "09:25 AM"
        )
    )

    private val _queueItems = MutableStateFlow<List<QueueItem>>(initialQueue)
    val queueItems: StateFlow<List<QueueItem>> = _queueItems.asStateFlow()

    private val additionalRegisteredPatients = listOf(
        Patient(
            patientId = "MF-2026-1014",
            fullName = "Pooja Deshmukh",
            age = 26,
            gender = "Female",
            phoneNumber = "9822044321",
            bloodGroup = "A+",
            chiefComplaints = "Routine annual physical checkup",
            departmentNeeded = "General Medicine",
            tokenNumber = 0,
            status = "Registered • No Active Visit",
            registrationTime = "08:30 AM",
            isStaffNotified = false
        )
    )

    private val _registeredPatients = MutableStateFlow<List<Patient>>(initialQueue.map { it.patient } + additionalRegisteredPatients)
    val registeredPatients: StateFlow<List<Patient>> = _registeredPatients.asStateFlow()

    fun toggleDoctorAvailability() {
        _doctorProfile.update { it.copy(isAvailable = !it.isAvailable) }
    }

    fun getPatientByIdOrPhone(query: String): Patient? {
        val clean = query.trim()
        val digitsOnly = clean.filter { it.isDigit() }
        return _registeredPatients.value.firstOrNull { p ->
            p.patientId.equals(clean, ignoreCase = true) ||
            (digitsOnly.length >= 4 && p.phoneNumber.filter { it.isDigit() }.endsWith(digitsOnly)) ||
            p.fullName.contains(clean, ignoreCase = true)
        }
    }

    fun getActiveTokenForPatient(query: String): QueueItem? {
        val clean = query.trim()
        val digitsOnly = clean.filter { it.isDigit() }
        return _queueItems.value.firstOrNull { item ->
            item.consultationStatus != ConsultationStatus.COMPLETED &&
            (item.patient.patientId.equals(clean, ignoreCase = true) ||
             (digitsOnly.length >= 4 && item.patient.phoneNumber.filter { it.isDigit() }.endsWith(digitsOnly)) ||
             item.patient.fullName.contains(clean, ignoreCase = true))
        }
    }

    fun generateOpdTokenForPatient(patientId: String, department: String, symptoms: String): QueueItem {
        val nextToken = (_queueItems.value.maxOfOrNull { it.patient.tokenNumber } ?: 100) + 1
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val existing = getPatientByIdOrPhone(patientId)
        val patientToUse = if (existing != null) {
            existing.copy(
                departmentNeeded = department,
                chiefComplaints = symptoms,
                tokenNumber = nextToken,
                status = "Waiting for Doctor",
                registrationTime = currentTime
            )
        } else {
            Patient(
                patientId = patientId,
                fullName = "Patient ($patientId)",
                age = 32,
                gender = "Other",
                phoneNumber = "",
                bloodGroup = "O+",
                chiefComplaints = symptoms,
                departmentNeeded = department,
                tokenNumber = nextToken,
                status = "Waiting for Doctor",
                registrationTime = currentTime,
                isStaffNotified = true
            )
        }
        val newQueueItem = QueueItem(
            patient = patientToUse,
            consultationStatus = ConsultationStatus.WAITING
        )
        _queueItems.update { it + newQueueItem }
        _registeredPatients.update { current ->
            if (current.any { it.patientId == patientToUse.patientId }) {
                current.map { if (it.patientId == patientToUse.patientId) patientToUse else it }
            } else {
                current + patientToUse
            }
        }
        return newQueueItem
    }

    fun addPatient(patient: Patient) {
        val newQueueItem = QueueItem(
            patient = patient,
            consultationStatus = ConsultationStatus.WAITING
        )
        _queueItems.update { currentList ->
            currentList + newQueueItem
        }
        _registeredPatients.update { currentList ->
            if (currentList.none { it.patientId == patient.patientId }) {
                currentList + patient
            } else {
                currentList.map { if (it.patientId == patient.patientId) patient else it }
            }
        }
    }

    fun updateConsultationStatus(patientId: String, newStatus: ConsultationStatus) {
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        _queueItems.update { list ->
            list.map { item ->
                if (item.patient.patientId == patientId) {
                    when (newStatus) {
                        ConsultationStatus.IN_CONSULTATION -> {
                            item.copy(
                                consultationStatus = newStatus,
                                consultationStartTime = currentTime
                            )
                        }
                        ConsultationStatus.COMPLETED -> {
                            item.copy(
                                consultationStatus = newStatus,
                                consultationEndTime = currentTime
                            )
                        }
                        ConsultationStatus.WAITING -> {
                            item.copy(
                                consultationStatus = newStatus,
                                consultationStartTime = null,
                                consultationEndTime = null
                            )
                        }
                    }
                } else item
            }
        }
    }

    // --- Laboratory Orders Management ---
    private val initialLabOrders = listOf(
        com.example.mediflow_ai.data.model.LabOrder(
            orderId = "LAB-2026-301",
            patientId = "MF-2026-1012",
            patientName = "Sneha Kulkarni",
            tokenNumber = 102,
            prescribedByDoctor = "Dr. Rajesh Patil",
            testName = "Complete Blood Count (CBC)",
            category = "Hematology",
            status = com.example.mediflow_ai.data.model.LabTestStatus.PROCESSING,
            sampleCollected = true,
            orderedAt = "09:35 AM",
            sampleCollectedAt = "09:45 AM"
        ),
        com.example.mediflow_ai.data.model.LabOrder(
            orderId = "LAB-2026-302",
            patientId = "MF-2026-1011",
            patientName = "Sunil Joshi",
            tokenNumber = 101,
            prescribedByDoctor = "Dr. Rajesh Patil",
            testName = "Fasting Blood Glucose (FBG)",
            category = "Biochemistry",
            status = com.example.mediflow_ai.data.model.LabTestStatus.REQUESTED,
            sampleCollected = false,
            orderedAt = "09:40 AM"
        ),
        com.example.mediflow_ai.data.model.LabOrder(
            orderId = "LAB-2026-300",
            patientId = "MF-2026-1010",
            patientName = "Ramesh Gaikwad",
            tokenNumber = 100,
            prescribedByDoctor = "Dr. Rajesh Patil",
            testName = "Lipid Profile",
            category = "Biochemistry",
            status = com.example.mediflow_ai.data.model.LabTestStatus.COMPLETED,
            sampleCollected = true,
            orderedAt = "09:10 AM",
            sampleCollectedAt = "09:18 AM",
            completedAt = "09:40 AM",
            resultSummary = "Total Cholesterol: 182 mg/dL (Normal: <200), HDL: 49 mg/dL, LDL: 108 mg/dL. Within safe parameters.",
            isReportNotified = true
        )
    )

    private val _labOrders = MutableStateFlow(initialLabOrders)
    val labOrders: StateFlow<List<com.example.mediflow_ai.data.model.LabOrder>> = _labOrders.asStateFlow()

    private var labOrderCounter = 303

    fun createLabOrder(
        patientId: String,
        patientName: String,
        tokenNumber: Int,
        testName: String,
        category: String,
        doctorName: String = "Dr. Rajesh Patil"
    ) {
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val newOrder = com.example.mediflow_ai.data.model.LabOrder(
            orderId = "LAB-2026-${labOrderCounter++}",
            patientId = patientId,
            patientName = patientName,
            tokenNumber = tokenNumber,
            prescribedByDoctor = doctorName,
            testName = testName,
            category = category,
            status = com.example.mediflow_ai.data.model.LabTestStatus.REQUESTED,
            sampleCollected = false,
            orderedAt = currentTime,
            isReportNotified = false
        )
        _labOrders.update { listOf(newOrder) + it }
    }

    fun collectLabSample(orderId: String) {
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        _labOrders.update { list ->
            list.map { order ->
                if (order.orderId == orderId) {
                    order.copy(
                        status = com.example.mediflow_ai.data.model.LabTestStatus.PROCESSING,
                        sampleCollected = true,
                        sampleCollectedAt = currentTime
                    )
                } else order
            }
        }
    }

    fun completeLabReport(orderId: String, results: String) {
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        _labOrders.update { list ->
            list.map { order ->
                if (order.orderId == orderId) {
                    order.copy(
                        status = com.example.mediflow_ai.data.model.LabTestStatus.COMPLETED,
                        completedAt = currentTime,
                        resultSummary = results,
                        isReportNotified = true
                    )
                } else order
            }
        }
    }

    // --- Pharmacy & Medicine Inventory Management ---
    private val initialInventory = listOf(
        com.example.mediflow_ai.data.model.MedicineItem(
            medicineId = "MED-101",
            name = "Paracetamol 650mg",
            category = "Analgesic & Antipyretic",
            stockUnits = 140,
            minThreshold = 20,
            pricePerUnit = 2.50
        ),
        com.example.mediflow_ai.data.model.MedicineItem(
            medicineId = "MED-102",
            name = "Amoxicillin 500mg",
            category = "Antibiotic",
            stockUnits = 12, // Low stock trigger
            minThreshold = 20,
            pricePerUnit = 12.00
        ),
        com.example.mediflow_ai.data.model.MedicineItem(
            medicineId = "MED-103",
            name = "Pantoprazole 40mg",
            category = "Antacid / Gastro",
            stockUnits = 85,
            minThreshold = 20,
            pricePerUnit = 8.50
        ),
        com.example.mediflow_ai.data.model.MedicineItem(
            medicineId = "MED-104",
            name = "Metformin 500mg",
            category = "Antidiabetic",
            stockUnits = 110,
            minThreshold = 25,
            pricePerUnit = 4.00
        ),
        com.example.mediflow_ai.data.model.MedicineItem(
            medicineId = "MED-105",
            name = "Azithromycin 500mg",
            category = "Antibiotic",
            stockUnits = 8, // Critical Low stock trigger
            minThreshold = 20,
            pricePerUnit = 22.00
        ),
        com.example.mediflow_ai.data.model.MedicineItem(
            medicineId = "MED-106",
            name = "Cetirizine 10mg",
            category = "Antihistamine / Cold",
            stockUnits = 95,
            minThreshold = 20,
            pricePerUnit = 3.00
        ),
        com.example.mediflow_ai.data.model.MedicineItem(
            medicineId = "MED-107",
            name = "Amlodipine 5mg",
            category = "Antihypertensive",
            stockUnits = 60,
            minThreshold = 15,
            pricePerUnit = 5.50
        )
    )

    private val _medicineInventory = MutableStateFlow(initialInventory)
    val medicineInventory: StateFlow<List<com.example.mediflow_ai.data.model.MedicineItem>> = _medicineInventory.asStateFlow()

    private val initialPharmacyOrders = listOf(
        com.example.mediflow_ai.data.model.PharmacyOrder(
            orderId = "PHARM-501",
            patientId = "MF-2026-1011",
            patientName = "Sunil Joshi",
            tokenNumber = 101,
            prescribedByDoctor = "Dr. Rajesh Patil",
            items = listOf(
                com.example.mediflow_ai.data.model.PrescriptionItem("Paracetamol 650mg", "1-0-1 after food", 5, 10),
                com.example.mediflow_ai.data.model.PrescriptionItem("Pantoprazole 40mg", "1-0-0 empty stomach", 5, 5)
            ),
            status = com.example.mediflow_ai.data.model.PharmacyOrderStatus.PENDING,
            orderedAt = "09:35 AM",
            totalAmount = 67.50
        ),
        com.example.mediflow_ai.data.model.PharmacyOrder(
            orderId = "PHARM-502",
            patientId = "MF-2026-1012",
            patientName = "Sneha Kulkarni",
            tokenNumber = 102,
            prescribedByDoctor = "Dr. Rajesh Patil",
            items = listOf(
                com.example.mediflow_ai.data.model.PrescriptionItem("Amoxicillin 500mg", "1-0-1 after food", 3, 6),
                com.example.mediflow_ai.data.model.PrescriptionItem("Cetirizine 10mg", "0-0-1 at bedtime", 5, 5)
            ),
            status = com.example.mediflow_ai.data.model.PharmacyOrderStatus.PENDING,
            orderedAt = "09:42 AM",
            totalAmount = 87.00
        ),
        com.example.mediflow_ai.data.model.PharmacyOrder(
            orderId = "PHARM-500",
            patientId = "MF-2026-1010",
            patientName = "Ramesh Gaikwad",
            tokenNumber = 100,
            prescribedByDoctor = "Dr. Rajesh Patil",
            items = listOf(
                com.example.mediflow_ai.data.model.PrescriptionItem("Amlodipine 5mg", "1-0-0 morning", 10, 10)
            ),
            status = com.example.mediflow_ai.data.model.PharmacyOrderStatus.DISPENSED,
            orderedAt = "09:12 AM",
            dispensedAt = "09:20 AM",
            totalAmount = 55.00
        )
    )

    private val _pharmacyOrders = MutableStateFlow(initialPharmacyOrders)
    val pharmacyOrders: StateFlow<List<com.example.mediflow_ai.data.model.PharmacyOrder>> = _pharmacyOrders.asStateFlow()

    private var pharmacyOrderCounter = 503

    fun createPharmacyOrder(
        patientId: String,
        patientName: String,
        tokenNumber: Int,
        items: List<com.example.mediflow_ai.data.model.PrescriptionItem>,
        doctorName: String = "Dr. Rajesh Patil"
    ) {
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val calculatedAmount = items.sumOf { item ->
            val med = _medicineInventory.value.find { it.name == item.medicineName }
            (med?.pricePerUnit ?: 5.0) * item.quantity
        }
        val newOrder = com.example.mediflow_ai.data.model.PharmacyOrder(
            orderId = "PHARM-${pharmacyOrderCounter++}",
            patientId = patientId,
            patientName = patientName,
            tokenNumber = tokenNumber,
            prescribedByDoctor = doctorName,
            items = items,
            status = com.example.mediflow_ai.data.model.PharmacyOrderStatus.PENDING,
            orderedAt = currentTime,
            totalAmount = calculatedAmount
        )
        _pharmacyOrders.update { listOf(newOrder) + it }
    }

    fun dispensePharmacyOrder(orderId: String) {
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val orderToDispense = _pharmacyOrders.value.find { it.orderId == orderId } ?: return

        // 1. Deduct stock for each prescribed medicine
        _medicineInventory.update { inventory ->
            inventory.map { med ->
                val orderedItem = orderToDispense.items.find { it.medicineName == med.name }
                if (orderedItem != null) {
                    med.copy(stockUnits = maxOf(0, med.stockUnits - orderedItem.quantity))
                } else med
            }
        }

        // 2. Mark order as DISPENSED
        _pharmacyOrders.update { orders ->
            orders.map { order ->
                if (order.orderId == orderId) {
                    order.copy(
                        status = com.example.mediflow_ai.data.model.PharmacyOrderStatus.DISPENSED,
                        dispensedAt = currentTime
                    )
                } else order
            }
        }
    }

    fun restockMedicine(medicineId: String, unitsToAdd: Int) {
        _medicineInventory.update { inventory ->
            inventory.map { med ->
                if (med.medicineId == medicineId) {
                    med.copy(stockUnits = med.stockUnits + unitsToAdd)
                } else med
            }
        }
    }

    // --- Bed Management & Ward Occupancy ---
    private val initialBeds = listOf(
        // ICU WARD
        com.example.mediflow_ai.data.model.HospitalBed(
            bedId = "ICU-01",
            ward = com.example.mediflow_ai.data.model.WardType.ICU,
            bedNumber = 1,
            status = com.example.mediflow_ai.data.model.BedStatus.OCCUPIED,
            occupiedByPatientId = "MF-2026-1011",
            occupiedByPatientName = "Sunil Joshi",
            tokenNumber = 101,
            admittedAt = "Yesterday 08:30 PM"
        ),
        com.example.mediflow_ai.data.model.HospitalBed(
            bedId = "ICU-02",
            ward = com.example.mediflow_ai.data.model.WardType.ICU,
            bedNumber = 2,
            status = com.example.mediflow_ai.data.model.BedStatus.AVAILABLE
        ),
        com.example.mediflow_ai.data.model.HospitalBed(
            bedId = "ICU-03",
            ward = com.example.mediflow_ai.data.model.WardType.ICU,
            bedNumber = 3,
            status = com.example.mediflow_ai.data.model.BedStatus.CLEANING
        ),
        com.example.mediflow_ai.data.model.HospitalBed(
            bedId = "ICU-04",
            ward = com.example.mediflow_ai.data.model.WardType.ICU,
            bedNumber = 4,
            status = com.example.mediflow_ai.data.model.BedStatus.AVAILABLE
        ),

        // EMERGENCY & TRAUMA
        com.example.mediflow_ai.data.model.HospitalBed(
            bedId = "EMG-01",
            ward = com.example.mediflow_ai.data.model.WardType.EMERGENCY,
            bedNumber = 1,
            status = com.example.mediflow_ai.data.model.BedStatus.OCCUPIED,
            occupiedByPatientId = "MF-2026-1012",
            occupiedByPatientName = "Sneha Kulkarni",
            tokenNumber = 102,
            admittedAt = "Today 08:00 AM"
        ),
        com.example.mediflow_ai.data.model.HospitalBed(
            bedId = "EMG-02",
            ward = com.example.mediflow_ai.data.model.WardType.EMERGENCY,
            bedNumber = 2,
            status = com.example.mediflow_ai.data.model.BedStatus.AVAILABLE
        ),
        com.example.mediflow_ai.data.model.HospitalBed(
            bedId = "EMG-03",
            ward = com.example.mediflow_ai.data.model.WardType.EMERGENCY,
            bedNumber = 3,
            status = com.example.mediflow_ai.data.model.BedStatus.AVAILABLE
        ),

        // GENERAL WARD - MALE
        com.example.mediflow_ai.data.model.HospitalBed(
            bedId = "GEN-M-01",
            ward = com.example.mediflow_ai.data.model.WardType.GENERAL_MALE,
            bedNumber = 1,
            status = com.example.mediflow_ai.data.model.BedStatus.OCCUPIED,
            occupiedByPatientId = "MF-2026-1010",
            occupiedByPatientName = "Ramesh Gaikwad",
            tokenNumber = 100,
            admittedAt = "2 Days Ago"
        ),
        com.example.mediflow_ai.data.model.HospitalBed(
            bedId = "GEN-M-02",
            ward = com.example.mediflow_ai.data.model.WardType.GENERAL_MALE,
            bedNumber = 2,
            status = com.example.mediflow_ai.data.model.BedStatus.AVAILABLE
        ),
        com.example.mediflow_ai.data.model.HospitalBed(
            bedId = "GEN-M-03",
            ward = com.example.mediflow_ai.data.model.WardType.GENERAL_MALE,
            bedNumber = 3,
            status = com.example.mediflow_ai.data.model.BedStatus.AVAILABLE
        ),
        com.example.mediflow_ai.data.model.HospitalBed(
            bedId = "GEN-M-04",
            ward = com.example.mediflow_ai.data.model.WardType.GENERAL_MALE,
            bedNumber = 4,
            status = com.example.mediflow_ai.data.model.BedStatus.CLEANING
        ),

        // GENERAL WARD - FEMALE
        com.example.mediflow_ai.data.model.HospitalBed(
            bedId = "GEN-F-01",
            ward = com.example.mediflow_ai.data.model.WardType.GENERAL_FEMALE,
            bedNumber = 1,
            status = com.example.mediflow_ai.data.model.BedStatus.AVAILABLE
        ),
        com.example.mediflow_ai.data.model.HospitalBed(
            bedId = "GEN-F-02",
            ward = com.example.mediflow_ai.data.model.WardType.GENERAL_FEMALE,
            bedNumber = 2,
            status = com.example.mediflow_ai.data.model.BedStatus.AVAILABLE
        ),
        com.example.mediflow_ai.data.model.HospitalBed(
            bedId = "GEN-F-03",
            ward = com.example.mediflow_ai.data.model.WardType.GENERAL_FEMALE,
            bedNumber = 3,
            status = com.example.mediflow_ai.data.model.BedStatus.OCCUPIED,
            occupiedByPatientId = "MF-2026-1009",
            occupiedByPatientName = "Priya Sharma",
            tokenNumber = 99,
            admittedAt = "Today 07:15 AM"
        ),
        com.example.mediflow_ai.data.model.HospitalBed(
            bedId = "GEN-F-04",
            ward = com.example.mediflow_ai.data.model.WardType.GENERAL_FEMALE,
            bedNumber = 4,
            status = com.example.mediflow_ai.data.model.BedStatus.AVAILABLE
        )
    )

    private val _hospitalBeds = MutableStateFlow(initialBeds)
    val hospitalBeds: StateFlow<List<com.example.mediflow_ai.data.model.HospitalBed>> = _hospitalBeds.asStateFlow()

    fun admitPatientToBed(
        bedId: String,
        patientId: String,
        patientName: String,
        tokenNumber: Int
    ) {
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        _hospitalBeds.update { beds ->
            beds.map { bed ->
                if (bed.bedId == bedId) {
                    bed.copy(
                        status = com.example.mediflow_ai.data.model.BedStatus.OCCUPIED,
                        occupiedByPatientId = patientId,
                        occupiedByPatientName = patientName,
                        tokenNumber = tokenNumber,
                        admittedAt = "Today $currentTime"
                    )
                } else bed
            }
        }
    }

    fun dischargePatientFromBed(bedId: String) {
        _hospitalBeds.update { beds ->
            beds.map { bed ->
                if (bed.bedId == bedId) {
                    bed.copy(
                        status = com.example.mediflow_ai.data.model.BedStatus.CLEANING,
                        occupiedByPatientId = null,
                        occupiedByPatientName = null,
                        tokenNumber = null,
                        admittedAt = null
                    )
                } else bed
            }
        }
    }

    fun markBedCleaned(bedId: String) {
        _hospitalBeds.update { beds ->
            beds.map { bed ->
                if (bed.bedId == bedId) {
                    bed.copy(
                        status = com.example.mediflow_ai.data.model.BedStatus.AVAILABLE
                    )
                } else bed
            }
        }
    }

    // --- Consolidated Hospital Billing Management ---
    private val initialInvoices = listOf(
        com.example.mediflow_ai.data.model.PatientInvoice(
            invoiceId = "INV-2026-901",
            patientId = "MF-2026-1011",
            patientName = "Sunil Joshi",
            tokenNumber = 101,
            lineItems = listOf(
                com.example.mediflow_ai.data.model.BillLineItem("Doctor Consultation - General Medicine", "Consultation", 500.0),
                com.example.mediflow_ai.data.model.BillLineItem("Fasting Blood Glucose (FBG)", "Laboratory", 250.0),
                com.example.mediflow_ai.data.model.BillLineItem("Prescription Dispensing #PHARM-501", "Pharmacy", 67.50),
                com.example.mediflow_ai.data.model.BillLineItem("ICU Bed Tariff (#ICU-01) - 1 Day", "Bed & Room", 4500.0)
            ),
            subtotal = 5317.50,
            discountAmount = 0.0,
            totalPayable = 5317.50,
            paidAmount = 0.0,
            balanceDue = 5317.50,
            status = com.example.mediflow_ai.data.model.PaymentStatus.PENDING,
            generatedAt = "Today 09:45 AM"
        ),
        com.example.mediflow_ai.data.model.PatientInvoice(
            invoiceId = "INV-2026-902",
            patientId = "MF-2026-1012",
            patientName = "Sneha Kulkarni",
            tokenNumber = 102,
            lineItems = listOf(
                com.example.mediflow_ai.data.model.BillLineItem("Doctor Consultation - General Medicine", "Consultation", 500.0),
                com.example.mediflow_ai.data.model.BillLineItem("Complete Blood Count (CBC)", "Laboratory", 350.0),
                com.example.mediflow_ai.data.model.BillLineItem("Prescription Dispensing #PHARM-502", "Pharmacy", 87.00),
                com.example.mediflow_ai.data.model.BillLineItem("Emergency Bed Tariff (#EMG-01) - 1 Day", "Bed & Room", 2500.0)
            ),
            subtotal = 3437.00,
            discountAmount = 0.0,
            totalPayable = 3437.00,
            paidAmount = 0.0,
            balanceDue = 3437.00,
            status = com.example.mediflow_ai.data.model.PaymentStatus.PENDING,
            generatedAt = "Today 09:48 AM"
        ),
        com.example.mediflow_ai.data.model.PatientInvoice(
            invoiceId = "INV-2026-900",
            patientId = "MF-2026-1010",
            patientName = "Ramesh Gaikwad",
            tokenNumber = 100,
            lineItems = listOf(
                com.example.mediflow_ai.data.model.BillLineItem("Doctor Consultation - General Medicine", "Consultation", 500.0),
                com.example.mediflow_ai.data.model.BillLineItem("Lipid Profile Test", "Laboratory", 600.0),
                com.example.mediflow_ai.data.model.BillLineItem("Prescription Dispensing #PHARM-500", "Pharmacy", 55.00),
                com.example.mediflow_ai.data.model.BillLineItem("General Ward Bed (#GEN-M-01) - 2 Days", "Bed & Room", 2400.0)
            ),
            subtotal = 3555.00,
            discountAmount = 155.00,
            totalPayable = 3400.00,
            paidAmount = 3400.00,
            balanceDue = 0.0,
            status = com.example.mediflow_ai.data.model.PaymentStatus.PAID,
            paymentMode = com.example.mediflow_ai.data.model.PaymentMode.UPI_ONLINE,
            generatedAt = "Yesterday 09:15 AM",
            paidAt = "Today 09:30 AM"
        )
    )

    private val _invoices = MutableStateFlow(initialInvoices)
    val invoices: StateFlow<List<com.example.mediflow_ai.data.model.PatientInvoice>> = _invoices.asStateFlow()

    fun recordPayment(
        invoiceId: String,
        amountPaid: Double,
        mode: com.example.mediflow_ai.data.model.PaymentMode
    ) {
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        _invoices.update { list ->
            list.map { invoice ->
                if (invoice.invoiceId == invoiceId) {
                    val newPaid = invoice.paidAmount + amountPaid
                    val newBalance = maxOf(0.0, invoice.totalPayable - newPaid)
                    val newStatus = if (newBalance <= 0.0) {
                        com.example.mediflow_ai.data.model.PaymentStatus.PAID
                    } else {
                        com.example.mediflow_ai.data.model.PaymentStatus.PARTIALLY_PAID
                    }
                    invoice.copy(
                        paidAmount = newPaid,
                        balanceDue = newBalance,
                        status = newStatus,
                        paymentMode = mode,
                        paidAt = if (newStatus == com.example.mediflow_ai.data.model.PaymentStatus.PAID) currentTime else invoice.paidAt
                    )
                } else invoice
            }
        }
    }
}
