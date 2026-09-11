package com.vitalsync.app.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vitalsync.app.presentation.components.VitalSyncButton
import com.vitalsync.app.presentation.components.VitalSyncTextField

@Composable
fun RegisterStepBasicInfo(
    firstName: String,
    lastName: String,
    username: String,
    isUsernameTaken: Boolean,
    isValid: Boolean,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        // Başlık
        Text(
            text = "Sizi Tanıyalım",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Temel bilgilerinizi girerek başlayalım",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Ad
        VitalSyncTextField(
            value = firstName,
            onValueChange = onFirstNameChange,
            label = "Ad",
            placeholder = "Adınızı girin",
            isRequired = true,
            isValid = firstName.isNotBlank(),
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Soyad
        VitalSyncTextField(
            value = lastName,
            onValueChange = onLastNameChange,
            label = "Soyad",
            placeholder = "Soyadınızı girin",
            isRequired = true,
            isValid = lastName.isNotBlank(),
            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Kullanıcı adı
        VitalSyncTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = "Kullanıcı Adı",
            placeholder = "En az 3 karakter",
            isRequired = true,
            isValid = username.length >= 3 && !isUsernameTaken,
            isError = isUsernameTaken,
            errorMessage = if (isUsernameTaken) "Bu kullanıcı adı zaten alınmış" else null,
            leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // İleri butonu
        VitalSyncButton(
            text = "Devam Et",
            onClick = onNext,
            enabled = isValid
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
