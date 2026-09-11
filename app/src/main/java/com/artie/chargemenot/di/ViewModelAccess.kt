package com.artie.chargemenot.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.artie.chargemenot.ui.navigation.AppRoutes

@Composable
inline fun <reified VM : ViewModel> NavBackStackEntry.meadowHubViewModel(
    navController: NavHostController
): VM {
    val parentEntry = remember(this) {
        navController.getBackStackEntry(AppRoutes.MEADOW_HUB)
    }
    return viewModel(
        viewModelStoreOwner = parentEntry,
        factory = AppViewModelProvider.Factory
    )
}

@Composable
inline fun <reified VM : ViewModel> destinationViewModel(): VM {
    return viewModel(factory = AppViewModelProvider.Factory)
}

inline fun <reified VM : ViewModel> NavController.destinationViewModelOrNull(
    route: String
): VM? {
    return runCatching {
        val entry = getBackStackEntry(route)
        ViewModelProvider(
            entry,
            AppViewModelProvider.Factory,
            entry.defaultViewModelCreationExtras
        )[VM::class.java]
    }.getOrNull()
}
