package com.artie.chargemenot.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.R
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.domain.model.MeadowCategories
import com.artie.chargemenot.ui.theme.MeadowGreen
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowWhite
import com.artie.chargemenot.ui.viewmodels.CategoryViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualBillBottomSheet(
    isVisible: Boolean,
    sessionKey: Int,
    categoryViewModel: CategoryViewModel,
    defaultParentCategory: String = MeadowCategories.VINES,
    defaultSubCategory: String = "Subscriptions",
    defaultDueDate: LocalDate? = null,
    onDismiss: () -> Unit,
    onSave: (Bill) -> Unit
) {
    if (!isVisible) {
        return
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormat = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }
    var nameInput by remember(sessionKey) { mutableStateOf("") }
    var amountInput by remember(sessionKey) { mutableStateOf("") }
    var selectedDueDate by remember(sessionKey, defaultDueDate) {
        mutableStateOf(defaultDueDate ?: LocalDate.now().plusDays(14))
    }
    var selectedParent by remember(sessionKey) { mutableStateOf(defaultParentCategory) }
    var selectedSubcategory by remember(sessionKey) { mutableStateOf(defaultSubCategory) }
    var validationError by remember(sessionKey) { mutableStateOf<String?>(null) }
    var showDatePicker by remember(sessionKey) { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDueDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            selectedDueDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.manual_bill_date_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.edit_bill_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.manual_bill_title),
                style = MaterialTheme.typography.titleLarge,
                color = MeadowGreenDark,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.manual_bill_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.edit_bill_name_label)) },
                placeholder = { Text(stringResource(R.string.manual_bill_name_placeholder)) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.edit_bill_amount_label)) },
                prefix = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = selectedDueDate.format(dateFormat),
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                readOnly = true,
                label = { Text(stringResource(R.string.edit_bill_date_label)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = stringResource(R.string.manual_bill_pick_date)
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            CategorySelector(
                categoryViewModel = categoryViewModel,
                selectedParent = selectedParent,
                selectedSubcategory = selectedSubcategory,
                onParentSelected = { parent ->
                    selectedParent = parent
                    selectedSubcategory = MeadowCategories.defaultSubcategoryByParent[parent]
                        ?: selectedSubcategory
                },
                onSubcategorySelected = { subcategory ->
                    selectedSubcategory = subcategory
                }
            )

            validationError?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.edit_bill_cancel))
                }

                Button(
                    onClick = {
                        val parsedAmount = amountInput
                            .replace(",", "")
                            .replace("$", "")
                            .trim()
                            .toDoubleOrNull()

                        when {
                            nameInput.isBlank() -> {
                                validationError = "Bill name is required."
                            }
                            parsedAmount == null || parsedAmount <= 0.0 -> {
                                validationError = "Enter a valid amount."
                            }
                            selectedParent.isBlank() || selectedSubcategory.isBlank() -> {
                                validationError = "Select a parent category and subcategory."
                            }
                            else -> {
                                validationError = null
                                onSave(
                                    Bill(
                                        name = nameInput.trim(),
                                        amount = parsedAmount,
                                        dueDate = selectedDueDate,
                                        parentCategory = selectedParent,
                                        subCategory = selectedSubcategory
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MeadowGreen,
                        contentColor = MeadowWhite
                    )
                ) {
                    Text(stringResource(R.string.manual_bill_save))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
