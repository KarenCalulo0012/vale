package com.kcalulo.vale.feature.calculate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcalulo.vale.core.common.ResultVerdict
import com.kcalulo.vale.core.common.ValeCalculations
import com.kcalulo.vale.core.database.entity.ItemCategory
import com.kcalulo.vale.core.database.entity.ItemEntity
import com.kcalulo.vale.core.database.entity.ItemStatus
import com.kcalulo.vale.core.database.entity.SkipReason
import com.kcalulo.vale.data.preferences.UserPreferencesRepository
import com.kcalulo.vale.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Everything the Calculate form knows, plus live-derived math for the preview. */
data class CalculateUiState(
    val name: String = "",
    val priceText: String = "",
    val expectedUses: Int = 10,
    val category: ItemCategory? = null,
    val nameError: String? = null,
    val priceError: String? = null,
) {
    val priceMinor: Long? = parsePriceMinor(priceText)

    /** Live cost-per-use preview; null until the price is valid. */
    val targetCostPerUseMinor: Double? =
        priceMinor?.takeIf { it > 0 }?.let { ValeCalculations.targetCostPerUse(it, expectedUses) }

    val verdict: ResultVerdict = ValeCalculations.resultVerdict(expectedUses)

    val isValid: Boolean = name.isNotBlank() && (priceMinor ?: 0) > 0 && expectedUses > 0
}

/** What happened after the user decided (drives celebration feedback). */
sealed interface DecisionState {
    data object Undecided : DecisionState
    data class Saved(val itemId: Long, val status: ItemStatus) : DecisionState
}

/** Parses "1,200.50" → 120050 minor units; null when not a number. */
internal fun parsePriceMinor(text: String): Long? =
    text.replace(",", "").replace("₱", "").trim()
        .takeIf { it.isNotEmpty() }
        ?.toBigDecimalOrNull()
        ?.multiply(BigDecimal(100))
        ?.toLong()

@HiltViewModel
class CalculateViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculateUiState())
    val uiState: StateFlow<CalculateUiState> = _uiState.asStateFlow()

    private val _decisionState = MutableStateFlow<DecisionState>(DecisionState.Undecided)
    val decisionState: StateFlow<DecisionState> = _decisionState.asStateFlow()

    val currencySymbol: StateFlow<String> = preferencesRepository.preferences
        .map { it.currencySymbol }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "₱")

    fun onNameChange(value: String) =
        _uiState.update { it.copy(name = value, nameError = null) }

    fun onPriceChange(value: String) =
        _uiState.update { it.copy(priceText = value, priceError = null) }

    fun onExpectedUsesChange(value: Int) =
        _uiState.update { it.copy(expectedUses = value.coerceAtLeast(1)) }

    fun onCategoryChange(value: ItemCategory?) =
        _uiState.update { it.copy(category = value) }

    /** Validates the form; true means it's safe to show the Result screen. */
    fun validate(): Boolean {
        val state = _uiState.value
        val nameError = if (state.name.isBlank()) "Give it a name, bestie." else null
        val priceError = when {
            state.priceText.isBlank() -> "How much is it?"
            (state.priceMinor ?: -1) <= 0 -> "That price isn't mathing."
            else -> null
        }
        _uiState.update { it.copy(nameError = nameError, priceError = priceError) }
        return nameError == null && priceError == null
    }

    /** Persists the calculation with the chosen decision (spec §8–§11). */
    fun saveDecision(status: ItemStatus, skipReason: SkipReason? = null) {
        val state = _uiState.value
        val priceMinor = state.priceMinor ?: return
        viewModelScope.launch {
            val id = itemRepository.saveItem(
                ItemEntity(
                    name = state.name.trim(),
                    category = state.category,
                    originalPriceMinor = priceMinor,
                    purchaseDate = if (status == ItemStatus.BOUGHT) LocalDate.now() else null,
                    createdAt = Instant.now(),
                    expectedUses = state.expectedUses,
                    targetCostPerUseMinor = ValeCalculations.targetCostPerUse(
                        priceMinor, state.expectedUses
                    ),
                    status = status,
                    skipReason = skipReason,
                )
            )
            _decisionState.value = DecisionState.Saved(id, status)
        }
    }

    /** Resets the flow after the celebration is dismissed. */
    fun startOver() {
        _uiState.value = CalculateUiState()
        _decisionState.value = DecisionState.Undecided
    }
}
