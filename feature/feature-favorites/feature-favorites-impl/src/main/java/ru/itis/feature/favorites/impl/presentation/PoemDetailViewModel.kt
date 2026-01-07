package ru.itis.feature.favorites.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import ru.itis.feature.favorites.api.IsFavoriteUseCase
import ru.itis.feature.favorites.api.ToggleFavoriteUseCase
import javax.inject.Inject

@HiltViewModel
class PoemDetailViewModel @Inject constructor(
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase,
    analyticsHelper: AnalyticsHelper,
    crashlyticsHelper: CrashlyticsHelper,
    performanceHelper: PerformanceHelper
) : BaseViewModel(analyticsHelper, crashlyticsHelper, performanceHelper),
    ContainerHost<PoemDetailContract.State, PoemDetailContract.SideEffect> {

    override val container: Container<PoemDetailContract.State, PoemDetailContract.SideEffect> =
        container(PoemDetailContract.State())

    private var screenLoadTrace: Trace? = null
    private var toggleFavoriteTrace: Trace? = null

    init {
        logScreenView("PublicPoemDetailScreen", "PoemDetailViewModel")
        screenLoadTrace = performanceHelper.startTrace("public_poem_detail_screen_load")
        screenLoadTrace?.incrementMetric("screen_load_count", 1)
    }

    override fun onCleared() {
        screenLoadTrace?.stop()
        toggleFavoriteTrace?.stop()
        super.onCleared()
    }

    fun onEvent(event: PoemDetailContract.Event) = intent {
        when (event) {
            is PoemDetailContract.Event.SetPoem -> {
                logEvent("public_poem_detail_viewed", mapOf(
                    "poem_author" to (event.poem.author ?: "unknown"),
                    "poem_title_length" to event.poem.title.length
                ))
                setPoem(event.poem)
            }
            PoemDetailContract.Event.ToggleFavorite -> {
                toggleFavoriteTrace = performanceHelper.startTrace("toggle_favorite_operation")
                toggleFavorite()
            }
            PoemDetailContract.Event.Retry -> {
                reduce { state.copy(errorMessage = null) }
                state.poem?.let { setPoem(it) }
            }
            PoemDetailContract.Event.ClearError -> {
                reduce { state.copy(errorMessage = null) }
            }
        }
    }

    private fun setPoem(poem: ru.itis.core.models.Poem) = intent {
        reduce {
            state.copy(
                poem = poem,
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val isFavorite = isFavoriteUseCase(poem.author, poem.title)

            intent {
                reduce {
                    state.copy(
                        isFavorite = isFavorite,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun toggleFavorite() = intent {
        val currentPoem = state.poem ?: return@intent
        val currentIsFavorite = state.isFavorite

        logEvent("favorite_toggled", mapOf(
            "action" to if (currentIsFavorite) "remove" else "add",
            "poem_author" to (currentPoem.author ?: "unknown")
        ))

        reduce { state.copy(isFavorite = !currentIsFavorite) }

        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(currentPoem)

                val actualIsFavorite = isFavoriteUseCase(currentPoem.author, currentPoem.title)

                logEvent("favorite_toggle_success", mapOf(
                    "new_state" to actualIsFavorite
                ))

                toggleFavoriteTrace?.stop()

                intent {
                    reduce { state.copy(isFavorite = actualIsFavorite) }

                    val message = if (actualIsFavorite)
                        "Добавлено в избранное"
                    else
                        "Удалено из избранного"

                    postSideEffect(PoemDetailContract.SideEffect.ShowMessage(message))
                }
            } catch (e: Exception) {
                logError(e)
                logEvent("favorite_toggle_failed", mapOf(
                    "error_type" to (e::class.simpleName ?: "Unknown")
                ))

                toggleFavoriteTrace?.incrementMetric("error_count", 1)
                toggleFavoriteTrace?.stop()
            }
        }
    }
}