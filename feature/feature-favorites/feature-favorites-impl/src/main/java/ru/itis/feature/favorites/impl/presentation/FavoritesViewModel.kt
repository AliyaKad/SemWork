package ru.itis.feature.favorites.impl.presentation

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
import ru.itis.feature.favorites.api.RemoveFromFavoritesUseCase
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    analyticsHelper: AnalyticsHelper,
    crashlyticsHelper: CrashlyticsHelper,
    performanceHelper: PerformanceHelper
) : BaseViewModel(analyticsHelper, crashlyticsHelper, performanceHelper),
    ContainerHost<FavoritesContract.State, FavoritesContract.SideEffect> {

    override val container: Container<FavoritesContract.State, FavoritesContract.SideEffect> =
        container(FavoritesContract.State(isLoading = true))

    private var screenLoadTrace: Trace? = null

    init {
        logScreenView("FavoritesScreen", "FavoritesViewModel")
        screenLoadTrace = performanceHelper.startTrace("favorites_screen_load")
        screenLoadTrace?.incrementMetric("screen_load_count", 1)

        viewModelScope.launch {
            observeFavoritesUseCase()
                .distinctUntilChanged()
                .collect { poems ->
                    intent {
                        reduce {
                            state.copy(
                                poems = poems,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                }
        }
    }

    override fun onCleared() {
        screenLoadTrace?.stop()
        super.onCleared()
    }

    fun onEvent(event: FavoritesContract.Event) = intent {
        when (event) {
            FavoritesContract.Event.LoadFavorites -> {
            }
            is FavoritesContract.Event.RemoveFavorite -> {
                removeFavorite(event.author, event.title)
            }
            is FavoritesContract.Event.PoemClicked -> {
                postSideEffect(FavoritesContract.SideEffect.NavigateToPoem(event.poem))
            }
            FavoritesContract.Event.Retry -> {
                reduce { state.copy(errorMessage = null) }
            }
            FavoritesContract.Event.ClearError -> {
                reduce { state.copy(errorMessage = null) }
            }
        }
    }

    private fun removeFavorite(author: String, title: String) = intent {
        val trace = performanceHelper.startTrace("favorites_remove_operation")

        val updatedPoems = state.poems.filterNot {
            it.author == author && it.title == title
        }
        reduce { state.copy(poems = updatedPoems) }

        viewModelScope.launch {
            try {
                removeFromFavoritesUseCase(author, title)
                trace.stop()
            } catch (e: Exception) {
                logError(e)
                trace.incrementMetric("error_count", 1)
                trace.stop()

                intent {
                    reduce {
                        state.copy(
                            poems = state.poems,
                            errorMessage = "Не удалось удалить: ${e.message}"
                        )
                    }
                }
            }
        }
    }
}