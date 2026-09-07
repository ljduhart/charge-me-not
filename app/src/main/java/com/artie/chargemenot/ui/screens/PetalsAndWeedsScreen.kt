package com.artie.chargemenot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    modifier: Modifier = Modifier
) {
    val currencyFormat = rememberCurrencyFormat()
    val dateFormat = DateTimeFormatter.ofPattern("MMM d, yyyy")

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
                                    bill.category.name,
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
        }
    }
}

@Composable
private fun rememberCurrencyFormat(): NumberFormat {
    return androidx.compose.runtime.remember {
        NumberFormat.getCurrencyInstance(Locale.US)
    }
}
