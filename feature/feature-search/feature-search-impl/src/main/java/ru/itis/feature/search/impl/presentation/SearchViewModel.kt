package ru.itis.feature.search.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.*
import org.orbitmvi.orbit.viewmodel.container
import ru.itis.core.analytics.AnalyticsHelper
import ru.itis.core.analytics.BaseViewModel
import ru.itis.core.analytics.CrashlyticsHelper
import ru.itis.core.analytics.PerformanceHelper
import ru.itis.core.analytics.Trace
import ru.itis.feature.favorites.api.ObserveFavoritesUseCase
import ru.itis.feature.favorites.api.ToggleFavoriteUseCase
import ru.itis.feature.search.api.SearchPoemsUseCase
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchPoemsUseCase: SearchPoemsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    analyticsHelper: AnalyticsHelper,
    crashlyticsHelper: CrashlyticsHelper,
    performanceHelper: PerformanceHelper
) : BaseViewModel(analyticsHelper, crashlyticsHelper, performanceHelper),
    ContainerHost<SearchContract.State, SearchContract.SideEffect> {

    override val container: Container<SearchContract.State, SearchContract.SideEffect> =
        container(SearchContract.State())

    private var screenLoadTrace: Trace? = null
    private var searchTrace: Trace? = null
    private var toggleFavoriteTrace: Trace? = null

    init {
        logScreenView("SearchScreen", "SearchViewModel")
        screenLoadTrace = performanceHelper.startTrace("search_screen_load")
        screenLoadTrace?.incrementMetric("screen_load_count", 1)

        viewModelScope.launch {
            observeFavoritesUseCase()
                .distinctUntilChanged()
                .collect { favorites ->
                    intent {
                        reduce { state.copy(favorites = favorites) }
                    }
                }
        }
    }

    override fun onCleared() {
        screenLoadTrace?.stop()
        searchTrace?.stop()
        toggleFavoriteTrace?.stop()
        super.onCleared()
    }

    fun onEvent(event: SearchContract.Event) = intent {
        when (event) {
            is SearchContract.Event.AuthorChanged -> {
                reduce { state.copy(author = event.author, searchPerformed = false) }
            }
            is SearchContract.Event.TitleChanged -> {
                reduce { state.copy(title = event.title, searchPerformed = false) }
            }
            SearchContract.Event.Search -> {
                if (state.canSearch) {
                    logEvent("search_performed", mapOf(
                        "has_author" to state.author.isNotBlank(),
                        "has_title" to state.title.isNotBlank(),
                        "author_length" to state.author.length,
                        "title_length" to state.title.length
                    ))
                    performSearch()
                }
            }
            is SearchContract.Event.ToggleFavorite -> {
                toggleFavoriteTrace = performanceHelper.startTrace("search_toggle_favorite_operation")
                toggleFavorite(event.poem)
            }
            is SearchContract.Event.PoemClicked -> {
                logEvent("search_poem_selected", mapOf(
                    "poem_author" to (event.poem.author ?: "unknown"),
                    "poem_title_length" to event.poem.title.length
                ))
                postSideEffect(SearchContract.SideEffect.NavigateToPoem(event.poem))
            }
            SearchContract.Event.Retry -> {
                logEvent("search_retry")
                reduce { state.copy(errorMessage = null, searchPerformed = false) }
                if (state.canSearch) {
                    performSearch()
                }
            }
            SearchContract.Event.ClearError -> {
                reduce { state.copy(errorMessage = null) }
            }
        }
    }

    private fun performSearch() = intent {
        searchTrace = performanceHelper.startTrace("search_operation")

        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null,
                poems = emptyList(),
                searchPerformed = true
            )
        }

        val author = state.author.takeIf { it.isNotBlank() }
        val title = state.title.takeIf { it.isNotBlank() }

        viewModelScope.launch {
            val result = searchPoemsUseCase(author, title)

            if (result.isSuccess) {
                val poems = result.getOrDefault(emptyList())
                logEvent("search_results_loaded", mapOf(
                    "results_count" to poems.size,
                    "search_type" to when {
                        author != null && title != null -> "author_and_title"
                        author != null -> "author_only"
                        title != null -> "title_only"
                        else -> "empty"
                    }
                ))
                searchTrace?.stop()

                intent {
                    reduce {
                        state.copy(
                            isLoading = false,
                            poems = poems,
                            searchPerformed = true
                        )
                    }
                }
            } else {
                val error = result.exceptionOrNull()
                logError(error ?: Exception("Search error"))
                logEvent("search_failed", mapOf(
                    "error_type" to (error?.javaClass?.simpleName ?: "Unknown")
                ))

                searchTrace?.incrementMetric("error_count", 1)
                searchTrace?.stop()

                intent {
                    reduce {
                        state.copy(
                            isLoading = false,
                            errorMessage = error?.message ?: "Ошибка поиска",
                            searchPerformed = true
                        )
                    }
                    postSideEffect(SearchContract.SideEffect.ShowError(error?.message ?: "Ошибка поиска"))
                }
            }
        }
    }

    private fun toggleFavorite(poem: ru.itis.core.models.Poem) = intent {
        val isCurrentlyFavorite = state.favorites.any {
            it.author == poem.author && it.title == poem.title
        }

        logEvent("search_favorite_toggled", mapOf(
            "action" to if (isCurrentlyFavorite) "remove" else "add",
            "poem_author" to (poem.author ?: "unknown")
        ))

        val updatedFavorites = if (isCurrentlyFavorite) {
            state.favorites.filterNot {
                it.author == poem.author && it.title == poem.title
            }
        } else {
            state.favorites + poem
        }

        reduce { state.copy(favorites = updatedFavorites) }

        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(poem)

                logEvent("search_favorite_toggle_success", mapOf(
                    "new_state" to !isCurrentlyFavorite
                ))

                toggleFavoriteTrace?.stop()

                val message = if (isCurrentlyFavorite)
                    "Удалено из избранного"
                else
                    "Добавлено в избранное"

                intent {
                    postSideEffect(SearchContract.SideEffect.ShowMessage(message))
                }
            } catch (e: Exception) {
                logError(e)
                logEvent("search_favorite_toggle_failed", mapOf(
                    "error_type" to (e::class.simpleName ?: "Unknown")
                ))

                toggleFavoriteTrace?.incrementMetric("error_count", 1)
                toggleFavoriteTrace?.stop()

                intent {
                    reduce { state.copy(favorites = state.favorites) }
                    postSideEffect(SearchContract.SideEffect.ShowError("Ошибка: ${e.message}"))
                }
            }
        }
    }
}