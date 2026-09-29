package com.example.mediflow_ai.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediflow_ai.data.repository.HospitalRepository
import com.example.mediflow_ai.ui.theme.HospitalGreen
import com.example.mediflow_ai.ui.theme.MedicalBackground
import com.example.mediflow_ai.ui.theme.MedicalBlue
import com.example.mediflow_ai.ui.theme.TealPrimary
import com.example.mediflow_ai.ui.theme.TextPrimary
import com.example.mediflow_ai.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    initialTab: LoginTab = LoginTab.PATIENT,
    onLoginPatient: (patientId: String, patientName: String, tokenNumber: Int?) -> Unit,
    onLoginStaff: (roleName: String, staffName: String) -> Unit,
    onNavigateToRegistration: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(initialTab) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var showStaffRequestDialog by remember { mutableStateOf(false) }

    // Patient Form State & Validation
    var patientIdentifier by remember { mutableStateOf("") }
    var patientPassword by remember { mutableStateOf("") }
    var isPatientPasswordVisible by remember { mutableStateOf(false) }
    var rememberPatient by remember { mutableStateOf(true) }
    var patientIdError by remember { mutableStateOf<String?>(null) }
    var patientPasswordError by remember { mutableStateOf<String?>(null) }

    // Staff Form State & Validation
    var staffEmail by remember { mutableStateOf("") }
    var staffPassword by remember { mutableStateOf("") }
    var isStaffPasswordVisible by remember { mutableStateOf(false) }
    var selectedStaffRole by remember { mutableStateOf("Doctor (OPD Physician)") }
    var isRoleDropdownExpanded by remember { mutableStateOf(false) }
    var staffEmailError by remember { mutableStateOf<String?>(null) }
    var staffPasswordError by remember { mutableStateOf<String?>(null) }

    // Forgot Password Recovery Dialog
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var recoveryPhoneInput by remember { mutableStateOf("") }
    var isRecoveryOtpSent by remember { mutableStateOf(false) }

    val staffRoles = listOf(
        "Doctor (OPD Physician)",
        "Hospital Administrator",
        "OPD Receptionist",
        "Laboratory Technician",
        "Clinical Pharmacist"
    )

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MedicalBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. BRAND IDENTITY & HEADER BLOCK
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = TealPrimary,
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
                Column {
                    Text(
                        text = "MediFlow AI",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TealPrimary
                    )
                    Text(
                        text = "Smart Hospital Workflow Automation",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 2. ROLE-BASED SEGMENTED SWITCHER
            TabRow(
                selectedTabIndex = if (selectedTab == LoginTab.PATIENT) 0 else 1,
                containerColor = Color.White,
                contentColor = TealPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (selectedTab == LoginTab.PATIENT) 0 else 1]),
                        color = if (selectedTab == LoginTab.PATIENT) TealPrimary else MedicalBlue,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == LoginTab.PATIENT,
                    onClick = {
                        selectedTab = LoginTab.PATIENT
                        patientIdError = null
                        patientPasswordError = null
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (selectedTab == LoginTab.PATIENT) TealPrimary else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Patient Portal",
                                fontWeight = if (selectedTab == LoginTab.PATIENT) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == LoginTab.PATIENT) TealPrimary else TextSecondary
                            )
                        }
                    }
                )

                Tab(
                    selected = selectedTab == LoginTab.STAFF,
                    onClick = {
                        selectedTab = LoginTab.STAFF
                        staffEmailError = null
                        staffPasswordError = null
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (selectedTab == LoginTab.STAFF) MedicalBlue else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hospital Staff",
                                fontWeight = if (selectedTab == LoginTab.STAFF) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == LoginTab.STAFF) MedicalBlue else TextSecondary
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. MAIN FORM CARD
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (selectedTab == LoginTab.PATIENT) {
                        // --- PATIENT LOGIN FORM ---
                        Text(
                            text = "Patient Sign In",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Access your live OPD queue token, doctor prescriptions, and verified lab reports.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                        )

                        // Identifier Input (Phone or Patient ID)
                        OutlinedTextField(
                            value = patientIdentifier,
                            onValueChange = {
                                patientIdentifier = it
                                if (patientIdError != null) patientIdError = null
                            },
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            label = { Text("Mobile Number or Patient ID") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = TealPrimary)
                            },
                            trailingIcon = {
                                if (patientIdentifier.isNotEmpty()) {
                                    IconButton(onClick = { patientIdentifier = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                                    }
                                }
                            },
                            isError = patientIdError != null,
                            supportingText = {
                                patientIdError?.let { Text(it, color = Color(0xFFD32F2F), fontSize = 12.sp) }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next,
                                autoCorrectEnabled = false
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = TealPrimary,
                                unfocusedBorderColor = Color(0xFFB0BEC5),
                                focusedLabelColor = TealPrimary,
                                unfocusedLabelColor = TextSecondary,
                                cursorColor = TealPrimary,
                                focusedPlaceholderColor = TextSecondary.copy(alpha = 0.6f),
                                unfocusedPlaceholderColor = TextSecondary.copy(alpha = 0.6f),
                                focusedLeadingIconColor = TealPrimary,
                                unfocusedLeadingIconColor = TextSecondary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Password / OTP Input
                        OutlinedTextField(
                            value = patientPassword,
                            onValueChange = {
                                patientPassword = it
                                if (patientPasswordError != null) patientPasswordError = null
                            },
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            label = { Text("Security PIN or Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = TealPrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPatientPasswordVisible = !isPatientPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPatientPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = TextSecondary
                                    )
                                }
                            },
                            isError = patientPasswordError != null,
                            supportingText = {
                                patientPasswordError?.let { Text(it, color = Color(0xFFD32F2F), fontSize = 12.sp) }
                            },
                            visualTransformation = if (isPatientPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                                autoCorrectEnabled = false
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = TealPrimary,
                                unfocusedBorderColor = Color(0xFFB0BEC5),
                                focusedLabelColor = TealPrimary,
                                unfocusedLabelColor = TextSecondary,
                                cursorColor = TealPrimary,
                                focusedPlaceholderColor = TextSecondary.copy(alpha = 0.6f),
                                unfocusedPlaceholderColor = TextSecondary.copy(alpha = 0.6f),
                                focusedLeadingIconColor = TealPrimary,
                                unfocusedLeadingIconColor = TextSecondary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 4. REMEMBER ME & FORGOT PASSWORD ACTIONS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = rememberPatient,
                                    onCheckedChange = { rememberPatient = it },
                                    colors = CheckboxDefaults.colors(checkedColor = TealPrimary)
                                )
                                Text(
                                    text = "Remember me",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }

                            TextButton(
                                onClick = {
                                    recoveryPhoneInput = patientIdentifier
                                    isRecoveryOtpSent = false
                                    showForgotPasswordDialog = true
                                }
                            ) {
                                Text(
                                    text = "Forgot PIN?",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 5. PRIMARY SUBMIT BUTTON WITH LOADING FEEDBACK
                        Button(
                            onClick = {
                                // Validation
                                var hasError = false
                                if (patientIdentifier.isBlank()) {
                                    patientIdError = "Please enter mobile number or Patient ID"
                                    hasError = true
                                }
                                if (patientPassword.length < 4) {
                                    patientPasswordError = "Security PIN must be at least 4 digits"
                                    hasError = true
                                } else if (patientPassword != "1234") {
                                    patientPasswordError = "Invalid PIN. Standard test PIN is: 1234"
                                    hasError = true
                                }

                                if (!hasError) {
                                    isAuthenticating = true
                                    coroutineScope.launch {
                                        delay(500) // Simulated secure authentication handshake
                                        isAuthenticating = false
                                        val cleanInput = patientIdentifier.trim()
                                        val activeTokenItem = HospitalRepository.getActiveTokenForPatient(cleanInput)
                                        val registeredPatient = HospitalRepository.getPatientByIdOrPhone(cleanInput)

                                        if (activeTokenItem != null) {
                                            // Patient has an ACTIVE live queue token today (Waiting or In Consultation)
                                            onLoginPatient(
                                                activeTokenItem.patient.patientId,
                                                activeTokenItem.patient.fullName,
                                                activeTokenItem.patient.tokenNumber
                                            )
                                        } else if (registeredPatient != null) {
                                            // Patient is registered in the hospital database, but has NO active token today
                                            // (e.g. consultation finished, or viewing records / booking visit)
                                            onLoginPatient(
                                                registeredPatient.patientId,
                                                registeredPatient.fullName,
                                                null
                                            )
                                        } else {
                                            // New unregistered patient credentials
                                            val digits = cleanInput.filter { it.isDigit() }
                                            val patientId = if (cleanInput.startsWith("MF-", ignoreCase = true)) cleanInput else "MF-2026-${digits.takeLast(4).ifEmpty { "1099" }}"
                                            onLoginPatient(
                                                patientId,
                                                "Patient ($patientId)",
                                                null
                                            )
                                        }
                                    }
                                }
                            },
                            enabled = !isAuthenticating,
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isAuthenticating) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verifying Patient...", color = Color.White, fontSize = 15.sp)
                            } else {
                                Text(
                                    text = "Sign In as Patient",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // New Patient Registration Link
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "New to MediFlow Hospital?",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(onClick = onNavigateToRegistration) {
                                Text(
                                    text = "Create Patient Account",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                            }
                        }

                    } else {
                        // --- STAFF LOGIN FORM ---
                        Text(
                            text = "Hospital Staff Sign In",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Clinical operations portal for OPD Doctors, Technicians, Pharmacists, and Hospital Admins.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                        )

                        // Role Selector Dropdown
                        ExposedDropdownMenuBox(
                            expanded = isRoleDropdownExpanded,
                            onExpandedChange = { isRoleDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedStaffRole,
                                onValueChange = {},
                                readOnly = true,
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                label = { Text("Assigned Clinical Role") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRoleDropdownExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = MedicalBlue,
                                    unfocusedBorderColor = Color(0xFFB0BEC5),
                                    focusedLabelColor = MedicalBlue,
                                    unfocusedLabelColor = TextSecondary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            )

                            ExposedDropdownMenu(
                                expanded = isRoleDropdownExpanded,
                                onDismissRequest = { isRoleDropdownExpanded = false }
                            ) {
                                staffRoles.forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role, fontSize = 14.sp, color = TextPrimary) },
                                        onClick = {
                                            selectedStaffRole = role
                                            isRoleDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Staff Email / ID
                        OutlinedTextField(
                            value = staffEmail,
                            onValueChange = {
                                staffEmail = it
                                if (staffEmailError != null) staffEmailError = null
                            },
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            label = { Text("Official Email / Staff ID") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = MedicalBlue)
                            },
                            trailingIcon = {
                                if (staffEmail.isNotEmpty()) {
                                    IconButton(onClick = { staffEmail = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                                    }
                                }
                            },
                            isError = staffEmailError != null,
                            supportingText = {
                                staffEmailError?.let { Text(it, color = Color(0xFFD32F2F), fontSize = 12.sp) }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                                autoCorrectEnabled = false
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = MedicalBlue,
                                unfocusedBorderColor = Color(0xFFB0BEC5),
                                focusedLabelColor = MedicalBlue,
                                unfocusedLabelColor = TextSecondary,
                                cursorColor = MedicalBlue,
                                focusedPlaceholderColor = TextSecondary.copy(alpha = 0.6f),
                                unfocusedPlaceholderColor = TextSecondary.copy(alpha = 0.6f),
                                focusedLeadingIconColor = MedicalBlue,
                                unfocusedLeadingIconColor = TextSecondary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Staff Password / PIN
                        OutlinedTextField(
                            value = staffPassword,
                            onValueChange = {
                                staffPassword = it
                                if (staffPasswordError != null) staffPasswordError = null
                            },
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            label = { Text("Security Password / PIN") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MedicalBlue)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isStaffPasswordVisible = !isStaffPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isStaffPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = TextSecondary
                                    )
                                }
                            },
                            isError = staffPasswordError != null,
                            supportingText = {
                                staffPasswordError?.let { Text(it, color = Color(0xFFD32F2F), fontSize = 12.sp) }
                            },
                            visualTransformation = if (isStaffPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                                autoCorrectEnabled = false
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = MedicalBlue,
                                unfocusedBorderColor = Color(0xFFB0BEC5),
                                focusedLabelColor = MedicalBlue,
                                unfocusedLabelColor = TextSecondary,
                                cursorColor = MedicalBlue,
                                focusedPlaceholderColor = TextSecondary.copy(alpha = 0.6f),
                                unfocusedPlaceholderColor = TextSecondary.copy(alpha = 0.6f),
                                focusedLeadingIconColor = MedicalBlue,
                                unfocusedLeadingIconColor = TextSecondary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Primary Submit Button with Loading
                        Button(
                            onClick = {
                                var hasError = false
                                if (staffEmail.isBlank()) {
                                    staffEmailError = "Please enter official email or ID"
                                    hasError = true
                                }
                                if (staffPassword.length < 4) {
                                    staffPasswordError = "Password must be at least 4 characters"
                                    hasError = true
                                } else if (staffPassword != "1234" && staffPassword != "admin") {
                                    staffPasswordError = "Invalid password. Standard test password is: 1234"
                                    hasError = true
                                }

                                if (!hasError) {
                                    isAuthenticating = true
                                    coroutineScope.launch {
                                        delay(500)
                                        isAuthenticating = false
                                        val resolvedRole = when {
                                            staffEmail.contains("admin", ignoreCase = true) -> "Hospital Administrator"
                                            staffEmail.contains("doc", ignoreCase = true) -> "Doctor (OPD Physician)"
                                            staffEmail.contains("lab", ignoreCase = true) -> "Laboratory Technician"
                                            staffEmail.contains("pharm", ignoreCase = true) -> "Clinical Pharmacist"
                                            staffEmail.contains("recep", ignoreCase = true) -> "OPD Receptionist"
                                            else -> selectedStaffRole
                                        }
                                        val staffName = when {
                                            resolvedRole.contains("Doctor") -> "Dr. Rajesh Sharma"
                                            resolvedRole.contains("Admin") -> "Hospital Administrator"
                                            resolvedRole.contains("Lab") -> "Diagnostic Operations"
                                            resolvedRole.contains("Pharm") -> "Pharmacy Operations"
                                            else -> {
                                                val namePart = staffEmail.substringBefore("@").replace(".", " ")
                                                    .split(" ")
                                                    .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                                                if (namePart.isNotBlank()) namePart else resolvedRole
                                            }
                                        }
                                        onLoginStaff(resolvedRole, staffName)
                                    }
                                }
                            },
                            enabled = !isAuthenticating,
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isAuthenticating) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Authenticating Staff...", color = Color.White, fontSize = 15.sp)
                            } else {
                                Text(
                                    text = "Sign In to Clinical Hub",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Staff Account Request
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "New staff member?",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(onClick = { showStaffRequestDialog = true }) {
                                Text(
                                    text = "Request Staff Account",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MedicalBlue
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 7. SECURITY & ABDM COMPLIANCE FOOTER
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = HospitalGreen,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "End-to-End Encrypted • ABDM Digital Health Compliant",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "MediFlow AI • Smart Hospital Automation System • v1.0",
                fontSize = 12.sp,
                color = TextSecondary.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // 8. FORGOT PASSWORD / OTP RECOVERY MODAL
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset Access PIN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enter your registered mobile number to receive a temporary 4-digit verification OTP.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = recoveryPhoneInput,
                        onValueChange = { recoveryPhoneInput = it },
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        label = { Text("Mobile Number") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            autoCorrectEnabled = false
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = Color(0xFFB0BEC5),
                            focusedLabelColor = TealPrimary,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = TealPrimary,
                            focusedPlaceholderColor = TextSecondary.copy(alpha = 0.6f),
                            unfocusedPlaceholderColor = TextSecondary.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isRecoveryOtpSent) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = HospitalGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "OTP sent to $recoveryPhoneInput. Temporary PIN: 1234",
                                    fontSize = 12.sp,
                                    color = HospitalGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isRecoveryOtpSent) {
                            isRecoveryOtpSent = true
                        } else {
                            patientPassword = "1234"
                            showForgotPasswordDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (!isRecoveryOtpSent) "Send OTP" else "Done",
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    // 9. STAFF ACCOUNT ACCESS REQUEST DIALOG
    if (showStaffRequestDialog) {
        AlertDialog(
            onDismissRequest = { showStaffRequestDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = MedicalBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clinical Staff Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Hospital staff accounts (Doctors, Nurses, Technicians, and Admin) are provisioned directly by the Medical Superintendent & Hospital Administration.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE3F2FD),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Hospital IT Support Desk:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalBlue
                            )
                            Text(
                                text = "Email: it-admin@mediflow.org\nInternal Extension: Ext. 104 / Block A",
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showStaffRequestDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Understood", color = Color.White)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}
