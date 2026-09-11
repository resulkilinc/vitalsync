package com.vitalsync.app.presentation.analysis

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.vitalsync.app.domain.model.HealthStatus
import com.vitalsync.app.presentation.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
            }
            Text(
                text = state.metricTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Filters Segment
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val options = listOf(7 to "7 Gün", 14 to "14 Gün", 30 to "30 Gün")
            var selectedDays by remember { mutableIntStateOf(30) }

            options.forEach { (days, label) ->
                FilterChip(
                    selected = selectedDays == days,
                    onClick = {
                        selectedDays = days
                        viewModel.loadAnalysis(days)
                    },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("ℹ️", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        state.error!!,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else if (state.result != null) {
                val result = state.result!!
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(12.dp))

                    // Başlık Durumu (Overall Status Badge)
                    val statusColor = when (result.overallStatus) {
                        HealthStatus.NORMAL -> StatusNormal
                        HealthStatus.ATTENTION -> StatusAttention
                        HealthStatus.HIGH, HealthStatus.CRITICAL -> StatusHigh
                        HealthStatus.LOW -> StatusLow
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Genel Durum",
                                style = MaterialTheme.typography.labelMedium,
                                color = statusColor
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                result.overallStatus.name,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = statusColor,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                result.personalizedMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        "Klinik Özet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnalysisMiniCard(
                            title = "Ortalama",
                            value = when {
                                state.metricType == "BP" && result.averageSecondaryValue != null ->
                                    "${result.averageValue.roundToInt()}/${result.averageSecondaryValue.roundToInt()} mmHg"
                                state.metricType == "BP" ->
                                    "${result.averageValue.roundToInt()} mmHg"
                                else -> result.averageValue.roundToInt().toString()
                            },
                            icon = "📊",
                            modifier = Modifier.weight(1f)
                        )
                        AnalysisMiniCard(
                            title = "Trend",
                            value = result.weeklyTrend,
                            icon = when(result.weeklyTrend) {
                                "Artıyor" -> "📈"
                                "Azalıyor" -> "📉"
                                else -> "➡️"
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnalysisMiniCard(
                            title = "En Yüksek",
                            value = result.peakValue.roundToInt().toString(),
                            icon = "⬆️",
                            modifier = Modifier.weight(1f)
                        )
                        AnalysisMiniCard(
                            title = "En Düşük",
                            value = result.troughValue.roundToInt().toString(),
                            icon = "⬇️",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Compliance Progress Bar (Hedef Uyum Oranı)
                    Text(
                        "Hedef Aralığa Uyum (%)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    val rate = result.complianceRate
                    val progressColor = when {
                        rate >= 80f -> StatusNormal
                        rate >= 50f -> StatusAttention
                        else -> StatusHigh
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { rate / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = progressColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            "${rate.roundToInt()}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = progressColor
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Öneriler (Recommendations)
                    if (result.recommendations.isNotEmpty()) {
                        Text(
                            "Klinik Yorumlar",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(8.dp))

                        result.recommendations.forEach { rec ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("💡", fontSize = 18.sp)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = rec,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Yasal Uyarı
                    Text(
                        result.disclaimer,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    )

                    if (result.sourceRefs.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Dayanak: ${ClinicalSourceRegistry.displayLabels(result.sourceRefs)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        )
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun AnalysisMiniCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
