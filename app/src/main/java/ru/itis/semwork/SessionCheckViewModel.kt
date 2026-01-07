package ru.itis.semwork

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.itis.feature.auth.api.AuthRepository
import javax.inject.Inject

@HiltViewModel
class SessionCheckViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    sealed class SessionState {
        object Checking : SessionState()
        object LoggedIn : SessionState()
        object LoggedOut : SessionState()
    }

    private val _state = MutableStateFlow<SessionState>(SessionState.Checking)
    val state: StateFlow<SessionState> = _state

    fun checkSession() {
        viewModelScope.launch {
            try {
                val isLoggedIn = authRepository.isUserLoggedIn()
                _state.value = if (isLoggedIn) {
                    SessionState.LoggedIn
                } else {
                    SessionState.LoggedOut
                }
            } catch (e: Exception) {
                _state.value = SessionState.LoggedOut
            }
        }
    }
}