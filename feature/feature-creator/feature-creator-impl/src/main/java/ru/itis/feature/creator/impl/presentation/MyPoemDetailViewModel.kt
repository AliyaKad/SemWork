package ru.itis.feature.creator.impl.presentation

import dagger.hilt.android.lifecycle.HiltViewModel
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
import ru.itis.core.models.Poem
import javax.inject.Inject

@HiltViewModel
class MyPoemDetailViewModel @Inject constructor(
    analyticsHelper: AnalyticsHelper,
    crashlyticsHelper: CrashlyticsHelper,
    performanceHelper: PerformanceHelper
) : BaseViewModel(analyticsHelper, crashlyticsHelper, performanceHelper),
    ContainerHost<MyPoemDetailContract.State, MyPoemDetailContract.SideEffect> {

    override val container: Container<MyPoemDetailContract.State, MyPoemDetailContract.SideEffect> =
        container(MyPoemDetailContract.State())

    private var screenLoadTrace: Trace? = null

    init {
        logScreenView("PoemDetailScreen", "MyPoemDetailViewModel")
        screenLoadTrace = performanceHelper.startTrace("poem_detail_screen_load")
        screenLoadTrace?.incrementMetric("screen_load_count", 1)
    }

    override fun onCleared() {
        screenLoadTrace?.stop()
        super.onCleared()
    }

    fun onEvent(event: MyPoemDetailContract.Event) = intent {
        when (event) {
            is MyPoemDetailContract.Event.SetPoem -> {
                logEvent("poem_detail_viewed", mapOf(
                    "poem_title_length" to event.poem.title.length,
                    "poem_lines_count" to event.poem.lines.size,
                    "poem_author" to (event.poem.author ?: "unknown")
                ))
                setPoem(event.poem)
            }
            MyPoemDetailContract.Event.Retry -> {
                logEvent("poem_detail_retry")
                reduce { state.copy(errorMessage = null) }
                state.poem?.let { setPoem(it) }
            }
            MyPoemDetailContract.Event.ClearError -> {
                reduce { state.copy(errorMessage = null) }
            }
        }
    }

    private fun setPoem(poem: Poem) = intent {
        reduce {
            state.copy(
                poem = poem,
                isLoading = false,
                errorMessage = null
            )
        }
    }
}