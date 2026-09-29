package com.example.mediflow_ai.ui.patient

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediflow_ai.ui.auth.AuthState
import com.example.mediflow_ai.ui.common.LogoutConfirmationDialog
import com.example.mediflow_ai.ui.common.NotificationsDialog
import com.example.mediflow_ai.ui.profile.PatientProfileDialog
import com.example.mediflow_ai.ui.theme.TealPrimary
import com.example.mediflow_ai.ui.theme.TextPrimary
import com.example.mediflow_ai.ui.theme.TextSecondary

enum class PatientDestination(val title: String, val icon: ImageVector) {
    HOME("My Visit", Icons.Default.Home),
    PRESCRIPTIONS("Medicines", Icons.Default.LocalPharmacy),
    LAB_REPORTS("Lab Tests", Icons.Default.Science),
    BILLING("My Bills", Icons.Default.Payment)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientPortalScaffold(
    session: AuthState.PatientSession,
    onLogout: () -> Unit
) {
    var activePatientId by remember(session) { mutableStateOf(session.patientId) }
    var activePatientName by remember(session) { mutableStateOf(session.patientName) }
    var activeTokenNumber by remember(session) { mutableStateOf(session.tokenNumber) }

    var currentDestination by remember { mutableStateOf(PatientDestination.HOME) }

    // Dialog States
    var showProfileDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MediFlow AI",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (activeTokenNumber != null) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = TealPrimary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "Token #$activeTokenNumber",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TealPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = "Outpatient",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "$activePatientName • ID: $activePatientId",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    // 1. Notification Bell
                    IconButton(onClick = { showNotificationsDialog = true }) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Color(0xFFD32F2F),
                                    modifier = Modifier.size(7.dp)
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Hospital Notifications",
                                tint = TextSecondary
                            )
                        }
                    }

                    // 2. User Avatar Button (Opens Profile Dialog)
                    Surface(
                        shape = CircleShape,
                        color = TealPrimary,
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { showProfileDialog = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = activePatientName.take(1).uppercase(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // 3. Logout Action Button
                    IconButton(onClick = { showLogoutConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Log Out",
                            tint = Color(0xFFD32F2F)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = NavigationBarDefaults.Elevation
            ) {
                PatientDestination.values().forEach { destination ->
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
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
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
                PatientDestination.HOME -> PatientHomeScreen(
                    patientId = activePatientId,
                    patientName = activePatientName,
                    tokenNumber = activeTokenNumber,
                    onTokenGenerated = { newToken ->
                        activeTokenNumber = newToken
                    }
                )
                PatientDestination.PRESCRIPTIONS -> PatientPrescriptionsScreen(
                    patientId = activePatientId
                )
                PatientDestination.LAB_REPORTS -> PatientLabReportsScreen(
                    patientId = activePatientId
                )
                PatientDestination.BILLING -> PatientBillingScreen(
                    patientId = activePatientId
                )
            }
        }
    }

    // --- Profile Modal Dialog ---
    if (showProfileDialog) {
        PatientProfileDialog(
            patientId = activePatientId,
            patientName = activePatientName,
            tokenNumber = activeTokenNumber,
            onLogoutClick = {
                showProfileDialog = false
                showLogoutConfirmDialog = true
            },
            onDismiss = { showProfileDialog = false }
        )
    }

    // --- Notifications Dialog ---
    if (showNotificationsDialog) {
        NotificationsDialog(
            onDismiss = { showNotificationsDialog = false }
        )
    }

    // --- Logout Confirmation Dialog ---
    if (showLogoutConfirmDialog) {
        LogoutConfirmationDialog(
            onConfirmLogout = {
                showLogoutConfirmDialog = false
                onLogout()
            },
            onDismiss = { showLogoutConfirmDialog = false }
        )
    }
}
