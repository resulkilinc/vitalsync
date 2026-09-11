package com.vitalsync.app.data.security

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * PBKDF2 tabanlı PIN hash'leme utility sınıfı.
 * 4 haneli PIN güvenliğini salt + iterasyon ile güçlendirir.
 */
object PinHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 10_000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16

    /**
     * Yeni bir PIN hash'ler.
     * @return Pair(hash, salt) — ikisi de hex string olarak döner
     */
    fun hashPin(pin: String): Pair<String, String> {
        val salt = generateSalt()
        val hash = computeHash(pin, salt)
        return Pair(hash.toHexString(), salt.toHexString())
    }

    /**
     * Girilen PIN'in kayıtlı hash ile eşleşip eşleşmediğini doğrular.
     */
    fun verifyPin(pin: String, storedHash: String, storedSalt: String): Boolean {
        val saltBytes = storedSalt.hexToByteArray()
        val computedHash = computeHash(pin, saltBytes)
        return computedHash.toHexString() == storedHash
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun computeHash(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02x".format(it) }

    private fun String.hexToByteArray(): ByteArray =
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
