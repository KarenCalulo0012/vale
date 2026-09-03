package com.kcalulo.vale.feature.realitycheck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kcalulo.vale.core.common.MoneyFormat
import com.kcalulo.vale.core.common.RealityCheckState
import com.kcalulo.vale.core.common.ValeCalculations
import com.kcalulo.vale.core.design.components.ValeBottomSheet
import com.kcalulo.vale.core.design.components.ValeMascot
import com.kcalulo.vale.core.design.components.ValeMascotMessage
import com.kcalulo.vale.core.design.components.ValePlaceholderScreen
import com.kcalulo.vale.core.design.components.ValePrimaryButton
import com.kcalulo.vale.core.design.components.ValeProgressBar
import com.kcalulo.vale.core.design.components.ValeSheetOption
import com.kcalulo.vale.core.design.theme.MintSoft
import com.kcalulo.vale.core.design.theme.PinkSoft
import com.kcalulo.vale.core.design.theme.SoftLavender
import com.kcalulo.vale.core.design.theme.StatusNotWorthIt
import com.kcalulo.vale.core.design.theme.StatusOnTrack

/** Reality Check — compare the original prediction against actual behavior (spec §16). */
@Composable
fun RealityCheckScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RealityCheckViewModel = hiltViewModel(),
) {
    val row by viewModel.itemState.collectAsStateWithLifecycle()
    val symbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    var showWhatNow by remember { mutableStateOf(false) }
    var showSellSheet by remember { mutableStateOf(false) }
    var showGiveAwaySheet by remember { mutableStateOf(false) }

    val current = row
    if (current == null) {
        ValePlaceholderScreen(
            title = "Item not found",
            subtitle = "This one may have been deleted.",
            modifier = modifier.fillMaxSize()
        )
        return
    }
    val item = current.item
    val actualUses = current.actualUses
    val currentCostPerUse = ValeCalculations.currentCostPerUse(item.originalPriceMinor, actualUses)
    val progress = ValeCalculations.progressFraction(actualUses, item.expectedUses)
    val remaining = ValeCalculations.remainingUses(item.expectedUses, actualUses)
    val state = ValeCalculations.realityCheckState(
        actualUses = actualUses,
        expectedUses = item.expectedUses,
        daysSincePurchase = viewModel.daysSincePurchase(item.purchaseDate),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(text = "Reality Check", style = MaterialTheme.typography.titleMedium)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ValeMascot(size = 56.dp)
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Original math vs actual math (spec §16)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MathCard(
                modifier = Modifier.weight(1f),
                title = "Original math",
                uses = "${item.expectedUses} uses",
                costPerUse = MoneyFormat.formatPerUse(item.targetCostPerUseMinor, symbol)
            )
            MathCard(
                modifier = Modifier.weight(1f),
                title = "Actual math",
                uses = "$actualUses uses",
                costPerUse = currentCostPerUse?.let { MoneyFormat.formatPerUse(it, symbol) } ?: "—"
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ValeProgressBar(progress = progress)
            Text(
                text = "$actualUses / ${item.expectedUses} uses · $remaining to go",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ValeMascotMessage(
            message = when (state) {
                RealityCheckState.WORTH_IT -> "Worth it! Your math actually mathed. 🏆"
                RealityCheckState.STILL_PROVING_IT -> "Still proving it. Keep going, bestie."
                RealityCheckState.MATH_ISNT_WORKING -> "Hmm… your original math isn't mathing yet. 👀"
            },
            positive = state != RealityCheckState.MATH_ISNT_WORKING,
            modifier = Modifier.fillMaxWidth()
        )

        if (state != RealityCheckState.WORTH_IT) {
            ValePrimaryButton(
                text = "See my options",
                onClick = { showWhatNow = true },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showWhatNow) {
        ValeBottomSheet(
            title = "Let's make it worth it.",
            subtitle = "What do you want to do with ${item.name}?",
            options = listOf(
                ValeSheetOption("Keep using it", Icons.Default.Check, StatusOnTrack, MintSoft),
                ValeSheetOption("Sell it", Icons.Default.ShoppingCart, MaterialTheme.colorScheme.primary, SoftLavender),
                ValeSheetOption("Give it away", Icons.Default.Favorite, StatusNotWorthIt, PinkSoft),
            ),
            onOptionSelected = { index ->
                showWhatNow = false
                when (index) {
                    0 -> Unit // Keep using it — no state change, just keep tracking.
                    1 -> showSellSheet = true
                    2 -> showGiveAwaySheet = true
                }
            },
            onDismiss = { showWhatNow = false }
        )
    }

    if (showSellSheet) {
        SellSheet(
            item = item,
            actualUses = actualUses,
            symbol = symbol,
            onSell = { soldPriceMinor ->
                viewModel.sellItem(soldPriceMinor)
                showSellSheet = false
                onBack()
            },
            onDismiss = { showSellSheet = false }
        )
    }

    if (showGiveAwaySheet) {
        GiveAwaySheet(
            onConfirm = { note ->
                viewModel.giveAwayItem(note)
                showGiveAwaySheet = false
                onBack()
            },
            onDismiss = { showGiveAwaySheet = false }
        )
    }
}

@Composable
private fun MathCard(title: String, uses: String, costPerUse: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = costPerUse,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = uses,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
