package com.kcalulo.vale.feature.things

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalulo.vale.core.database.dao.ItemWithUsageCount
import com.kcalulo.vale.core.database.entity.ItemStatus
import com.kcalulo.vale.data.preferences.UserPreferencesRepository
import com.kcalulo.vale.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Filters exposed in Things (spec §12). Archived sits at the end since it's a reversible
 * "tucked away" bucket rather than a day-to-day status, per the spec's "keep the rest under
 * More" guidance — it's still a plain chip here since the row already scrolls horizontally.
 */
enum class ThingsFilter(val label: String) {
    ALL("All"),
    BOUGHT("Bought"),
    CONSIDERING("Considering"),
    SKIPPED("Skipped"),
    ARCHIVED("Archived");

    fun toItemStatus(): ItemStatus? = when (this) {
        ALL, ARCHIVED -> null
        BOUGHT -> ItemStatus.BOUGHT
        CONSIDERING -> ItemStatus.CONSIDERING
        SKIPPED -> ItemStatus.SKIPPED
    }
}

enum class ThingsSort(val label: String) {
    RECENTLY_ADDED("Recently added"),
    OLDEST("Oldest"),
    MOST_USED("Most used"),
    LEAST_USED("Least used"),
    HIGHEST_PRICE("Highest price"),
}

data class ThingsUiState(
    val items: List<ItemWithUsageCount> = emptyList(),
    val hasAnyItems: Boolean = true,
    val filter: ThingsFilter = ThingsFilter.ALL,
    val sort: ThingsSort = ThingsSort.RECENTLY_ADDED,
    val query: String = "",
    val currencySymbol: String = "₱",
    val isLoading: Boolean = true,
)

@HiltViewModel
class ThingsViewModel @Inject constructor(
    itemRepository: ItemRepository,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow(ThingsFilter.ALL)
    private val _sort = MutableStateFlow(ThingsSort.RECENTLY_ADDED)
    private val _query = MutableStateFlow("")

    /**
     * Archived lives in a separate source (spec §12) — every other filter reads active items.
     * [Source.hasAnyItems] reflects the whole app (active + archived), not just this filter's
     * bucket, so switching to an empty filter shows "nothing matches" instead of the
     * first-time "nothing here yet" empty state.
     */
    private data class Source(val items: List<ItemWithUsageCount>, val hasAnyItems: Boolean)

    private val sourceByFilter = combine(
        itemRepository.observeItems(),
        itemRepository.observeArchivedItems(),
        _filter,
    ) { active, archived, filter ->
        Source(
            items = if (filter == ThingsFilter.ARCHIVED) archived else active,
            hasAnyItems = active.isNotEmpty() || archived.isNotEmpty(),
        )
    }

    val uiState: StateFlow<ThingsUiState> = combine(
        sourceByFilter,
        _filter,
        _sort,
        _query,
        preferencesRepository.preferences,
    ) { source, filter, sort, query, prefs ->
        val targetStatus = filter.toItemStatus()
        var visible = if (targetStatus == null) source.items else source.items.filter { it.item.status == targetStatus }
        if (query.isNotBlank()) {
            visible = visible.filter { it.item.name.contains(query, ignoreCase = true) }
        }
        visible = when (sort) {
            ThingsSort.RECENTLY_ADDED -> visible.sortedByDescending { it.item.createdAt }
            ThingsSort.OLDEST -> visible.sortedBy { it.item.createdAt }
            ThingsSort.MOST_USED -> visible.sortedByDescending { it.actualUses }
            ThingsSort.LEAST_USED -> visible.sortedBy { it.actualUses }
            ThingsSort.HIGHEST_PRICE -> visible.sortedByDescending { it.item.originalPriceMinor }
        }
        ThingsUiState(
            items = visible,
            hasAnyItems = source.hasAnyItems,
            filter = filter,
            sort = sort,
            query = query,
            currencySymbol = prefs.currencySymbol,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThingsUiState(),
    )

    fun onFilterChange(filter: ThingsFilter) {
        _filter.value = filter
    }

    fun onSortChange(sort: ThingsSort) {
        _sort.value = sort
    }

    fun onQueryChange(query: String) {
        _query.value = query
    }
}
