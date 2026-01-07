package ru.itis.feature.favorites.impl.presentation

import ru.itis.core.models.Poem

object PoemDetailContract {
    data class State(
        val poem: Poem? = null,
        val isFavorite: Boolean = false,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    ) {
        val isSuccess: Boolean get() = poem != null && !isLoading && errorMessage == null
        val shouldShowLoading: Boolean get() = isLoading && poem == null
        val isError: Boolean get() = errorMessage != null && !isLoading
    }

    sealed interface Event {
        data class SetPoem(val poem: Poem) : Event
        object ToggleFavorite : Event
        object Retry : Event
        object ClearError : Event
    }

    sealed interface SideEffect {
        data class ShowMessage(val message: String) : SideEffect
        object NavigateBack : SideEffect
    }
}