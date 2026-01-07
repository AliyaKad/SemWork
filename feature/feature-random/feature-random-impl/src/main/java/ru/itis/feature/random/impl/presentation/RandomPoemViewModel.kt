package ru.itis.feature.random.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import ru.itis.core.analytics.AnalyticsHelper
import ru.itis.core.analytics.BaseViewModel
import ru.itis.core.analytics.CrashlyticsHelper
import ru.itis.core.analytics.PerformanceHelper
import ru.itis.core.analytics.Trace
import ru.itis.feature.favorites.api.IsFavoriteUseCase
import ru.itis.feature.favorites.api.ToggleFavoriteUseCase
import ru.itis.feature.random.api.GetRandomPoemUseCase
import javax.inject.Inject

@HiltViewModel
class RandomPoemViewModel @Inject constructor(
    private val getRandomPoemUseCase: GetRandomPoemUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase,
    analyticsHelper: AnalyticsHelper,
    crashlyticsHelper: CrashlyticsHelper,
    performanceHelper: PerformanceHelper
) : BaseViewModel(analyticsHelper, crashlyticsHelper, performanceHelper),
    ContainerHost<RandomPoemContract.State, Unit> {

    override val container: Container<RandomPoemContract.State, Unit> =
        container(RandomPoemContract.State())

    private var screenLoadTrace: Trace? = null
    private var loadPoemTrace: Trace? = null
    private var toggleFavoriteTrace: Trace? = null

    init {
        logScreenView("RandomPoemScreen", "RandomPoemViewModel")
        screenLoadTrace = performanceHelper.startTrace("random_poem_screen_load")
        screenLoadTrace?.incrementMetric("screen_load_count", 1)
        loadRandomPoem()
    }

    override fun onCleared() {
        screenLoadTrace?.stop()
        loadPoemTrace?.stop()
        toggleFavoriteTrace?.stop()
        super.onCleared()
    }

    fun onEvent(event: RandomPoemContract.Event) = intent {
        when (event) {
            RandomPoemContract.Event.LoadRandomPoem -> {
                logEvent("random_poem_refresh")
                loadRandomPoem()
            }
            RandomPoemContract.Event.Retry -> {
                logEvent("random_poem_retry")
                loadRandomPoem()
            }
            RandomPoemContract.Event.ToggleFavorite -> {
                toggleFavoriteTrace = performanceHelper.startTrace("random_toggle_favorite_operation")
                toggleFavorite()
            }
        }
    }

    private fun loadRandomPoem() = intent {
        loadPoemTrace = performanceHelper.startTrace("random_poem_load_operation")

        reduce {
            state.copy(
                isLoading = true,
                poem = null,
                errorMessage = null,
                isFavorite = false
            )
        }

        val result = getRandomPoemUseCase()

        if (result.isSuccess) {
            val poem = result.getOrNull()
            if (poem != null) {
                logEvent("random_poem_loaded", mapOf(
                    "poem_author" to (poem.author ?: "unknown"),
                    "poem_title_length" to poem.title.length
                ))

                viewModelScope.launch {
                    val isFavorite = isFavoriteUseCase(poem.author, poem.title)
                    intent {
                        reduce { state.copy(isFavorite = isFavorite) }
                    }
                }

                reduce {
                    state.copy(
                        poem = poem,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                loadPoemTrace?.stop()
            } else {
                logEvent("random_poem_empty")
                loadPoemTrace?.incrementMetric("empty_count", 1)
                loadPoemTrace?.stop()

                reduce {
                    state.copy(
                        isLoading = false,
                        errorMessage = "Получено пустое стихотворение"
                    )
                }
            }
        } else {
            val error = result.exceptionOrNull()
            logError(error ?: Exception("Unknown error"))
            logEvent("random_poem_load_failed", mapOf(
                "error_type" to (error?.javaClass?.simpleName ?: "Unknown")
            ))

            loadPoemTrace?.incrementMetric("error_count", 1)
            loadPoemTrace?.stop()

            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = error?.message ?: "Неизвестная ошибка"
                )
            }
        }
    }

    private fun toggleFavorite() = intent {
        val currentPoem = state.poem
        if (currentPoem == null) {
            toggleFavoriteTrace?.stop()
            return@intent
        }

        logEvent("random_favorite_toggled", mapOf(
            "action" to if (state.isFavorite) "remove" else "add",
            "poem_author" to (currentPoem.author ?: "unknown")
        ))

        val optimisticFavorite = !state.isFavorite
        reduce { state.copy(isFavorite = optimisticFavorite) }

        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(currentPoem)

                val actualFavorite = isFavoriteUseCase(currentPoem.author, currentPoem.title)

                logEvent("random_favorite_toggle_success", mapOf(
                    "new_state" to actualFavorite
                ))

                toggleFavoriteTrace?.stop()

                intent {
                    reduce { state.copy(isFavorite = actualFavorite) }
                }
            } catch (e: Exception) {
                logError(e)
                logEvent("random_favorite_toggle_failed", mapOf(
                    "error_type" to (e::class.simpleName ?: "Unknown")
                ))

                toggleFavoriteTrace?.incrementMetric("error_count", 1)
                toggleFavoriteTrace?.stop()

                intent {
                    reduce { state.copy(isFavorite = !optimisticFavorite) }
                }
            }
        }
    }
}