package com.kcalulo.vale.feature.itemdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalulo.vale.core.common.ValeCalculations
import com.kcalulo.vale.core.common.asBought
import com.kcalulo.vale.core.common.asGivenAway
import com.kcalulo.vale.core.common.asSkipped
import com.kcalulo.vale.core.common.asSold
import com.kcalulo.vale.core.database.dao.ItemWithUsageCount
import com.kcalulo.vale.core.database.entity.ItemCategory
import com.kcalulo.vale.core.database.entity.ItemStatus
import com.kcalulo.vale.core.database.entity.SkipReason
import com.kcalulo.vale.core.database.entity.UsageEntity
import com.kcalulo.vale.data.preferences.UserPreferencesRepository
import com.kcalulo.vale.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LoggedUsage(val usageId: Long)

@HiltViewModel
class ItemDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val itemRepository: ItemRepository,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val itemId: Long = checkNotNull(savedStateHandle["itemId"])

    val itemState: StateFlow<ItemWithUsageCount?> = itemRepository.observeItem(itemId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    val usages: StateFlow<List<UsageEntity>> = itemRepository.observeUsages(itemId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val currencySymbol: StateFlow<String> = preferencesRepository.preferences
        .map { it.currencySymbol }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "₱")

    private val _lastLogged = MutableStateFlow<LoggedUsage?>(null)
    val lastLogged: StateFlow<LoggedUsage?> = _lastLogged.asStateFlow()

    /** Primary "+ I used it" action (spec §13/§14). */
    fun logUsage() {
        viewModelScope.launch {
            val usageId = itemRepository.logUsage(itemId)
            _lastLogged.value = LoggedUsage(usageId)
        }
    }

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

    /** Usage History sheet: log a use at a chosen past moment. */
    fun addMissedUsage(at: Instant) {
        viewModelScope.launch { itemRepository.logUsage(itemId, at) }
    }

    /** Usage History sheet: remove an incorrect entry. */
    fun removeUsage(usageId: Long) {
        viewModelScope.launch { itemRepository.removeUsage(usageId) }
    }

    /** Considering → Bought (spec §10: reopen a Considering item and finish deciding). */
    fun markAsBought(purchaseDate: LocalDate = LocalDate.now()) {
        val item = itemState.value?.item ?: return
        viewModelScope.launch { itemRepository.updateItem(item.asBought(purchaseDate)) }
    }

    /** Considering → Skipped (spec §10). */
    fun markAsSkipped(reason: SkipReason?) {
        val item = itemState.value?.item ?: return
        viewModelScope.launch { itemRepository.updateItem(item.asSkipped(reason)) }
    }

    /**
     * Edits a Considering item's name/category/price/expected uses before deciding
     * (spec §10). Recomputes `targetCostPerUseMinor` from the new numbers — a
     * Considering item hasn't been bought yet, so there's no frozen promise to protect
     * (product decision; a Bought item's target stays frozen once set).
     */
    fun updateConsideringDetails(
        name: String,
        category: ItemCategory?,
        priceMinor: Long,
        expectedUses: Int,
    ) {
        val item = itemState.value?.item ?: return
        if (item.status != ItemStatus.CONSIDERING) return
        viewModelScope.launch {
            itemRepository.updateItem(
                item.copy(
                    name = name,
                    category = category,
                    originalPriceMinor = priceMinor,
                    expectedUses = expectedUses,
                    targetCostPerUseMinor = ValeCalculations.targetCostPerUse(priceMinor, expectedUses),
                )
            )
        }
    }

    /** Sell flow (spec §18) — stops normal usage tracking, preserves full history. */
    fun sellItem(soldPriceMinor: Long) {
        val item = itemState.value?.item ?: return
        viewModelScope.launch { itemRepository.updateItem(item.asSold(soldPriceMinor)) }
    }

    /** Give Away flow (spec §19) — never treated as profit or savings, just a closed lifecycle. */
    fun giveAwayItem(note: String?) {
        val item = itemState.value?.item ?: return
        viewModelScope.launch { itemRepository.updateItem(item.asGivenAway(note)) }
    }

    fun archiveItem() {
        val item = itemState.value?.item ?: return
        viewModelScope.launch { itemRepository.updateItem(item.copy(isArchived = true)) }
    }

    /** Undoes an archive — the item reappears in its normal lists (spec: reversible action). */
    fun unarchiveItem() {
        val item = itemState.value?.item ?: return
        viewModelScope.launch { itemRepository.updateItem(item.copy(isArchived = false)) }
    }

    fun deleteItem() {
        val item = itemState.value?.item ?: return
        viewModelScope.launch { itemRepository.deleteItem(item) }
    }
}
