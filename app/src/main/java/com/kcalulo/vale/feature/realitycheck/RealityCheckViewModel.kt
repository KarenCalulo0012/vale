package com.kcalulo.vale.feature.realitycheck

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalulo.vale.core.common.asGivenAway
import com.kcalulo.vale.core.common.asSold
import com.kcalulo.vale.core.database.dao.ItemWithUsageCount
import com.kcalulo.vale.data.preferences.UserPreferencesRepository
import com.kcalulo.vale.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RealityCheckViewModel @Inject constructor(
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

    val currencySymbol: StateFlow<String> = preferencesRepository.preferences
        .map { it.currencySymbol }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "₱")

    /** Days since purchase — the "is this item old enough to judge" input to the verdict. */
    fun daysSincePurchase(purchaseDate: LocalDate?): Long =
        purchaseDate?.let { ChronoUnit.DAYS.between(it, LocalDate.now()) } ?: 0L

    fun sellItem(soldPriceMinor: Long) {
        val item = itemState.value?.item ?: return
        viewModelScope.launch { itemRepository.updateItem(item.asSold(soldPriceMinor)) }
    }

    fun giveAwayItem(note: String?) {
        val item = itemState.value?.item ?: return
        viewModelScope.launch { itemRepository.updateItem(item.asGivenAway(note)) }
    }
}
