package com.kcalulo.vale.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

/**
 * Item card, e.g. "Canvas Tote Bag — ₱1,200, ₱40.00 per use, 12 / 30 uses".
 * [thumbnail] slot takes an image or emoji placeholder; [status] renders the badge;
 * [trailingAction] renders a compact slot in the header row, e.g. Track's circular
 * "+ used it" button; [progress] renders a usage bar (0..1) when non-null, tinted
 * [progressColor]; [actionButton] renders a full-width slot below everything instead,
 * for callers that need more room than [trailingAction] offers.
 */
@Composable
fun ValeItemCard(
    title: String,
    price: String,
    perUse: String,
    usesText: String,
    modifier: Modifier = Modifier,
    status: ValeStatus? = null,
    thumbnail: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    progress: Float? = null,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    priceStrikethrough: Boolean = false,
    trailingAction: (@Composable () -> Unit)? = null,
    actionButton: (@Composable () -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick ?: {}
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.shapes.medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    thumbnail?.invoke()
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = price,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (priceStrikethrough) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            textDecoration = if (priceStrikethrough) TextDecoration.LineThrough else null
                        )
                        Text(
                            text = perUse,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = usesText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                status?.let { ValeStatusChip(it) }
                trailingAction?.invoke()
            }
            if (progress != null) {
                ValeProgressBar(
                    progress = progress,
                    color = progressColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            actionButton?.invoke()
        }
    }
}
