package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.repository.BillRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class CompostBinViewModel(
    private val billRepository: BillRepository,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val searchQuery = MutableStateFlow("")

    private val _uiState = MutableStateFlow(CompostBinUiState())
    val uiState: StateFlow<CompostBinUiState> = _uiState.asStateFlow()

    init {
        coroutineScope.launch(ioDispatcher) {
            searchQuery
                .debounce(SEARCH_DEBOUNCE_MS)
                .flatMapLatest { query ->
                    billRepository.searchCompost(query)
                }
                .collect { results ->
                    _uiState.update { current ->
                        current.copy(
                            results = results,
                            isSearching = false
                        )
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                hasActiveQuery = query.isNotBlank(),
                isSearching = query.isNotBlank()
            )
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
    }
}
