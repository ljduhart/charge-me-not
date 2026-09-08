package com.artie.chargemenot.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artie.chargemenot.domain.model.MeadowCategories
import com.artie.chargemenot.ui.theme.meadowParentColor
import com.artie.chargemenot.ui.theme.meadowParentIcon
import com.artie.chargemenot.ui.viewmodels.CategoryViewModel

@Composable
fun CategorySelector(
    categoryViewModel: CategoryViewModel,
    selectedParent: String?,
    selectedSubcategory: String?,
    onParentSelected: (String) -> Unit,
    onSubcategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val parentCategories by categoryViewModel.parentCategories.collectAsStateWithLifecycle(
        initialValue = MeadowCategories.parentNames
    )
    val resolvedParent = selectedParent ?: parentCategories.firstOrNull()
    val subcategoriesFlow = remember(resolvedParent) {
        categoryViewModel.getSubcategoriesForParent(resolvedParent ?: MeadowCategories.CANOPY)
    }
    val subcategoriesFromDb by subcategoriesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val subcategories = remember(subcategoriesFromDb, selectedSubcategory) {
        if (selectedSubcategory != null && selectedSubcategory !in subcategoriesFromDb) {
            subcategoriesFromDb + selectedSubcategory
        } else {
            subcategoriesFromDb
        }
    }

    var showCustomInput by remember { mutableStateOf(false) }
    var customSubcategoryInput by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Meadow Category",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            parentCategories.forEach { parent ->
                SelectableIcon(
                    label = MeadowCategories.shortDisplayName(parent),
                    icon = meadowParentIcon(parent),
                    tint = meadowParentColor(parent),
                    selected = parent == resolvedParent,
                    onClick = {
                        showCustomInput = false
                        customSubcategoryInput = ""
                        onParentSelected(parent)
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = resolvedParent != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = 14.dp)) {
                CategorySubcategoryFlowRow(
                    subcategories = subcategories,
                    selectedSubcategory = selectedSubcategory,
                    accentColor = meadowParentColor(resolvedParent ?: MeadowCategories.CANOPY),
                    showCustomInput = showCustomInput,
                    customSubcategoryInput = customSubcategoryInput,
                    onSubcategorySelected = onSubcategorySelected,
                    onShowCustomInput = {
                        showCustomInput = true
                        customSubcategoryInput = ""
                    },
                    onCustomInputChanged = { customSubcategoryInput = it },
                    onSaveCustomSubcategory = {
                        val trimmed = customSubcategoryInput.trim()
                        if (trimmed.isNotBlank() && resolvedParent != null) {
                            categoryViewModel.addCustomSubcategory(resolvedParent, trimmed)
                            onSubcategorySelected(trimmed)
                            showCustomInput = false
                            customSubcategoryInput = ""
                        }
                    },
                    onDismissCustomInput = {
                        showCustomInput = false
                        customSubcategoryInput = ""
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySubcategoryFlowRow(
    subcategories: List<String>,
    selectedSubcategory: String?,
    accentColor: Color,
    showCustomInput: Boolean,
    customSubcategoryInput: String,
    onSubcategorySelected: (String) -> Unit,
    onShowCustomInput: () -> Unit,
    onCustomInputChanged: (String) -> Unit,
    onSaveCustomSubcategory: () -> Unit,
    onDismissCustomInput: () -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        subcategories.forEach { subcategory ->
            val selected = subcategory == selectedSubcategory
            FilterChip(
                selected = selected,
                onClick = { onSubcategorySelected(subcategory) },
                label = { Text(subcategory) },
                leadingIcon = if (selected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else {
                    null
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accentColor.copy(alpha = 0.22f),
                    selectedLabelColor = accentColor
                )
            )
        }

        AssistChip(
            onClick = onShowCustomInput,
            label = { Text("Add Custom") }
        )
    }

    if (showCustomInput) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = customSubcategoryInput,
                onValueChange = onCustomInputChanged,
                modifier = Modifier.weight(1f),
                label = { Text("Custom subcategory") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            FilterChip(
                selected = true,
                onClick = onSaveCustomSubcategory,
                enabled = customSubcategoryInput.isNotBlank(),
                label = { Text("Save") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accentColor.copy(alpha = 0.25f)
                )
            )
            FilterChip(
                selected = false,
                onClick = onDismissCustomInput,
                label = { Text("Cancel") }
            )
        }
    }
}

@Composable
fun SelectableIcon(
    label: String,
    icon: ImageVector,
    tint: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) tint else tint.copy(alpha = 0.25f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(
                    width = if (selected) 2.dp else 0.dp,
                    color = if (selected) tint else Color.Transparent,
                    shape = CircleShape
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) tint else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
