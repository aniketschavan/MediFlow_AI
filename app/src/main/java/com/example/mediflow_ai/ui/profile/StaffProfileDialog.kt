package com.example.mediflow_ai.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediflow_ai.data.repository.HospitalRepository
import com.example.mediflow_ai.ui.theme.HospitalGreen
import com.example.mediflow_ai.ui.theme.LightGreenBadge
import com.example.mediflow_ai.ui.theme.MedicalBlue
import com.example.mediflow_ai.ui.theme.TextPrimary
import com.example.mediflow_ai.ui.theme.TextSecondary
import com.example.mediflow_ai.ui.theme.WarningAmber

@Composable
fun StaffProfileDialog(
    staffName: String,
    roleName: String,
    onLogoutClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val doctorProfile by HospitalRepository.doctorProfile.collectAsState()
    val queueItems by HospitalRepository.queueItems.collectAsState()
    val beds by HospitalRepository.hospitalBeds.collectAsState()

    val waitingCount = queueItems.count { it.consultationStatus == com.example.mediflow_ai.data.model.ConsultationStatus.WAITING }
    val completedCount = queueItems.count { it.consultationStatus == com.example.mediflow_ai.data.model.ConsultationStatus.COMPLETED }
    val occupiedBeds = beds.count { it.status == com.example.mediflow_ai.data.model.BedStatus.OCCUPIED }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Doctor / Staff Avatar
                Surface(
                    shape = CircleShape,
                    color = MedicalBlue,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = staffName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "$roleName • Cabin 104",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(12.dp))

                // Shift & Duty Availability Toggle
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (doctorProfile.isAvailable) LightGreenBadge else Color(0xFFFFF3E0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (doctorProfile.isAvailable) "On Duty (Accepting Patients)" else "Off Duty / On Break",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (doctorProfile.isAvailable) HospitalGreen else WarningAmber
                            )
                            Text(
                                text = "Live hospital-wide availability toggle",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Switch(
                            checked = doctorProfile.isAvailable,
                            onCheckedChange = { HospitalRepository.toggleDoctorAvailability() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = HospitalGreen,
                                checkedTrackColor = LightGreenBadge
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Clinical Credentials
                Text(
                    text = "Clinical Credentials & Hospital ID",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MedicalBlue
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF7FBFB),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StaffInfoItem(label = "Medical License", value = "MCI-2018-99214")
                            StaffInfoItem(label = "Employee ID", value = doctorProfile.doctorId)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        StaffInfoItem(
                            label = "Primary Specialization",
                            value = doctorProfile.specialty
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StaffInfoItem(
                            label = "Assigned Location",
                            value = "${doctorProfile.roomNumber} (First Floor, OPD Block)"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Shift KPI Summary
                Text(
                    text = "Today's Clinical Shift Metrics",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiBox(label = "In Queue", value = "$waitingCount", color = MedicalBlue, modifier = Modifier.weight(1f))
                    KpiBox(label = "Consulted", value = "$completedCount", color = HospitalGreen, modifier = Modifier.weight(1f))
                    KpiBox(label = "Beds Occ.", value = "$occupiedBeds/16", color = WarningAmber, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sign Out Button inside Profile
                OutlinedButton(
                    onClick = onLogoutClick,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign Out of Staff Console",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Close", color = Color.White)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
private fun StaffInfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
private fun KpiBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.10f),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}
