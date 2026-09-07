package com.artie.chargemenot.ui.components

import androidx.compose.runtime.Composable
import com.artie.chargemenot.domain.model.Bill

@Composable
fun MeadowAppOverlays(
    selectedBillForEdit: Bill?,
    selectedCategoryForEdit: String?,
    categoryBills: List<Bill>,
    isProfileEditVisible: Boolean,
    isManualBillVisible: Boolean,
    manualBillEntrySession: Int,
    userDisplayName: String,
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
        onDismiss = onClearEditSelection,
        onSave = onSaveBillEdits
    )

    if (selectedCategoryForEdit != null && selectedBillForEdit == null) {
        CategoryDetailBottomSheet(
            selectedCategory = selectedCategoryForEdit,
            bills = categoryBills,
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
            onDismiss = onDismissManualBillEntry,
            onSave = onSaveManualBill
        )
    }
}
