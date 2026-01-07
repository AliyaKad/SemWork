package ru.itis.feature.creator.impl.presentation

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
import ru.itis.core.models.Poem
import ru.itis.feature.creator.api.SavePoemUseCase
import javax.inject.Inject

@HiltViewModel
class CreatorViewModel @Inject constructor(
    private val savePoemUseCase: SavePoemUseCase,
    analyticsHelper: AnalyticsHelper,
    crashlyticsHelper: CrashlyticsHelper,
    performanceHelper: PerformanceHelper
) : BaseViewModel(analyticsHelper, crashlyticsHelper, performanceHelper),
    ContainerHost<CreatorContract.State, CreatorContract.SideEffect> {

    override val container: Container<CreatorContract.State, CreatorContract.SideEffect> =
        container(CreatorContract.State())

    private var screenLoadTrace: Trace? = null

    init {
        logScreenView("CreatorScreen", "CreatorViewModel")
        screenLoadTrace = performanceHelper.startTrace("creator_screen_load")
        screenLoadTrace?.incrementMetric("screen_load_count", 1)
    }

    override fun onCleared() {
        screenLoadTrace?.stop()
        super.onCleared()
    }

    fun onEvent(event: CreatorContract.Event) = intent {
        when (event) {
            is CreatorContract.Event.UpdateTitle -> {
                reduce { state.copy(title = event.title) }
            }
            is CreatorContract.Event.UpdateLine -> {
                val newLines = state.lines.toMutableList()
                if (event.index < newLines.size) {
                    newLines[event.index] = event.text
                    reduce { state.copy(lines = newLines) }
                }
            }
            CreatorContract.Event.AddLine -> {
                logEvent("creator_line_added", mapOf(
                    "current_lines_count" to state.lines.size
                ))
                reduce { state.copy(lines = state.lines + "") }
            }
            is CreatorContract.Event.RemoveLine -> {
                if (state.lines.size > 1) {
                    logEvent("creator_line_removed", mapOf(
                        "removed_line_index" to event.index,
                        "remaining_lines_count" to (state.lines.size - 1)
                    ))
                    val newLines = state.lines.toMutableList()
                    newLines.removeAt(event.index)
                    reduce { state.copy(lines = newLines) }
                }
            }
            CreatorContract.Event.SavePoem -> {
                logEvent("creator_save_attempt", mapOf(
                    "title_length" to state.title.length,
                    "lines_count" to state.lines.size,
                    "non_empty_lines" to state.lines.count { it.isNotBlank() }
                ))
                savePoem()
            }
            CreatorContract.Event.ResetState -> {
                reduce {
                    state.copy(
                        isSuccess = false,
                        errorMessage = null,
                        isLoading = false
                    )
                }
            }
            CreatorContract.Event.ClearError -> {
                reduce { state.copy(errorMessage = null) }
            }
        }
    }

    private fun savePoem() = intent {
        val currentTitle = state.title.trim()
        val currentLines = state.lines.map { it.trim() }.filter { it.isNotBlank() }

        if (currentTitle.isEmpty()) {
            logEvent("creator_validation_failed", mapOf(
                "error_type" to "empty_title"
            ))
            postSideEffect(CreatorContract.SideEffect.ShowError("Введите название стихотворения"))
            return@intent
        }

        if (currentLines.isEmpty()) {
            logEvent("creator_validation_failed", mapOf(
                "error_type" to "no_lines"
            ))
            postSideEffect(CreatorContract.SideEffect.ShowError("Добавьте хотя бы одну строку"))
            return@intent
        }

        val trace = performanceHelper.startTrace("creator_save_operation")

        reduce { state.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val poem = Poem(
                    title = currentTitle,
                    author = "Я",
                    lines = currentLines,
                    linecount = currentLines.size.toString()
                )

                savePoemUseCase(poem)

                logEvent("creator_save_success", mapOf(
                    "poem_title_length" to currentTitle.length,
                    "poem_lines_count" to currentLines.size,
                    "total_characters" to currentLines.sumOf { it.length }
                ))

                trace.stop()

                intent {
                    reduce {
                        state.copy(
                            isLoading = false,
                            isSuccess = true,
                            title = "",
                            lines = listOf("")
                        )
                    }
                    postSideEffect(CreatorContract.SideEffect.ShowSuccess)
                }
            } catch (e: Exception) {
                logError(e)
                logEvent("creator_save_failed", mapOf(
                    "error_type" to (e::class.simpleName ?: "Unknown")
                ))

                trace.incrementMetric("error_count", 1)
                trace.stop()

                intent {
                    reduce {
                        state.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Ошибка сохранения"
                        )
                    }
                    postSideEffect(
                        CreatorContract.SideEffect.ShowError(
                            e.message ?: "Ошибка сохранения"
                        )
                    )
                }
            }
        }
    }
}