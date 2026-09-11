package com.vitalsync.app.presentation.charts

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitalsync.app.presentation.theme.*
import kotlin.math.roundToInt

/**
 * Özel Canvas tabanlı çizgi grafik bileşeni.
 * Vico kütüphanesi yerine tam kontrol sağlamak için sıfırdan yazıldı.
 */
@Composable
fun VitalLineChart(
    dataPoints: List<ChartDataPoint>,
    lineColor: Color,
    secondaryLineColor: Color? = null,
    targetMin: Float? = null,
    targetMax: Float? = null,
    unit: String = "",
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) {
        EmptyChartPlaceholder(modifier)
        return
    }

    // Animasyon
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(dataPoints) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
    }

    val textMeasurer = rememberTextMeasurer()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    val targetBandColor = ChartTargetBand

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 16.dp)
    ) {
        val progress = animProgress.value
        val chartWidth = size.width - 48.dp.toPx()
        val chartHeight = size.height - 32.dp.toPx()
        val chartLeft = 40.dp.toPx()
        val chartTop = 8.dp.toPx()

        // Tüm değerlerin min/max'ını hesapla
        val allValues = dataPoints.map { it.value } +
                (dataPoints.mapNotNull { it.secondaryValue })
        val dataMin = (allValues.minOrNull() ?: 0f) * 0.9f
        val dataMax = (allValues.maxOrNull() ?: 100f) * 1.1f
        val yRange = if (dataMax - dataMin < 1f) 10f else dataMax - dataMin

        // Hedef aralık bandı (varsa)
        if (targetMin != null && targetMax != null) {
            val bandTop = chartTop + chartHeight - ((targetMax - dataMin) / yRange * chartHeight)
            val bandBottom = chartTop + chartHeight - ((targetMin - dataMin) / yRange * chartHeight)
            drawRect(
                color = targetBandColor,
                topLeft = Offset(chartLeft, bandTop.coerceAtLeast(chartTop)),
                size = androidx.compose.ui.geometry.Size(
                    chartWidth,
                    (bandBottom - bandTop).coerceAtLeast(0f)
                )
            )
        }

        // Yatay kılavuz çizgileri (5 adet)
        for (i in 0..4) {
            val y = chartTop + chartHeight * i / 4f
            drawLine(
                color = gridColor,
                start = Offset(chartLeft, y),
                end = Offset(chartLeft + chartWidth, y),
                strokeWidth = 1.dp.toPx()
            )

            // Y ekseni etiketleri
            val yValue = dataMax - (yRange * i / 4f)
            val labelText = yValue.roundToInt().toString()
            val textResult = textMeasurer.measure(
                text = AnnotatedString(labelText),
                style = TextStyle(
                    fontSize = 9.sp,
                    color = labelColor
                )
            )
            drawText(
                textLayoutResult = textResult,
                topLeft = Offset(
                    chartLeft - textResult.size.width - 4.dp.toPx(),
                    y - textResult.size.height / 2f
                )
            )
        }

        // X ekseni etiketleri (eşit aralıklı)
        val labelCount = minOf(dataPoints.size, 6)
        if (labelCount > 0) {
            val step = if (dataPoints.size > 1) dataPoints.size / labelCount else 1
            for (i in 0 until dataPoints.size step step.coerceAtLeast(1)) {
                val x = chartLeft + (i.toFloat() / (dataPoints.size - 1).coerceAtLeast(1)) * chartWidth
                val labelText = dataPoints[i].label
                val textResult = textMeasurer.measure(
                    text = AnnotatedString(labelText),
                    style = TextStyle(fontSize = 8.sp, color = labelColor)
                )
                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(
                        x - textResult.size.width / 2f,
                        chartTop + chartHeight + 4.dp.toPx()
                    )
                )
            }
        }

        // Ana çizgi (primary values)
        drawAnimatedLine(
            dataPoints = dataPoints.map { it.value },
            dataMin = dataMin,
            yRange = yRange,
            chartLeft = chartLeft,
            chartTop = chartTop,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            color = lineColor,
            progress = progress
        )

        // İkincil çizgi (BP diastolic gibi)
        val secondaryValues = dataPoints.mapNotNull { it.secondaryValue }
        if (secondaryValues.size == dataPoints.size && secondaryLineColor != null) {
            drawAnimatedLine(
                dataPoints = secondaryValues,
                dataMin = dataMin,
                yRange = yRange,
                chartLeft = chartLeft,
                chartTop = chartTop,
                chartWidth = chartWidth,
                chartHeight = chartHeight,
                color = secondaryLineColor,
                progress = progress
            )
        }

        // Veri noktaları (küçük daireler)
        val visibleCount = (dataPoints.size * progress).toInt()
        for (i in 0 until visibleCount) {
            val x = chartLeft + (i.toFloat() / (dataPoints.size - 1).coerceAtLeast(1)) * chartWidth
            val y = chartTop + chartHeight - ((dataPoints[i].value - dataMin) / yRange * chartHeight)

            // Dış halka
            drawCircle(
                color = lineColor.copy(alpha = 0.3f),
                radius = 5.dp.toPx(),
                center = Offset(x, y)
            )
            // İç daire
            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

/**
 * Animasyonlu çizgi çizme yardımcı fonksiyonu
 */
private fun DrawScope.drawAnimatedLine(
    dataPoints: List<Float>,
    dataMin: Float,
    yRange: Float,
    chartLeft: Float,
    chartTop: Float,
    chartWidth: Float,
    chartHeight: Float,
    color: Color,
    progress: Float
) {
    if (dataPoints.size < 2) return

    val path = Path()
    val fillPath = Path()
    val visibleCount = (dataPoints.size * progress).toInt().coerceAtLeast(2)

    for (i in 0 until visibleCount) {
        val x = chartLeft + (i.toFloat() / (dataPoints.size - 1).coerceAtLeast(1)) * chartWidth
        val y = chartTop + chartHeight - ((dataPoints[i] - dataMin) / yRange * chartHeight)

        if (i == 0) {
            path.moveTo(x, y)
            fillPath.moveTo(x, chartTop + chartHeight)
            fillPath.lineTo(x, y)
        } else {
            // Bezier eğrisi ile yumuşak geçiş
            val prevX = chartLeft + ((i - 1).toFloat() / (dataPoints.size - 1).coerceAtLeast(1)) * chartWidth
            val prevY = chartTop + chartHeight - ((dataPoints[i - 1] - dataMin) / yRange * chartHeight)
            val cpX = (prevX + x) / 2f
            path.cubicTo(cpX, prevY, cpX, y, x, y)
            fillPath.cubicTo(cpX, prevY, cpX, y, x, y)
        }
    }

    // Çizgiyi çiz
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 2.5f.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Gradient dolgu
    val lastX = chartLeft + ((visibleCount - 1).toFloat() / (dataPoints.size - 1).coerceAtLeast(1)) * chartWidth
    fillPath.lineTo(lastX, chartTop + chartHeight)
    fillPath.close()

    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = 0.2f),
                color.copy(alpha = 0.02f)
            ),
            startY = chartTop,
            endY = chartTop + chartHeight
        )
    )
}

/**
 * Veri olmadığında gösterilen placeholder
 */
@Composable
fun EmptyChartPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📊", fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "Henüz yeterli veri yok",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                "Ölçüm ekleyerek grafikleri görebilirsiniz",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

/**
 * İstatistik kartı
 */
@Composable
fun StatCard(
    label: String,
    value: String,
    unit: String = "",
    icon: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon.isNotEmpty()) {
            Text(icon, fontSize = 16.sp)
            Spacer(Modifier.height(2.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (unit.isNotEmpty()) {
                Spacer(Modifier.width(2.dp))
                Text(
                    unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }
    }
}
