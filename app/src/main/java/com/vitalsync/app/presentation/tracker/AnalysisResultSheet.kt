package com.vitalsync.app.presentation.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitalsync.app.domain.clinical.ClinicalSourceRegistry
import com.vitalsync.app.domain.model.AnalysisResult
import com.vitalsync.app.domain.model.HealthStatus
import com.vitalsync.app.presentation.components.VitalSyncButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisResultSheet(
    analysisResult: AnalysisResult,
    onDismiss: () -> Unit,
    onGoHome: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Durum ikonu
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(analysisResult.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(analysisResult.emoji, fontSize = 36.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Durum etiketi
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(analysisResult.color.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = analysisResult.statusText,
                    color = analysisResult.color,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Başlık
            Text(
                text = analysisResult.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Açıklama
            Text(
                text = analysisResult.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (analysisResult.sourceRefs.isNotEmpty()) {
                Text(
                    text = "Dayanak: ${ClinicalSourceRegistry.displayLabels(analysisResult.sourceRefs)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Öneri kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (analysisResult.status) {
                        HealthStatus.CRITICAL -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        HealthStatus.HIGH -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = if (analysisResult.status == HealthStatus.CRITICAL || analysisResult.status == HealthStatus.HIGH) "⚠️" else "💡",
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            text = "Öneri",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = analysisResult.recommendation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Butonlar
            VitalSyncButton(
                text = "Ana Sayfaya Dön",
                onClick = onGoHome
            )

            Spacer(modifier = Modifier.height(12.dp))

            VitalSyncButton(
                text = "Yeni Ölçüm Ekle",
                onClick = onDismiss,
                isOutline = true
            )
        }
    }
}
