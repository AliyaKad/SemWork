package ru.itis.feature.creator.impl.presentation

import ru.itis.core.models.Poem

object MyPoemsContract {
    data class State(
        val poems: List<Poem> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val deletingPoemId: String? = null
    ) {
        val isSuccess: Boolean get() = poems.isNotEmpty() && !isLoading && errorMessage == null
        val isEmpty: Boolean get() = poems.isEmpty() && !isLoading && errorMessage == null
        val isError: Boolean get() = errorMessage != null && !isLoading
        val shouldShowLoading: Boolean get() = isLoading && poems.isEmpty()
    }

    sealed interface Event {
        object LoadPoems : Event
        data class DeletePoem(val poem: Poem) : Event
        data class PoemClicked(val poem: Poem) : Event
        object Retry : Event
        object ClearError : Event
        object AddNewPoem : Event
    }

    sealed interface SideEffect {
        data class NavigateToPoem(val poem: Poem) : SideEffect
        object NavigateToCreatePoem : SideEffect
        object NavigateBack : SideEffect
        data class ShowMessage(val message: String) : SideEffect
        data class ShowDeleteConfirmation(val poem: Poem) : SideEffect
    }
}