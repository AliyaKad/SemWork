package ru.itis.feature.favorites.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.itis.feature.favorites.api.AddToFavoritesUseCase
import ru.itis.feature.favorites.api.FavoritesRepository
import ru.itis.feature.favorites.api.IsFavoriteUseCase
import ru.itis.feature.favorites.api.ObserveFavoritesUseCase
import ru.itis.feature.favorites.api.RemoveFromFavoritesUseCase
import ru.itis.feature.favorites.api.ToggleFavoriteUseCase
import ru.itis.feature.favorites.impl.data.FavoritesRepositoryImpl
import ru.itis.feature.favorites.impl.domain.AddToFavoritesUseCaseImpl
import ru.itis.feature.favorites.impl.domain.IsFavoriteUseCaseImpl
import ru.itis.feature.favorites.impl.domain.ObserveFavoritesUseCaseImpl
import ru.itis.feature.favorites.impl.domain.RemoveFromFavoritesUseCaseImpl
import ru.itis.feature.favorites.impl.domain.ToggleFavoriteUseCaseImpl

@Module
@InstallIn(SingletonComponent::class)
interface FavoritesModule {


    @Binds
    fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds
    fun bindAddToFavoritesUseCase(impl: AddToFavoritesUseCaseImpl): AddToFavoritesUseCase

    @Binds
    fun bindRemoveFromFavoritesUseCase(impl: RemoveFromFavoritesUseCaseImpl): RemoveFromFavoritesUseCase

    @Binds
    fun bindIsFavoriteUseCase(impl: IsFavoriteUseCaseImpl): IsFavoriteUseCase

    @Binds
    fun bindToggleFavoriteUseCase(impl: ToggleFavoriteUseCaseImpl): ToggleFavoriteUseCase

    @Binds
    fun bindObserveFavoritesUseCase(impl: ObserveFavoritesUseCaseImpl): ObserveFavoritesUseCase
}