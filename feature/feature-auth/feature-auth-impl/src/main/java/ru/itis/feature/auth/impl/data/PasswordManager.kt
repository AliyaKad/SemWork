package ru.itis.feature.auth.impl.data

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject

class PasswordManager @Inject constructor() {

    private val secureRandom = SecureRandom()

    fun hashPassword(password: String): String {
        val salt = ByteArray(16)
        secureRandom.nextBytes(salt)

        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val hashBytes = digest.digest(password.toByteArray())

        return "${Base64.encodeToString(hashBytes, Base64.NO_WRAP)}:${Base64.encodeToString(salt, Base64.NO_WRAP)}"
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            val parts = storedHash.split(":")
            if (parts.size != 2) return false

            val storedHashBase64 = parts[0]
            val saltBase64 = parts[1]

            val salt = Base64.decode(saltBase64, Base64.NO_WRAP)

            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(salt)
            val hashBytes = digest.digest(password.toByteArray())
            val passwordHashBase64 = Base64.encodeToString(hashBytes, Base64.NO_WRAP)

            passwordHashBase64 == storedHashBase64
        } catch (e: Exception) {
            false
        }
    }

    fun generateUserId(): String {
        val bytes = ByteArray(16)
        secureRandom.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}