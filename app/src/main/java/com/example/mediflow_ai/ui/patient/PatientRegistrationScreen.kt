package com.example.mediflow_ai.ui.patient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mediflow_ai.data.model.Patient
import com.example.mediflow_ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRegistrationScreen(
    viewModel: PatientRegistrationViewModel = viewModel(),
    onBackToLogin: (() -> Unit)? = null,
    onRegistrationComplete: ((Patient) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (onBackToLogin != null) {
                        IconButton(onClick = onBackToLogin) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Login",
                                tint = TealPrimary
                            )
                        }
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = TealPrimaryLight.copy(alpha = 0.3f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocalHospital,
                                    contentDescription = "MediFlow Logo",
                                    tint = TealPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "MediFlow AI",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            Text(
                                text = "Smart Hospital • Patient Desk",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MedicalBackground
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // If patient was registered, show the Digital ID Pass
            val registeredPatient = uiState.registeredPatient
            if (registeredPatient != null) {
                DigitalPatientIdCard(
                    patient = registeredPatient,
                    onRegisterAnother = { viewModel.registerAnotherPatient() },
                    onProceedToPortal = onRegistrationComplete?.let { callback -> { callback(registeredPatient) } }
                )
            } else {
                // Header Banner
                RegistrationHeaderBanner()

                // Form Error Alert
                if (uiState.errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.errorMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Section 1: Personal Details
                Card(
                    colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SectionHeader(
                            title = "Personal Information",
                            icon = Icons.Default.Person
                        )

                        // Full Name
                        OutlinedTextField(
                            value = uiState.fullName,
                            onValueChange = { viewModel.onFullNameChange(it) },
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            label = { Text("Full Name *") },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = TealPrimary)
                            },
                            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
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

                        // Age & Gender Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.age,
                                onValueChange = { viewModel.onAgeChange(it) },
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                label = { Text("Age *") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    autoCorrectEnabled = false
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
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
                                modifier = Modifier.weight(1f)
                            )

                            // Blood Group Selector
                            BloodGroupDropdown(
                                selectedBloodGroup = uiState.bloodGroup,
                                onBloodGroupSelected = { viewModel.onBloodGroupChange(it) },
                                modifier = Modifier.weight(1.2f)
                            )
                        }

                        // Gender Chips
                        Column {
                            Text(
                                text = "Gender",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Male", "Female", "Other").forEach { genderOption ->
                                    val isSelected = uiState.gender == genderOption
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.onGenderChange(genderOption) },
                                        label = { Text(genderOption) },
                                        leadingIcon = if (isSelected) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        } else null,
                                        shape = RoundedCornerShape(20.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SoftCyan,
                                            selectedLabelColor = TealPrimaryDark
                                        )
                                    )
                                }
                            }
                        }

                        // Contact Number
                        OutlinedTextField(
                            value = uiState.phoneNumber,
                            onValueChange = { viewModel.onPhoneChange(it) },
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            label = { Text("Mobile Number (10 Digits) *") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = TealPrimary)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                autoCorrectEnabled = false
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
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
                    }
                }

                // Section 2: Clinical / Visit Details
                Card(
                    colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SectionHeader(
                            title = "Consultation & Symptoms",
                            icon = Icons.Default.MedicalServices
                        )

                        // Department Dropdown
                        DepartmentDropdown(
                            selectedDepartment = uiState.department,
                            onDepartmentSelected = { viewModel.onDepartmentChange(it) }
                        )

                        // Chief Complaints
                        OutlinedTextField(
                            value = uiState.chiefComplaints,
                            onValueChange = { viewModel.onComplaintsChange(it) },
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            label = { Text("Primary Symptoms / Complaints *") },
                            leadingIcon = {
                                Icon(Icons.Default.Description, contentDescription = null, tint = TealPrimary)
                            },
                            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false),
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(12.dp),
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
                    }
                }

                // Register Action Button
                Button(
                    onClick = { viewModel.registerPatient() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.AppRegistration,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Register & Generate Patient ID",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            // Recent Registrations History
            if (uiState.recentRegistrations.isNotEmpty()) {
                RecentRegistrationsSection(patients = uiState.recentRegistrations)
            }
        }
    }
}

@Composable
fun RegistrationHeaderBanner() {
    Surface(
        color = SoftCyan,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "New Patient Registration",
                    style = MaterialTheme.typography.titleLarge,
                    color = TealPrimaryDark,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Generate digital patient record and alert staff queue automatically.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TealPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodGroupDropdown(
    selectedBloodGroup: String,
    onBloodGroupSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedBloodGroup,
            onValueChange = {},
            readOnly = true,
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            ),
            label = { Text("Blood Grp") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = Color(0xFFB0BEC5),
                focusedLabelColor = TealPrimary,
                unfocusedLabelColor = TextSecondary
            ),
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            bloodGroups.forEach { bg ->
                DropdownMenuItem(
                    text = { Text(bg, color = TextPrimary) },
                    onClick = {
                        onBloodGroupSelected(bg)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentDropdown(
    selectedDepartment: String,
    onDepartmentSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val departments = listOf(
        "General Medicine",
        "Cardiology",
        "Orthopedics",
        "Pediatrics",
        "ENT & Ophthalmology",
        "Emergency Care"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedDepartment,
            onValueChange = {},
            readOnly = true,
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            ),
            label = { Text("Consultation Department *") },
            leadingIcon = {
                Icon(Icons.Default.Apartment, contentDescription = null, tint = TealPrimary)
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = Color(0xFFB0BEC5),
                focusedLabelColor = TealPrimary,
                unfocusedLabelColor = TextSecondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            departments.forEach { dept ->
                DropdownMenuItem(
                    text = { Text(dept, color = TextPrimary) },
                    onClick = {
                        onDepartmentSelected(dept)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DigitalPatientIdCard(
    patient: Patient,
    onRegisterAnother: () -> Unit,
    onProceedToPortal: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Success header badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = LightGreenBadge
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = HospitalGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REGISTRATION CONFIRMED",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = HospitalGreen
                        )
                    }
                }

                Text(
                    text = patient.registrationTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Patient Card Body
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = TealPrimary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = TealPrimary.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PATIENT ID",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = patient.patientId,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TealPrimaryDark
                            )
                        }

                        // Token Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TealPrimary
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "TOKEN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "#${patient.tokenNumber}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderLight)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Details Grid
                    DetailRow(label = "Patient Name", value = patient.fullName)
                    Spacer(modifier = Modifier.height(6.dp))
                    DetailRow(label = "Age & Gender", value = "${patient.age} Yrs • ${patient.gender}")
                    Spacer(modifier = Modifier.height(6.dp))
                    DetailRow(label = "Blood Group", value = patient.bloodGroup)
                    Spacer(modifier = Modifier.height(6.dp))
                    DetailRow(label = "Mobile", value = patient.phoneNumber)
                    Spacer(modifier = Modifier.height(6.dp))
                    DetailRow(label = "Department", value = patient.departmentNeeded)
                    Spacer(modifier = Modifier.height(6.dp))
                    DetailRow(label = "Complaints", value = patient.chiefComplaints)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real-time Staff Alert Status
            Surface(
                color = SoftCyan,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Staff & OPD Notified",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimaryDark
                        )
                        Text(
                            text = "Patient details synchronized with Doctor Queue & Reception.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Buttons
            if (onProceedToPortal != null) {
                Button(
                    onClick = onProceedToPortal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HospitalGreen)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open My Patient Portal", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onRegisterAnother,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Register Another Account", color = TealPrimary)
                }
            } else {
                Button(
                    onClick = onRegisterAnother,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Register Next Patient", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
fun RecentRegistrationsSection(patients: List<Patient>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Today's Patient Queue (${patients.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        patients.forEach { patient ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SoftCyan,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "#${patient.tokenNumber}",
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimaryDark,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = patient.fullName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${patient.patientId} • ${patient.departmentNeeded}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = LightGreenBadge
                    ) {
                        Text(
                            text = "Waiting",
                            style = MaterialTheme.typography.labelSmall,
                            color = HospitalGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
