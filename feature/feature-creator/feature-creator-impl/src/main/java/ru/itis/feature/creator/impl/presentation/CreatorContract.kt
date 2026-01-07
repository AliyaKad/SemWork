package ru.itis.feature.creator.impl.presentation

object CreatorContract {
    data class State(
        val title: String = "",
        val lines: List<String> = listOf(""),
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isSuccess: Boolean = false
    ) {
        val canSave: Boolean get() = title.isNotBlank() && lines.any { it.isNotBlank() }
        val shouldShowLoading: Boolean get() = isLoading && !isSuccess
        val isError: Boolean get() = errorMessage != null && !isLoading
        val isValid: Boolean get() = title.isNotBlank() && lines.any { it.isNotBlank() }
    }

    sealed interface Event {
        data class UpdateTitle(val title: String) : Event
        data class UpdateLine(val index: Int, val text: String) : Event
        object AddLine : Event
        data class RemoveLine(val index: Int) : Event
        object SavePoem : Event
        object ResetState : Event
        object ClearError : Event
    }

    sealed interface SideEffect {
        object NavigateBack : SideEffect
        object ShowSuccess : SideEffect
        data class ShowError(val message: String) : SideEffect
    }
}