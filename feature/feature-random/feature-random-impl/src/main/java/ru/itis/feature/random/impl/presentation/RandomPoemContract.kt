package ru.itis.feature.random.impl.presentation

import ru.itis.core.models.Poem

object RandomPoemContract {
    data class State(
        val poem: Poem? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isFavorite: Boolean = false
    ) {
        val shouldShowLoading: Boolean get() = isLoading && poem == null
        val isError: Boolean get() = errorMessage != null
        val isSuccess: Boolean get() = poem != null
    }

    sealed interface Event {
        object LoadRandomPoem : Event
        object Retry : Event
        object ToggleFavorite : Event
    }
}