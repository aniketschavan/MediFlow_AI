package com.example.mediflow_ai.ui.common

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediflow_ai.ui.theme.HospitalGreen
import com.example.mediflow_ai.ui.theme.MedicalBlue
import com.example.mediflow_ai.ui.theme.TealPrimary
import com.example.mediflow_ai.ui.theme.TextPrimary
import com.example.mediflow_ai.ui.theme.TextSecondary
import com.example.mediflow_ai.ui.theme.WarningAmber

data class HospitalNotification(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val icon: ImageVector,
    val iconColor: Color
)

@Composable
fun NotificationsDialog(
    onDismiss: () -> Unit
) {
    val sampleNotifications = listOf(
        HospitalNotification(
            id = "NOTIF-1",
            title = "Doctor Consultation Active",
            description = "Token #101 (Sunil Joshi) called into OPD Cabin 104.",
            timestamp = "Just now",
            icon = Icons.Default.CheckCircle,
            iconColor = HospitalGreen
        ),
        HospitalNotification(
            id = "NOTIF-2",
            title = "Lab Report Certified",
            description = "Lipid Profile results for Token #100 verified by Senior Pathologist.",
            timestamp = "12 mins ago",
            icon = Icons.Default.Science,
            iconColor = MedicalBlue
        ),
        HospitalNotification(
            id = "NOTIF-3",
            title = "Pharmacy Dispensing Complete",
            description = "Prescription #PHARM-500 dispensed at Pharmacy Counter 2.",
            timestamp = "25 mins ago",
            icon = Icons.Default.LocalPharmacy,
            iconColor = TealPrimary
        ),
        HospitalNotification(
            id = "NOTIF-4",
            title = "Inventory Low Stock Alert",
            description = "Azithromycin 500mg has 8 units remaining (Threshold: 20).",
            timestamp = "40 mins ago",
            icon = Icons.Default.Warning,
            iconColor = WarningAmber
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = TealPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Hospital Event Feed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Real-time workflow alerts",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sampleNotifications) { notif ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF7FBFB), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = notif.iconColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = notif.icon,
                                    contentDescription = null,
                                    tint = notif.iconColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = notif.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = notif.timestamp,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = notif.description,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Close", color = Color.White)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}
