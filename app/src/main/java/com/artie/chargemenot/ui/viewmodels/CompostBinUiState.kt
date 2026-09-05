package com.artie.chargemenot.ui.viewmodels

import com.artie.chargemenot.data.local.BillWithCompost

data class CompostBinUiState(
    val searchQuery: String = "",
    val results: List<BillWithCompost> = emptyList(),
    val isSearching: Boolean = false,
    val hasActiveQuery: Boolean = false
)
