package com.kcalulo.vale.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalulo.vale.core.database.dao.ItemWithUsageCount
import com.kcalulo.vale.data.preferences.UserPreferencesRepository
import com.kcalulo.vale.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val isLoading: Boolean = true,
    val recentItems: List<ItemWithUsageCount> = emptyList(),
    val currencySymbol: String = "₱",
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    itemRepository: ItemRepository,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        itemRepository.observeRecentItems(limit = 5),
        preferencesRepository.preferences,
    ) { recent, prefs ->
        HomeUiState(
            isLoading = false,
            recentItems = recent,
            currencySymbol = prefs.currencySymbol,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )
}
