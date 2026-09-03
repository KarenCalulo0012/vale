package com.kcalulo.vale.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalulo.vale.core.common.HomeAttention
import com.kcalulo.vale.core.common.ValeAttention
import com.kcalulo.vale.core.database.dao.ItemWithUsageCount
import com.kcalulo.vale.core.database.entity.ItemStatus
import com.kcalulo.vale.data.preferences.UserPreferencesRepository
import com.kcalulo.vale.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeSnapshot(
    val checked: Int = 0,
    val bought: Int = 0,
    val skipped: Int = 0,
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val recentItems: List<ItemWithUsageCount> = emptyList(),
    val currencySymbol: String = "₱",
    val snapshot: HomeSnapshot = HomeSnapshot(),
    val attention: HomeAttention = HomeAttention(emptyList(), emptyList(), emptyList()),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    itemRepository: ItemRepository,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val statusCounts = combine(
        itemRepository.observeCountByStatus(ItemStatus.BOUGHT),
        itemRepository.observeCountByStatus(ItemStatus.SKIPPED),
        itemRepository.observeCountByStatus(ItemStatus.CONSIDERING),
    ) { bought, skipped, considering ->
        HomeSnapshot(
            checked = bought + skipped + considering,
            bought = bought,
            skipped = skipped,
        )
    }

    private val attention = itemRepository.observeBoughtItemsWithLastUse().map { ValeAttention.summarize(it) }

    val uiState: StateFlow<HomeUiState> = combine(
        itemRepository.observeRecentItems(limit = 5),
        preferencesRepository.preferences,
        statusCounts,
        attention,
    ) { recent, prefs, snapshot, attention ->
        HomeUiState(
            isLoading = false,
            recentItems = recent,
            currencySymbol = prefs.currencySymbol,
            snapshot = snapshot,
            attention = attention,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )
}
