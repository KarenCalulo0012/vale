package com.kcalulo.vale.feature.calculate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kcalulo.vale.core.common.MoneyFormat
import com.kcalulo.vale.core.common.ResultVerdict
import com.kcalulo.vale.core.database.entity.ItemStatus
import com.kcalulo.vale.core.database.entity.SkipReason
import com.kcalulo.vale.core.design.components.ValeDialog
import com.kcalulo.vale.core.design.components.ValeMascotMessage
import com.kcalulo.vale.core.design.components.ValePrimaryButton
import com.kcalulo.vale.core.design.components.ValeSecondaryButton
import com.kcalulo.vale.core.design.components.ValeSkipReasonSheet
import com.kcalulo.vale.core.design.components.ValeTextButton

/** Result / Decide — turn a calculation into an intentional decision (spec §8). */
@Composable
fun ResultScreen(
    viewModel: CalculateViewModel,
    onDecided: () -> Unit,
    onViewItem: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val decision by viewModel.decisionState.collectAsStateWithLifecycle()
    val symbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    var showBuyConfirm by remember { mutableStateOf(false) }
    var showSkipSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Okay, girl. Let's see if your math is actually true. 👀",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // The big number
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.extraLarge)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = state.name.ifBlank { "Your item" },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = state.targetCostPerUseMinor
                    ?.let { MoneyFormat.formatPerUse(it, symbol) }
                    ?: "$symbol—",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "per use",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "${state.priceMinor?.let { MoneyFormat.format(it, symbol) } ?: ""} · ${state.expectedUses} uses",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ValeMascotMessage(
            message = when (state.verdict) {
                ResultVerdict.APPROVED -> "GIRL MATH VALIDATED. That's a real commitment. 💅"
                ResultVerdict.NEUTRAL -> "Hmm… the math works if you do. Your call, bestie."
                ResultVerdict.QUESTIONABLE -> "That's not many uses. Is the math mathing? 👀"
            },
            positive = state.verdict != ResultVerdict.QUESTIONABLE,
            modifier = Modifier.fillMaxWidth()
        )

        ValePrimaryButton(
            text = "Yes, I'm buying it",
            onClick = { showBuyConfirm = true },
            modifier = Modifier.fillMaxWidth()
        )
        ValeSecondaryButton(
            text = "Let me think",
            onClick = { viewModel.saveDecision(ItemStatus.CONSIDERING) },
            modifier = Modifier.fillMaxWidth()
        )
        ValeTextButton(
            text = "Skip it",
            onClick = { showSkipSheet = true }
        )
    }

    if (showBuyConfirm) {
        ValeDialog(
            title = "Buying it today?",
            message = "We'll start counting from today and see if you actually use her.",
            confirmText = "Yes, bought it!",
            onConfirm = {
                showBuyConfirm = false
                viewModel.saveDecision(ItemStatus.BOUGHT)
            },
            onDismiss = { showBuyConfirm = false }
        )
    }

    if (showSkipSheet) {
        ValeSkipReasonSheet(
            onReasonSelected = { reason ->
                showSkipSheet = false
                viewModel.saveDecision(ItemStatus.SKIPPED, skipReason = reason)
            },
            onDismiss = { showSkipSheet = false }
        )
    }

    (decision as? DecisionState.Saved)?.let { saved ->
        val (title, message) = when (saved.status) {
            ItemStatus.BOUGHT ->
                "Okayyy, she got it. 🛍️" to "Now let's see if you actually use her. Tracking starts today."
            ItemStatus.CONSIDERING ->
                "Saved for later. 💭" to "Take your time, bestie. The math will be here when you're ready."
            else ->
                "YOU RESISTED. 🥹" to "Future You is screaming. That's ${state.priceMinor?.let { MoneyFormat.format(it, symbol) } ?: "money"} of purchases skipped."
        }
        // spec §9 — after buying, the CTA leads into tracking rather than just dismissing.
        val confirmText = if (saved.status == ItemStatus.BOUGHT) "View Item" else "Done"
        ValeDialog(
            title = title,
            message = message,
            confirmText = confirmText,
            onConfirm = {
                viewModel.startOver()
                if (saved.status == ItemStatus.BOUGHT) onViewItem(saved.itemId) else onDecided()
            },
            onDismiss = {
                viewModel.startOver()
                onDecided()
            },
            cancelText = "",
            showMascot = true
        )
    }
}
