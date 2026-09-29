package com.example.mediflow_ai.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.mediflow_ai.ui.beds.BedManagementScreen
import com.example.mediflow_ai.ui.billing.BillingScreen
import com.example.mediflow_ai.ui.doctor.DoctorQueueScreen
import com.example.mediflow_ai.ui.lab.LabWorklistScreen
import com.example.mediflow_ai.ui.patient.PatientRegistrationScreen
import com.example.mediflow_ai.ui.pharmacy.PharmacyScreen
import com.example.mediflow_ai.ui.theme.TealPrimary

enum class AppDestination(val title: String, val icon: ImageVector) {
    PATIENT_REGISTRATION("Patient", Icons.Default.PersonAdd),
    DOCTOR_QUEUE("Doctor", Icons.Default.MedicalServices),
    LAB_WORKLIST("Lab", Icons.Default.Science),
    PHARMACY("Pharmacy", Icons.Default.LocalPharmacy),
    BEDS("Beds", Icons.Default.Hotel),
    BILLING("Billing", Icons.Default.Payment)
}

@Composable
fun MainAppScaffold() {
    var currentDestination by remember { mutableStateOf(AppDestination.PATIENT_REGISTRATION) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = NavigationBarDefaults.Elevation
            ) {
                AppDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealPrimary.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestination.PATIENT_REGISTRATION -> PatientRegistrationScreen()
                AppDestination.DOCTOR_QUEUE -> DoctorQueueScreen()
                AppDestination.LAB_WORKLIST -> LabWorklistScreen()
                AppDestination.PHARMACY -> PharmacyScreen()
                AppDestination.BEDS -> BedManagementScreen()
                AppDestination.BILLING -> BillingScreen()
            }
        }
    }
}
