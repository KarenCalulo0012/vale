package com.kcalulo.vale.core.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kcalulo.vale.R

/**
 * The Vale mascot — your financially responsible bestie.
 * Size guide from the style board: 16, 24, 32, 48, 128.
 */
@Composable
fun ValeMascot(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    Image(
        painter = painterResource(R.drawable.ic_vale_mascot),
        contentDescription = "Vale mascot",
        modifier = modifier.size(size)
    )
}

/**
 * Mascot with a reaction bubble, e.g. "Great choice! You just avoided a bad purchase."
 * Matches the "Usage in UI" pattern from the style board.
 */
@Composable
fun ValeMascotMessage(
    message: String,
    modifier: Modifier = Modifier,
    positive: Boolean = true,
    mascotSize: Dp = 48.dp,
) {
    val bubbleColor = if (positive) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val textColor = if (positive) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    Row(
        modifier = modifier
            .background(bubbleColor, MaterialTheme.shapes.medium)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ValeMascot(size = mascotSize)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}
