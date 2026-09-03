package com.kcalulo.vale.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kcalulo.vale.core.common.AttentionItem
import com.kcalulo.vale.core.common.AttentionReason
import com.kcalulo.vale.core.common.HomeAttention
import com.kcalulo.vale.core.common.MoneyFormat
import com.kcalulo.vale.core.common.displayCostPerUseMinor
import com.kcalulo.vale.core.common.summaryLine
import com.kcalulo.vale.core.design.components.toValeStatus
import com.kcalulo.vale.core.database.dao.ItemWithUsageCount
import com.kcalulo.vale.core.database.entity.ItemStatus
import com.kcalulo.vale.core.design.components.ValeChip
import com.kcalulo.vale.core.design.components.ValeEmptyState
import com.kcalulo.vale.core.design.components.ValeItemCard
import com.kcalulo.vale.core.design.components.ValeMascot
import com.kcalulo.vale.core.design.components.ValePrimaryButton
import com.kcalulo.vale.core.design.components.ValeStatus

/** Home — show what needs attention, make the next action obvious (spec §6). */
@Composable
fun HomeScreen(
    onCalculateClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onAttentionSeeAll: (AttentionReason) -> Unit,
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

        if (state.snapshot.checked > 0) {
            MonthlySnapshotRow(snapshot = state.snapshot)
        }

        if (!state.attention.isEmpty) {
            AttentionSection(
                attention = state.attention,
                onItemClick = onItemClick,
                onSeeAll = onAttentionSeeAll
            )
        }

        if (state.recentItems.isEmpty() && !state.isLoading) {
            ValeEmptyState(
                title = "Nothing here yet.",
                subtitle = "Calculate your first item and Vale will keep the receipts. 🧾"
            )
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

/**
 * What needs a look right now (spec §6): items going stale, items about to prove
 * themselves, items ready for their Reality Check. Deliberately terse — one line per
 * item, no extra chrome — so it doesn't turn Home into a dashboard. A group past
 * [ATTENTION_GROUP_VISIBLE_LIMIT] hands off the rest to Track instead of growing in
 * place, which is the actual screen for doing something about a bought item.
 */
@Composable
private fun AttentionSection(
    attention: HomeAttention,
    onItemClick: (Long) -> Unit,
    onSeeAll: (AttentionReason) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Needs a look",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        AttentionGroup(AttentionReason.NOT_USED_RECENTLY, attention.notUsedRecently, onItemClick, onSeeAll)
        AttentionGroup(AttentionReason.CLOSE_TO_TARGET, attention.closeToTarget, onItemClick, onSeeAll)
        AttentionGroup(AttentionReason.READY_FOR_REALITY_CHECK, attention.readyForRealityCheck, onItemClick, onSeeAll)
    }
}

/** Groups past this size truncate, with "+N more" handing off to Track's filtered list. */
private const val ATTENTION_GROUP_VISIBLE_LIMIT = 2

@Composable
private fun AttentionGroup(
    reason: AttentionReason,
    items: List<AttentionItem>,
    onItemClick: (Long) -> Unit,
    onSeeAll: (AttentionReason) -> Unit,
) {
    if (items.isEmpty()) return
    val overflowCount = items.size - ATTENTION_GROUP_VISIBLE_LIMIT
    val visibleItems = if (overflowCount <= 0) items else items.take(ATTENTION_GROUP_VISIBLE_LIMIT)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = reason.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            // A count, not a warning — VALE doesn't treat "needs a look" as an alarm,
            // so this stays in-brand purple rather than red. Tapping it also jumps to
            // Track, same as "+N more" below.
            if (overflowCount > 0) {
                ValeChip(
                    text = items.size.toString(),
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { onSeeAll(reason) }
                        .semantics { role = Role.Button }
                )
            }
        }
        visibleItems.forEach { attentionItem ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                    .clickable { onItemClick(attentionItem.itemId) }
                    .semantics(mergeDescendants = true) {
                        role = Role.Button
                        contentDescription = "${attentionItem.itemName}, ${attentionItem.detail}"
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = attentionItem.itemName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = attentionItem.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (overflowCount > 0) {
            Text(
                text = "+$overflowCount more",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onSeeAll(reason) }
                    .semantics { role = Role.Button }
                    .padding(vertical = 4.dp)
            )
        }
    }
}

/** Lightweight snapshot — spec §6 explicitly warns this must not look like a banking dashboard. */
@Composable
private fun MonthlySnapshotRow(snapshot: HomeSnapshot) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SnapshotStat(value = snapshot.checked, label = "Checked")
        SnapshotStat(value = snapshot.bought, label = "Bought")
        SnapshotStat(value = snapshot.skipped, label = "Skipped")
    }
}

@Composable
private fun SnapshotStat(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
        usesText = item.summaryLine(row.actualUses, symbol),
        status = item.status.toValeStatus(),
        thumbnail = {
            Text(
                text = item.category?.emoji ?: "🛍️",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        onClick = onClick
    )
}
