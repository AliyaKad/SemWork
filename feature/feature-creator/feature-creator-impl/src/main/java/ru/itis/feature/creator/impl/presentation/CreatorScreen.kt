package ru.itis.feature.creator.impl.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun CreatorScreen(
    onNavigateBack: () -> Unit,
    onShowSuccess: () -> Unit,
    onShowError: (String) -> Unit,
    viewModel: CreatorViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.container.sideEffectFlow.collect { sideEffect ->
            when (sideEffect) {
                CreatorContract.SideEffect.NavigateBack -> {
                    onNavigateBack()
                }
                CreatorContract.SideEffect.ShowSuccess -> {
                    onShowSuccess()
                    viewModel.onEvent(CreatorContract.Event.ResetState)
                }
                is CreatorContract.SideEffect.ShowError -> {
                    onShowError(sideEffect.message)
                }
            }
        }
    }

    CreatorScreenContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorScreenContent(
    state: CreatorContract.State,
    onEvent: (CreatorContract.Event) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Новое стихотворение") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEvent(CreatorContract.Event.SavePoem) },
                        enabled = state.canSave && !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Add, contentDescription = "Сохранить")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (state.isError) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.errorMessage ?: "Ошибка",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onEvent(CreatorContract.Event.ClearError) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Закрыть",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = state.title,
                onValueChange = { onEvent(CreatorContract.Event.UpdateTitle(it)) },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.title.isBlank() && state.title.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Строки стихотворения:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            state.lines.forEachIndexed { index, line ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}.",
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    OutlinedTextField(
                        value = line,
                        onValueChange = { onEvent(CreatorContract.Event.UpdateLine(index, it)) },
                        label = { Text("Строка ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    if (state.lines.size > 1) {
                        IconButton(
                            onClick = { onEvent(CreatorContract.Event.RemoveLine(index)) },
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить строку")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onEvent(CreatorContract.Event.AddLine) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Добавить строку")
            }
        }
    }
}