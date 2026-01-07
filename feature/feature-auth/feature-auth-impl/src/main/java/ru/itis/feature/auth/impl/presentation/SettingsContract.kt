package ru.itis.feature.auth.impl.presentation

object SettingsContract {
    data class State(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val userEmail: String? = null,
        val userName: String? = null,
        val showLogoutDialog: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val isDeleting: Boolean = false
    )

    sealed interface Event {
        object LoadUserData : Event
        object ShowLogoutDialog : Event
        object ShowDeleteDialog : Event
        object HideLogoutDialog : Event
        object HideDeleteDialog : Event
        object ConfirmLogout : Event
        object ClearError : Event
    }

    sealed interface SideEffect {
        object NavigateBack : SideEffect
        object NavigateToAuth : SideEffect
        data class ShowError(val message: String) : SideEffect
        data class ShowMessage(val message: String) : SideEffect
    }
}