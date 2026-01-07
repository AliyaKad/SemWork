package ru.itis.feature.search.impl.presentation

import ru.itis.core.models.Poem

object SearchContract {
    data class State(
        val author: String = "",
        val title: String = "",
        val poems: List<Poem> = emptyList(),
        val favorites: List<Poem> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val searchPerformed: Boolean = false
    ) {
        val canSearch: Boolean get() = author.isNotBlank() || title.isNotBlank()
        val shouldShowLoading: Boolean get() = isLoading && poems.isEmpty()
        val isError: Boolean get() = errorMessage != null && !isLoading
        val isSuccess: Boolean get() = poems.isNotEmpty() && !isLoading
        val shouldShowEmptyState: Boolean get() = searchPerformed && poems.isEmpty() && errorMessage == null && !isLoading
    }

    sealed interface Event {
        data class AuthorChanged(val author: String) : Event
        data class TitleChanged(val title: String) : Event
        object Search : Event
        data class ToggleFavorite(val poem: Poem) : Event
        data class PoemClicked(val poem: Poem) : Event
        object Retry : Event
        object ClearError : Event
    }

    sealed interface SideEffect {
        data class NavigateToPoem(val poem: Poem) : SideEffect
        data class ShowMessage(val message: String) : SideEffect
        data class ShowError(val message: String) : SideEffect
    }
}