package com.kcalulo.vale.feature.itemdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kcalulo.vale.core.common.MoneyFormat
import com.kcalulo.vale.core.common.ValeCalculations
import com.kcalulo.vale.core.common.displayCostPerUseMinor
import com.kcalulo.vale.core.database.entity.ItemStatus
import com.kcalulo.vale.core.design.components.ValeDaysAgoPicker
import com.kcalulo.vale.core.design.components.ValeDialog
import com.kcalulo.vale.core.design.components.ValeMascot
import com.kcalulo.vale.core.design.components.ValePlaceholderScreen
import com.kcalulo.vale.core.design.components.ValePrimaryButton
import com.kcalulo.vale.core.design.components.ValeProgressBar
import com.kcalulo.vale.core.design.components.ValeSecondaryButton
import com.kcalulo.vale.core.design.components.ValeSkipReasonSheet
import com.kcalulo.vale.core.design.components.ValeStatusChip
import com.kcalulo.vale.core.design.components.toValeStatus
import com.kcalulo.vale.feature.realitycheck.GiveAwaySheet
import com.kcalulo.vale.feature.realitycheck.SellSheet

/** Item Details — the control center for one item (spec §13). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    onBack: () -> Unit,
    onRealityCheck: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemDetailsViewModel = hiltViewModel(),
) {
    val row by viewModel.itemState.collectAsStateWithLifecycle()
    val usages by viewModel.usages.collectAsStateWithLifecycle()
    val symbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val lastLogged by viewModel.lastLogged.collectAsStateWithLifecycle()

    var showMoreSheet by remember { mutableStateOf(false) }
    var showUsageHistory by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showArchiveConfirm by remember { mutableStateOf(false) }
    var showBuyDatePicker by remember { mutableStateOf(false) }
    var buyDaysAgo by remember { mutableIntStateOf(0) }
    var showSkipSheet by remember { mutableStateOf(false) }
    var showSellSheet by remember { mutableStateOf(false) }
    var showGiveAwaySheet by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(lastLogged) {
        if (lastLogged == null) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "Usage added",
            actionLabel = "UNDO",
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) viewModel.undoLastLog() else viewModel.consumeSnackbar()
    }

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
    val costPerUse = item.displayCostPerUseMinor(actualUses)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Item Details",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showMoreSheet = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.extraLarge)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = item.category?.emoji ?: "🛍️", style = MaterialTheme.typography.displayMedium)
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                item.status.toValeStatus()?.let { ValeStatusChip(it) }
                if (item.isArchived) {
                    Text(
                        text = "Archived — hidden from your active lists",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (item.status == ItemStatus.BOUGHT && actualUses == 0) {
                    Text(
                        text = "No uses logged yet — tap below to start proving the math!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Text(
                        text = costPerUse?.let { "${MoneyFormat.formatPerUse(it, symbol)} per use" } ?: "—",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (item.status) {
                            ItemStatus.BOUGHT -> "current cost/use"
                            ItemStatus.SOLD -> "final cost/use"
                            else -> "target cost/use"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (item.status == ItemStatus.BOUGHT) {
                    ValeProgressBar(
                        progress = ValeCalculations.progressFraction(actualUses, item.expectedUses),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "$actualUses / ${item.expectedUses} uses · " +
                            "${ValeCalculations.remainingUses(item.expectedUses, actualUses)} to go",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DetailStatsGrid(item = item, symbol = symbol)

            if (item.status == ItemStatus.BOUGHT) {
                ValePrimaryButton(
                    text = "+ I used it",
                    onClick = viewModel::logUsage,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ValeSecondaryButton(
                        text = "Usage History",
                        onClick = { showUsageHistory = true },
                        modifier = Modifier.weight(1f)
                    )
                    ValeSecondaryButton(
                        text = "Reality Check",
                        onClick = { onRealityCheck(item.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (item.status == ItemStatus.CONSIDERING) {
                // spec §10 — reopen a Considering item and finish deciding.
                ValePrimaryButton(
                    text = "Yes, I'm buying it",
                    onClick = { showBuyDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                )
                ValeSecondaryButton(
                    text = "Skip it",
                    onClick = { showSkipSheet = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    if (showMoreSheet) {
        MoreOptionsSheet(
            // Sell/Give Away only make sense for something you actually own and are tracking
            // (spec §18/§19) — a Considering or Skipped item hasn't entered that lifecycle yet.
            showSellGiveAway = item.status == ItemStatus.BOUGHT,
            // Editing price/expected uses before deciding (spec §10) only makes sense
            // while an item is still Considering.
            showEdit = item.status == ItemStatus.CONSIDERING,
            isArchived = item.isArchived,
            onEdit = { showMoreSheet = false; showEditSheet = true },
            onSell = { showMoreSheet = false; showSellSheet = true },
            onGiveAway = { showMoreSheet = false; showGiveAwaySheet = true },
            onArchive = { showMoreSheet = false; showArchiveConfirm = true },
            onUnarchive = { showMoreSheet = false; viewModel.unarchiveItem() },
            onDelete = { showMoreSheet = false; showDeleteConfirm = true },
            onDismiss = { showMoreSheet = false }
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
            },
            onDismiss = { showSellSheet = false }
        )
    }

    if (showGiveAwaySheet) {
        GiveAwaySheet(
            onConfirm = { note ->
                viewModel.giveAwayItem(note)
                showGiveAwaySheet = false
            },
            onDismiss = { showGiveAwaySheet = false }
        )
    }

    if (showUsageHistory) {
        UsageHistorySheet(
            usages = usages,
            onAddMissedUsage = viewModel::addMissedUsage,
            onRemoveUsage = viewModel::removeUsage,
            onDismiss = { showUsageHistory = false }
        )
    }

    if (showDeleteConfirm) {
        ValeDialog(
            title = "Delete this item?",
            message = "This can't be undone — all usage history goes with it.",
            confirmText = "Delete",
            onConfirm = {
                viewModel.deleteItem()
                showDeleteConfirm = false
                onBack()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    if (showBuyDatePicker) {
        ModalBottomSheet(
            onDismissRequest = { showBuyDatePicker = false; buyDaysAgo = 0 },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                ValeDaysAgoPicker(
                    title = "When did you buy it? We'll start counting from there.",
                    daysAgo = buyDaysAgo,
                    onDaysAgoChange = { buyDaysAgo = it },
                    onConfirm = {
                        val date = java.time.LocalDate.now().minusDays(buyDaysAgo.toLong())
                        showBuyDatePicker = false
                        buyDaysAgo = 0
                        viewModel.markAsBought(date)
                    },
                    onCancel = { showBuyDatePicker = false; buyDaysAgo = 0 },
                    confirmText = "Yes, bought it!"
                )
            }
        }
    }

    if (showEditSheet) {
        EditConsideringSheet(
            item = item,
            symbol = symbol,
            onSave = { name, category, priceMinor, expectedUses ->
                viewModel.updateConsideringDetails(name, category, priceMinor, expectedUses)
                showEditSheet = false
            },
            onDismiss = { showEditSheet = false }
        )
    }

    if (showSkipSheet) {
        ValeSkipReasonSheet(
            onReasonSelected = { reason ->
                showSkipSheet = false
                viewModel.markAsSkipped(reason)
            },
            onDismiss = { showSkipSheet = false }
        )
    }

    if (showArchiveConfirm) {
        ValeDialog(
            title = "Archive this item?",
            message = "It'll be tucked away from your active lists.",
            confirmText = "Archive",
            onConfirm = {
                viewModel.archiveItem()
                showArchiveConfirm = false
                onBack()
            },
            onDismiss = { showArchiveConfirm = false }
        )
    }
}

@Composable
private fun DetailStatsGrid(item: com.kcalulo.vale.core.database.entity.ItemEntity, symbol: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatRow("Price", MoneyFormat.format(item.originalPriceMinor, symbol))
        item.category?.let { StatRow("Category", "${it.emoji} ${it.label}") }
        item.purchaseDate?.let { StatRow("Purchase date", it.toString()) }
        StatRow("Expected uses", item.expectedUses.toString())
        StatRow("Target cost/use", MoneyFormat.formatPerUse(item.targetCostPerUseMinor, symbol))
        item.skipReason?.let { StatRow("Why skipped", it.label) }
        item.soldPriceMinor?.let { StatRow("Sold for", MoneyFormat.format(it, symbol)) }
        item.soldDate?.let { StatRow("Sold date", it.toString()) }
        item.givenAwayDate?.let { StatRow("Given away on", it.toString()) }
        item.note?.let { StatRow("Note", it) }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreOptionsSheet(
    showSellGiveAway: Boolean,
    showEdit: Boolean,
    isArchived: Boolean,
    onEdit: () -> Unit,
    onSell: () -> Unit,
    onGiveAway: () -> Unit,
    onArchive: () -> Unit,
    onUnarchive: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showEdit) {
                MoreOptionRow(label = "Edit", onClick = onEdit)
            }
            if (showSellGiveAway) {
                MoreOptionRow(label = "Sell", onClick = onSell)
                MoreOptionRow(label = "Give Away", onClick = onGiveAway)
            }
            if (isArchived) {
                MoreOptionRow(label = "Unarchive", onClick = onUnarchive)
            } else {
                MoreOptionRow(label = "Archive", onClick = onArchive)
            }
            MoreOptionRow(
                label = "Delete",
                onClick = onDelete,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun MoreOptionRow(
    label: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    tint: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (tint == androidx.compose.ui.graphics.Color.Unspecified) {
                MaterialTheme.colorScheme.onSurface
            } else tint,
            modifier = Modifier.weight(1f)
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
