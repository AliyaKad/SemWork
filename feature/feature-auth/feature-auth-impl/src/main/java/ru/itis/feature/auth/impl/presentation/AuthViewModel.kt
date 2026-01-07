package ru.itis.feature.auth.impl.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import ru.itis.core.analytics.AnalyticsHelper
import ru.itis.core.analytics.BaseViewModel
import ru.itis.core.analytics.CrashlyticsHelper
import ru.itis.core.analytics.PerformanceHelper
import ru.itis.core.analytics.Trace
import ru.itis.feature.auth.api.LoginUseCase
import ru.itis.feature.auth.api.RegisterUseCase
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    analyticsHelper: AnalyticsHelper,
    crashlyticsHelper: CrashlyticsHelper,
    performanceHelper: PerformanceHelper
) : BaseViewModel(analyticsHelper, crashlyticsHelper, performanceHelper),
    ContainerHost<AuthContract.State, AuthContract.SideEffect> {

    override val container = container<AuthContract.State, AuthContract.SideEffect>(AuthContract.State())

    private val currentState: AuthContract.State
        get() = container.stateFlow.value

    private var screenLoadTrace: Trace? = null

    init {
        logScreenView("AuthScreen", "AuthViewModel")
        screenLoadTrace = performanceHelper.startTrace("auth_screen_load")
        screenLoadTrace?.incrementMetric("screen_load_count", 1)
    }

    override fun onCleared() {
        screenLoadTrace?.stop()
        super.onCleared()
    }

    fun onEvent(event: AuthContract.Event) {
        when (event) {
            is AuthContract.Event.UpdateEmail -> updateEmail(event.email)
            is AuthContract.Event.UpdatePassword -> updatePassword(event.password)
            is AuthContract.Event.UpdateName -> updateName(event.name)
            AuthContract.Event.ToggleMode -> toggleMode()
            AuthContract.Event.Login -> login()
            AuthContract.Event.Register -> register()
            AuthContract.Event.TestCrash -> testCrash()
            AuthContract.Event.ClearError -> clearError()
            AuthContract.Event.ResetState -> resetState()
            AuthContract.Event.ValidateFields -> validateFields()
        }
    }

    private fun updateEmail(email: String) = intent {
        val emailError = if (email.isNotBlank() && !isValidEmail(email)) {
            "Invalid email format"
        } else {
            null
        }
        reduce {
            state.copy(
                email = email,
                emailError = emailError,
                errorMessage = null
            )
        }
    }

    private fun updatePassword(password: String) = intent {
        val passwordError = when {
            password.isBlank() -> null
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
        reduce {
            state.copy(
                password = password,
                passwordError = passwordError,
                errorMessage = null
            )
        }
    }

    private fun updateName(name: String) = intent {
        val nameError = if (name.isBlank() && !currentState.isLoginMode) {
            "Name is required"
        } else {
            null
        }
        reduce {
            state.copy(
                name = name,
                nameError = nameError,
                errorMessage = null
            )
        }
    }

    private fun toggleMode() = intent {
        reduce {
            state.copy(
                isLoginMode = !state.isLoginMode,
                errorMessage = null,
                emailError = null,
                passwordError = null,
                nameError = null,
                email = "",
                password = "",
                name = ""
            )
        }
    }

    private fun testCrash() {
        throw RuntimeException("Test Crash from Auth Screen - ${System.currentTimeMillis()}")
    }

    private fun login() = intent {
        val validationResult = validateAllFields()
        if (validationResult != AuthContract.ValidationResult.Valid) {
            return@intent
        }

        val trace = performanceHelper.startTrace("auth_login_operation")

        reduce { state.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = loginUseCase.invoke(currentState.email, currentState.password)
            result.fold(
                onSuccess = { user ->
                    trace.stop()
                    reduce {
                        state.copy(
                            isLoading = false,
                            isSuccess = true,
                            isAuthenticated = true,
                            errorMessage = null
                        )
                    }
                    postSideEffect(AuthContract.SideEffect.NavigateToHome)
                    postSideEffect(AuthContract.SideEffect.ShowMessage("Welcome back!"))
                },
                onFailure = { error ->
                    logError(error)
                    trace.incrementMetric("error_count", 1)
                    trace.stop()
                    reduce {
                        state.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Login failed"
                        )
                    }
                    postSideEffect(AuthContract.SideEffect.ShowError(error.message ?: "Login failed"))
                }
            )
        }
    }

    private fun register() = intent {
        val validationResult = validateAllFields()
        if (validationResult != AuthContract.ValidationResult.Valid) {
            return@intent
        }

        val trace = performanceHelper.startTrace("auth_register_operation")

        reduce { state.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = registerUseCase.invoke(currentState.email, currentState.password, currentState.name)
            result.fold(
                onSuccess = { user ->
                    trace.stop()
                    reduce {
                        state.copy(
                            isLoading = false,
                            isSuccess = true,
                            isAuthenticated = true,
                            errorMessage = null
                        )
                    }
                    postSideEffect(AuthContract.SideEffect.NavigateToHome)
                    postSideEffect(AuthContract.SideEffect.ShowMessage("Account created successfully!"))
                },
                onFailure = { error ->
                    logError(error)
                    trace.incrementMetric("error_count", 1)
                    trace.stop()
                    reduce {
                        state.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Registration failed"
                        )
                    }
                    postSideEffect(AuthContract.SideEffect.ShowError(error.message ?: "Registration failed"))
                }
            )
        }
    }

    private fun clearError() = intent {
        reduce { state.copy(errorMessage = null) }
    }

    private fun resetState() = intent {
        reduce {
            AuthContract.State(
                isLoginMode = currentState.isLoginMode,
                isAuthenticated = currentState.isAuthenticated
            )
        }
    }

    private fun validateFields() = intent {
        validateAllFields()
    }

    private fun validateAllFields(): AuthContract.ValidationResult {
        val validationResult = currentState.validateFields()

        return when (validationResult) {
            is AuthContract.ValidationResult.Error -> {
                intent {
                    reduce {
                        when (validationResult.field) {
                            AuthContract.FieldType.EMAIL -> state.copy(
                                emailError = validationResult.message,
                                passwordError = null,
                                nameError = null
                            )
                            AuthContract.FieldType.PASSWORD -> state.copy(
                                emailError = null,
                                passwordError = validationResult.message,
                                nameError = null
                            )
                            AuthContract.FieldType.NAME -> state.copy(
                                emailError = null,
                                passwordError = null,
                                nameError = validationResult.message
                            )
                        }
                    }
                }
                validationResult
            }
            AuthContract.ValidationResult.Valid -> {
                intent {
                    reduce {
                        state.copy(
                            emailError = null,
                            passwordError = null,
                            nameError = null
                        )
                    }
                }
                AuthContract.ValidationResult.Valid
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}