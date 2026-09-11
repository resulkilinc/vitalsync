package com.vitalsync.app.presentation.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalsync.app.data.repository.UserRepository
import com.vitalsync.app.data.repository.VitalsRepository
import com.vitalsync.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// === Chart Veri Modelleri ===

data class ChartDataPoint(
    val timestamp: Long,
    val value: Float,
    val secondaryValue: Float? = null, // BP diastolic gibi ikinci değer
    val label: String = ""
)

enum class ChartPeriod(val label: String, val days: Int) {
    WEEK("7 Gün", 7),
    MONTH("30 Gün", 30),
    THREE_MONTHS("3 Ay", 90)
}

enum class ChartTab(val title: String, val emoji: String) {
    BLOOD_PRESSURE("Kan Basıncı", "🩺"),
    GLUCOSE("Kan Şekeri", "🩸"),
    HEART_RATE("Nabız", "❤️"),
    OXYGEN("Oksijen", "🫁")
}

data class ChartStats(
    val average: Float = 0f,
    val min: Float = 0f,
    val max: Float = 0f,
    val count: Int = 0,
    val secondaryAverage: Float? = null, // BP diastolic avg
    val trend: String = "—" // "↑ Artış", "↓ Azalış", "→ Stabil"
)

data class ChartsState(
    val selectedTab: ChartTab = ChartTab.BLOOD_PRESSURE,
    val selectedPeriod: ChartPeriod = ChartPeriod.WEEK,
    val dataPoints: List<ChartDataPoint> = emptyList(),
    val stats: ChartStats = ChartStats(),
    val isLoading: Boolean = true,
    val hasData: Boolean = false,
    val userName: String = ""
)

@HiltViewModel
class ChartsViewModel @Inject constructor(
    private val vitalsRepository: VitalsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChartsState())
    val state: StateFlow<ChartsState> = _state.asStateFlow()

    private var userId: Long = -1

    fun setUserId(id: Long) {
        userId = id
        viewModelScope.launch {
            val user = userRepository.getUserById(id)
            _state.update { it.copy(userName = user?.firstName ?: "") }
        }
        loadChartData()
    }

    fun selectTab(tab: ChartTab) {
        _state.update { it.copy(selectedTab = tab) }
        loadChartData()
    }

    fun selectPeriod(period: ChartPeriod) {
        _state.update { it.copy(selectedPeriod = period) }
        loadChartData()
    }

    fun loadChartData() {
        if (userId <= 0) return
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val period = _state.value.selectedPeriod
            val now = System.currentTimeMillis()
            val startTime = now - (period.days.toLong() * 24 * 60 * 60 * 1000)

            val sdf = SimpleDateFormat("dd/MM", Locale("tr", "TR"))

            val dataPoints: List<ChartDataPoint>
            val stats: ChartStats

            when (_state.value.selectedTab) {
                ChartTab.BLOOD_PRESSURE -> {
                    val records = vitalsRepository.getBloodPressureByDateRange(userId, startTime, now)
                    dataPoints = records.map {
                        ChartDataPoint(
                            timestamp = it.timestamp,
                            value = it.systolic.toFloat(),
                            secondaryValue = it.diastolic.toFloat(),
                            label = sdf.format(Date(it.timestamp))
                        )
                    }
                    stats = if (records.isNotEmpty()) {
                        ChartStats(
                            average = records.map { it.systolic }.average().toFloat(),
                            min = records.minOf { it.systolic }.toFloat(),
                            max = records.maxOf { it.systolic }.toFloat(),
                            count = records.size,
                            secondaryAverage = records.map { it.diastolic }.average().toFloat(),
                            trend = calculateTrend(records.map { it.systolic.toFloat() })
                        )
                    } else ChartStats()
                }

                ChartTab.GLUCOSE -> {
                    val records = vitalsRepository.getGlucoseByDateRange(userId, startTime, now)
                    dataPoints = records.map {
                        ChartDataPoint(
                            timestamp = it.timestamp,
                            value = it.value.toFloat(),
                            label = sdf.format(Date(it.timestamp))
                        )
                    }
                    stats = if (records.isNotEmpty()) {
                        ChartStats(
                            average = records.map { it.value }.average().toFloat(),
                            min = records.minOf { it.value }.toFloat(),
                            max = records.maxOf { it.value }.toFloat(),
                            count = records.size,
                            trend = calculateTrend(records.map { it.value.toFloat() })
                        )
                    } else ChartStats()
                }

                ChartTab.HEART_RATE -> {
                    val records = vitalsRepository.getHeartRateByDateRange(userId, startTime, now)
                    dataPoints = records.map {
                        ChartDataPoint(
                            timestamp = it.timestamp,
                            value = it.value.toFloat(),
                            label = sdf.format(Date(it.timestamp))
                        )
                    }
                    stats = if (records.isNotEmpty()) {
                        ChartStats(
                            average = records.map { it.value }.average().toFloat(),
                            min = records.minOf { it.value }.toFloat(),
                            max = records.maxOf { it.value }.toFloat(),
                            count = records.size,
                            trend = calculateTrend(records.map { it.value.toFloat() })
                        )
                    } else ChartStats()
                }

                ChartTab.OXYGEN -> {
                    val records = vitalsRepository.getOxygenByDateRange(userId, startTime, now)
                    dataPoints = records.map {
                        ChartDataPoint(
                            timestamp = it.timestamp,
                            value = it.value.toFloat(),
                            label = sdf.format(Date(it.timestamp))
                        )
                    }
                    stats = if (records.isNotEmpty()) {
                        ChartStats(
                            average = records.map { it.value }.average().toFloat(),
                            min = records.minOf { it.value }.toFloat(),
                            max = records.maxOf { it.value }.toFloat(),
                            count = records.size,
                            trend = calculateTrend(records.map { it.value.toFloat() })
                        )
                    } else ChartStats()
                }
            }

            _state.update {
                it.copy(
                    dataPoints = dataPoints,
                    stats = stats,
                    isLoading = false,
                    hasData = dataPoints.isNotEmpty()
                )
            }
        }
    }

    private fun calculateTrend(values: List<Float>): String {
        if (values.size < 2) return "—"
        val firstHalf = values.take(values.size / 2).average()
        val secondHalf = values.drop(values.size / 2).average()
        val diff = secondHalf - firstHalf
        return when {
            diff > 3 -> "↑ Artış Eğilimi"
            diff < -3 -> "↓ Azalış Eğilimi"
            else -> "→ Stabil"
        }
    }
}
