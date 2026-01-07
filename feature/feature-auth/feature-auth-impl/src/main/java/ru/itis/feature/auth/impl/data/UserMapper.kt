package ru.itis.feature.auth.impl.data

import ru.itis.core.data.UserEntity
import ru.itis.core.models.User
import javax.inject.Inject

class UserMapper @Inject constructor() {

    fun mapToEntity(user: User, passwordHash: String): UserEntity {
        return UserEntity(
            id = user.id,
            email = user.email,
            passwordHash = passwordHash,
            name = user.name
        )
    }

    fun mapFromEntity(entity: UserEntity): User {
        return User(
            id = entity.id,
            email = entity.email,
            name = entity.name,
            createdAt = System.currentTimeMillis()
        )
    }
}