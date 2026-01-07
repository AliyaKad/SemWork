package ru.itis.feature.creator.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.itis.feature.creator.api.DeletePoemUseCase
import ru.itis.feature.creator.api.GetUserPoemsUseCase
import ru.itis.feature.creator.api.ObserveUserPoemsUseCase
import ru.itis.feature.creator.api.SavePoemUseCase
import ru.itis.feature.creator.api.UserPoemsRepository
import ru.itis.feature.creator.impl.data.UserPoemsRepositoryImpl
import ru.itis.feature.creator.impl.domain.DeletePoemUseCaseImpl
import ru.itis.feature.creator.impl.domain.GetUserPoemsUseCaseImpl
import ru.itis.feature.creator.impl.domain.ObserveUserPoemsUseCaseImpl
import ru.itis.feature.creator.impl.domain.SavePoemUseCaseImpl


@Module
@InstallIn(SingletonComponent::class)
interface CreatorModule {

    @Binds
    fun bindUserPoemsRepository(impl: UserPoemsRepositoryImpl): UserPoemsRepository

    @Binds
    fun bindSavePoemUseCase(impl: SavePoemUseCaseImpl): SavePoemUseCase

    @Binds
    fun bindGetUserPoemsUseCase(impl: GetUserPoemsUseCaseImpl): GetUserPoemsUseCase

    @Binds
    fun bindObserveUserPoemsUseCase(impl: ObserveUserPoemsUseCaseImpl): ObserveUserPoemsUseCase

    @Binds
    fun bindDeletePoemUseCase(impl: DeletePoemUseCaseImpl): DeletePoemUseCase
}