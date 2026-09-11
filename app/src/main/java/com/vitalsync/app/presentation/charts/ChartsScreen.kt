package com.vitalsync.app.presentation.charts

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitalsync.app.presentation.theme.*
import kotlin.math.roundToInt


@Composable
fun ChartsScreen(
    state: ChartsState,
    onTabSelected: (ChartTab) -> Unit,
    onPeriodSelected: (ChartPeriod) -> Unit,
    onNavigateToAnalysis: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        // Başlık
        Text(
            "Trend Analizi",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "Sağlık verilerinizin zaman içindeki değişimi",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )

        Spacer(Modifier.height(20.dp))

        // Tab Seçimi
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ChartTab.entries.forEach { tab ->
                val isSelected = state.selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(tab.emoji, fontSize = 16.sp)
                        Text(
                            tab.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Periyot Seçimi
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChartPeriod.entries.forEach { period ->
                val isSelected = state.selectedPeriod == period
                FilterChip(
                    selected = isSelected,
                    onClick = { onPeriodSelected(period) },
                    label = {
                        Text(
                            period.label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Grafik
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            AnimatedContent(
                targetState = state.selectedTab,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                },
                label = "chartAnim"
            ) { tab ->
                val (lineColor, secondaryColor, targetMin, targetMax, unit) = getChartConfig(tab)
                VitalLineChart(
                    dataPoints = state.dataPoints,
                    lineColor = lineColor,
                    secondaryLineColor = secondaryColor,
                    targetMin = targetMin,
                    targetMax = targetMax,
                    unit = unit
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // İstatistikler
        if (state.hasData) {
            Text(
                "İstatistikler",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    label = "Ortalama",
                    value = formatStatValue(state.stats.average, state.selectedTab),
                    unit = getUnit(state.selectedTab),
                    icon = "📊",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "En Düşük",
                    value = formatStatValue(state.stats.min, state.selectedTab),
                    unit = getUnit(state.selectedTab),
                    icon = "⬇️",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "En Yüksek",
                    value = formatStatValue(state.stats.max, state.selectedTab),
                    unit = getUnit(state.selectedTab),
                    icon = "⬆️",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    label = "Toplam Ölçüm",
                    value = state.stats.count.toString(),
                    icon = "🔢",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Trend",
                    value = state.stats.trend,
                    icon = when {
                        state.stats.trend.startsWith("↑") -> "📈"
                        state.stats.trend.startsWith("↓") -> "📉"
                        else -> "➡️"
                    },
                    modifier = Modifier.weight(1f)
                )
                if (state.stats.secondaryAverage != null) {
                    StatCard(
                        label = "Diastolik Ort.",
                        value = state.stats.secondaryAverage.roundToInt().toString(),
                        unit = "mmHg",
                        icon = "💙",
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Bilgi kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("💡", fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = getChartTip(state.selectedTab),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                }
            }

            // Renk Açıklaması (Legend)
            if (state.selectedTab == ChartTab.BLOOD_PRESSURE) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = ChartSystolic, label = "Sistolik")
                    Spacer(Modifier.width(20.dp))
                    LegendItem(color = ChartDiastolic, label = "Diastolik")
                    Spacer(Modifier.width(20.dp))
                    LegendItem(color = StatusNormal.copy(alpha = 0.3f), label = "Hedef Aralık")
                }
            }

            Spacer(Modifier.height(24.dp))

            // Faz 5: Klinik Analiz Butonu
            Button(
                onClick = {
                    val metricType = when (state.selectedTab) {
                        ChartTab.BLOOD_PRESSURE -> "BP"
                        ChartTab.GLUCOSE -> "Glucose"
                        ChartTab.HEART_RATE -> "HR"
                        ChartTab.OXYGEN -> "O2"
                    }
                    onNavigateToAnalysis(metricType)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text("🧠 Detaylı Klinik Analiz", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// Grafik konfigürasyonu
private data class ChartConfig(
    val lineColor: Color,
    val secondaryColor: Color?,
    val targetMin: Float?,
    val targetMax: Float?,
    val unit: String
)

private fun getChartConfig(tab: ChartTab): ChartConfig = when (tab) {
    ChartTab.BLOOD_PRESSURE -> ChartConfig(
        lineColor = ChartSystolic,
        secondaryColor = ChartDiastolic,
        targetMin = 80f,   // Diastolic normal alt
        targetMax = 120f,  // Systolic normal üst
        unit = "mmHg"
    )
    ChartTab.GLUCOSE -> ChartConfig(
        lineColor = ChartGlucose,
        secondaryColor = null,
        targetMin = 70f,
        targetMax = 100f,
        unit = "mg/dL"
    )
    ChartTab.HEART_RATE -> ChartConfig(
        lineColor = ChartHeartRate,
        secondaryColor = null,
        targetMin = 60f,
        targetMax = 100f,
        unit = "bpm"
    )
    ChartTab.OXYGEN -> ChartConfig(
        lineColor = ChartOxygen,
        secondaryColor = null,
        targetMin = 95f,
        targetMax = 100f,
        unit = "%"
    )
}

private fun getUnit(tab: ChartTab): String = when (tab) {
    ChartTab.BLOOD_PRESSURE -> "mmHg"
    ChartTab.GLUCOSE -> "mg/dL"
    ChartTab.HEART_RATE -> "bpm"
    ChartTab.OXYGEN -> "%"
}

private fun formatStatValue(value: Float, tab: ChartTab): String {
    return value.roundToInt().toString()
}

private fun getChartTip(tab: ChartTab): String = when (tab) {
    ChartTab.BLOOD_PRESSURE -> "Yeşil bant normal aralığı gösterir (120/80 mmHg altı). Ölçümlerinizi hep aynı saatte, oturarak ve dinlenerek yapmanız daha tutarlı sonuçlar verir."
    ChartTab.GLUCOSE -> "Açlık kan şekeri 70-100 mg/dL arasında olmalıdır. Sabah aç karnına ölçüm en güvenilir sonucu verir."
    ChartTab.HEART_RATE -> "Yetişkinlerde normal istirahat nabzı 60-100 bpm arasındadır. Düzenli egzersiz istirahat nabzını olumlu etkiler."
    ChartTab.OXYGEN -> "Normal SpO2 değeri %95-100 arasıdır. %94 altı değerler dikkat gerektirir. Yüksek rakımda bu değer düşebilir."
}
