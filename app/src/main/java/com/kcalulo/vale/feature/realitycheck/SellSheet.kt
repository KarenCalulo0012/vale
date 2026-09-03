package com.kcalulo.vale.feature.realitycheck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kcalulo.vale.core.common.MoneyFormat
import com.kcalulo.vale.core.common.ValeCalculations
import com.kcalulo.vale.core.database.entity.ItemEntity
import com.kcalulo.vale.core.design.components.ValeInputField
import com.kcalulo.vale.core.design.components.ValePrimaryButton
import com.kcalulo.vale.core.design.components.ValeTextButton
import java.math.BigDecimal

/** Sell / Recover Value (spec §18) — target resale price is a target, never called "market value". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellSheet(
    item: ItemEntity,
    actualUses: Int,
    symbol: String,
    onSell: (soldPriceMinor: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var soldPriceText by remember { mutableStateOf("") }
    val targetResaleMinor = ValeCalculations.targetResalePriceMinor(
        priceMinor = item.originalPriceMinor,
        targetCostPerUseMinor = item.targetCostPerUseMinor,
        actualUses = actualUses,
    )
    val soldPriceMinor = soldPriceText.toBigDecimalOrNull()?.multiply(BigDecimal(100))?.toLong()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Sell it",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.large)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Target resale price",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = MoneyFormat.format(targetResaleMinor, symbol),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "What you'd need to recover to hit your original target",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ValeInputField(
                label = "What did you actually sell it for?",
                value = soldPriceText,
                onValueChange = { soldPriceText = it },
                placeholder = "${symbol}0",
                keyboardType = KeyboardType.Decimal
            )

            ValePrimaryButton(
                text = "Mark as sold",
                onClick = { soldPriceMinor?.let(onSell) },
                enabled = soldPriceMinor != null && soldPriceMinor >= 0,
                modifier = Modifier.fillMaxWidth()
            )
            ValeTextButton(text = "Cancel", onClick = onDismiss)
        }
    }
}
