package com.example.mediflow_ai

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mediflow_ai.ui.navigation.RootNavigationContainer
import com.example.mediflow_ai.ui.theme.MediFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pure Kotlin: Dismiss Android 12+ system splash screen & logo immediately
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                splashScreenView.remove()
            }
        }

        // Pure Kotlin: Match window background with MediFlow dark teal (no white flash)
        window.setBackgroundDrawable(ColorDrawable(0xFF041C24.toInt()))

        enableEdgeToEdge()
        setContent {
            MediFlowTheme {
                RootNavigationContainer()
            }
        }
    }
}