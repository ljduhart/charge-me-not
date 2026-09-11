package com.artie.chargemenot.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Scale
import com.artie.chargemenot.R
import com.artie.chargemenot.data.local.BillEntity
import com.artie.chargemenot.data.local.BillWithCompost
import com.artie.chargemenot.domain.model.MeadowCategories
import com.artie.chargemenot.ui.theme.ChargeMeNotTheme
import com.artie.chargemenot.ui.theme.MeadowCream
import com.artie.chargemenot.ui.theme.MeadowEarth
import com.artie.chargemenot.ui.theme.MeadowEarthLight
import com.artie.chargemenot.ui.theme.MeadowGreen
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowSage
import com.artie.chargemenot.ui.theme.MeadowWhite
import com.artie.chargemenot.ui.viewmodels.CompostBinUiState
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompostBinScreen(
    uiState: CompostBinUiState,
    onSearchQueryChanged: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = DateTimeFormatter.ofPattern("MMM d, yyyy")

    BackHandler(onBack = onNavigateBack)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MeadowCream,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.compost_bin_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.dashboard_menu)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MeadowEarth,
                    titleContentColor = MeadowWhite,
                    navigationIconContentColor = MeadowWhite
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MeadowCream)
        ) {
            CompostSearchBar(
                query = uiState.searchQuery,
                onQueryChanged = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )

            when {
                !uiState.hasActiveQuery -> {
                    CompostEmptyPrompt(
                        message = stringResource(R.string.compost_bin_empty_prompt),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                uiState.isSearching && uiState.results.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MeadowGreen)
                    }
                }

                uiState.results.isEmpty() -> {
                    CompostEmptyPrompt(
                        message = stringResource(R.string.compost_bin_no_results),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.results,
                            key = { result -> "compost_${result.bill.id}" }
                        ) { result ->
                            CompostResultRow(
                                result = result,
                                dateFormat = dateFormat
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompostSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier,
        placeholder = {
            Text(
                text = stringResource(R.string.compost_bin_search_hint),
                color = MeadowEarth.copy(alpha = 0.7f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MeadowGreenDark
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MeadowWhite,
            unfocusedContainerColor = MeadowWhite,
            focusedIndicatorColor = MeadowGreen,
            unfocusedIndicatorColor = MeadowSage,
            cursorColor = MeadowGreen
        )
    )
}

@Composable
private fun CompostEmptyPrompt(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MeadowEarth,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun CompostResultRow(
    result: BillWithCompost,
    dateFormat: DateTimeFormatter,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MeadowWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            val receiptPath = result.bill.receiptImagePath
            if (!receiptPath.isNullOrBlank() && File(receiptPath).exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(receiptPath))
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .size(width = 96, height = 96)
                        .scale(Scale.FILL)
                        .build(),
                    contentDescription = stringResource(R.string.compost_bin_receipt_image),
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.bill.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MeadowGreenDark,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = result.bill.dueDate.format(dateFormat),
                    style = MaterialTheme.typography.bodySmall,
                    color = MeadowEarth,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = buildMatchedSnippet(result.matchedText),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

private fun buildMatchedSnippet(rawText: String): String {
    val normalized = rawText.replace('\n', ' ').trim()
    if (normalized.length <= 140) {
        return normalized
    }
    return normalized.take(137) + "..."
}

@Preview(showBackground = true)
@Composable
private fun CompostBinScreenPreview() {
    val sampleBill = BillEntity(
        id = 1L,
        name = "Pacific Gas & Electric",
        amount = 9_417L,
        dueDate = LocalDate.of(2026, 9, 12),
        parentCategory = MeadowCategories.ROOT_SYSTEM,
        subCategory = "Utilities",
        receiptImagePath = null
    )

    ChargeMeNotTheme {
        CompostBinScreen(
            uiState = CompostBinUiState(
                searchQuery = "electric",
                hasActiveQuery = true,
                results = listOf(
                    BillWithCompost(
                        bill = sampleBill,
                        matchedText = "PACIFIC GAS & ELECTRIC Statement Total Due $94.17"
                    )
                )
            ),
            onSearchQueryChanged = {},
            onNavigateBack = {},
            onOpenDrawer = {}
        )
    }
}
