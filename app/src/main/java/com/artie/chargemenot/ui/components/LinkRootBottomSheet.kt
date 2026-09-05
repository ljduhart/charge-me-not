package com.artie.chargemenot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.artie.chargemenot.R
import com.artie.chargemenot.domain.model.Bill
import com.artie.chargemenot.ui.theme.MeadowCream
import com.artie.chargemenot.ui.theme.MeadowEarth
import com.artie.chargemenot.ui.theme.MeadowGreen
import com.artie.chargemenot.ui.theme.MeadowGreenDark
import com.artie.chargemenot.ui.theme.MeadowSage
import com.artie.chargemenot.ui.theme.MeadowWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkRootBottomSheet(
    bill: Bill,
    availableParents: List<Bill>,
    onLinkToParent: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedParentId by remember(bill.id, bill.parentBillId) {
        mutableStateOf(bill.parentBillId)
    }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val selectedParentName = availableParents
        .firstOrNull { parent -> parent.id == selectedParentId }
        ?.name
        ?: stringResource(R.string.link_roots_no_parent)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MeadowCream,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MeadowCream)
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.link_roots_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MeadowGreenDark,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.link_roots_subtitle, bill.name),
                style = MaterialTheme.typography.bodyMedium,
                color = MeadowEarth,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            Text(
                text = stringResource(R.string.link_roots_parent_label),
                style = MaterialTheme.typography.labelLarge,
                color = MeadowGreenDark,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = selectedParentName,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MeadowWhite,
                        unfocusedContainerColor = MeadowWhite,
                        focusedIndicatorColor = MeadowGreen,
                        unfocusedIndicatorColor = MeadowSage
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false },
                    containerColor = MeadowWhite
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.link_roots_no_parent),
                                color = MeadowGreenDark
                            )
                        },
                        onClick = {
                            selectedParentId = null
                            isDropdownExpanded = false
                        }
                    )
                    availableParents.forEach { parent ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = parent.name,
                                    color = MeadowGreenDark
                                )
                            },
                            onClick = {
                                selectedParentId = parent.id
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onLinkToParent(selectedParentId)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MeadowGreen,
                    contentColor = MeadowWhite
                )
            ) {
                Text(stringResource(R.string.link_roots_save))
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.link_roots_cancel),
                    color = MeadowGreenDark
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
