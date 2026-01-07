package ru.itis.feature.favorites.impl.presentation

import ru.itis.core.models.Poem

object FavoritesContract {
    data class State(
        val poems: List<Poem> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    ) {
        val isSuccess: Boolean get() = poems.isNotEmpty() && !isLoading && errorMessage == null
        val isEmpty: Boolean get() = poems.isEmpty() && !isLoading && errorMessage == null
        val isError: Boolean get() = errorMessage != null && !isLoading
        val shouldShowLoading: Boolean get() = isLoading && poems.isEmpty()
    }

    sealed interface Event {
        object LoadFavorites : Event
        data class RemoveFavorite(val author: String, val title: String) : Event
        data class PoemClicked(val poem: Poem) : Event
        object Retry : Event
        object ClearError : Event
    }

    sealed interface SideEffect {
        data class NavigateToPoem(val poem: Poem) : SideEffect
    }
}