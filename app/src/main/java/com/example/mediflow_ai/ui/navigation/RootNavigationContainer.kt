package com.example.mediflow_ai.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.mediflow_ai.ui.auth.AuthState
import com.example.mediflow_ai.ui.auth.LoginScreen
import com.example.mediflow_ai.ui.auth.LoginTab
import com.example.mediflow_ai.ui.common.LogoutConfirmationDialog
import com.example.mediflow_ai.ui.patient.PatientPortalScaffold
import com.example.mediflow_ai.ui.patient.PatientRegistrationScreen
import com.example.mediflow_ai.ui.splash.SplashScreen
import com.example.mediflow_ai.ui.staff.StaffPortalScaffold

@Composable
fun RootNavigationContainer() {
    val backstack = remember { mutableStateListOf<AuthState>(AuthState.Splash) }
    val currentState = backstack.lastOrNull() ?: AuthState.Login(initialTab = LoginTab.PATIENT)
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Jetpack Compose BackHandler for robust hardware and gesture back navigation
    BackHandler(enabled = backstack.size > 1 || currentState is AuthState.PatientSession || currentState is AuthState.StaffSession) {
        when (currentState) {
            is AuthState.PatientRegistration -> {
                if (backstack.size > 1) {
                    backstack.removeLast()
                } else {
                    backstack.clear()
                    backstack.add(AuthState.Login(initialTab = LoginTab.PATIENT))
                }
            }
            is AuthState.PatientSession, is AuthState.StaffSession -> {
                showLogoutDialog = true
            }
            else -> {
                if (backstack.size > 1) {
                    backstack.removeLast()
                }
            }
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            userRole = if (currentState is AuthState.PatientSession) "Patient" else "Staff",
            userName = when (currentState) {
                is AuthState.PatientSession -> currentState.patientName
                is AuthState.StaffSession -> currentState.staffName
                else -> "User"
            },
            onConfirmLogout = {
                showLogoutDialog = false
                val targetTab = if (currentState is AuthState.PatientSession) LoginTab.PATIENT else LoginTab.STAFF
                backstack.clear()
                backstack.add(AuthState.Login(initialTab = targetTab))
            },
            onDismiss = {
                showLogoutDialog = false
            }
        )
    }

    Crossfade(
        targetState = currentState,
        animationSpec = tween(durationMillis = 350),
        label = "RootNavigationCrossfade"
    ) { state ->
        when (state) {
            is AuthState.Splash -> {
                SplashScreen(
                    onFinish = {
                        backstack.clear()
                        backstack.add(AuthState.Login(initialTab = LoginTab.PATIENT))
                    }
                )
            }

            is AuthState.Login -> {
                LoginScreen(
                    initialTab = state.initialTab,
                    onLoginPatient = { id, name, token ->
                        backstack.clear()
                        backstack.add(AuthState.Login(initialTab = LoginTab.PATIENT))
                        backstack.add(
                            AuthState.PatientSession(
                                patientId = id,
                                patientName = name,
                                tokenNumber = token
                            )
                        )
                    },
                    onLoginStaff = { role, name ->
                        backstack.clear()
                        backstack.add(AuthState.Login(initialTab = LoginTab.STAFF))
                        backstack.add(
                            AuthState.StaffSession(
                                roleName = role,
                                staffName = name
                            )
                        )
                    },
                    onNavigateToRegistration = {
                        backstack.add(AuthState.PatientRegistration)
                    }
                )
            }

            is AuthState.PatientRegistration -> {
                PatientRegistrationScreen(
                    onBackToLogin = {
                        if (backstack.size > 1) {
                            backstack.removeLast()
                        } else {
                            backstack.clear()
                            backstack.add(AuthState.Login(initialTab = LoginTab.PATIENT))
                        }
                    },
                    onRegistrationComplete = { newPatient ->
                        backstack.clear()
                        backstack.add(AuthState.Login(initialTab = LoginTab.PATIENT))
                        backstack.add(
                            AuthState.PatientSession(
                                patientId = newPatient.patientId,
                                patientName = newPatient.fullName,
                                tokenNumber = newPatient.tokenNumber
                            )
                        )
                    }
                )
            }

            is AuthState.PatientSession -> {
                PatientPortalScaffold(
                    session = state,
                    onLogout = {
                        backstack.clear()
                        backstack.add(AuthState.Login(initialTab = LoginTab.PATIENT))
                    }
                )
            }

            is AuthState.StaffSession -> {
                StaffPortalScaffold(
                    session = state,
                    onLogout = {
                        backstack.clear()
                        backstack.add(AuthState.Login(initialTab = LoginTab.STAFF))
                    }
                )
            }
        }
    }
}
