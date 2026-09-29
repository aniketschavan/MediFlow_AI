package com.example.mediflow_ai.data.model

enum class LabTestStatus(val label: String) {
    REQUESTED("Requested"),
    PROCESSING("Processing"),
    COMPLETED("Completed")
}

data class LabCatalogItem(
    val name: String,
    val category: String,
    val standardDuration: String
)

data class LabOrder(
    val orderId: String,
    val patientId: String,
    val patientName: String,
    val tokenNumber: Int,
    val prescribedByDoctor: String = "Dr. Rajesh Patil",
    val testName: String,
    val category: String,
    val status: LabTestStatus = LabTestStatus.REQUESTED,
    val sampleCollected: Boolean = false,
    val orderedAt: String,
    val sampleCollectedAt: String? = null,
    val completedAt: String? = null,
    val resultSummary: String? = null,
    val isReportNotified: Boolean = false
)

object StandardLabCatalog {
    val items = listOf(
        LabCatalogItem("Complete Blood Count (CBC)", "Hematology", "30 mins"),
        LabCatalogItem("Fasting Blood Glucose (FBG)", "Biochemistry", "20 mins"),
        LabCatalogItem("Lipid Profile", "Biochemistry", "45 mins"),
        LabCatalogItem("Liver Function Test (LFT)", "Biochemistry", "45 mins"),
        LabCatalogItem("Kidney Function Test (KFT)", "Biochemistry", "45 mins"),
        LabCatalogItem("Urine Routine & Microscopic", "Pathology", "25 mins"),
        LabCatalogItem("Serum Electrolytes (Na+, K+)", "Biochemistry", "30 mins")
    )
}
