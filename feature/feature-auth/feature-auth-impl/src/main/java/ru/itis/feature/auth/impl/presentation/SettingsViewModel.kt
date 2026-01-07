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
import ru.itis.feature.auth.api.GetCurrentUserUseCase
import ru.itis.feature.auth.api.LogoutUseCase
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    analyticsHelper: AnalyticsHelper,
    crashlyticsHelper: CrashlyticsHelper,
    performanceHelper: PerformanceHelper
) : BaseViewModel(analyticsHelper, crashlyticsHelper, performanceHelper),
    ContainerHost<SettingsContract.State, SettingsContract.SideEffect> {

    override val container = container<SettingsContract.State, SettingsContract.SideEffect>(
        SettingsContract.State()
    )

    private var screenLoadTrace: Trace? = null

    init {
        logScreenView("SettingsScreen", "SettingsViewModel")
        screenLoadTrace = performanceHelper.startTrace("settings_screen_load")
        screenLoadTrace?.incrementMetric("screen_load_count", 1)
    }

    override fun onCleared() {
        screenLoadTrace?.stop()
        super.onCleared()
    }

    fun onEvent(event: SettingsContract.Event) {
        when (event) {
            SettingsContract.Event.LoadUserData -> loadUserData()
            SettingsContract.Event.ShowLogoutDialog -> {
                logEvent("settings_logout_button_clicked")
                showLogoutDialog()
            }
            SettingsContract.Event.ShowDeleteDialog -> showDeleteDialog()
            SettingsContract.Event.HideLogoutDialog -> hideLogoutDialog()
            SettingsContract.Event.HideDeleteDialog -> hideDeleteDialog()
            SettingsContract.Event.ConfirmLogout -> confirmLogout()
            SettingsContract.Event.ClearError -> clearError()
        }
    }

    private fun loadUserData() = intent {
        reduce { state.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val user = getCurrentUserUseCase.invoke()

                reduce {
                    state.copy(
                        isLoading = false,
                        userEmail = user?.email,
                        userName = user?.name
                    )
                }
            } catch (e: Exception) {
                logError(e)

                reduce {
                    state.copy(
                        isLoading = false,
                        errorMessage = "Не удалось загрузить данные пользователя"
                    )
                }
                postSideEffect(SettingsContract.SideEffect.ShowError("Не удалось загрузить данные пользователя"))
            }
        }
    }

    private fun showLogoutDialog() = intent {
        reduce { state.copy(showLogoutDialog = true) }
    }

    private fun showDeleteDialog() = intent {
        reduce { state.copy(showDeleteDialog = true) }
    }

    private fun hideLogoutDialog() = intent {
        reduce { state.copy(showLogoutDialog = false) }
    }

    private fun hideDeleteDialog() = intent {
        reduce { state.copy(showDeleteDialog = false) }
    }

    private fun confirmLogout() = intent {
        val trace = performanceHelper.startTrace("settings_logout_operation")

        reduce { state.copy(isLoading = true, showLogoutDialog = false) }

        viewModelScope.launch {
            try {
                logoutUseCase.invoke()

                trace.stop()

                reduce { state.copy(isLoading = false) }
                postSideEffect(SettingsContract.SideEffect.NavigateToAuth)
                postSideEffect(SettingsContract.SideEffect.ShowMessage("Вы успешно вышли из аккаунта"))
            } catch (e: Exception) {
                logError(e)

                trace.incrementMetric("error_count", 1)
                trace.stop()

                reduce {
                    state.copy(
                        isLoading = false,
                        errorMessage = "Ошибка при выходе из аккаунта"
                    )
                }
                postSideEffect(SettingsContract.SideEffect.ShowError("Ошибка при выходе из аккаунта"))
            }
        }
    }

    private fun clearError() = intent {
        reduce { state.copy(errorMessage = null) }
    }
}