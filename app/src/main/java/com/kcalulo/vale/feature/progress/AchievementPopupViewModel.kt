package com.kcalulo.vale.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalulo.vale.core.common.AchievementId
import com.kcalulo.vale.data.repository.AchievementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Scoped to the whole app (hoisted once in [com.kcalulo.vale.ValeAppScaffold]) rather
 * than to Progress, so an achievement unlocked from Calculate, Item Details, or Reality
 * Check can still show its popup wherever the user happens to be (spec §21 unlock
 * behavior) without every one of those screens needing to know achievements exist.
 */
@HiltViewModel
class AchievementPopupViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository,
) : ViewModel() {

    private val _current = MutableStateFlow<AchievementId?>(null)
    val current: StateFlow<AchievementId?> = _current.asStateFlow()

    init {
        achievementRepository.newlyUnlocked
            .onEach { _current.value = it }
            .launchIn(viewModelScope)
    }

    fun dismiss() {
        _current.value = null
    }
}
