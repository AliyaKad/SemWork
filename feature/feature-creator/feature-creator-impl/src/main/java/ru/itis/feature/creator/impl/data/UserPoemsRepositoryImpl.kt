package ru.itis.feature.creator.impl.data

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import ru.itis.core.data.UserPoemsDao
import ru.itis.core.models.Poem
import ru.itis.feature.creator.api.UserPoemsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPoemsRepositoryImpl @Inject constructor(
    private val dao: UserPoemsDao,
    private val mapper: UserPoemMapper,
    @ApplicationContext private val context: Context
) : UserPoemsRepository {

    companion object {
        private const val TAG = "UserPoemsRepository"
        private const val PREFS_NAME = "auth_session"
        private const val KEY_USER_ID = "current_user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    private val prefs by lazy {
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
            Log.e(TAG, "Failed to create EncryptedSharedPreferences, using regular", e)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private suspend fun getCurrentUserId(): String? = withContext(Dispatchers.IO) {
        val userId = prefs.getString(KEY_USER_ID, null)
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

        Log.d(TAG, "getCurrentUserId(): userId=$userId, isLoggedIn=$isLoggedIn")

        return@withContext if (userId != null && isLoggedIn) {
            userId
        } else {
            null
        }
    }

    override suspend fun savePoem(poem: Poem) {
        val userId = getCurrentUserId() ?: return
        dao.insert(mapper.toEntity(poem, userId))
    }

    override suspend fun deletePoem(poemId: String) {
        val userId = getCurrentUserId() ?: return
        dao.deleteById(userId, poemId)
    }

    override suspend fun getAllPoems(): List<Poem> {
        val userId = getCurrentUserId() ?: return emptyList()
        return dao.getAllByUser(userId).map { mapper.fromEntity(it) }
    }

    override fun observeAllPoems(): Flow<List<Poem>> {
        return flow {
            val userId = withContext(Dispatchers.IO) {
                prefs.getString(KEY_USER_ID, null)?.takeIf {
                    prefs.getBoolean(KEY_IS_LOGGED_IN, false)
                }
            }

            if (userId == null) {
                emit(emptyList())
                return@flow
            }

            dao.observeAllByUser(userId).collect { entities ->
                emit(entities.map { mapper.fromEntity(it) })
            }
        }
    }

    override suspend fun getPoemsCount(): Int {
        val userId = getCurrentUserId() ?: return 0
        return dao.getAllByUser(userId).size
    }
}