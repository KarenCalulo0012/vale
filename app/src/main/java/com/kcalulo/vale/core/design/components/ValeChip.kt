package com.kcalulo.vale.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kcalulo.vale.core.design.theme.StatusBought
import com.kcalulo.vale.core.design.theme.StatusBoughtBg
import com.kcalulo.vale.core.design.theme.StatusConsidering
import com.kcalulo.vale.core.design.theme.StatusConsideringBg
import com.kcalulo.vale.core.design.theme.StatusNotWorthIt
import com.kcalulo.vale.core.design.theme.StatusNotWorthItBg
import com.kcalulo.vale.core.database.entity.ItemStatus
import com.kcalulo.vale.core.design.theme.StatusOnTrack
import com.kcalulo.vale.core.design.theme.StatusOnTrackBg
import com.kcalulo.vale.core.design.theme.StatusSkipped
import com.kcalulo.vale.core.design.theme.StatusSkippedBg
import com.kcalulo.vale.core.design.theme.WarmYellow
import com.kcalulo.vale.core.design.theme.YellowSoft
import com.kcalulo.vale.core.design.theme.ValePink
import com.kcalulo.vale.core.design.theme.PinkSoft

/** Item / decision statuses from the style board. */
enum class ValeStatus(val label: String, val contentColor: Color, val containerColor: Color) {
    Bought("Bought", StatusBought, StatusBoughtBg),
    Considering("Considering", StatusConsidering, StatusConsideringBg),
    Skipped("Skipped", StatusSkipped, StatusSkippedBg),
    OnTrack("On track", StatusOnTrack, StatusOnTrackBg),
    StillProvingIt("Still proving it", StatusConsidering, StatusConsideringBg),
    NotWorthIt("Not worth it", StatusNotWorthIt, StatusNotWorthItBg),
    Sold("Sold", WarmYellow, YellowSoft),
    GivenAway("Given away", ValePink, PinkSoft),
}

/**
 * Maps a persisted [ItemStatus] to its chip, if it has one — used consistently across
 * Home, Things, and Item Details so a status can't display differently in different places.
 */
fun ItemStatus.toValeStatus(): ValeStatus? = when (this) {
    ItemStatus.BOUGHT -> ValeStatus.Bought
    ItemStatus.CONSIDERING -> ValeStatus.Considering
    ItemStatus.SKIPPED -> ValeStatus.Skipped
    ItemStatus.SOLD -> ValeStatus.Sold
    ItemStatus.GIVEN_AWAY -> ValeStatus.GivenAway
    ItemStatus.COMPLETED, ItemStatus.ARCHIVED -> null
}

/** Small pill chip / badge, e.g. "Bought", "On track", "Not worth it". */
@Composable
fun ValeStatusChip(
    status: ValeStatus,
    modifier: Modifier = Modifier,
) {
    ValeChip(
        text = status.label,
        contentColor = status.contentColor,
        containerColor = status.containerColor,
        modifier = modifier
    )
}

@Composable
fun ValeChip(
    text: String,
    contentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = contentColor,
        modifier = modifier
            .background(containerColor, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}
