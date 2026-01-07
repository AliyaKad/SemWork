package ru.itis.feature.auth.impl.data

import ru.itis.core.data.UserDao
import ru.itis.core.data.UserEntity
import ru.itis.core.data.userExists
import ru.itis.core.models.User
import ru.itis.feature.auth.api.AuthRepository
import java.util.regex.Pattern
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val passwordManager: PasswordManager,
    private val sessionManager: SessionManager
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepositoryImpl"
        private val EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)\$"
        )

        private fun isValidEmail(email: String): Boolean {
            return EMAIL_PATTERN.matcher(email).matches()
        }
    }

    override suspend fun register(email: String, password: String, name: String?): Result<User> {
        return try {
            if (!isValidEmail(email)) {
                return Result.failure(Exception("Invalid email format"))
            }

            if (userDao.userExists(email)) {
                return Result.failure(Exception("User with this email already exists"))
            }

            if (password.length < 6) {
                return Result.failure(Exception("Password must be at least 6 characters"))
            }

            val userId = passwordManager.generateUserId()
            val passwordHash = passwordManager.hashPassword(password)

            val userEntity = UserEntity(
                id = userId,
                email = email,
                passwordHash = passwordHash,
                name = name
            )

            userDao.insertUser(userEntity)
            val user = mapToUser(userEntity)
            sessionManager.saveSession(userId)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            if (!isValidEmail(email)) {
                return Result.failure(Exception("Invalid email format"))
            }

            val userEntity = userDao.getUserByEmail(email)
            if (userEntity == null) {
                return Result.failure(Exception("User not found"))
            }

            if (!passwordManager.verifyPassword(password, userEntity.passwordHash)) {
                return Result.failure(Exception("Invalid password"))
            }

            val user = mapToUser(userEntity)

            sessionManager.saveSession(user.id)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val userId = sessionManager.getCurrentUserId()
            userId?.let { id ->
                userDao.getUserById(id)?.let { entity ->
                    mapToUser(entity)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun isUserLoggedIn(): Boolean {
        return try {
            val result = sessionManager.isLoggedIn()
            result
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateUser(user: User): Result<User> {
        return try {
            val currentUserId = sessionManager.getCurrentUserId()
                ?: return Result.failure(Exception("Not logged in"))

            if (currentUserId != user.id) {
                return Result.failure(Exception("Can't update another user"))
            }

            if (!isValidEmail(user.email)) {
                return Result.failure(Exception("Invalid email format"))
            }

            val existingUser = userDao.getUserByEmail(user.email)
            if (existingUser != null && existingUser.id != user.id) {
                return Result.failure(Exception("Email already in use"))
            }

            val oldEntity = userDao.getUserById(user.id)
                ?: return Result.failure(Exception("User not found"))

            val updatedEntity = UserEntity(
                id = user.id,
                email = user.email,
                passwordHash = oldEntity.passwordHash,
                name = user.name
            )

            userDao.updateUser(updatedEntity)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val userId = sessionManager.getCurrentUserId()
                ?: return Result.failure(Exception("Not logged in"))

            userDao.deleteUser(userId)
            sessionManager.clearSession()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapToUser(entity: UserEntity): User {
        return User(
            id = entity.id,
            email = entity.email,
            name = entity.name
        )
    }
}