package ru.itis.feature.search.impl.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SearchScreen(
    onNavigateToPoem: (ru.itis.core.models.Poem) -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SearchContract.SideEffect.NavigateToPoem -> {
                onNavigateToPoem(sideEffect.poem)
            }
            is SearchContract.SideEffect.ShowMessage -> {
                onShowMessage(sideEffect.message)
            }
            is SearchContract.SideEffect.ShowError -> {
                onShowMessage("Ошибка: ${sideEffect.message}")
            }
        }
    }

    SearchScreenContent(
        state = state,
        onEvent = viewModel::onEvent
    )
}


@Composable
private fun SearchScreenContent(
    state: SearchContract.State,
    onEvent: (SearchContract.Event) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = state.author,
            onValueChange = { onEvent(SearchContract.Event.AuthorChanged(it)) },
            label = { Text("Автор") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.title,
            onValueChange = { onEvent(SearchContract.Event.TitleChanged(it)) },
            label = { Text("Название") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onEvent(SearchContract.Event.Search) },
            enabled = state.canSearch,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Поиск")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator()
                }

                state.isError -> {
                    ErrorContent(
                        message = state.errorMessage ?: "Неизвестная ошибка",
                        onRetry = { onEvent(SearchContract.Event.Retry) }
                    )
                }
                state.shouldShowEmptyState -> {
                    EmptySearchResults()
                }
                state.isSuccess -> {
                    PoemsList(
                        poems = state.poems,
                        favorites = state.favorites,
                        onPoemClick = { poem -> onEvent(SearchContract.Event.PoemClicked(poem)) },
                        onToggleFavorite = { poem -> onEvent(SearchContract.Event.ToggleFavorite(poem)) }
                    )
                }
                else -> {
                }
            }
        }
    }
}

@Composable
private fun EmptySearchResults() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Стихотворения не найдены")
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ошибка",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry
        ) {
            Text("Попробовать снова")
        }
    }
}

@Composable
private fun PoemsList(
    poems: List<ru.itis.core.models.Poem>,
    favorites: List<ru.itis.core.models.Poem>,
    onPoemClick: (ru.itis.core.models.Poem) -> Unit,
    onToggleFavorite: (ru.itis.core.models.Poem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(poems) { poem ->
            val isFavorite = favorites.any {
                it.author == poem.author && it.title == poem.title
            }

            PoemListItem(
                poem = poem,
                isFavorite = isFavorite,
                onToggleFavorite = { onToggleFavorite(poem) },
                onClick = { onPoemClick(poem) }
            )
        }
    }
}

@Composable
private fun PoemListItem(
    poem: ru.itis.core.models.Poem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = poem.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "— ${poem.author}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}