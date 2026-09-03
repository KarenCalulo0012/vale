package com.kcalulo.vale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalulo.vale.data.preferences.UserPreferences
import com.kcalulo.vale.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Ready(val preferences: UserPreferences) : MainUiState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    /** Loading keeps the splash screen up until preferences are read. */
    val uiState: StateFlow<MainUiState> = preferencesRepository.preferences
        .map<UserPreferences, MainUiState>(MainUiState::Ready)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainUiState.Loading,
        )
}
