package com.vitalsync.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StepIndicator(
    totalSteps: Int,
    currentStep: Int, // 0-indexed
    stepTitles: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Adım numaraları ve çizgiler
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until totalSteps) {
                val isCompleted = i < currentStep
                val isCurrent = i == currentStep

                val circleColor by animateColorAsState(
                    targetValue = when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isCurrent -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    animationSpec = tween(300),
                    label = "stepColor$i"
                )

                val textColor by animateColorAsState(
                    targetValue = when {
                        isCompleted || isCurrent -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    },
                    animationSpec = tween(300),
                    label = "stepTextColor$i"
                )

                val scaleValue by animateFloatAsState(
                    targetValue = if (isCurrent) 1.15f else 1f,
                    animationSpec = tween(300),
                    label = "stepScale$i"
                )

                // Daire
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .scale(scaleValue)
                        .clip(CircleShape)
                        .background(circleColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCompleted) "✓" else "${i + 1}",
                        color = textColor,
                        fontSize = if (isCompleted) 16.sp else 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Adımlar arası çizgi
                if (i < totalSteps - 1) {
                    val lineProgress by animateFloatAsState(
                        targetValue = if (i < currentStep) 1f else 0f,
                        animationSpec = tween(400),
                        label = "lineProgress$i"
                    )

                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(lineProgress)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        // Adım başlığı (varsa)
        if (stepTitles.isNotEmpty() && currentStep < stepTitles.size) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stepTitles[currentStep],
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
