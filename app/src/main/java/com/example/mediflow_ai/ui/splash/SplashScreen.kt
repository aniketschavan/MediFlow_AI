package com.example.mediflow_ai.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinish: () -> Unit
) {
    // Auto-advance to role selection after 2400 ms
    LaunchedEffect(Unit) {
        delay(2400)
        onFinish()
    }

    // Infinite transitions for heartbeat pulse
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Deep Healthcare Gradient Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF041C24),
                        Color(0xFF003840),
                        Color(0xFF002229)
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFinish
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Heartbeat Pulse Icon Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
                // Outer glowing circle
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(Color(0xFF00B4D8).copy(alpha = 0.18f * glowAlpha))
                )

                // Mid glowing circle
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(pulseScale * 0.96f)
                        .clip(CircleShape)
                        .background(Color(0xFF83C5BE).copy(alpha = 0.30f * glowAlpha))
                )

                // Inner Solid Circle with Medical Icon
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF006D77),
                    shadowElevation = 12.dp,
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "MediFlow Logo",
                            tint = Color.White,
                            modifier = Modifier
                                .size(42.dp)
                                .scale(pulseScale * 0.95f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Title
            Text(
                text = "MediFlow AI",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Tagline
            Text(
                text = "Agentic AI-Based Smart Hospital Workflow",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF83C5BE),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Stylized Animated ECG Heartbeat Waveform
            EcgWaveCanvas(glowAlpha = glowAlpha)

            Spacer(modifier = Modifier.height(36.dp))

            // Project & University Tag
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0D3B43).copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "MediFlow AI Smart Hospital System",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE2F1F1)
                    )
                    Text(
                        text = "Intelligent Hospital Workflow Automation",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF90E0EF)
                    )
                }
            }
        }

        // Tap to skip hint at bottom
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Tap anywhere to skip",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.55f)
            )
            Spacer(modifier = Modifier.size(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun EcgWaveCanvas(glowAlpha: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .height(48.dp)
    ) {
        val width = size.width
        val height = size.height
        val midY = height / 2

        val path = Path().apply {
            moveTo(0f, midY)
            lineTo(width * 0.20f, midY)
            lineTo(width * 0.26f, midY - height * 0.18f)
            lineTo(width * 0.32f, midY)
            lineTo(width * 0.40f, midY)
            // Heartbeat spike
            lineTo(width * 0.45f, midY + height * 0.32f) // Dip
            lineTo(width * 0.50f, midY - height * 0.65f) // Peak Spike
            lineTo(width * 0.55f, midY + height * 0.40f) // Counter dip
            lineTo(width * 0.60f, midY) // Return
            // T-wave
            lineTo(width * 0.68f, midY - height * 0.22f)
            lineTo(width * 0.74f, midY)
            lineTo(width, midY)
        }

        // Glow line behind
        drawPath(
            path = path,
            color = Color(0xFF00B4D8).copy(alpha = 0.35f * glowAlpha),
            style = Stroke(
                width = 8f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Sharp bright line in front
        drawPath(
            path = path,
            color = Color(0xFF83C5BE),
            style = Stroke(
                width = 3.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Glowing focal dot at the peak
        drawCircle(
            color = Color(0xFF00B4D8).copy(alpha = glowAlpha),
            radius = 6f,
            center = Offset(width * 0.50f, midY - height * 0.65f)
        )
    }
}
