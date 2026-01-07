package ru.itis.feature.favorites.impl.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import ru.itis.core.data.FavoritesDao
import ru.itis.core.models.Poem
import ru.itis.feature.favorites.api.FavoritesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val dao: FavoritesDao,
    private val mapper: PoemMapper,
    @ApplicationContext private val context: Context
) : FavoritesRepository {

    companion object {
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
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private suspend fun getCurrentUserId(): String? = withContext(Dispatchers.IO) {
        val userId = prefs.getString(KEY_USER_ID, null)
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

        if (userId != null && isLoggedIn) {
            userId
        } else {
            null
        }
    }

    override suspend fun addToFavorites(poem: Poem) {
        val userId = getCurrentUserId() ?: return
        dao.insert(mapper.toEntity(poem, userId))
    }

    override suspend fun removeFromFavorites(author: String, title: String) {
        val userId = getCurrentUserId() ?: return
        dao.deleteByUserAuthorAndTitle(userId, author, title)
    }

    override suspend fun getFavorites(): List<Poem> {
        val userId = getCurrentUserId() ?: return emptyList()
        return dao.getAllByUser(userId).map { mapper.fromEntity(it) }
    }

    override suspend fun isFavorite(author: String, title: String): Boolean {
        val userId = getCurrentUserId() ?: return false
        return dao.countByUserAuthorAndTitle(userId, author, title) > 0
    }

    override fun observeFavorites(): Flow<List<Poem>> {
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
}