package ru.itis.feature.creator.impl.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.itis.core.models.Poem

@Composable
fun MyPoemsScreen(
    onNavigateToPoem: (Poem) -> Unit,
    onNavigateToCreatePoem: () -> Unit,
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    onShowDeleteDialog: (Poem, () -> Unit) -> Unit,
    viewModel: MyPoemsViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var poemToDelete by remember { mutableStateOf<Poem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.container.sideEffectFlow.collect { sideEffect ->
            when (sideEffect) {
                is MyPoemsContract.SideEffect.NavigateToPoem -> {
                    onNavigateToPoem(sideEffect.poem)
                }
                MyPoemsContract.SideEffect.NavigateToCreatePoem -> {
                    onNavigateToCreatePoem()
                }
                MyPoemsContract.SideEffect.NavigateBack -> {
                    onNavigateBack()
                }
                is MyPoemsContract.SideEffect.ShowMessage -> {
                    onShowMessage(sideEffect.message)
                }
                is MyPoemsContract.SideEffect.ShowDeleteConfirmation -> {
                    poemToDelete = sideEffect.poem
                    showDeleteDialog = true
                }
            }
        }
    }

    if (showDeleteDialog && poemToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                poemToDelete = null
            },
            title = { Text("Удалить стихотворение") },
            text = {
                Text("Вы уверены, что хотите удалить стихотворение \"${poemToDelete?.title}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        poemToDelete?.let { poem ->
                            viewModel.confirmDelete(poem)
                        }
                        showDeleteDialog = false
                        poemToDelete = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        poemToDelete = null
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    MyPoemsScreenContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onNavigateBack,
        onNavigateToCreatePoem = onNavigateToCreatePoem
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPoemsScreenContent(
    state: MyPoemsContract.State,
    onEvent: (MyPoemsContract.Event) -> Unit,
    onBackClick: () -> Unit,
    onNavigateToCreatePoem: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Мои стихи") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.poems.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onNavigateToCreatePoem,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить новое")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.shouldShowLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.isError -> {
                    ErrorContent(
                        message = state.errorMessage ?: "Неизвестная ошибка",
                        onRetry = { onEvent(MyPoemsContract.Event.Retry) },
                        onBackClick = onBackClick
                    )
                }

                state.isEmpty -> {
                    EmptyContent(
                        onAddNewClick = { onNavigateToCreatePoem() }
                    )
                }

                state.isSuccess -> {
                    PoemsList(
                        poems = state.poems,
                        deletingPoemId = state.deletingPoemId,
                        onPoemClick = { poem -> onEvent(MyPoemsContract.Event.PoemClicked(poem)) },
                        onDeleteClick = { poem -> onEvent(MyPoemsContract.Event.DeletePoem(poem)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
            Text("Повторить")
        }
    }
}

@Composable
private fun EmptyContent(
    onAddNewClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "У вас пока нет стихотворений",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAddNewClick
        ) {
            Text("Создать первое")
        }
    }
}

@Composable
private fun PoemsList(
    poems: List<Poem>,
    deletingPoemId: String?,
    onPoemClick: (Poem) -> Unit,
    onDeleteClick: (Poem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(poems) { poem ->
            val poemId = generatePoemId(poem)

            UserPoemItem(
                poem = poem,
                isDeleting = deletingPoemId == poemId,
                onClick = { onPoemClick(poem) },
                onDelete = { onDeleteClick(poem) }
            )
        }
    }
}

private fun generatePoemId(poem: Poem): String {
    return "${poem.author}_${poem.title}".hashCode().toString()
}

@Composable
private fun UserPoemItem(
    poem: Poem,
    isDeleting: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !isDeleting,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
                Text(
                    text = "${poem.lines.size} строк",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (poem.lines.isNotEmpty()) {
                    Text(
                        text = "\"${poem.lines.first()}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp),
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            if (isDeleting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(
                    onClick = onDelete,
                    enabled = !isDeleting
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}