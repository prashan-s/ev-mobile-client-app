package lk.chargehere.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lk.chargehere.app.data.repository.StationRepository
import lk.chargehere.app.domain.model.Station
import lk.chargehere.app.utils.Result
import javax.inject.Inject

data class SearchUiState(
    val isLoading: Boolean = false,
    val searchResults: List<Station> = emptyList(),
    val error: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val stationRepository: StationRepository
) : ViewModel() {

    private val searchQueryFlow = MutableStateFlow("")
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        loadDefaultStations()
                    } else {
                        performHybridSearch(query)
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQueryFlow.value = query
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                error = null
            )
        }
    }

    fun searchStations(query: String) {
        onSearchQueryChange(query)
    }

    fun clearSearch() {
        onSearchQueryChange("")
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private suspend fun loadDefaultStations() {
        val cached = stationRepository.getAvailableStations(DEFAULT_SUGGESTION_LIMIT)
        val limitedCached = cached.take(DEFAULT_SUGGESTION_LIMIT)
        val needsApi = limitedCached.size < DEFAULT_SUGGESTION_LIMIT

        _uiState.update { current ->
            current.copy(
                searchResults = limitedCached,
                isLoading = needsApi && limitedCached.isEmpty(),
                error = null
            )
        }

        if (!needsApi) {
            return
        }

        when (val result = stationRepository.getAllStations(
            page = 1,
            pageSize = DEFAULT_API_PAGE_SIZE,
            search = null
        )) {
            is Result.Success -> {
                val refreshed = stationRepository
                    .getAvailableStations(DEFAULT_SUGGESTION_LIMIT)
                    .take(DEFAULT_SUGGESTION_LIMIT)
                _uiState.update { current ->
                    current.copy(
                        searchResults = refreshed,
                        isLoading = false
                    )
                }
            }

            is Result.Error -> {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        error = if (limitedCached.isEmpty()) result.message else null
                    )
                }
            }

            is Result.Loading -> Unit
        }
    }

    private suspend fun performHybridSearch(query: String) {
        val cachedResults = stationRepository.searchCachedStations(query)
        val limitedCached = cachedResults.take(SEARCH_RESULT_LIMIT)
        val needsApi = limitedCached.size < SEARCH_RESULT_LIMIT

        _uiState.update { current ->
            current.copy(
                searchResults = limitedCached,
                isLoading = needsApi && limitedCached.isEmpty(),
                error = null
            )
        }

        if (!needsApi) {
            return
        }

        when (val result = stationRepository.searchStations(query, SEARCH_API_LIMIT)) {
            is Result.Success -> {
                val refreshed = stationRepository
                    .searchCachedStations(query)
                    .take(SEARCH_RESULT_LIMIT)
                _uiState.update { current ->
                    current.copy(
                        searchResults = refreshed,
                        isLoading = false
                    )
                }
            }

            is Result.Error -> {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        error = if (limitedCached.isEmpty()) result.message else null
                    )
                }
            }

            is Result.Loading -> Unit
        }
    }

    private companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
        private const val DEFAULT_SUGGESTION_LIMIT = 10
        private const val SEARCH_RESULT_LIMIT = 25
        private const val SEARCH_API_LIMIT = 50
        private const val DEFAULT_API_PAGE_SIZE = 40
    }
}