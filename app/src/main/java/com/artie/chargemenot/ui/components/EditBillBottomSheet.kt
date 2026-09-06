package com.artie.chargemenot.ui.components

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.artie.chargemenot.domain.model.BillCategory
import com.artie.chargemenot.ui.theme.MeadowGreen
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowWhite
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBillBottomSheet(
    selectedBill: Bill?,
    onDismiss: () -> Unit,
    onSave: (Bill) -> Unit
) {
    if (selectedBill == null) {
        return
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormat = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }

    var nameInput by remember(selectedBill.id) { mutableStateOf(selectedBill.name) }
    var amountInput by remember(selectedBill.id) {
        mutableStateOf(String.format("%.2f", selectedBill.amount))
    }
    var dateInput by remember(selectedBill.id) {
        mutableStateOf(selectedBill.dueDate.format(dateFormat))
    }
    var categoryInput by remember(selectedBill.id) {
        mutableStateOf(selectedBill.category.name)
    }
    var validationError by remember(selectedBill.id) { mutableStateOf<String?>(null) }

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
                text = stringResource(R.string.edit_bill_title),
                style = MaterialTheme.typography.titleLarge,
                color = MeadowGreenDark,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.edit_bill_name_label)) },
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
                value = dateInput,
                onValueChange = { dateInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.edit_bill_date_label)) },
                placeholder = { Text("Sep 12, 2026") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = categoryInput,
                onValueChange = { categoryInput = it.uppercase() },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.edit_bill_category_label)) },
                placeholder = { Text("UTILITIES") },
                singleLine = true
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
                        val parsedDate = parseDueDate(dateInput.trim(), dateFormat)
                        val parsedCategory = runCatching {
                            BillCategory.valueOf(categoryInput.trim().uppercase())
                        }.getOrNull()

                        when {
                            nameInput.isBlank() -> {
                                validationError = "Bill name is required."
                            }
                            parsedAmount == null || parsedAmount <= 0.0 -> {
                                validationError = "Enter a valid amount."
                            }
                            parsedDate == null -> {
                                validationError = "Enter a valid date (e.g. Sep 12, 2026)."
                            }
                            parsedCategory == null -> {
                                validationError = "Enter a valid category (e.g. UTILITIES)."
                            }
                            else -> {
                                validationError = null
                                onSave(
                                    selectedBill.copy(
                                        name = nameInput.trim(),
                                        amount = parsedAmount,
                                        dueDate = parsedDate,
                                        category = parsedCategory
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
                    Text(stringResource(R.string.edit_bill_save))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun parseDueDate(rawInput: String, formatter: DateTimeFormatter): LocalDate? {
    return try {
        LocalDate.parse(rawInput, formatter)
    } catch (_: DateTimeParseException) {
        null
    }
}
