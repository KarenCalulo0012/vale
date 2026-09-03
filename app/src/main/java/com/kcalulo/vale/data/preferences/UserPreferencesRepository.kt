package com.kcalulo.vale.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemePreference { SYSTEM, LIGHT, DARK }

data class UserPreferences(
    val hasCompletedOnboarding: Boolean = false,
    val currencyCode: String = "PHP",
    val currencySymbol: String = "₱",
    val theme: ThemePreference = ThemePreference.SYSTEM,
)

/** Local-only preferences (spec §5, §23). No account, no cloud. */
@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val THEME = stringPreferencesKey("theme")
    }

    val preferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            hasCompletedOnboarding = prefs[Keys.HAS_COMPLETED_ONBOARDING] ?: false,
            currencyCode = prefs[Keys.CURRENCY_CODE] ?: "PHP",
            currencySymbol = prefs[Keys.CURRENCY_SYMBOL] ?: "₱",
            theme = prefs[Keys.THEME]
                ?.let { runCatching { ThemePreference.valueOf(it) }.getOrNull() }
                ?: ThemePreference.SYSTEM,
        )
    }

    suspend fun setOnboardingCompleted() {
        dataStore.edit { it[Keys.HAS_COMPLETED_ONBOARDING] = true }
    }

    suspend fun setCurrency(code: String, symbol: String) {
        dataStore.edit {
            it[Keys.CURRENCY_CODE] = code
            it[Keys.CURRENCY_SYMBOL] = symbol
        }
    }

    suspend fun setTheme(theme: ThemePreference) {
        dataStore.edit { it[Keys.THEME] = theme.name }
    }
}
