package co.edu.uniquindio.akjevents.features.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.domain.model.RecommendationFeed
import co.edu.uniquindio.akjevents.domain.usecase.recommendation.GetRecommendationsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.recommendation.ObserveRecommendationChangesUseCase
import co.edu.uniquindio.akjevents.domain.usecase.recommendation.RevalidateRecommendationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecommendationsUiState(
    val feed: RecommendationFeed = RecommendationFeed(),
    val isLoading: Boolean = true
)

/** La navegación comparte esta instancia entre Inicio y la lista completa. */
@HiltViewModel
class RecommendationsViewModel(
    observeRecommendationChanges: ObserveRecommendationChangesUseCase,
    private val getRecommendations: GetRecommendationsUseCase,
    private val revalidateRecommendations: RevalidateRecommendationsUseCase,
    private val now: () -> LocalDateTime
) : ViewModel() {

    @Inject
    constructor(
        observeRecommendationChanges: ObserveRecommendationChangesUseCase,
        getRecommendations: GetRecommendationsUseCase,
        revalidateRecommendations: RevalidateRecommendationsUseCase
    ) : this(observeRecommendationChanges, getRecommendations, revalidateRecommendations, LocalDateTime::now)

    private val _uiState = MutableStateFlow(RecommendationsUiState())
    val uiState = _uiState.asStateFlow()
    private val refreshVersion = MutableStateFlow(0)
    private var expiryJob: Job? = null

    init {
        viewModelScope.launch {
            combine(observeRecommendationChanges(), refreshVersion) { _, version -> version }.collectLatest {
                expiryJob?.cancel()
                // No se conservan recomendaciones de una sesión o contexto anterior mientras se clasifica.
                _uiState.value = RecommendationsUiState()
                val feed = getRecommendations()
                _uiState.value = RecommendationsUiState(feed, isLoading = false)
                // También retira eventos que comienzan aunque no haya cambios en los repositorios.
                feed.items.minOfOrNull { it.event.startsAt }?.let { nextStart ->
                    expiryJob = viewModelScope.launch {
                        delay(Duration.between(now(), nextStart).toMillis().coerceAtLeast(1))
                        refresh()
                    }
                }
            }
        }
    }

    fun refresh() = refreshVersion.update { it + 1 }

    /** Última comprobación al componer o al pulsar una tarjeta. */
    fun visibleFeed(feed: RecommendationFeed): RecommendationFeed = revalidateRecommendations(feed)
}
