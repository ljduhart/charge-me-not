package com.artie.chargemenot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.R
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.ui.components.MeadowHubScaffold
import com.artie.chargemenot.ui.theme.MeadowCream
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowSky
import com.artie.chargemenot.ui.theme.MeadowWhite
import com.artie.chargemenot.ui.theme.WeedRed
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PetalsAndWeedsScreen(
    upcomingBills: List<Bill>,
    onOpenDrawer: () -> Unit,
    onNavigateBack: () -> Unit,
    onSelectBillForEdit: (Bill) -> Unit,
    onShowManualBillEntry: () -> Unit,
    onDeleteBill: (Bill) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = rememberCurrencyFormat()
    val dateFormat = DateTimeFormatter.ofPattern("MMM d, yyyy")
    var billPendingDelete by remember { mutableStateOf<Bill?>(null) }

    billPendingDelete?.let { bill ->
        AlertDialog(
            onDismissRequest = { billPendingDelete = null },
            title = {
                Text(text = stringResource(R.string.petals_and_weeds_delete_title))
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.petals_and_weeds_delete_message,
                        bill.name
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteBill(bill)
                        billPendingDelete = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.petals_and_weeds_delete_confirm),
                        color = WeedRed
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { billPendingDelete = null }) {
                    Text(text = stringResource(R.string.petals_and_weeds_delete_cancel))
                }
            }
        )
    }

    MeadowHubScaffold(
        title = stringResource(R.string.meadow_route_petals_and_weeds),
        onOpenDrawer = onOpenDrawer,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onShowManualBillEntry,
                containerColor = MeadowSky,
                contentColor = MeadowGreenDark,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.dashboard_manual_bill_entry)
                    )
                },
                text = {
                    Text(stringResource(R.string.dashboard_manual_bill_entry))
                }
            )
        }
    ) { innerPadding ->
        if (upcomingBills.isEmpty()) {
            Text(
                text = stringResource(R.string.petals_and_weeds_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MeadowCream),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = upcomingBills,
                    key = { bill -> "petal_bill_${bill.id}" }
                ) { bill ->
                    SwipeablePetalBillCard(
                        bill = bill,
                        currencyFormat = currencyFormat,
                        dateFormat = dateFormat,
                        onSelectBillForEdit = onSelectBillForEdit,
                        onRequestDelete = { billPendingDelete = it }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeablePetalBillCard(
    bill: Bill,
    currencyFormat: NumberFormat,
    dateFormat: DateTimeFormatter,
    onSelectBillForEdit: (Bill) -> Unit,
    onRequestDelete: (Bill) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { direction ->
            if (direction == SwipeToDismissBoxValue.EndToStart) {
                onRequestDelete(bill)
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = WeedRed.copy(alpha = 0.88f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.petals_and_weeds_swipe_delete),
                    tint = MeadowWhite
                )
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectBillForEdit(bill) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MeadowWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = bill.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(
                        R.string.petals_and_weeds_bill_meta,
                        "${bill.parentCategory} · ${bill.subCategory}",
                        dateFormat.format(bill.dueDate)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = currencyFormat.format(bill.amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = MeadowGreenDark,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun rememberCurrencyFormat(): NumberFormat {
    return androidx.compose.runtime.remember {
        NumberFormat.getCurrencyInstance(Locale.US)
    }
}
