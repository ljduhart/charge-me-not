package com.artie.chargemenot.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.ui.components.FinancialBloomCanvas
import com.artie.chargemenot.ui.components.categoryColor
import com.artie.chargemenot.ui.components.categoryDisplayName
import com.artie.chargemenot.ui.dashboard.DashboardUiState
import com.artie.chargemenot.ui.theme.ChargeMeNotTheme
import com.artie.chargemenot.ui.theme.LeafGreen
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowGreenLight
import com.artie.chargemenot.ui.theme.MeadowSage
import com.artie.chargemenot.ui.theme.MeadowWhite
import com.artie.chargemenot.ui.theme.WeedRed
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.artie.chargemenot.domain.model.UserSettings

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onKeepSubscription: (Bill) -> Unit,
    onPullSubscription: (Bill) -> Unit,
    onMonthlyBudgetChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val dateFormat = DateTimeFormatter.ofPattern("MMM d")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        GreetingHeader(
            greeting = uiState.greeting,
            totalUpcoming = currencyFormat.format(uiState.totalUpcoming),
            monthlyBudget = uiState.monthlyBudget,
            currencyFormat = currencyFormat,
            onMonthlyBudgetChange = onMonthlyBudgetChange
        )

        Spacer(modifier = Modifier.height(28.dp))

        FinancialBloomSection(
            categoryTotals = uiState.categoryTotals
        )

        Spacer(modifier = Modifier.height(32.dp))

        SubscriptionsSection(
            subscriptions = uiState.subscriptionBills,
            currencyFormat = currencyFormat,
            dateFormat = dateFormat,
            onKeep = onKeepSubscription,
            onPull = onPullSubscription
        )
    }
}

@Composable
private fun GreetingHeader(
    greeting: String,
    totalUpcoming: String,
    monthlyBudget: Double,
    currencyFormat: NumberFormat,
    onMonthlyBudgetChange: (String) -> Unit
) {
    var isEditingBudget by remember { mutableStateOf(false) }
    var budgetInput by remember(monthlyBudget) {
        mutableStateOf(monthlyBudget.toInt().toString())
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Your meadow awaits",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "TOTAL UPCOMING",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = totalUpcoming,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MONTHLY BUDGET",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!isEditingBudget) {
                            Text(
                                text = currencyFormat.format(monthlyBudget),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(onClick = {
                        if (isEditingBudget) {
                            onMonthlyBudgetChange(budgetInput)
                            isEditingBudget = false
                        } else {
                            budgetInput = monthlyBudget.toInt().toString()
                            isEditingBudget = true
                        }
                    }) {
                        Icon(
                            imageVector = if (isEditingBudget) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditingBudget) "Save monthly budget" else "Edit monthly budget"
                        )
                    }
                }

                if (isEditingBudget) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Monthly budget") },
                        prefix = { Text("$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Text(
                        text = "Minimum ${currencyFormat.format(UserSettings.MIN_MONTHLY_BUDGET)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FinancialBloomSection(
    categoryTotals: Map<BillCategory, Double>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Financial Bloom",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Each petal reflects where your money grows",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MeadowWhite
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MeadowSage.copy(alpha = 0.15f),
                                MeadowGreenLight.copy(alpha = 0.08f),
                                MeadowWhite
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                FinancialBloomCanvas(
                    categoryTotals = categoryTotals,
                    modifier = Modifier
                        .size(260.dp)
                        .padding(16.dp)
                )
            }
        }

        if (categoryTotals.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            CategoryLegend(categoryTotals = categoryTotals)
        }
    }
}

@Composable
private fun CategoryLegend(
    categoryTotals: Map<BillCategory, Double>
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        categoryTotals.entries.sortedByDescending { it.value }.forEach { (category, amount) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(categoryColor(category))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = categoryDisplayName(category),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = currencyFormat.format(amount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SubscriptionsSection(
    subscriptions: List<Bill>,
    currencyFormat: NumberFormat,
    dateFormat: DateTimeFormatter,
    onKeep: (Bill) -> Unit,
    onPull: (Bill) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Subscriptions: Weeds or Flowers?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Tend your garden — keep what blooms, pull what doesn't",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (subscriptions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "No subscriptions sprouting — your garden is clear!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(20.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                subscriptions.forEach { bill ->
                    SubscriptionBillCard(
                        bill = bill,
                        currencyFormat = currencyFormat,
                        dateFormat = dateFormat,
                        onKeep = { onKeep(bill) },
                        onPull = { onPull(bill) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionBillCard(
    bill: Bill,
    currencyFormat: NumberFormat,
    dateFormat: DateTimeFormatter,
    onKeep: () -> Unit,
    onPull: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MeadowGreenLight.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = bill.name.first().uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MeadowGreenDark,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Due ${bill.dueDate.format(dateFormat)} · ${currencyFormat.format(bill.amount)}/mo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onKeep) {
                Icon(
                    imageVector = Icons.Filled.Eco,
                    contentDescription = "Keep subscription",
                    tint = LeafGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = onPull) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Pull subscription",
                    tint = WeedRed,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun previewDummyBills(): List<Bill> {
    val today = LocalDate.of(2026, 9, 4)
    return listOf(
        Bill(1, "Maple Street Apartment", 1_450.00, today.plusDays(3), BillCategory.RENT),
        Bill(2, "Whole Foods Groceries", 186.42, today.plusDays(5), BillCategory.FOOD),
        Bill(3, "Pacific Gas & Electric", 94.17, today.plusDays(8), BillCategory.UTILITIES),
        Bill(4, "Spotify Premium", 11.99, today.plusDays(12), BillCategory.SUBSCRIPTIONS),
        Bill(5, "Netflix", 15.49, today.plusDays(12), BillCategory.SUBSCRIPTIONS),
        Bill(6, "Adobe Creative Cloud", 54.99, today.plusDays(15), BillCategory.SUBSCRIPTIONS),
        Bill(7, "LA Metro Pass", 100.00, today.plusDays(18), BillCategory.TRANSPORTATION),
        Bill(8, "Kaiser Health", 325.00, today.plusDays(22), BillCategory.HEALTHCARE),
        Bill(9, "Trader Joe's", 72.30, today.plusDays(6), BillCategory.FOOD),
        Bill(10, "Disney+", 13.99, today.plusDays(20), BillCategory.SUBSCRIPTIONS)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DashboardScreenPreview() {
    val bills = previewDummyBills()
    val categoryTotals = bills
        .groupBy { it.category }
        .mapValues { (_, items) -> items.sumOf { it.amount } }

    ChargeMeNotTheme {
        DashboardScreen(
            uiState = DashboardUiState(
                greeting = "Good morning",
                totalUpcoming = bills.sumOf { it.amount },
                upcomingBills = bills,
                subscriptionBills = bills.filter { it.category == BillCategory.SUBSCRIPTIONS },
                categoryTotals = categoryTotals,
                isLoading = false
            ),
            onKeepSubscription = {},
            onPullSubscription = {},
            onMonthlyBudgetChange = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty Bloom")
@Composable
private fun DashboardScreenEmptyPreview() {
    ChargeMeNotTheme {
        DashboardScreen(
            uiState = DashboardUiState(
                greeting = "Good evening",
                totalUpcoming = 0.0,
                isLoading = false
            ),
            onKeepSubscription = {},
            onPullSubscription = {},
            onMonthlyBudgetChange = {}
        )
    }
}
