package com.vitalsync.app.util

import android.content.Context
import android.net.Uri
import com.vitalsync.app.data.db.dao.*
import com.vitalsync.app.data.db.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Kullanıcı verilerini JSON formatında dışa aktarma ve geri yükleme yöneticisi.
 */
class DataBackupManager(
    private val userDao: UserDao,
    private val bpDao: BloodPressureDao,
    private val glucoseDao: GlucoseDao,
    private val heartRateDao: HeartRateDao,
    private val oxygenDao: OxygenDao,
    private val reminderDao: ReminderDao
) {

    /**
     * Tüm kullanıcı verilerini JSON olarak dışa aktarır.
     * @return Oluşturulan JSON dosyasının File nesnesi
     */
    suspend fun exportData(context: Context, userId: Long): File = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId)
            ?: throw Exception("Kullanıcı bulunamadı")

        val now = System.currentTimeMillis()
        val allBp = bpDao.getByDateRange(userId, 0, now)
        val allGlucose = glucoseDao.getByDateRange(userId, 0, now)
        val allHr = heartRateDao.getByDateRange(userId, 0, now)
        val allO2 = oxygenDao.getByDateRange(userId, 0, now)
        val allReminders = reminderDao.getEnabledReminders(userId)

        val root = JSONObject().apply {
            put("appVersion", "1.0.0")
            put("appName", "VitalÖlçüm")
            put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("tr", "TR")).format(Date()))
            put("user", userToJson(user))
            put("bloodPressure", bpListToJson(allBp))
            put("glucose", glucoseListToJson(allGlucose))
            put("heartRate", hrListToJson(allHr))
            put("oxygen", o2ListToJson(allO2))
            put("reminders", remindersToJson(allReminders))
        }

        val fileName = "VitalÖlçüm_Yedek_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.json"
        val dir = File(context.getExternalFilesDir(null), "backups")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, fileName)
        file.writeText(root.toString(2))
        file
    }

    /**
     * JSON dosyasından veri geri yükleme işlemi yapar.
     * @return Import edilen toplam kayıt sayısı
     */
    suspend fun importData(context: Context, uri: Uri, userId: Long): Int = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Dosya açılamadı")

        val jsonText = inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(jsonText)

        var totalImported = 0

        // Kan Basıncı
        val bpArray = root.optJSONArray("bloodPressure")
        if (bpArray != null) {
            for (i in 0 until bpArray.length()) {
                val obj = bpArray.getJSONObject(i)
                bpDao.insert(BloodPressureEntity(
                    userId = userId,
                    systolic = obj.getInt("systolic"),
                    diastolic = obj.getInt("diastolic"),
                    pulse = if (obj.has("pulse") && !obj.isNull("pulse")) obj.getInt("pulse") else null,
                    arm = obj.optString("arm", "Sol"),
                    context = obj.optString("context", "İstirahat"),
                    notes = if (obj.has("notes") && !obj.isNull("notes")) obj.getString("notes") else null,
                    timestamp = obj.getLong("timestamp")
                ))
                totalImported++
            }
        }

        // Kan Şekeri
        val glucoseArray = root.optJSONArray("glucose")
        if (glucoseArray != null) {
            for (i in 0 until glucoseArray.length()) {
                val obj = glucoseArray.getJSONObject(i)
                glucoseDao.insert(GlucoseEntity(
                    userId = userId,
                    value = obj.getInt("value"),
                    context = obj.getString("context"),
                    method = obj.optString("method", "Parmak Ucu"),
                    notes = if (obj.has("notes") && !obj.isNull("notes")) obj.getString("notes") else null,
                    timestamp = obj.getLong("timestamp")
                ))
                totalImported++
            }
        }

        // Nabız
        val hrArray = root.optJSONArray("heartRate")
        if (hrArray != null) {
            for (i in 0 until hrArray.length()) {
                val obj = hrArray.getJSONObject(i)
                heartRateDao.insert(HeartRateEntity(
                    userId = userId,
                    value = obj.getInt("value"),
                    context = obj.optString("context", "İstirahat"),
                    hasFever = obj.optBoolean("hasFever", false),
                    hasPalpitations = obj.optBoolean("hasPalpitations", false),
                    notes = if (obj.has("notes") && !obj.isNull("notes")) obj.getString("notes") else null,
                    timestamp = obj.getLong("timestamp")
                ))
                totalImported++
            }
        }

        // Oksijen
        val o2Array = root.optJSONArray("oxygen")
        if (o2Array != null) {
            for (i in 0 until o2Array.length()) {
                val obj = o2Array.getJSONObject(i)
                oxygenDao.insert(OxygenEntity(
                    userId = userId,
                    value = obj.getInt("value"),
                    hasBreathingDifficulty = obj.optBoolean("hasBreathingDifficulty", false),
                    hasPalpitations = obj.optBoolean("hasPalpitations", false),
                    altitude = if (obj.has("altitude") && !obj.isNull("altitude")) obj.getInt("altitude") else null,
                    notes = if (obj.has("notes") && !obj.isNull("notes")) obj.getString("notes") else null,
                    timestamp = obj.getLong("timestamp")
                ))
                totalImported++
            }
        }

        totalImported
    }

    // === JSON Dönüşüm Yardımcıları ===

    private fun userToJson(user: UserEntity): JSONObject = JSONObject().apply {
        put("firstName", user.firstName)
        put("lastName", user.lastName)
        put("username", user.username)
        put("age", user.age)
        put("gender", user.gender)
        put("heightCm", user.heightCm)
        put("weightKg", user.weightKg)
        put("bloodType", user.bloodType)
        put("rhFactor", user.rhFactor)
        put("activityLevel", user.activityLevel)
        put("isSmoker", user.isSmoker)
        put("hasDiabetes", user.hasDiabetes)
        put("hasHypertension", user.hasHypertension)
        put("hasCOPD", user.hasCOPD)
        put("hasHeartFailure", user.hasHeartFailure)
        put("hasKidneyDisease", user.hasKidneyDisease)
        put("allergies", user.allergies)
        put("healthNotes", user.healthNotes)
    }

    private fun bpListToJson(list: List<BloodPressureEntity>): JSONArray {
        val arr = JSONArray()
        list.forEach { bp ->
            arr.put(JSONObject().apply {
                put("systolic", bp.systolic)
                put("diastolic", bp.diastolic)
                put("pulse", bp.pulse)
                put("arm", bp.arm)
                put("context", bp.context)
                put("notes", bp.notes)
                put("timestamp", bp.timestamp)
            })
        }
        return arr
    }

    private fun glucoseListToJson(list: List<GlucoseEntity>): JSONArray {
        val arr = JSONArray()
        list.forEach { g ->
            arr.put(JSONObject().apply {
                put("value", g.value)
                put("context", g.context)
                put("method", g.method)
                put("notes", g.notes)
                put("timestamp", g.timestamp)
            })
        }
        return arr
    }

    private fun hrListToJson(list: List<HeartRateEntity>): JSONArray {
        val arr = JSONArray()
        list.forEach { hr ->
            arr.put(JSONObject().apply {
                put("value", hr.value)
                put("context", hr.context)
                put("hasFever", hr.hasFever)
                put("hasPalpitations", hr.hasPalpitations)
                put("notes", hr.notes)
                put("timestamp", hr.timestamp)
            })
        }
        return arr
    }

    private fun o2ListToJson(list: List<OxygenEntity>): JSONArray {
        val arr = JSONArray()
        list.forEach { o2 ->
            arr.put(JSONObject().apply {
                put("value", o2.value)
                put("hasBreathingDifficulty", o2.hasBreathingDifficulty)
                put("hasPalpitations", o2.hasPalpitations)
                put("altitude", o2.altitude)
                put("notes", o2.notes)
                put("timestamp", o2.timestamp)
            })
        }
        return arr
    }

    private fun remindersToJson(list: List<ReminderEntity>): JSONArray {
        val arr = JSONArray()
        list.forEach { r ->
            arr.put(JSONObject().apply {
                put("title", r.title)
                put("description", r.description)
                put("type", r.type)
                put("hour", r.hour)
                put("minute", r.minute)
                put("daysOfWeek", r.daysOfWeek)
            })
        }
        return arr
    }
}
