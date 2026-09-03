package com.kcalulo.vale.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kcalulo.vale.ui.theme.StatusBought
import com.kcalulo.vale.ui.theme.StatusBoughtBg
import com.kcalulo.vale.ui.theme.StatusConsidering
import com.kcalulo.vale.ui.theme.StatusConsideringBg
import com.kcalulo.vale.ui.theme.StatusNotWorthIt
import com.kcalulo.vale.ui.theme.StatusNotWorthItBg
import com.kcalulo.vale.ui.theme.StatusOnTrack
import com.kcalulo.vale.ui.theme.StatusOnTrackBg
import com.kcalulo.vale.ui.theme.StatusSkipped
import com.kcalulo.vale.ui.theme.StatusSkippedBg

/** Item / decision statuses from the style board. */
enum class ValeStatus(val label: String, val contentColor: Color, val containerColor: Color) {
    Bought("Bought", StatusBought, StatusBoughtBg),
    Considering("Considering", StatusConsidering, StatusConsideringBg),
    Skipped("Skipped", StatusSkipped, StatusSkippedBg),
    OnTrack("On track", StatusOnTrack, StatusOnTrackBg),
    StillProvingIt("Still proving it", StatusConsidering, StatusConsideringBg),
    NotWorthIt("Not worth it", StatusNotWorthIt, StatusNotWorthItBg),
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
