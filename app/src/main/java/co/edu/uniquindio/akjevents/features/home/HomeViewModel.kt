package co.edu.uniquindio.akjevents.features.home

import androidx.lifecycle.ViewModel
import co.edu.uniquindio.akjevents.data.demo.SampleEvents
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HomeUiState(
    val category: EventCategory? = null,
    val events: List<CommunityEvent> = emptyList()
)

class HomeViewModel : ViewModel() {
    private val publicEvents = SampleEvents.all.filter { it.status == EventStatus.VERIFIED }
    private val _uiState = MutableStateFlow(HomeUiState(events = publicEvents))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun selectCategory(category: EventCategory?) {
        _uiState.update { current ->
            current.copy(
                category = category,
                events = publicEvents.filter { category == null || it.category == category }
            )
        }
    }
}
