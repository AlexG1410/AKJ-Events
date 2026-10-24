package co.edu.uniquindio.akjevents.features.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.data.repository.LocalRecommendationRepository
import co.edu.uniquindio.akjevents.domain.model.RecommendationFeed
import co.edu.uniquindio.akjevents.domain.repository.RecommendationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

data class RecommendationsUiState(
    val feed: RecommendationFeed = RecommendationFeed(),
    val isLoading: Boolean = true
)

/** La navegación comparte esta instancia entre Inicio y la lista completa. */
class RecommendationsViewModel(
    private val repository: RecommendationRepository = LocalRecommendationRepository(),
    private val now: () -> LocalDateTime = LocalDateTime::now
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecommendationsUiState())
    val uiState = _uiState.asStateFlow()
    private val refreshVersion = MutableStateFlow(0)
    private var expiryJob: Job? = null

    init {
        viewModelScope.launch {
            combine(repository.changes, refreshVersion) { _, version -> version }.collectLatest {
                expiryJob?.cancel()
                // No se conservan recomendaciones de una sesión o contexto anterior mientras se clasifica.
                _uiState.value = RecommendationsUiState()
                val feed = repository.revalidate(repository.recommend())
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
    fun visibleFeed(feed: RecommendationFeed): RecommendationFeed = repository.revalidate(feed)
}
