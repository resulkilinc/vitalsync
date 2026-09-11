package com.vitalsync.app.data.repository

import com.vitalsync.app.data.db.dao.*
import com.vitalsync.app.data.db.entities.*
import com.vitalsync.app.domain.model.*
import com.vitalsync.app.sync.SyncConstants
import com.vitalsync.app.sync.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VitalsRepository @Inject constructor(
    private val bpDao: BloodPressureDao,
    private val glucoseDao: GlucoseDao,
    private val heartRateDao: HeartRateDao,
    private val oxygenDao: OxygenDao,
    private val syncRepository: SyncRepository,
) {
    private data class LatestCandidate(val timestamp: Long, val delete: suspend () -> Unit)

    // === KAN BASINCI ===
    suspend fun insertBloodPressure(
        userId: Long,
        systolic: Int,
        diastolic: Int,
        pulse: Int? = null,
        arm: String = "Sol",
        context: String = "İstirahat",
        notes: String? = null,
    ): Long {
        val ts = System.currentTimeMillis()
        val id = bpDao.insert(
            BloodPressureEntity(
                userId = userId,
                systolic = systolic,
                diastolic = diastolic,
                pulse = pulse,
                arm = arm,
                context = context,
                notes = notes,
                timestamp = ts,
            ),
        )
        syncRepository.enqueueVitalPayload(
            userId = userId,
            vitalKind = SyncConstants.KIND_BP,
            localRecordId = id,
            payload = mapOf(
                "type" to "blood_pressure",
                "timestamp" to ts,
                "systolic" to systolic,
                "diastolic" to diastolic,
                "pulse" to pulse,
                "arm" to arm,
                "context" to context,
                "notes" to notes,
            ),
        )
        return id
    }

    suspend fun getLatestBloodPressure(userId: Long): BloodPressureRecord? =
        bpDao.getLatest(userId)?.toDomain()

    fun getAllBloodPressure(userId: Long): Flow<List<BloodPressureRecord>> =
        bpDao.getAllByUser(userId).map { list -> list.map { it.toDomain() } }

    suspend fun getBloodPressureByDateRange(
        userId: Long,
        startTime: Long,
        endTime: Long,
    ): List<BloodPressureRecord> =
        bpDao.getByDateRange(userId, startTime, endTime).map { it.toDomain() }

    // === KAN ŞEKERİ ===
    suspend fun insertGlucose(
        userId: Long,
        value: Int,
        context: String,
        method: String = "Parmak Ucu",
        notes: String? = null,
    ): Long {
        val ts = System.currentTimeMillis()
        val id = glucoseDao.insert(
            GlucoseEntity(
                userId = userId,
                value = value,
                context = context,
                method = method,
                notes = notes,
                timestamp = ts,
            ),
        )
        syncRepository.enqueueVitalPayload(
            userId = userId,
            vitalKind = SyncConstants.KIND_GLUCOSE,
            localRecordId = id,
            payload = mapOf(
                "type" to "glucose",
                "timestamp" to ts,
                "valueMgDl" to value,
                "context" to context,
                "method" to method,
                "notes" to notes,
            ),
        )
        return id
    }

    suspend fun getLatestGlucose(userId: Long): GlucoseRecord? =
        glucoseDao.getLatest(userId)?.toDomain()

    fun getAllGlucose(userId: Long): Flow<List<GlucoseRecord>> =
        glucoseDao.getAllByUser(userId).map { list -> list.map { it.toDomain() } }

    suspend fun getGlucoseByDateRange(
        userId: Long,
        startTime: Long,
        endTime: Long,
    ): List<GlucoseRecord> =
        glucoseDao.getByDateRange(userId, startTime, endTime).map { it.toDomain() }

    // === NABIZ ===
    suspend fun insertHeartRate(
        userId: Long,
        value: Int,
        context: String = "İstirahat",
        hasFever: Boolean = false,
        hasPalpitations: Boolean = false,
        notes: String? = null,
    ): Long {
        val ts = System.currentTimeMillis()
        val id = heartRateDao.insert(
            HeartRateEntity(
                userId = userId,
                value = value,
                context = context,
                hasFever = hasFever,
                hasPalpitations = hasPalpitations,
                notes = notes,
                timestamp = ts,
            ),
        )
        syncRepository.enqueueVitalPayload(
            userId = userId,
            vitalKind = SyncConstants.KIND_HEART_RATE,
            localRecordId = id,
            payload = mapOf(
                "type" to "heart_rate",
                "timestamp" to ts,
                "bpm" to value,
                "context" to context,
                "hasFever" to hasFever,
                "hasPalpitations" to hasPalpitations,
                "notes" to notes,
            ),
        )
        return id
    }

    suspend fun getLatestHeartRate(userId: Long): HeartRateRecord? =
        heartRateDao.getLatest(userId)?.toDomain()

    fun getAllHeartRate(userId: Long): Flow<List<HeartRateRecord>> =
        heartRateDao.getAllByUser(userId).map { list -> list.map { it.toDomain() } }

    suspend fun getHeartRateByDateRange(
        userId: Long,
        startTime: Long,
        endTime: Long,
    ): List<HeartRateRecord> =
        heartRateDao.getByDateRange(userId, startTime, endTime).map { it.toDomain() }

    // === OKSİJEN ===
    suspend fun insertOxygen(
        userId: Long,
        value: Int,
        hasBreathingDifficulty: Boolean = false,
        hasPalpitations: Boolean = false,
        altitude: Int? = null,
        notes: String? = null,
    ): Long {
        val ts = System.currentTimeMillis()
        val id = oxygenDao.insert(
            OxygenEntity(
                userId = userId,
                value = value,
                hasBreathingDifficulty = hasBreathingDifficulty,
                hasPalpitations = hasPalpitations,
                altitude = altitude,
                notes = notes,
                timestamp = ts,
            ),
        )
        syncRepository.enqueueVitalPayload(
            userId = userId,
            vitalKind = SyncConstants.KIND_OXYGEN,
            localRecordId = id,
            payload = mapOf(
                "type" to "oxygen",
                "timestamp" to ts,
                "spo2" to value,
                "hasBreathingDifficulty" to hasBreathingDifficulty,
                "hasPalpitations" to hasPalpitations,
                "altitudeM" to altitude,
                "notes" to notes,
            ),
        )
        return id
    }

    suspend fun getLatestOxygen(userId: Long): OxygenRecord? =
        oxygenDao.getLatest(userId)?.toDomain()

    fun getAllOxygen(userId: Long): Flow<List<OxygenRecord>> =
        oxygenDao.getAllByUser(userId).map { list -> list.map { it.toDomain() } }

    suspend fun getOxygenByDateRange(
        userId: Long,
        startTime: Long,
        endTime: Long,
    ): List<OxygenRecord> =
        oxygenDao.getByDateRange(userId, startTime, endTime).map { it.toDomain() }

    /**
     * En güncel ölçüm kaydını (türler arasında tek zaman damgasına göre) siler — CRUD kanıtı.
     */
    suspend fun deleteLatestMeasurement(userId: Long): Boolean {
        val candidates = buildList {
            bpDao.getLatest(userId)?.let { row ->
                add(LatestCandidate(row.timestamp) { bpDao.delete(row) })
            }
            glucoseDao.getLatest(userId)?.let { row ->
                add(LatestCandidate(row.timestamp) { glucoseDao.delete(row) })
            }
            heartRateDao.getLatest(userId)?.let { row ->
                add(LatestCandidate(row.timestamp) { heartRateDao.delete(row) })
            }
            oxygenDao.getLatest(userId)?.let { row ->
                add(LatestCandidate(row.timestamp) { oxygenDao.delete(row) })
            }
        }
        val best = candidates.maxByOrNull { it.timestamp } ?: return false
        best.delete()
        return true
    }
}
