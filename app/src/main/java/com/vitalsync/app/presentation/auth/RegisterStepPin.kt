package com.vitalsync.app.presentation.auth

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vitalsync.app.presentation.components.VitalSyncButton
import com.vitalsync.app.presentation.components.VitalSyncTextField
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun RegisterStepPin(
    pin: String,
    pinConfirm: String,
    pinError: String?,
    isLoading: Boolean,
    isValid: Boolean,
    onPinChange: (String) -> Unit,
    onPinConfirmChange: (String) -> Unit,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PIN Oluşturun",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Profilinizi korumak için 4 haneli bir PIN belirleyin",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Kilit ikonu
        Text("🔐", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(32.dp))

        // PIN girişi
        VitalSyncTextField(
            value = pin,
            onValueChange = onPinChange,
            label = "PIN",
            placeholder = "4 haneli PIN",
            isRequired = true,
            keyboardType = KeyboardType.NumberPassword,
            isValid = pin.length == 4,
            isError = pin.isNotBlank() && pin.length < 4 && pin.length > 0
        )

        Spacer(modifier = Modifier.height(16.dp))

        // PIN tekrar
        VitalSyncTextField(
            value = pinConfirm,
            onValueChange = onPinConfirmChange,
            label = "PIN Tekrar",
            placeholder = "PIN'inizi tekrar girin",
            isRequired = true,
            keyboardType = KeyboardType.NumberPassword,
            isValid = pinConfirm.length == 4 && pinConfirm == pin,
            isError = pinError != null,
            errorMessage = pinError
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Güvenlik notu
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Text(
                text = "🔒 PIN'iniz güvenli bir şekilde şifrelenerek cihazınızda saklanır. " +
                        "Sunucuya gönderilmez.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Tamamla butonu
        VitalSyncButton(
            text = if (isLoading) "Kaydediliyor..." else "Kaydı Tamamla ✓",
            onClick = onComplete,
            enabled = isValid && !isLoading,
            isSecondary = true
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
