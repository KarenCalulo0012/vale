package com.kcalulo.vale.feature.itemdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kcalulo.vale.core.database.entity.UsageEntity
import com.kcalulo.vale.core.design.components.ValePrimaryButton
import com.kcalulo.vale.core.design.components.ValeStepperField
import com.kcalulo.vale.core.design.components.ValeTextButton
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val HISTORY_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a")

/** Quick presets for the missed-usage picker, in days-ago terms. */
private data class DaysAgoPreset(val label: String, val daysAgo: Int)

private val DAYS_AGO_PRESETS = listOf(
    DaysAgoPreset("Today", 0),
    DaysAgoPreset("Yesterday", 1),
    DaysAgoPreset("2 days ago", 2),
    DaysAgoPreset("3 days ago", 3),
)

/**
 * Usage History — chronological log, add a missed use, remove an incorrect one (spec §15).
 * A full calendar isn't required per spec, so missed usages are logged via quick day-offset
 * presets plus a stepper, rather than a native date picker.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageHistorySheet(
    usages: List<UsageEntity>,
    onAddMissedUsage: (Instant) -> Unit,
    onRemoveUsage: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var showAddPicker by remember { mutableStateOf(false) }
    var daysAgo by remember { mutableIntStateOf(0) }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Usage History",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (usages.isEmpty()) {
                Text(
                    text = "No uses logged yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(usages, key = { it.id }) { usage ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = HISTORY_DATE_FORMAT.format(usage.usedAt.atZone(ZoneId.systemDefault())),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onRemoveUsage(usage.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove this usage",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (showAddPicker) {
                MissedUsagePicker(
                    daysAgo = daysAgo,
                    onDaysAgoChange = { daysAgo = it },
                    onConfirm = {
                        val date = LocalDate.now().minusDays(daysAgo.toLong())
                        onAddMissedUsage(date.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant())
                        showAddPicker = false
                        daysAgo = 0
                    },
                    onCancel = {
                        showAddPicker = false
                        daysAgo = 0
                    }
                )
            } else {
                ValeTextButton(
                    text = "+ Add missed usage",
                    onClick = { showAddPicker = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun MissedUsagePicker(
    daysAgo: Int,
    onDaysAgoChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.large)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "When did you use it?",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DAYS_AGO_PRESETS.forEach { preset ->
                val isSelected = preset.daysAgo == daysAgo
                Text(
                    text = preset.label,
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
                        .clickable { onDaysAgoChange(preset.daysAgo) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
        ValeStepperField(
            label = "Or exactly how many days ago?",
            value = daysAgo,
            onValueChange = onDaysAgoChange,
            minValue = 0,
            maxValue = 3650
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ValePrimaryButton(
                text = "Add",
                onClick = onConfirm,
                modifier = Modifier.weight(1f)
            )
            ValeTextButton(text = "Cancel", onClick = onCancel)
        }
    }
}
