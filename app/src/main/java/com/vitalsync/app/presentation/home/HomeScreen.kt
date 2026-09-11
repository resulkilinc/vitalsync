package com.vitalsync.app.presentation.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import com.vitalsync.app.domain.model.*
import com.vitalsync.app.presentation.components.EmptyVitalCard
import com.vitalsync.app.presentation.components.VitalSummaryCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    homeState: HomeState,
    onTrackerClick: (VitalType) -> Unit,
    onAddMeasurement: () -> Unit
) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 6 -> "İyi Geceler"
            hour < 12 -> "Günaydın"
            hour < 18 -> "İyi Günler"
            else -> "İyi Akşamlar"
        }
    }

    val today = remember {
        SimpleDateFormat("dd MMMM yyyy, EEEE", Locale("tr", "TR"))
            .format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Karşılama
        Text(
            text = "$greeting,",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Text(
            text = if (homeState.userName.isNotBlank()) homeState.userName else "Kullanıcı",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { 
                contentDescription = "Hoşgeldin ${if (homeState.userName.isNotBlank()) homeState.userName else "Kullanıcı"}" 
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = today,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Sağlık özeti başlık
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sağlık Özeti",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            TextButton(
                onClick = onAddMeasurement,
                modifier = Modifier.semantics { contentDescription = "Yeni ölçüm ekle" }
            ) {
                Text("+ Ölçüm Ekle")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (homeState.isLoading) {
            // Yükleniyor durumu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                visible = true
            }

            // Vital sign kartları
            homeState.vitals.forEachIndexed { index, summary ->
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(androidx.compose.animation.core.tween(300, delayMillis = index * 100)) + 
                            slideInVertically(androidx.compose.animation.core.tween(300, delayMillis = index * 100)) { it / 4 }
                ) {
                    Column {
                        when {
                    summary.record != null && summary.analysis != null -> {
                        val displayValue = when (val rec = summary.record) {
                            is BloodPressureRecord -> rec.displayValue
                            is GlucoseRecord -> rec.displayValue
                            is HeartRateRecord -> rec.displayValue
                            is OxygenRecord -> rec.displayValue
                            else -> "—"
                        }
                        val subtitle = when (val rec = summary.record) {
                            is BloodPressureRecord -> rec.formattedDate + " · " + rec.formattedTime
                            is GlucoseRecord -> rec.formattedDate + " · " + rec.context
                            is HeartRateRecord -> rec.formattedDate + " · " + rec.context
                            is OxygenRecord -> rec.formattedDate + " · " + rec.formattedTime
                            else -> ""
                        }

                        VitalSummaryCard(
                            title = summary.type.title,
                            value = displayValue,
                            unit = summary.type.unit,
                            emoji = summary.analysis.emoji,
                            statusColor = summary.analysis.color,
                            statusText = summary.analysis.statusText,
                            subtitle = subtitle,
                            onClick = { onTrackerClick(summary.type) }
                        )
                    }
                    else -> {
                        EmptyVitalCard(
                            title = summary.type.title,
                            emoji = summary.type.emoji,
                            onClick = { onTrackerClick(summary.type) }
                        )
                    }
                } // end when
                Spacer(modifier = Modifier.height(12.dp))
            } // end Column
        } // end AnimatedVisibility
    } // end forEachIndexed

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(androidx.compose.animation.core.tween(300, delayMillis = 400))
            ) {
                // Alt uyarı
                Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Text(
                    text = "⚕️ Bu analizler yalnızca bilgi amaçlıdır.\nTıbbi tanı yerine geçmez.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        } // end AnimatedVisibility
        } // end else block // <--- ADDED

        Spacer(modifier = Modifier.height(24.dp))
    } // end Column
} // end Form
