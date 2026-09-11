package com.vitalsync.app.domain.engine

import com.vitalsync.app.domain.model.HealthStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class HealthAnalysisEngineTest {

    @Test
    fun `BP 85 55 is LOW not optimal`() {
        val r = HealthAnalysisEngine.analyzeBloodPressure(85, 55, hasHypertension = false, context = "İstirahat")
        assertEquals(HealthStatus.LOW, r.status)
        assertNotEquals(HealthStatus.NORMAL, r.status)
    }

    @Test
    fun `BP 180 120 without emergency symptoms is HIGH`() {
        val r = HealthAnalysisEngine.analyzeBloodPressure(
            180, 120,
            hasHypertension = false,
            context = "İstirahat",
            hasEmergencySymptoms = false,
        )
        assertEquals(HealthStatus.HIGH, r.status)
    }

    @Test
    fun `BP 180 120 with emergency symptoms is CRITICAL`() {
        val r = HealthAnalysisEngine.analyzeBloodPressure(
            180, 120,
            hasHypertension = false,
            context = "İstirahat",
            hasEmergencySymptoms = true,
        )
        assertEquals(HealthStatus.CRITICAL, r.status)
    }

    @Test
    fun `home BP 135 85 meets HBPM hypertension style threshold`() {
        val r = HealthAnalysisEngine.analyzeBloodPressure(135, 85, false, "İstirahat")
        assertEquals(HealthStatus.HIGH, r.status)
    }

    @Test
    fun `office BP 135 85 is elevated not hypertension`() {
        val r = HealthAnalysisEngine.analyzeBloodPressure(135, 85, false, "Klinik/Ofis")
        assertEquals(HealthStatus.ATTENTION, r.status)
    }

    @Test
    fun `fasting glucose 99 non-diabetic is NORMAL`() {
        val r = HealthAnalysisEngine.analyzeGlucose(99, "Açlık", hasDiabetes = false)
        assertEquals(HealthStatus.NORMAL, r.status)
    }

    @Test
    fun `fasting glucose 100 non-diabetic is ATTENTION`() {
        val r = HealthAnalysisEngine.analyzeGlucose(100, "Açlık", hasDiabetes = false)
        assertEquals(HealthStatus.ATTENTION, r.status)
    }

    @Test
    fun `fasting glucose 126 non-diabetic is HIGH`() {
        val r = HealthAnalysisEngine.analyzeGlucose(126, "Açlık", hasDiabetes = false)
        assertEquals(HealthStatus.HIGH, r.status)
    }

    @Test
    fun `fasting glucose 120 diabetic is within TEMD target band`() {
        val r = HealthAnalysisEngine.analyzeGlucose(120, "Açlık", hasDiabetes = true)
        assertEquals(HealthStatus.NORMAL, r.status)
    }

    @Test
    fun `fasting glucose below 54 is CRITICAL`() {
        val r = HealthAnalysisEngine.analyzeGlucose(50, "Açlık", hasDiabetes = false)
        assertEquals(HealthStatus.CRITICAL, r.status)
    }

    @Test
    fun `random glucose 180 non-diabetic is not NORMAL`() {
        val r = HealthAnalysisEngine.analyzeGlucose(180, "Rastgele", hasDiabetes = false)
        assertEquals(HealthStatus.ATTENTION, r.status)
    }

    @Test
    fun `random glucose 180 diabetic requests context clarification`() {
        val r = HealthAnalysisEngine.analyzeGlucose(180, "Rastgele", hasDiabetes = true)
        assertEquals(HealthStatus.ATTENTION, r.status)
    }

    @Test
    fun `SpO2 94 without COPD is ATTENTION`() {
        val r = HealthAnalysisEngine.analyzeOxygen(94, false, hasCOPD = false, altitude = null)
        assertEquals(HealthStatus.ATTENTION, r.status)
    }

    @Test
    fun `SpO2 92 without COPD suggests professional contact`() {
        val r = HealthAnalysisEngine.analyzeOxygen(92, false, hasCOPD = false, altitude = null)
        assertEquals(HealthStatus.HIGH, r.status)
    }

    @Test
    fun `SpO2 88 without COPD is CRITICAL`() {
        val r = HealthAnalysisEngine.analyzeOxygen(88, false, hasCOPD = false, altitude = null)
        assertEquals(HealthStatus.CRITICAL, r.status)
    }

    @Test
    fun `SpO2 89 with COPD can align personal oxygen target band`() {
        val r = HealthAnalysisEngine.analyzeOxygen(89, false, hasCOPD = true, altitude = null)
        assertEquals(HealthStatus.NORMAL, r.status)
    }
}
