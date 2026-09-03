package com.kcalulo.vale.feature.track

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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The usage just logged, kept around so the undo snackbar can delete it. */
data class LoggedUsage(val usageId: Long, val itemName: String)

@HiltViewModel
class TrackViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val items: StateFlow<List<ItemWithUsageCount>> =
        itemRepository.observeItemsByStatus(ItemStatus.BOUGHT).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val currencySymbol: StateFlow<String> = preferencesRepository.preferences
        .map { it.currencySymbol }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "₱")

    private val _lastLogged = MutableStateFlow<LoggedUsage?>(null)
    val lastLogged: StateFlow<LoggedUsage?> = _lastLogged.asStateFlow()

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
