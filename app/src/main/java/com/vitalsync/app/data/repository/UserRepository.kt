package com.vitalsync.app.data.repository

import com.vitalsync.app.data.db.dao.UserDao
import com.vitalsync.app.data.db.entities.UserEntity
import com.vitalsync.app.data.security.PinHasher
import com.vitalsync.app.domain.model.User
import com.vitalsync.app.domain.model.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    /**
     * Yeni kullanıcı oluşturur. PIN hash'lenir ve salt ile birlikte saklanır.
     * @return Oluşturulan kullanıcının ID'si
     */
    suspend fun createUser(
        firstName: String,
        lastName: String,
        username: String,
        pin: String,
        age: Int,
        gender: String,
        heightCm: Float,
        weightKg: Float,
        bloodType: String,
        rhFactor: String,
        activityLevel: String = "Belirtilmedi",
        isSmoker: Boolean = false,
        hasDiabetes: Boolean = false,
        hasHypertension: Boolean = false,
        hasCOPD: Boolean = false,
        hasHeartFailure: Boolean = false,
        hasKidneyDisease: Boolean = false,
        allergies: String? = null,
        healthNotes: String? = null,
        isHealthInfoCompleted: Boolean = false
    ): Long {
        val (hash, salt) = PinHasher.hashPin(pin)
        val bmi = calculateBmi(heightCm, weightKg)

        val entity = UserEntity(
            firstName = firstName,
            lastName = lastName,
            username = username,
            pinHash = hash,
            salt = salt,
            age = age,
            gender = gender,
            heightCm = heightCm,
            weightKg = weightKg,
            bmi = bmi,
            bloodType = bloodType,
            rhFactor = rhFactor,
            activityLevel = activityLevel,
            isSmoker = isSmoker,
            hasDiabetes = hasDiabetes,
            hasHypertension = hasHypertension,
            hasCOPD = hasCOPD,
            hasHeartFailure = hasHeartFailure,
            hasKidneyDisease = hasKidneyDisease,
            allergies = allergies,
            healthNotes = healthNotes,
            isHealthInfoCompleted = isHealthInfoCompleted
        )
        return userDao.insert(entity)
    }

    /**
     * PIN doğrulama. Kullanıcı ID ile PIN eşleşmesini kontrol eder.
     */
    suspend fun verifyPin(userId: Long, pin: String): Boolean {
        val user = userDao.getUserById(userId) ?: return false
        return PinHasher.verifyPin(pin, user.pinHash, user.salt)
    }

    /**
     * Tüm kullanıcıları Flow olarak döner (real-time güncellemeler).
     */
    fun getAllUsers(): Flow<List<User>> =
        userDao.getAllUsers().map { entities -> entities.map { it.toDomain() } }

    /**
     * Kullanıcı adının daha önce alınıp alınmadığını kontrol eder.
     */
    suspend fun isUsernameTaken(username: String): Boolean =
        userDao.isUsernameTaken(username)

    /**
     * Kullanıcı sayısını döner.
     */
    suspend fun getUserCount(): Int = userDao.getUserCount()

    /**
     * ID ile kullanıcı getirir.
     */
    suspend fun getUserById(userId: Long): User? =
        userDao.getUserById(userId)?.toDomain()

    /**
     * Kullanıcı bilgilerini günceller.
     */
    suspend fun updateUser(
        userId: Long,
        firstName: String? = null,
        lastName: String? = null,
        age: Int? = null,
        heightCm: Float? = null,
        weightKg: Float? = null,
        activityLevel: String? = null,
        isSmoker: Boolean? = null,
        hasDiabetes: Boolean? = null,
        hasHypertension: Boolean? = null,
        hasCOPD: Boolean? = null,
        hasHeartFailure: Boolean? = null,
        hasKidneyDisease: Boolean? = null,
        allergies: String? = null,
        healthNotes: String? = null
    ): Boolean {
        val entity = userDao.getUserById(userId) ?: return false
        val updatedEntity = entity.copy(
            firstName = firstName ?: entity.firstName,
            lastName = lastName ?: entity.lastName,
            age = age ?: entity.age,
            heightCm = heightCm ?: entity.heightCm,
            weightKg = weightKg ?: entity.weightKg,
            bmi = calculateBmi(heightCm ?: entity.heightCm, weightKg ?: entity.weightKg),
            activityLevel = activityLevel ?: entity.activityLevel,
            isSmoker = isSmoker ?: entity.isSmoker,
            hasDiabetes = hasDiabetes ?: entity.hasDiabetes,
            hasHypertension = hasHypertension ?: entity.hasHypertension,
            hasCOPD = hasCOPD ?: entity.hasCOPD,
            hasHeartFailure = hasHeartFailure ?: entity.hasHeartFailure,
            hasKidneyDisease = hasKidneyDisease ?: entity.hasKidneyDisease,
            allergies = allergies ?: entity.allergies,
            healthNotes = healthNotes ?: entity.healthNotes
        )
        userDao.update(updatedEntity)
        return true
    }

    /**
     * Kullanıcıyı ve tüm ilişkili verileri siler.
     */
    suspend fun deleteUser(userId: Long): Boolean {
        val entity = userDao.getUserById(userId) ?: return false
        userDao.delete(entity)
        return true
    }

    /**
     * BMI hesaplama: kg / (m^2)
     */
    fun calculateBmi(heightCm: Float, weightKg: Float): Float {
        if (heightCm <= 0f) return 0f
        val heightM = heightCm / 100f
        return weightKg / (heightM * heightM)
    }
}
