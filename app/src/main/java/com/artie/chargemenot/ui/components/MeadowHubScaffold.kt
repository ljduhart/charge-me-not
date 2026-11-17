package com.artie.chargemenot.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.artie.chargemenot.R
import com.artie.chargemenot.ui.theme.MeadowCream
import com.artie.chargemenot.ui.theme.MeadowGreenDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeadowHubScaffold(
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    showBackNavigation: Boolean = false,
    handleSystemBack: Boolean = true,
    containerColor: Color = MeadowCream,
    topBarContainerColor: Color = MeadowCream,
    titleContentColor: Color = MeadowGreenDark,
    navigationIconContentColor: Color = MeadowGreenDark,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    if (handleSystemBack) {
        BackHandler(onBack = onNavigateBack)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = containerColor,
        floatingActionButton = floatingActionButton,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (showBackNavigation) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.meadow_navigate_back)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarContainerColor,
                    titleContentColor = titleContentColor,
                    navigationIconContentColor = navigationIconContentColor
                )
            )
        },
        content = content
    )
}
