package com.kcalulo.vale.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** One quick-preset option for [ValeDaysAgoPicker], e.g. "Yesterday" -> 1 day ago. */
data class DaysAgoPreset(val label: String, val daysAgo: Int)

val DEFAULT_DAYS_AGO_PRESETS = listOf(
    DaysAgoPreset("Today", 0),
    DaysAgoPreset("Yesterday", 1),
    DaysAgoPreset("2 days ago", 2),
    DaysAgoPreset("3 days ago", 3),
)

/**
 * Quick day-offset picker: presets plus an exact-days stepper. A full calendar isn't
 * required per spec for either missed-usage logging (§15) or picking a purchase date
 * (§9) — both just need "how many days ago," so this one component serves both.
 */
@Composable
fun ValeDaysAgoPicker(
    title: String,
    daysAgo: Int,
    onDaysAgoChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "Confirm",
    stepperLabel: String = "Or exactly how many days ago?",
    presets: List<DaysAgoPreset> = DEFAULT_DAYS_AGO_PRESETS,
    maxDaysAgo: Int = 3650,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.large)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { preset ->
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
            label = stepperLabel,
            value = daysAgo,
            onValueChange = onDaysAgoChange,
            minValue = 0,
            maxValue = maxDaysAgo
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ValePrimaryButton(
                text = confirmText,
                onClick = onConfirm,
                modifier = Modifier.weight(1f)
            )
            ValeTextButton(text = "Cancel", onClick = onCancel)
        }
    }
}
