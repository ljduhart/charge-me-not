package com.artie.chargemenot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.R
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.ui.components.CategoryDetailBottomSheet
import com.artie.chargemenot.ui.components.PhotorealisticBloomCanvas
import com.artie.chargemenot.ui.components.CrossPollinateShareDialog
import com.artie.chargemenot.ui.components.EditBillBottomSheet
import com.artie.chargemenot.ui.components.LinkRootBottomSheet
import com.artie.chargemenot.ui.components.MeadowTickerAmount
import com.artie.chargemenot.ui.components.NagModeCard
import com.artie.chargemenot.ui.components.SubscriptionBrandIcon
import com.artie.chargemenot.ui.components.WeatherForecastCard
import com.artie.chargemenot.ui.dashboard.DashboardBottomNavItem
import com.artie.chargemenot.domain.model.UserSettings
import com.artie.chargemenot.ui.dashboard.DashboardUiState
import com.artie.chargemenot.ui.dashboard.toHighlightCategory
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.artie.chargemenot.ui.theme.MeadowEarth
import com.artie.chargemenot.ui.theme.ChargeMeNotTheme
import com.artie.chargemenot.ui.theme.LeafGreen
import com.artie.chargemenot.ui.theme.MeadowCream
import com.artie.chargemenot.ui.theme.MeadowGreen
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowSage
import com.artie.chargemenot.ui.theme.MeadowSky
import com.artie.chargemenot.ui.theme.MeadowWhite
import com.artie.chargemenot.ui.theme.WeedRed
import com.artie.chargemenot.ui.viewmodels.SettingsUiState
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    settingsUiState: SettingsUiState,
    selectedBillForEdit: Bill?,
    selectedCategoryForEdit: String?,
    categoryBills: List<Bill>,
    onKeepSubscription: (Bill) -> Unit,
    onPullSubscription: (Bill) -> Unit,
    onMonthlyBudgetChange: (String) -> Unit,
    onNagModeToggleRequested: (Boolean) -> Unit,
    onNotificationPermissionResult: (Boolean) -> Unit,
    onNotificationPermissionRequestHandled: () -> Unit,
    onRefreshNotificationPermissionState: () -> Unit,
    onNavigateToPruningSimulator: () -> Unit,
    onNavigateToWeedWhacker: () -> Unit,
    onNavigateToCompostBin: () -> Unit,
    onLinkBillToParent: (Long, Long?) -> Unit,
    onSelectBillForEdit: (Bill) -> Unit,
    onClearEditSelection: () -> Unit,
    onSaveBillEdits: (Bill) -> Unit,
    onPetalTapped: (String) -> Unit,
    onClearCategorySelection: () -> Unit,
    onAddBillToCategory: (String) -> Unit,
    onSelectBottomNavItem: (DashboardBottomNavItem) -> Unit,
    onBloomSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var billToShare by remember { mutableStateOf<Bill?>(null) }
    var billToLink by remember { mutableStateOf<Bill?>(null) }

    billToShare?.let { bill ->
        CrossPollinateShareDialog(
            bill = bill,
            onDismiss = { billToShare = null }
        )
    }

    billToLink?.let { bill ->
        LinkRootBottomSheet(
            bill = bill,
            availableParents = eligibleParentBills(
                childBill = bill,
                allBills = uiState.allBills
            ),
            onLinkToParent = { parentId ->
                onLinkBillToParent(bill.id, parentId)
            },
            onDismiss = { billToLink = null }
        )
    }

    EditBillBottomSheet(
        selectedBill = selectedBillForEdit,
        onDismiss = onClearEditSelection,
        onSave = onSaveBillEdits
    )

    CategoryDetailBottomSheet(
        selectedCategory = selectedCategoryForEdit,
        bills = categoryBills,
        onDismiss = onClearCategorySelection,
        onAddNewBill = onAddBillToCategory,
        onBillClick = onSelectBillForEdit
    )

    val filteredSubscriptions = remember(uiState.subscriptionBills, searchQuery) {
        if (searchQuery.isBlank()) {
            uiState.subscriptionBills
        } else {
            uiState.subscriptionBills.filter { bill ->
                bill.name.contains(searchQuery.trim(), ignoreCase = true)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(R.string.dashboard_drawer_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MeadowGreenDark,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                )
                DrawerNavRow(
                    label = stringResource(R.string.pruning_simulator_entry),
                    icon = Icons.Default.ContentCut,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToPruningSimulator()
                    }
                )
                DrawerNavRow(
                    label = stringResource(R.string.weed_whacker_entry),
                    icon = Icons.Default.Grass,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToWeedWhacker()
                    }
                )
                DrawerNavRow(
                    label = stringResource(R.string.compost_bin_entry),
                    icon = Icons.Default.Recycling,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToCompostBin()
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                NagModeCard(
                    uiState = settingsUiState,
                    onNagModeToggleRequested = onNagModeToggleRequested,
                    onNotificationPermissionResult = onNotificationPermissionResult,
                    onNotificationPermissionRequestHandled = onNotificationPermissionRequestHandled,
                    onRefreshPermissionState = onRefreshNotificationPermissionState,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MeadowCream,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.dashboard_title),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.dashboard_menu)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                isSearchVisible = !isSearchVisible
                                if (!isSearchVisible) {
                                    searchQuery = ""
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.dashboard_search)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MeadowCream,
                        titleContentColor = MeadowGreenDark,
                        navigationIconContentColor = MeadowGreenDark,
                        actionIconContentColor = MeadowGreenDark
                    )
                )
            },
            bottomBar = {
                DashboardBottomNavigationBar(
                    selectedItem = uiState.selectedBottomNavItem,
                    onSelectItem = onSelectBottomNavItem
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MeadowCream),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                item(key = "greeting_row") {
                    DashboardGreetingRow(
                        userName = uiState.userDisplayName,
                        formattedDate = uiState.formattedDate
                    )
                }

                if (isSearchVisible) {
                    item(key = "subscription_search") {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            placeholder = { Text(stringResource(R.string.dashboard_search)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null
                                )
                            },
                            singleLine = true
                        )
                    }
                }

                item(key = "total_upcoming_card") {
                    TotalUpcomingSummaryCard(
                        totalUpcoming = uiState.totalUpcoming,
                        billCount = uiState.upcomingBillCount,
                        currencyFormat = currencyFormat,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                item(key = "financial_bloom_card") {
                    FinancialBloomCard(
                        categoryTotals = uiState.categoryTotals,
                        pendingSubscriptionCount = uiState.subscriptionBills.size,
                        monthlyBudget = uiState.monthlyBudget,
                        selectedBottomNavItem = uiState.selectedBottomNavItem,
                        onBloomSettingsClick = onBloomSettingsClick,
                        onMonthlyBudgetChange = onMonthlyBudgetChange,
                        onPetalTapped = onPetalTapped,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                uiState.forecastResult?.let { forecast ->
                    item(key = "weather_forecast") {
                        WeatherForecastCard(
                            forecastResult = forecast,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }

                item(key = "subscriptions_header") {
                    SubscriptionsWeedsHeader(modifier = Modifier.padding(top = 20.dp))
                }

                if (filteredSubscriptions.isEmpty()) {
                    item(key = "subscriptions_empty") {
                        Text(
                            text = stringResource(R.string.dashboard_subscriptions_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                        )
                    }
                } else {
                    items(
                        items = filteredSubscriptions,
                        key = { bill -> "subscription_${bill.id}" }
                    ) { bill ->
                        SubscriptionWeedFlowerRow(
                            bill = bill,
                            onRowClick = { onSelectBillForEdit(bill) },
                            onKeep = { onKeepSubscription(bill) },
                            onPull = { onPullSubscription(bill) },
                            onShare = { billToShare = bill },
                            onLinkRoots = { billToLink = bill },
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        }
    }
}

@Composable
private fun DashboardGreetingRow(
    userName: String,
    formattedDate: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(MeadowSage, MeadowSky)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = MeadowWhite,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = stringResource(R.string.dashboard_welcome_back, userName),
                style = MaterialTheme.typography.titleMedium,
                color = MeadowGreenDark,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TotalUpcomingSummaryCard(
    totalUpcoming: Double,
    billCount: Int,
    currencyFormat: NumberFormat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MeadowWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.dashboard_total_upcoming_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            MeadowTickerAmount(
                amount = totalUpcoming,
                formatter = currencyFormat,
                style = MaterialTheme.typography.displaySmall,
                color = Color(0xFF1F4E79),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.dashboard_bill_count, billCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun FinancialBloomCard(
    categoryTotals: Map<BillCategory, Double>,
    pendingSubscriptionCount: Int,
    monthlyBudget: Double,
    selectedBottomNavItem: DashboardBottomNavItem,
    onBloomSettingsClick: () -> Unit,
    onMonthlyBudgetChange: (String) -> Unit,
    onPetalTapped: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    var isEditingBudget by remember { mutableStateOf(false) }
    var budgetInput by remember(monthlyBudget) {
        mutableStateOf(monthlyBudget.toInt().toString())
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MeadowWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_financial_bloom_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MeadowGreenDark,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onBloomSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.dashboard_bloom_settings),
                        tint = MeadowGreenDark
                    )
                }
            }

            PhotorealisticBloomCanvas(
                categoryTotals = categoryTotals,
                monthlyBudget = monthlyBudget,
                highlightedCategory = selectedBottomNavItem.toHighlightCategory(),
                onPetalTapped = onPetalTapped,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.dashboard_monthly_budget_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isEditingBudget) {
                        Text(
                            text = currencyFormat.format(monthlyBudget),
                            style = MaterialTheme.typography.titleMedium,
                            color = MeadowGreenDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                IconButton(
                    onClick = {
                        if (isEditingBudget) {
                            onMonthlyBudgetChange(budgetInput)
                            isEditingBudget = false
                        } else {
                            budgetInput = monthlyBudget.toInt().toString()
                            isEditingBudget = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isEditingBudget) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = stringResource(
                            if (isEditingBudget) {
                                R.string.dashboard_monthly_budget_save
                            } else {
                                R.string.dashboard_monthly_budget_edit
                            }
                        ),
                        tint = MeadowGreenDark
                    )
                }
            }

            if (isEditingBudget) {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.dashboard_monthly_budget_label)) },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Text(
                    text = stringResource(
                        R.string.dashboard_monthly_budget_minimum,
                        currencyFormat.format(UserSettings.MIN_MONTHLY_BUDGET)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SubscriptionsWeedsHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.dashboard_subscriptions_title),
            style = MaterialTheme.typography.titleMedium,
            color = MeadowGreenDark,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.dashboard_weeds_or_flowers),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SubscriptionWeedFlowerRow(
    bill: Bill,
    onRowClick: () -> Unit,
    onKeep: () -> Unit,
    onPull: () -> Unit,
    onShare: () -> Unit,
    onLinkRoots: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onRowClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MeadowWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubscriptionBrandIcon(bill = bill)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.dashboard_keep_pull_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onLinkRoots) {
                Icon(
                    imageVector = Icons.Default.Nature,
                    contentDescription = stringResource(R.string.link_roots_icon),
                    tint = MeadowEarth,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Default.LocalFlorist,
                    contentDescription = stringResource(R.string.cross_pollinate_share),
                    tint = MeadowGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = onPull) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = stringResource(R.string.dashboard_pull_weed),
                    tint = WeedRed,
                    modifier = Modifier.size(26.dp)
                )
            }
            IconButton(onClick = onKeep) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = stringResource(R.string.dashboard_keep_flower),
                    tint = LeafGreen,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardBottomNavigationBar(
    selectedItem: DashboardBottomNavItem,
    onSelectItem: (DashboardBottomNavItem) -> Unit
) {
    NavigationBar(
        containerColor = MeadowWhite,
        tonalElevation = 4.dp
    ) {
        NavigationBarItem(
            selected = selectedItem == DashboardBottomNavItem.RENT,
            onClick = { onSelectItem(DashboardBottomNavItem.RENT) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.dashboard_nav_rent)) }
        )
        NavigationBarItem(
            selected = selectedItem == DashboardBottomNavItem.LOANS,
            onClick = { onSelectItem(DashboardBottomNavItem.LOANS) },
            icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
            label = { Text(stringResource(R.string.dashboard_nav_loans)) }
        )
        NavigationBarItem(
            selected = selectedItem == DashboardBottomNavItem.UTILITIES,
            onClick = { onSelectItem(DashboardBottomNavItem.UTILITIES) },
            icon = { Icon(Icons.Default.Bolt, contentDescription = null) },
            label = { Text(stringResource(R.string.dashboard_nav_utilities)) }
        )
        NavigationBarItem(
            selected = selectedItem == DashboardBottomNavItem.OTHERS,
            onClick = { onSelectItem(DashboardBottomNavItem.OTHERS) },
            icon = { Icon(Icons.Default.Build, contentDescription = null) },
            label = { Text(stringResource(R.string.dashboard_nav_others)) }
        )
    }
}

@Composable
private fun DrawerNavRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MeadowGreen)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MeadowGreenDark
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    val today = LocalDate.of(2026, 9, 4)
    val bills = listOf(
        Bill(4, "Netflix", 15.49, today.plusDays(12), BillCategory.SUBSCRIPTIONS),
        Bill(5, "Spotify Premium", 11.99, today.plusDays(12), BillCategory.SUBSCRIPTIONS)
    )
    ChargeMeNotTheme {
        DashboardScreen(
            uiState = DashboardUiState(
                userDisplayName = "Sarah",
                formattedDate = "(Friday, September 4, 2026)",
                totalUpcoming = 1_230.0,
                upcomingBillCount = 12,
                subscriptionBills = bills,
                categoryTotals = mapOf(
                    BillCategory.RENT to 1_450.0,
                    BillCategory.SUBSCRIPTIONS to 27.48
                ),
                isLoading = false
            ),
            settingsUiState = SettingsUiState(),
            selectedBillForEdit = null,
            selectedCategoryForEdit = null,
            categoryBills = emptyList(),
            onKeepSubscription = {},
            onPullSubscription = {},
            onMonthlyBudgetChange = {},
            onNagModeToggleRequested = {},
            onNotificationPermissionResult = {},
            onNotificationPermissionRequestHandled = {},
            onRefreshNotificationPermissionState = {},
            onNavigateToPruningSimulator = {},
            onNavigateToWeedWhacker = {},
            onNavigateToCompostBin = {},
            onLinkBillToParent = { _, _ -> },
            onSelectBillForEdit = {},
            onClearEditSelection = {},
            onSaveBillEdits = {},
            onPetalTapped = {},
            onClearCategorySelection = {},
            onAddBillToCategory = {},
            onSelectBottomNavItem = {},
            onBloomSettingsClick = {}
        )
    }
}

private fun eligibleParentBills(childBill: Bill, allBills: List<Bill>): List<Bill> {
    val descendantIds = collectDescendantBillIds(childBill.id, allBills)
    return allBills.filter { bill ->
        bill.id != childBill.id && bill.id !in descendantIds
    }
}

private fun collectDescendantBillIds(parentId: Long, bills: List<Bill>): Set<Long> {
    return bills
        .filter { bill -> bill.parentBillId == parentId }
        .flatMap { child ->
            setOf(child.id) + collectDescendantBillIds(child.id, bills)
        }
        .toSet()
}
