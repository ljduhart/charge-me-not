package com.artie.chargemenot.ui.components

import androidx.compose.runtime.Composable
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.ui.dashboard.DashboardUiState
import com.artie.chargemenot.ui.dashboard.DashboardViewModel
import com.artie.chargemenot.ui.viewmodels.CategoryViewModel
import java.time.LocalDate

@Composable
fun MeadowAppOverlays(
    uiState: DashboardUiState,
    categoryViewModel: CategoryViewModel,
    dashboardViewModel: DashboardViewModel,
    onAddBillToCategory: (String) -> Unit
) {
    MeadowAppOverlays(
        selectedBillForEdit = uiState.selectedBillForEdit,
        categoryViewModel = categoryViewModel,
        selectedCategoryForEdit = uiState.selectedCategoryForEdit,
        categoryBills = uiState.categoryBills,
        isProfileEditVisible = uiState.isProfileEditVisible,
        isManualBillVisible = uiState.isManualBillVisible,
        manualBillEntrySession = uiState.manualBillEntrySession,
        manualBillPrefillDate = uiState.manualBillPrefillDate,
        userDisplayName = uiState.userDisplayName,
        currency = uiState.selectedCurrency,
        onClearEditSelection = dashboardViewModel::clearEditSelection,
        onSaveBillEdits = dashboardViewModel::saveBillEdits,
        onClearCategorySelection = dashboardViewModel::clearCategorySelection,
        onAddBillToCategory = onAddBillToCategory,
        onSelectBillForEdit = dashboardViewModel::selectBillForEdit,
        onDismissProfileEdit = dashboardViewModel::dismissProfileEdit,
        onUpdateDisplayName = dashboardViewModel::updateDisplayName,
        onDismissManualBillEntry = dashboardViewModel::dismissManualBillEntry,
        onSaveManualBill = dashboardViewModel::insertManualBill
    )
}

@Composable
fun MeadowAppOverlays(
    selectedBillForEdit: Bill?,
    categoryViewModel: CategoryViewModel,
    selectedCategoryForEdit: String?,
    categoryBills: List<Bill>,
    isProfileEditVisible: Boolean,
    isManualBillVisible: Boolean,
    manualBillEntrySession: Int,
    manualBillPrefillDate: LocalDate?,
    userDisplayName: String,
    currency: SupportedCurrency,
    onClearEditSelection: () -> Unit,
    onSaveBillEdits: (Bill) -> Unit,
    onClearCategorySelection: () -> Unit,
    onAddBillToCategory: (String) -> Unit,
    onSelectBillForEdit: (Bill) -> Unit,
    onDismissProfileEdit: () -> Unit,
    onUpdateDisplayName: (String) -> Unit,
    onDismissManualBillEntry: () -> Unit,
    onSaveManualBill: (Bill) -> Unit
) {
    EditBillBottomSheet(
        selectedBill = selectedBillForEdit,
        categoryViewModel = categoryViewModel,
        onDismiss = onClearEditSelection,
        onSave = onSaveBillEdits
    )

    if (selectedCategoryForEdit != null && selectedBillForEdit == null) {
        CategoryDetailBottomSheet(
            selectedCategory = selectedCategoryForEdit,
            bills = categoryBills,
            currency = currency,
            onDismiss = onClearCategorySelection,
            onAddNewBill = onAddBillToCategory,
            onBillClick = onSelectBillForEdit
        )
    }

    if (isProfileEditVisible && selectedBillForEdit == null && selectedCategoryForEdit == null) {
        ProfileEditBottomSheet(
            currentDisplayName = userDisplayName,
            isVisible = true,
            onDismiss = onDismissProfileEdit,
            onSave = onUpdateDisplayName
        )
    }

    if (isManualBillVisible && selectedBillForEdit == null && selectedCategoryForEdit == null) {
        ManualBillBottomSheet(
            isVisible = true,
            sessionKey = manualBillEntrySession,
            categoryViewModel = categoryViewModel,
            defaultDueDate = manualBillPrefillDate,
            onDismiss = onDismissManualBillEntry,
            onSave = onSaveManualBill
        )
    }
}
