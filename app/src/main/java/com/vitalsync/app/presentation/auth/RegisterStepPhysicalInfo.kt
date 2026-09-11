package com.vitalsync.app.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitalsync.app.presentation.components.VitalSyncButton
import com.vitalsync.app.presentation.components.VitalSyncTextField

@Composable
fun RegisterStepPhysicalInfo(
    age: String,
    gender: String,
    heightCm: String,
    weightKg: String,
    bloodType: String,
    rhFactor: String,
    calculatedBmi: Float,
    isValid: Boolean,
    onAgeChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onBloodTypeChange: (String) -> Unit,
    onRhFactorChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Fiziksel Bilgiler",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Bu veriler sağlık analizleriniz için kullanılacak",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Cinsiyet seçimi
        Text(
            text = "Cinsiyet *",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("Erkek", "Kadın", "Belirtmek İstemiyorum").forEach { option ->
                val isSelected = gender == option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .border(
                            width = if (isSelected) 0.dp else 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onGenderChange(option) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (option) {
                            "Erkek" -> "♂ Erkek"
                            "Kadın" -> "♀ Kadın"
                            else -> "Diğer"
                        },
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Yaş
        VitalSyncTextField(
            value = age,
            onValueChange = onAgeChange,
            label = "Yaş",
            placeholder = "Örn: 25",
            isRequired = true,
            keyboardType = KeyboardType.Number,
            isValid = (age.toIntOrNull() ?: 0) in 1..150,
            isError = age.isNotBlank() && ((age.toIntOrNull() ?: 0) !in 1..150),
            errorMessage = if (age.isNotBlank() && (age.toIntOrNull() ?: 0) !in 1..150) "Geçerli bir yaş girin" else null
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Boy ve Kilo (yan yana)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            VitalSyncTextField(
                value = heightCm,
                onValueChange = onHeightChange,
                label = "Boy (cm)",
                placeholder = "170",
                isRequired = true,
                keyboardType = KeyboardType.Decimal,
                isValid = (heightCm.toFloatOrNull() ?: 0f) > 0f,
                modifier = Modifier.weight(1f)
            )
            VitalSyncTextField(
                value = weightKg,
                onValueChange = onWeightChange,
                label = "Kilo (kg)",
                placeholder = "70",
                isRequired = true,
                keyboardType = KeyboardType.Decimal,
                isValid = (weightKg.toFloatOrNull() ?: 0f) > 0f,
                modifier = Modifier.weight(1f)
            )
        }

        // BMI göstergesi
        AnimatedVisibility(
            visible = calculatedBmi > 0f,
            enter = fadeIn()
        ) {
            val bmiCategory = when {
                calculatedBmi < 18.5f -> "Zayıf"
                calculatedBmi < 25f -> "Normal"
                calculatedBmi < 30f -> "Fazla Kilolu"
                else -> "Obez"
            }
            val bmiColor = when {
                calculatedBmi < 18.5f -> MaterialTheme.colorScheme.tertiary
                calculatedBmi < 25f -> MaterialTheme.colorScheme.secondary
                calculatedBmi < 30f -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.error
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = bmiColor.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vücut Kitle İndeksi (BMI)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "%.1f — %s".format(calculatedBmi, bmiCategory),
                        style = MaterialTheme.typography.labelLarge,
                        color = bmiColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Kan Grubu
        Text(
            text = "Kan Grubu *",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("A", "B", "AB", "0").forEach { type ->
                val isSelected = bloodType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .border(
                            width = if (isSelected) 0.dp else 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onBloodTypeChange(type) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Rh Faktörü
        Text(
            text = "Rh Faktörü *",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("+" to "Rh Pozitif (+)", "-" to "Rh Negatif (-)").forEach { (value, label) ->
                val isSelected = rhFactor == value
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .border(
                            width = if (isSelected) 0.dp else 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onRhFactorChange(value) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        VitalSyncButton(
            text = "Devam Et",
            onClick = onNext,
            enabled = isValid
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
