package com.kcalulo.vale.feature.calculate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kcalulo.vale.core.common.MoneyFormat
import com.kcalulo.vale.core.database.entity.ItemCategory
import com.kcalulo.vale.core.design.components.ValeInputField
import com.kcalulo.vale.core.design.components.ValeMascot
import com.kcalulo.vale.core.design.components.ValePrimaryButton
import com.kcalulo.vale.core.design.components.ValeStepperField

/** Calculate — evaluate an item in seconds (spec §7). */
@Composable
fun CalculateScreen(
    onCalculated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalculateViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val symbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ValeMascot(size = 56.dp)
            Column {
                Text(
                    text = "Let's do the math. 🧮",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Is it worth it? Let's find out.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        ValeInputField(
            label = "What is it?",
            value = state.name,
            onValueChange = viewModel::onNameChange,
            placeholder = "e.g. Canvas Tote Bag",
            errorText = state.nameError
        )

        ValeInputField(
            label = "How much is it?",
            value = state.priceText,
            onValueChange = viewModel::onPriceChange,
            placeholder = "${symbol}0",
            keyboardType = KeyboardType.Decimal,
            errorText = state.priceError
        )

        ValeStepperField(
            label = "How many times will you use it?",
            value = state.expectedUses,
            onValueChange = viewModel::onExpectedUsesChange
        )

        CategoryPicker(
            selected = state.category,
            onSelected = viewModel::onCategoryChange
        )

        // Live preview — updates as expected uses change (spec §7 UX rule)
        val preview = state.targetCostPerUseMinor
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.large)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "That would be",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = if (preview != null) {
                    "${MoneyFormat.formatPerUse(preview, symbol)} per use"
                } else {
                    "$symbol— per use"
                },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        ValePrimaryButton(
            text = "Calculate",
            onClick = { if (viewModel.validate()) onCalculated() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CategoryPicker(
    selected: ItemCategory?,
    onSelected: (ItemCategory?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Category (optional)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ItemCategory.entries.forEach { category ->
                val isSelected = category == selected
                Text(
                    text = "${category.emoji} ${category.label}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface,
                            MaterialTheme.shapes.small
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.small
                        )
                        .clickable { onSelected(if (isSelected) null else category) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}
