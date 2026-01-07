package ru.itis.feature.creator.impl.presentation

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
import ru.itis.core.models.Poem
import ru.itis.feature.creator.api.DeletePoemUseCase
import ru.itis.feature.creator.api.ObserveUserPoemsUseCase
import javax.inject.Inject

@HiltViewModel
class MyPoemsViewModel @Inject constructor(
    private val observeUserPoemsUseCase: ObserveUserPoemsUseCase,
    private val deletePoemUseCase: DeletePoemUseCase,
    analyticsHelper: AnalyticsHelper,
    crashlyticsHelper: CrashlyticsHelper,
    performanceHelper: PerformanceHelper
) : BaseViewModel(analyticsHelper, crashlyticsHelper, performanceHelper),
    ContainerHost<MyPoemsContract.State, MyPoemsContract.SideEffect> {

    override val container: Container<MyPoemsContract.State, MyPoemsContract.SideEffect> =
        container(MyPoemsContract.State(isLoading = true))

    private var screenLoadTrace: Trace? = null

    init {
        logScreenView("MyPoemsScreen", "MyPoemsViewModel")
        screenLoadTrace = performanceHelper.startTrace("my_poems_screen_load")
        screenLoadTrace?.incrementMetric("screen_load_count", 1)

        viewModelScope.launch {
            observeUserPoemsUseCase()
                .distinctUntilChanged()
                .collect { poems ->
                    logEvent("my_poems_loaded", mapOf(
                        "poems_count" to poems.size,
                        "total_lines" to poems.sumOf { it.lines.size }
                    ))

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

    fun onEvent(event: MyPoemsContract.Event) = intent {
        when (event) {
            MyPoemsContract.Event.LoadPoems -> {
                logEvent("my_poems_refresh")
            }
            is MyPoemsContract.Event.DeletePoem -> {
                logEvent("my_poems_delete_initiated", mapOf(
                    "poem_title" to event.poem.title,
                    "poem_lines_count" to event.poem.lines.size
                ))
                postSideEffect(
                    MyPoemsContract.SideEffect.ShowDeleteConfirmation(event.poem)
                )
            }
            is MyPoemsContract.Event.PoemClicked -> {
                logEvent("my_poems_poem_selected", mapOf(
                    "poem_title_length" to event.poem.title.length,
                    "poem_author" to (event.poem.author ?: "unknown")
                ))
                postSideEffect(MyPoemsContract.SideEffect.NavigateToPoem(event.poem))
            }
            MyPoemsContract.Event.Retry -> {
                logEvent("my_poems_retry")
                reduce { state.copy(errorMessage = null, isLoading = true) }
            }
            MyPoemsContract.Event.ClearError -> {
                reduce { state.copy(errorMessage = null) }
            }
            MyPoemsContract.Event.AddNewPoem -> {
                logEvent("my_poems_add_new_clicked")
                postSideEffect(MyPoemsContract.SideEffect.NavigateToCreatePoem)
            }
        }
    }

    fun confirmDelete(poem: Poem) = intent {
        logEvent("my_poems_delete_confirmed", mapOf(
            "poem_title" to poem.title,
            "total_poems_before" to state.poems.size
        ))

        val poemId = generatePoemId(poem)
        reduce { state.copy(deletingPoemId = poemId) }

        val trace = performanceHelper.startTrace("poem_delete_operation")

        viewModelScope.launch {
            try {
                deletePoemUseCase(poemId)

                logEvent("my_poems_delete_success", mapOf(
                    "poem_title" to poem.title,
                    "total_poems_after" to (state.poems.size - 1)
                ))

                trace.stop()

                intent {
                    reduce { state.copy(deletingPoemId = null) }
                    postSideEffect(MyPoemsContract.SideEffect.ShowMessage("Стихотворение удалено"))
                }
            } catch (e: Exception) {
                logError(e)
                logEvent("my_poems_delete_failed", mapOf(
                    "error_type" to (e::class.simpleName ?: "Unknown"),
                    "poem_title" to poem.title
                ))

                trace.incrementMetric("error_count", 1)
                trace.stop()

                intent {
                    reduce {
                        state.copy(
                            deletingPoemId = null,
                            errorMessage = "Не удалось удалить: ${e.message}"
                        )
                    }
                    postSideEffect(
                        MyPoemsContract.SideEffect.ShowMessage(
                            "Ошибка удаления: ${e.message}"
                        )
                    )
                }
            }
        }
    }

    private fun generatePoemId(poem: Poem): String {
        return "${poem.author}_${poem.title}".hashCode().toString()
    }
}