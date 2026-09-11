package com.vitalsync.app.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinScreen(
    title: String = "PIN Girin",
    subtitle: String = "",
    pinLength: Int = 4,
    enteredPin: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    isLoading: Boolean = false,
    onDigitClick: (Char) -> Unit,
    onDeleteClick: () -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    // Hata animasyonu — sağa sola titreşim
    val shakeOffset by animateFloatAsState(
        targetValue = if (isError) 1f else 0f,
        animationSpec = if (isError) {
            keyframes {
                durationMillis = 400
                0f at 0
                -12f at 50
                12f at 100
                -12f at 150
                12f at 200
                -6f at 250
                6f at 300
                0f at 400
            }
        } else {
            tween(0)
        },
        label = "shake"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Geri butonu (varsa)
        if (onBackClick != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Geri",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(if (onBackClick != null) 16.dp else 48.dp))

        // Kilit ikonu
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text("🔒", fontSize = 28.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Başlık
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        if (subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // PIN göstergeleri
        Row(
            modifier = Modifier.graphicsLayer { translationX = shakeOffset * 3 },
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            repeat(pinLength) { index ->
                val isFilled = index < enteredPin.length

                val scaleAnim by animateFloatAsState(
                    targetValue = if (isFilled) 1f else 0.85f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    ),
                    label = "pinScale$index"
                )

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .scale(scaleAnim)
                        .clip(CircleShape)
                        .then(
                            if (isFilled) {
                                Modifier.background(
                                    if (isError) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    CircleShape
                                )
                            }
                        )
                )
            }
        }

        // Hata mesajı
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Sayısal tuş takımı
        NumberPad(
            onDigitClick = onDigitClick,
            onDeleteClick = onDeleteClick,
            enabled = !isLoading && enteredPin.length < pinLength
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun NumberPad(
    onDigitClick: (Char) -> Unit,
    onDeleteClick: () -> Unit,
    enabled: Boolean
) {
    val digits = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
        listOf(' ', '0', '⌫')
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        digits.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                row.forEach { digit ->
                    when (digit) {
                        ' ' -> Spacer(modifier = Modifier.size(72.dp))
                        '⌫' -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true),
                                        onClick = onDeleteClick
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Sil",
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true),
                                        enabled = enabled,
                                        onClick = { onDigitClick(digit) }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = digit.toString(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
