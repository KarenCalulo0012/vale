package com.kcalulo.vale.feature.track

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalulo.vale.core.common.AttentionReason
import com.kcalulo.vale.core.common.ValeAttention
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

@HiltViewModel
class TrackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val itemRepository: ItemRepository,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    /**
     * Home's Attention "+N more" deep link (spec §6) arrives here as a nav argument.
     * Cleared in-memory only — the nav arg itself resets to null the moment the user
     * leaves via the bottom nav, since that always targets the plain, argument-free
     * [com.kcalulo.vale.core.navigation.ValeRoutes.TRACK] route.
     */
    private val _activeFilter = MutableStateFlow(
        savedStateHandle.get<String>("reason")?.let { runCatching { AttentionReason.valueOf(it) }.getOrNull() }
    )
    val activeFilter: StateFlow<AttentionReason?> = _activeFilter.asStateFlow()

    val items: StateFlow<List<ItemWithUsageCount>> = combine(
        itemRepository.observeBoughtItemsWithLastUse(),
        _activeFilter,
    ) { boughtItems, filter ->
        val filtered = if (filter == null) boughtItems else boughtItems.filter { ValeAttention.matches(it, filter) }
        filtered.map { ItemWithUsageCount(it.item, it.actualUses) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val currencySymbol: StateFlow<String> = preferencesRepository.preferences
        .map { it.currencySymbol }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "₱")

    private val _lastLogged = MutableStateFlow<LoggedUsage?>(null)
    val lastLogged: StateFlow<LoggedUsage?> = _lastLogged.asStateFlow()

    /** Clears the Attention filter without leaving Track — the "✕" on the filter chip. */
    fun clearFilter() {
        _activeFilter.value = null
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
