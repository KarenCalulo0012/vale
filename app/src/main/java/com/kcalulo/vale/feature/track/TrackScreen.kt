package com.kcalulo.vale.feature.track

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kcalulo.vale.core.common.AttentionReason
import com.kcalulo.vale.core.common.MoneyFormat
import com.kcalulo.vale.core.common.ValeCalculations
import com.kcalulo.vale.core.common.displayCostPerUseMinor
import com.kcalulo.vale.core.database.dao.ItemWithUsageCount
import com.kcalulo.vale.core.design.components.ValeChip
import com.kcalulo.vale.core.design.components.ValeItemCard
import com.kcalulo.vale.core.design.components.ValeMascot
import com.kcalulo.vale.core.design.components.ValePrimaryButton

/** Track — one-tap usage logging for bought items (spec §14). */
@Composable
fun TrackScreen(
    onItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrackViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val symbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val lastLogged by viewModel.lastLogged.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(lastLogged) {
        val logged = lastLogged ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "Usage added · ${logged.itemName}",
            actionLabel = "UNDO",
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undoLastLog()
        } else {
            viewModel.consumeSnackbar()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Track",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            activeFilter?.let { reason ->
                FilterChipRow(reason = reason, onClear = viewModel::clearFilter)
            }

            if (items.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ValeMascot(size = 64.dp)
                    Text(
                        text = if (activeFilter != null) "Nothing needs a look here" else "Nothing to track yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (activeFilter != null) {
                            "Every bought item has cleared this one — nice."
                        } else {
                            "Buy something from Calculate and it'll show up here."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(items, key = { it.item.id }) { row ->
                        TrackRow(
                            row = row,
                            symbol = symbol,
                            onClick = { onItemClick(row.item.id) },
                            onLogUsage = { viewModel.logUsage(row) },
                        )
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/** Shows which Attention group deep-linked here (spec §6) with a clear way back to everything. */
@Composable
private fun FilterChipRow(reason: AttentionReason, onClear: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ValeChip(
            text = "Showing: ${reason.label}",
            contentColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.primary
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Clear filter",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clickable(onClick = onClear)
                .padding(4.dp)
        )
    }
}

@Composable
private fun TrackRow(
    row: ItemWithUsageCount,
    symbol: String,
    onClick: () -> Unit,
    onLogUsage: () -> Unit,
) {
    val item = row.item
    val costPerUse = item.displayCostPerUseMinor(row.actualUses)
    ValeItemCard(
        title = item.name,
        price = MoneyFormat.format(item.originalPriceMinor, symbol),
        perUse = costPerUse?.let { "${MoneyFormat.formatPerUse(it, symbol)} per use" } ?: "Not used yet",
        usesText = "${row.actualUses} / ${item.expectedUses} uses",
        progress = ValeCalculations.progressFraction(row.actualUses, item.expectedUses),
        thumbnail = {
            Text(text = item.category?.emoji ?: "🛍️", style = MaterialTheme.typography.headlineSmall)
        },
        onClick = onClick,
        actionButton = {
            ValePrimaryButton(
                text = "+ Used it",
                onClick = onLogUsage,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}
