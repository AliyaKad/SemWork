package ru.itis.feature.auth.impl.presentation

object AuthContract {
    data class State(
        val email: String = "",
        val password: String = "",
        val name: String = "",
        val isLoginMode: Boolean = true,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isSuccess: Boolean = false,
        val isAuthenticated: Boolean = false,
        val emailError: String? = null,
        val passwordError: String? = null,
        val nameError: String? = null
    ) {
        val canLogin: Boolean get() = email.isNotBlank() && password.isNotBlank() && !isLoading && emailError == null && passwordError == null
        val canRegister: Boolean get() = email.isNotBlank() && password.isNotBlank() && name.isNotBlank() && !isLoading && emailError == null && passwordError == null && nameError == null
        val shouldShowLoading: Boolean get() = isLoading && !isSuccess
        val isError: Boolean get() = errorMessage != null && !isLoading
        val hasFieldErrors: Boolean get() = emailError != null || passwordError != null || nameError != null

        fun validateFields(): ValidationResult {
            return when {
                email.isEmpty() -> ValidationResult.Error("Email is required", FieldType.EMAIL)
                !isValidEmail(email) -> ValidationResult.Error("Invalid email format", FieldType.EMAIL)
                password.isEmpty() -> ValidationResult.Error("Password is required", FieldType.PASSWORD)
                password.length < 6 -> ValidationResult.Error("Password must be at least 6 characters", FieldType.PASSWORD)
                !isLoginMode && name.isEmpty() -> ValidationResult.Error("Name is required", FieldType.NAME)
                else -> ValidationResult.Valid
            }
        }

        private fun isValidEmail(email: String): Boolean {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Error(val message: String, val field: FieldType) : ValidationResult()
    }

    enum class FieldType {
        EMAIL, PASSWORD, NAME
    }

    sealed interface Event {
        data class UpdateEmail(val email: String) : Event
        data class UpdatePassword(val password: String) : Event
        data class UpdateName(val name: String) : Event
        object ToggleMode : Event
        object Login : Event
        object Register : Event
        object ClearError : Event
        object ResetState : Event
        object TestCrash : Event
        object ValidateFields : Event
    }

    sealed interface SideEffect {
        object NavigateToHome : SideEffect
        data class ShowError(val message: String) : SideEffect
        data class ShowMessage(val message: String) : SideEffect
    }
}