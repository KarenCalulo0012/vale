package com.kcalulo.vale.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalulo.vale.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/** Currencies offered during onboarding (spec §5). */
data class CurrencyOption(val code: String, val symbol: String, val label: String)

val CurrencyOptions = listOf(
    CurrencyOption("PHP", "₱", "Philippine Peso"),
    CurrencyOption("USD", "$", "US Dollar"),
    CurrencyOption("EUR", "€", "Euro"),
    CurrencyOption("GBP", "£", "British Pound"),
    CurrencyOption("JPY", "¥", "Japanese Yen"),
    CurrencyOption("KRW", "₩", "Korean Won"),
    CurrencyOption("SGD", "S$", "Singapore Dollar"),
    CurrencyOption("AUD", "A$", "Australian Dollar"),
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    fun completeOnboarding(currency: CurrencyOption? = null) {
        viewModelScope.launch {
            currency?.let { preferencesRepository.setCurrency(it.code, it.symbol) }
            preferencesRepository.setOnboardingCompleted()
        }
    }
}
