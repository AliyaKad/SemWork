package ru.itis.feature.auth.impl.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val context: Context
) {

    companion object {
        private const val PREFS_NAME = "auth_session"
        private const val KEY_USER_ID = "current_user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    private val sharedPreferences by lazy {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    suspend fun saveSession(userId: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .putString(KEY_USER_ID, userId)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    suspend fun clearSession() = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .remove(KEY_USER_ID)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }

    suspend fun getCurrentUserId(): String? = withContext(Dispatchers.IO) {
        val userId = sharedPreferences.getString(KEY_USER_ID, null)
        userId
    }

    suspend fun isLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        val userId = sharedPreferences.getString(KEY_USER_ID, null)
        val isLoggedInFlag = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        val result = userId != null && isLoggedInFlag
        result
    }
}