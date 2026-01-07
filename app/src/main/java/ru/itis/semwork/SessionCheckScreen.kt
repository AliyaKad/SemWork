package ru.itis.semwork

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun SessionCheckScreen(
    onLoggedIn: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val viewModel: SessionCheckViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkSession()
    }

    LaunchedEffect(state) {
        when (state) {
            SessionCheckViewModel.SessionState.LoggedIn -> onLoggedIn()
            SessionCheckViewModel.SessionState.LoggedOut -> onLoggedOut()
            SessionCheckViewModel.SessionState.Checking -> Unit
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
