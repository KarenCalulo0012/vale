package com.kcalulo.vale.feature.home

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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kcalulo.vale.core.common.MoneyFormat
import com.kcalulo.vale.core.common.displayCostPerUseMinor
import com.kcalulo.vale.core.database.dao.ItemWithUsageCount
import com.kcalulo.vale.core.database.entity.ItemStatus
import com.kcalulo.vale.core.design.components.ValeItemCard
import com.kcalulo.vale.core.design.components.ValeMascot
import com.kcalulo.vale.core.design.components.ValePrimaryButton
import com.kcalulo.vale.core.design.components.ValeStatus

/** Home — show what needs attention, make the next action obvious (spec §6). */
@Composable
fun HomeScreen(
    onCalculateClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VALE",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { /* Settings — later phase */ }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Hero
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.extraLarge)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ValeMascot(size = 72.dp)
            Text(
                text = "Hi, girl! What are we checking today?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            ValePrimaryButton(
                text = "Calculate it",
                onClick = onCalculateClick,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (state.recentItems.isEmpty() && !state.isLoading) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Nothing here yet.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Calculate your first item and Vale will keep the receipts. 🧾",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else if (state.recentItems.isNotEmpty()) {
            Text(
                text = "Recent Things",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            state.recentItems.forEach { row ->
                RecentItemCard(
                    row = row,
                    symbol = state.currencySymbol,
                    onClick = { onItemClick(row.item.id) }
                )
            }
        }
    }
}

@Composable
private fun RecentItemCard(row: ItemWithUsageCount, symbol: String, onClick: () -> Unit) {
    val item = row.item
    val costPerUse = item.displayCostPerUseMinor(row.actualUses)
    ValeItemCard(
        title = item.name,
        price = MoneyFormat.format(item.originalPriceMinor, symbol),
        perUse = costPerUse
            ?.let { "${MoneyFormat.formatPerUse(it, symbol)} per use" }
            ?: "Not used yet",
        usesText = when (item.status) {
            ItemStatus.BOUGHT -> "${row.actualUses} / ${item.expectedUses} uses"
            ItemStatus.CONSIDERING -> "Planned: ${item.expectedUses} uses"
            else -> "Skipped — ${MoneyFormat.format(item.originalPriceMinor, symbol)} not spent"
        },
        status = when (item.status) {
            ItemStatus.BOUGHT -> ValeStatus.Bought
            ItemStatus.CONSIDERING -> ValeStatus.Considering
            ItemStatus.SKIPPED -> ValeStatus.Skipped
            else -> null
        },
        thumbnail = {
            Text(
                text = item.category?.emoji ?: "🛍️",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        onClick = onClick
    )
}
