package com.kcalulo.vale.feature.track

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kcalulo.vale.core.common.MoneyFormat
import com.kcalulo.vale.core.common.ValeCalculations
import com.kcalulo.vale.core.common.displayCostPerUseMinor
import com.kcalulo.vale.core.database.dao.ItemWithUsageCount
import com.kcalulo.vale.core.design.components.ValeChip
import com.kcalulo.vale.core.design.components.ValeEmptyState
import com.kcalulo.vale.core.design.components.ValeItemCard

/** Track — one-tap usage logging for bought items (spec §14). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackScreen(
    onItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrackViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val symbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val lastLogged by viewModel.lastLogged.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val statusCounts by viewModel.statusCounts.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFilterSheet by remember { mutableStateOf(false) }

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

            StatusSummaryRow(
                counts = statusCounts,
                onFilterClick = { showFilterSheet = true }
            )

            if (filter != TrackFilter.ALL) {
                FilterChipRow(filter = filter, onClear = viewModel::clearFilter)
            }

            if (items.isEmpty()) {
                ValeEmptyState(
                    title = if (filter != TrackFilter.ALL) "Nothing here for this filter" else "Nothing to track yet",
                    subtitle = if (filter != TrackFilter.ALL) {
                        "Every bought item has cleared this one — nice."
                    } else {
                        "Buy something from Calculate and it'll show up here."
                    }
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(items, key = { it.item.id }) { row ->
                        TrackRow(
                            row = row,
                            symbol = symbol,
                            onClick = { onItemClick(row.item.id) },
                            onLogUsage = { viewModel.logUsage(row) },
                            modifier = Modifier.animateItem()
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

    if (showFilterSheet) {
        FilterSheet(
            selected = filter,
            onSelected = { selected ->
                viewModel.setFilter(selected)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}

/** "● N active   ● M completed" plus the funnel that opens [FilterSheet]. */
@Composable
private fun StatusSummaryRow(counts: TrackStatusCounts, onFilterClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatusDot(
                count = counts.active,
                label = "active",
                color = MaterialTheme.colorScheme.primary,
                emphasized = true
            )
            StatusDot(
                count = counts.completed,
                label = "completed",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                emphasized = false
            )
        }
        IconButton(onClick = onFilterClick) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "Filter",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusDot(count: Int, label: String, color: Color, emphasized: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = if (emphasized) FontWeight.SemiBold else null
        )
    }
}

/** Shows the active funnel selection, with a clear way back to [TrackFilter.ALL]. */
@Composable
private fun FilterChipRow(filter: TrackFilter, onClear: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ValeChip(
            text = "Showing: ${filter.label}",
            contentColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.primary
        )
        IconButton(onClick = onClear) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear filter",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    selected: TrackFilter,
    onSelected: (TrackFilter) -> Unit,
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
                text = "Filter Track",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            TrackFilter.entries.forEach { option ->
                val isSelected = option == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.shapes.medium
                        )
                        .selectable(
                            selected = isSelected,
                            role = Role.RadioButton,
                            onClick = { onSelected(option) }
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackRow(
    row: ItemWithUsageCount,
    symbol: String,
    onClick: () -> Unit,
    onLogUsage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = row.item
    val completed = ValeCalculations.isCompleted(row.actualUses, item.expectedUses)
    val costPerUse = item.displayCostPerUseMinor(row.actualUses)
    ValeItemCard(
        modifier = modifier,
        title = item.name,
        price = MoneyFormat.format(item.originalPriceMinor, symbol),
        perUse = costPerUse?.let { "${MoneyFormat.formatPerUse(it, symbol)} per use" } ?: "Not used yet",
        usesText = "${row.actualUses} / ${item.expectedUses} uses",
        progress = ValeCalculations.progressFraction(row.actualUses, item.expectedUses),
        progressColor = if (completed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
        priceStrikethrough = completed,
        thumbnail = {
            Text(text = item.category?.emoji ?: "🛍️", style = MaterialTheme.typography.headlineSmall)
        },
        onClick = onClick,
        trailingAction = {
            AnimatedContent(
                targetState = completed,
                transitionSpec = {
                    (scaleIn(initialScale = 0.6f) + fadeIn()) togetherWith (scaleOut(targetScale = 0.6f) + fadeOut())
                },
                label = "trackAction"
            ) { isCompleted ->
                if (isCompleted) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    Surface(
                        onClick = onLogUsage,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "+ Used it",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}
