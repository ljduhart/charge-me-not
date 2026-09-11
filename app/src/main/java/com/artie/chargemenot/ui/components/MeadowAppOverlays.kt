package com.artie.chargemenot.ui.components

import androidx.compose.runtime.Composable
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.SupportedCurrency
import com.artie.chargemenot.ui.viewmodels.CategoryViewModel
import java.time.LocalDate

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
