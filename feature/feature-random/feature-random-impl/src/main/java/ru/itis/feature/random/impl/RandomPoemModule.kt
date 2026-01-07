package ru.itis.feature.random.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.itis.feature.random.api.GetRandomPoemUseCase
import ru.itis.feature.random.api.RandomPoemRepository
import ru.itis.feature.random.impl.data.RandomPoemRepositoryImpl
import ru.itis.feature.random.impl.domain.GetRandomPoemUseCaseImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class RandomModule {

    @Binds
    abstract fun bindRandomPoemRepository(
        impl: RandomPoemRepositoryImpl
    ): RandomPoemRepository

    @Binds
    abstract fun bindGetRandomPoemUseCase(
        impl: GetRandomPoemUseCaseImpl
    ): GetRandomPoemUseCase
}

