package com.kcalulo.vale.feature.track

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalulo.vale.core.common.AttentionReason
import com.kcalulo.vale.core.common.ValeAttention
import com.kcalulo.vale.core.common.ValeCalculations
import com.kcalulo.vale.core.database.dao.ItemWithLastUse
import com.kcalulo.vale.core.database.dao.ItemWithUsageCount
import com.kcalulo.vale.data.preferences.UserPreferencesRepository
import com.kcalulo.vale.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The usage just logged, kept around so the undo snackbar can delete it. */
data class LoggedUsage(val usageId: Long, val itemName: String)

/**
 * The one filter control on Track (funnel icon): a manual Active/Completed status split,
 * plus the three Attention groups Home can deep-link into (spec §6) — one unified menu
 * instead of two separate filter mechanisms doing overlapping jobs.
 */
enum class TrackFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    NOT_USED_RECENTLY(AttentionReason.NOT_USED_RECENTLY.label),
    CLOSE_TO_TARGET(AttentionReason.CLOSE_TO_TARGET.label),
    READY_FOR_REALITY_CHECK(AttentionReason.READY_FOR_REALITY_CHECK.label);

    companion object {
        fun from(reason: AttentionReason): TrackFilter = when (reason) {
            AttentionReason.NOT_USED_RECENTLY -> NOT_USED_RECENTLY
            AttentionReason.CLOSE_TO_TARGET -> CLOSE_TO_TARGET
            AttentionReason.READY_FOR_REALITY_CHECK -> READY_FOR_REALITY_CHECK
        }
    }
}

/** How many bought items are still active vs. have completed their usage commitment. */
data class TrackStatusCounts(val active: Int, val completed: Int)

@HiltViewModel
class TrackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val itemRepository: ItemRepository,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    /**
     * Home's Attention "+N more" deep link (spec §6) arrives here as a nav argument and
     * seeds the same filter the funnel icon controls. Cleared/changed in-memory only —
     * the nav arg itself resets to null the moment the user leaves via the bottom nav,
     * since that always targets the plain, argument-free
     * [com.kcalulo.vale.core.navigation.ValeRoutes.TRACK] route.
     */
    private val _filter = MutableStateFlow(
        savedStateHandle.get<String>("reason")
            ?.let { runCatching { AttentionReason.valueOf(it) }.getOrNull() }
            ?.let { TrackFilter.from(it) }
            ?: TrackFilter.ALL
    )
    val filter: StateFlow<TrackFilter> = _filter.asStateFlow()

    private val boughtItems = itemRepository.observeBoughtItemsWithLastUse()

    val items: StateFlow<List<ItemWithUsageCount>> = combine(boughtItems, _filter) { all, filter ->
        all.filter { it.matches(filter) }.map { ItemWithUsageCount(it.item, it.actualUses) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    /** Always reflects everything bought, regardless of [filter] — the header's true totals. */
    val statusCounts: StateFlow<TrackStatusCounts> = boughtItems.map { all ->
        val completed = all.count { ValeCalculations.isCompleted(it.actualUses, it.item.expectedUses) }
        TrackStatusCounts(active = all.size - completed, completed = completed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrackStatusCounts(0, 0))

    val currencySymbol: StateFlow<String> = preferencesRepository.preferences
        .map { it.currencySymbol }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "₱")

    private val _lastLogged = MutableStateFlow<LoggedUsage?>(null)
    val lastLogged: StateFlow<LoggedUsage?> = _lastLogged.asStateFlow()

    /** The funnel sheet's selection. */
    fun setFilter(filter: TrackFilter) {
        _filter.value = filter
    }

    /** Clears back to [TrackFilter.ALL] — the "✕" on the filter chip. */
    fun clearFilter() {
        _filter.value = TrackFilter.ALL
    }

    /** Logs one use now (spec §14: create record, increment, recalculate, celebrate). */
    fun logUsage(row: ItemWithUsageCount) {
        viewModelScope.launch {
            val usageId = itemRepository.logUsage(row.item.id)
            _lastLogged.value = LoggedUsage(usageId, row.item.name)
        }
    }

    /** Undo deletes the just-created usage record (spec §14). */
    fun undoLastLog() {
        val logged = _lastLogged.value ?: return
        viewModelScope.launch {
            itemRepository.removeUsage(logged.usageId)
            _lastLogged.value = null
        }
    }

    fun consumeSnackbar() {
        _lastLogged.value = null
    }
}

private fun ItemWithLastUse.matches(filter: TrackFilter): Boolean = when (filter) {
    TrackFilter.ALL -> true
    TrackFilter.ACTIVE -> !ValeCalculations.isCompleted(actualUses, item.expectedUses)
    TrackFilter.COMPLETED -> ValeCalculations.isCompleted(actualUses, item.expectedUses)
    TrackFilter.NOT_USED_RECENTLY -> ValeAttention.matches(this, AttentionReason.NOT_USED_RECENTLY)
    TrackFilter.CLOSE_TO_TARGET -> ValeAttention.matches(this, AttentionReason.CLOSE_TO_TARGET)
    TrackFilter.READY_FOR_REALITY_CHECK -> ValeAttention.matches(this, AttentionReason.READY_FOR_REALITY_CHECK)
}
