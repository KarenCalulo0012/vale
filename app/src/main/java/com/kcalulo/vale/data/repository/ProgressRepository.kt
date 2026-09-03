package com.kcalulo.vale.data.repository

import com.kcalulo.vale.core.common.ProgressHighlights
import com.kcalulo.vale.core.common.ProgressMonthlySnapshot
import com.kcalulo.vale.core.common.ProgressOverview
import com.kcalulo.vale.core.common.ValeProgressSummary
import com.kcalulo.vale.core.database.dao.ItemDao
import com.kcalulo.vale.core.database.dao.UsageDao
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class ProgressStats(
    val overview: ProgressOverview,
    val monthlySnapshot: ProgressMonthlySnapshot,
    val highlights: ProgressHighlights,
)

/** Read model for the Progress screen (spec §20) — pure aggregation, no writes of its own. */
interface ProgressRepository {
    fun observeStats(): Flow<ProgressStats>
}

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val itemDao: ItemDao,
    private val usageDao: UsageDao,
) : ProgressRepository {

    override fun observeStats(): Flow<ProgressStats> {
        val startOfMonth = LocalDate.now()
            .withDayOfMonth(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()

        return combine(
            itemDao.observeAllItems(),
            usageDao.observeUsageCountSince(startOfMonth),
        ) { items, usageLogsThisMonth ->
            ProgressStats(
                overview = ValeProgressSummary.overview(items),
                monthlySnapshot = ValeProgressSummary.monthlySnapshot(items, usageLogsThisMonth),
                highlights = ValeProgressSummary.highlights(items),
            )
        }
    }
}
