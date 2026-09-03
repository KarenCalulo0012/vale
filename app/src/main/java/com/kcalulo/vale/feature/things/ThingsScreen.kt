package com.kcalulo.vale.feature.things

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kcalulo.vale.core.common.MoneyFormat
import com.kcalulo.vale.core.common.ValeCalculations
import com.kcalulo.vale.core.common.displayCostPerUseMinor
import com.kcalulo.vale.core.database.dao.ItemWithUsageCount
import com.kcalulo.vale.core.database.entity.ItemStatus
import com.kcalulo.vale.core.design.components.ValeInputField
import com.kcalulo.vale.core.design.components.ValeItemCard
import com.kcalulo.vale.core.design.components.ValeMascot
import com.kcalulo.vale.core.design.components.ValeStatus

/** Things — every evaluated item, filterable and searchable (spec §12). */
@Composable
fun ThingsScreen(
    onItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ThingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSortSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Things",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showSortSheet = true }) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Sort",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        ValeInputField(
            label = "Search",
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = "Search your things…"
        )

        FilterRow(selected = state.filter, onSelected = viewModel::onFilterChange)

        when {
            state.isLoading -> Unit
            state.items.isEmpty() && !state.hasAnyItems -> EmptyThings()
            state.items.isEmpty() && state.query.isNotBlank() -> EmptyState(
                title = "No matches",
                subtitle = "Nothing named \"${state.query}\". Try another search."
            )
            state.items.isEmpty() -> EmptyState(
                title = "Nothing here",
                subtitle = "No ${state.filter.label.lowercase()} things yet."
            )
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.items, key = { it.item.id }) { row ->
                    ThingRow(row = row, symbol = state.currencySymbol, onClick = { onItemClick(row.item.id) })
                }
            }
        }
    }

    if (showSortSheet) {
        SortSheet(
            selected = state.sort,
            onSelected = {
                viewModel.onSortChange(it)
                showSortSheet = false
            },
            onDismiss = { showSortSheet = false }
        )
    }
}

@Composable
private fun FilterRow(selected: ThingsFilter, onSelected: (ThingsFilter) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ThingsFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Text(
                text = filter.label,
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
                    .clickable { onSelected(filter) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun ThingRow(row: ItemWithUsageCount, symbol: String, onClick: () -> Unit) {
    val item = row.item
    val costPerUse = item.displayCostPerUseMinor(row.actualUses)
    ValeItemCard(
        title = item.name,
        price = MoneyFormat.format(item.originalPriceMinor, symbol),
        perUse = costPerUse?.let { "${MoneyFormat.formatPerUse(it, symbol)} per use" } ?: "Not used yet",
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
        progress = if (item.status == ItemStatus.BOUGHT) {
            ValeCalculations.progressFraction(row.actualUses, item.expectedUses)
        } else null,
        thumbnail = {
            Text(text = item.category?.emoji ?: "🛍️", style = MaterialTheme.typography.headlineSmall)
        },
        onClick = onClick
    )
}

@Composable
private fun EmptyThings() {
    EmptyState(
        title = "Nothing here yet.",
        subtitle = "Calculate your first item and Vale will keep the receipts. 🧾"
    )
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ValeMascot(size = 64.dp)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortSheet(
    selected: ThingsSort,
    onSelected: (ThingsSort) -> Unit,
    onDismiss: () -> Unit,
) {
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
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Sort by",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            ThingsSort.entries.forEach { sort ->
                val isSelected = sort == selected
                Text(
                    text = sort.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface,
                            MaterialTheme.shapes.medium
                        )
                        .clickable { onSelected(sort) }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }
        }
    }
}
