package com.kcalulo.vale.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalulo.vale.core.common.AchievementId
import com.kcalulo.vale.core.common.ProgressHighlights
import com.kcalulo.vale.core.common.ProgressMonthlySnapshot
import com.kcalulo.vale.core.common.ProgressOverview
import com.kcalulo.vale.data.preferences.UserPreferencesRepository
import com.kcalulo.vale.data.repository.AchievementRepository
import com.kcalulo.vale.data.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ProgressUiState(
    val overview: ProgressOverview? = null,
    val monthlySnapshot: ProgressMonthlySnapshot? = null,
    val highlights: ProgressHighlights? = null,
    val unlockedAchievements: Set<AchievementId> = emptySet(),
    val currencySymbol: String = "₱",
) {
    val isLoading: Boolean = overview == null
}

@HiltViewModel
class ProgressViewModel @Inject constructor(
    progressRepository: ProgressRepository,
    achievementRepository: AchievementRepository,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<ProgressUiState> = combine(
        progressRepository.observeStats(),
        achievementRepository.observeUnlocked(),
        preferencesRepository.preferences.map { it.currencySymbol },
    ) { stats, unlocked, symbol ->
        ProgressUiState(
            overview = stats.overview,
            monthlySnapshot = stats.monthlySnapshot,
            highlights = stats.highlights,
            unlockedAchievements = unlocked,
            currencySymbol = symbol,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgressUiState(),
    )
}
