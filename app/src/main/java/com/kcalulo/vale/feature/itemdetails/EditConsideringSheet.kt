package com.kcalulo.vale.feature.itemdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kcalulo.vale.core.database.entity.ItemCategory
import com.kcalulo.vale.core.database.entity.ItemEntity
import com.kcalulo.vale.core.design.components.ValeCategoryPicker
import com.kcalulo.vale.core.design.components.ValeInputField
import com.kcalulo.vale.core.design.components.ValePrimaryButton
import com.kcalulo.vale.core.design.components.ValeStepperField
import com.kcalulo.vale.feature.calculate.parsePriceMinor

/**
 * Edit a Considering item's name/category/price/expected uses before deciding (spec §10).
 * Recomputes the target cost/use live, same preview pattern as Calculate — a Considering
 * item's target isn't frozen yet, so there's nothing wrong with it changing here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditConsideringSheet(
    item: ItemEntity,
    symbol: String,
    onSave: (name: String, category: ItemCategory?, priceMinor: Long, expectedUses: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(item.name) }
    var priceText by remember {
        mutableStateOf(
            java.math.BigDecimal(item.originalPriceMinor).movePointLeft(2).stripTrailingZeros().toPlainString()
        )
    }
    var expectedUses by remember { mutableStateOf(item.expectedUses) }
    var category by remember { mutableStateOf(item.category) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    val priceMinor = parsePriceMinor(priceText)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Edit before you decide",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            ValeInputField(
                label = "What is it?",
                value = name,
                onValueChange = { name = it; nameError = null },
                errorText = nameError
            )

            ValeInputField(
                label = "How much is it?",
                value = priceText,
                onValueChange = { priceText = it; priceError = null },
                placeholder = "${symbol}0",
                keyboardType = KeyboardType.Decimal,
                errorText = priceError
            )

            ValeStepperField(
                label = "How many times will you use it?",
                value = expectedUses,
                onValueChange = { expectedUses = it }
            )

            ValeCategoryPicker(selected = category, onSelected = { category = it })

            ValePrimaryButton(
                text = "Save",
                onClick = {
                    val trimmedName = name.trim()
                    nameError = if (trimmedName.isBlank()) "Give it a name, bestie." else null
                    priceError = when {
                        priceText.isBlank() -> "How much is it?"
                        priceMinor == null -> "That number is too big, bestie."
                        priceMinor <= 0 -> "That price isn't mathing."
                        else -> null
                    }
                    if (nameError == null && priceError == null && priceMinor != null) {
                        onSave(trimmedName, category, priceMinor, expectedUses)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
