package com.kcalulo.vale.feature.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kcalulo.vale.core.common.AchievementId
import com.kcalulo.vale.core.common.MoneyFormat
import com.kcalulo.vale.core.common.ProgressHighlight
import com.kcalulo.vale.core.common.ProgressMonthlySnapshot
import com.kcalulo.vale.core.common.ProgressOverview

/** Progress — monthly stats, highlights, achievements (spec §20–21). */
@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    val overview = state.overview ?: return
    val monthly = state.monthlySnapshot ?: return
    val highlights = state.highlights

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item { OverviewSection(overview) }

        item { MonthlySnapshotSection(monthly) }

        if (highlights?.bestMath != null || highlights?.questionableMath != null || highlights?.bestSkip != null) {
            item {
                HighlightsSection(
                    bestMath = highlights?.bestMath,
                    questionableMath = highlights?.questionableMath,
                    bestSkip = highlights?.bestSkip,
                    symbol = state.currencySymbol,
                )
            }
        }

        item { AchievementsSection(state.unlockedAchievements) }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) { content() }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OverviewSection(overview: ProgressOverview) {
    SectionCard(title = "Overview") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatTile("Things checked", overview.thingsChecked.toString(), Modifier.weight(1f))
                StatTile("Bought", overview.bought.toString(), Modifier.weight(1f))
                StatTile("Skipped", overview.skipped.toString(), Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatTile("Proven worth it", overview.provenWorthIt.toString(), Modifier.weight(1f))
                StatTile("Still proving", overview.stillProving.toString(), Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatTile("Sold", overview.sold.toString(), Modifier.weight(1f))
                StatTile("Given away", overview.givenAway.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MonthlySnapshotSection(monthly: ProgressMonthlySnapshot) {
    SectionCard(title = "This month") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SnapshotRow("Calculations made", monthly.calculationsMade.toString())
            SnapshotRow("Purchases made", monthly.purchasesMade.toString())
            SnapshotRow("Purchases skipped", monthly.purchasesSkipped.toString())
            SnapshotRow("Usage logs", monthly.usageLogs.toString())
            SnapshotRow("Items with math validated", monthly.mathValidated.toString())
        }
    }
}

@Composable
private fun SnapshotRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun HighlightsSection(
    bestMath: ProgressHighlight?,
    questionableMath: ProgressHighlight?,
    bestSkip: ProgressHighlight?,
    symbol: String,
) {
    SectionCard(title = "Highlights") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            bestMath?.let {
                HighlightRow("Best Math", it.itemName, "${MoneyFormat.formatPerUse(it.value, symbol)}/use")
            }
            questionableMath?.let {
                HighlightRow("Questionable Math", it.itemName, "${(it.value * 100).toInt()}% of target")
            }
            bestSkip?.let {
                HighlightRow("Best Skip", it.itemName, "${MoneyFormat.format(it.value.toLong(), symbol)} not spent")
            }
        }
    }
}

@Composable
private fun HighlightRow(label: String, itemName: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = itemName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AchievementsSection(unlocked: Set<AchievementId>) {
    SectionCard(title = "Achievements") {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AchievementId.entries.forEach { achievement ->
                AchievementRow(achievement, isUnlocked = achievement in unlocked)
            }
        }
    }
}

@Composable
private fun AchievementRow(achievement: AchievementId, isUnlocked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val alpha = if (isUnlocked) 1f else 0.35f
        Text(text = achievement.emoji, style = MaterialTheme.typography.headlineSmall)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
            Text(
                text = if (isUnlocked) achievement.shortCopy else "Not unlocked yet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
            )
        }
    }
}
