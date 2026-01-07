package ru.itis.feature.search.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.itis.feature.search.api.SearchPoemRepository
import ru.itis.feature.search.api.SearchPoemsUseCase
import ru.itis.feature.search.impl.data.SearchPoemRepositoryImpl
import ru.itis.feature.search.impl.domain.SearchPoemsUseCaseImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SearchModule {

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        impl: SearchPoemRepositoryImpl
    ): SearchPoemRepository

    @Binds
    @Singleton
    abstract fun bindSearchPoemsUseCase(
        impl: SearchPoemsUseCaseImpl
    ): SearchPoemsUseCase
}