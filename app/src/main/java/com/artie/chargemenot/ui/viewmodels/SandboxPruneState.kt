package com.artie.chargemenot.ui.viewmodels

data class SandboxPruneState(
    val manualPrune: Boolean = false,
    val cascadePrune: Boolean = false
) {
    val isPruned: Boolean
        get() = manualPrune || cascadePrune
}
