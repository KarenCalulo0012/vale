package com.kcalulo.vale

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Exercises the core lifecycle end to end (spec §34's Definition of Done): onboarding →
 * Calculate → Buy → Item Details → log usage until the target is reached. Runs against an
 * in-memory database (see [com.kcalulo.vale.data.di.TestDataModule]), never the real vale.db.
 */
@HiltAndroidTest
class CoreLifecycleTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    /** DB writes run on a background dispatcher, outside Compose's own idle tracking. */
    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(text, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun calculateBuyAndLogUsageToCompletion() {
        with(composeRule) {
            // Onboarding: intro -> currency (default) -> Get Started.
            onNodeWithText(useUnmergedTree = true, text = "Next").performClick()
            onNodeWithText(useUnmergedTree = true, text = "Get Started").performClick()

            // Home -> Calculate.
            onNodeWithText(useUnmergedTree = true, text = "Calculate it").performClick()

            onNodeWithTag("What is it?").performTextInput("Test Item")
            onNodeWithTag("How much is it?").performTextInput("1000")
            // Expected uses defaults to 10; drop it to 2 so completion only needs two taps.
            repeat(8) { onNodeWithContentDescription("Decrease").performClick() }

            // Distinct testTag: "Calculate" text alone also matches the bottom-nav tab label.
            onNodeWithTag("submitCalculate").performClick()

            // Result -> buy it today -> celebration dialog leads into Item Details.
            onNodeWithText(useUnmergedTree = true, text = "Yes, I'm buying it").performClick()
            onNodeWithText(useUnmergedTree = true, text = "Yes, bought it!").performClick()
            waitForText("View Item")
            onNodeWithText(useUnmergedTree = true, text = "View Item").performClick()

            waitForText("Test Item")
            onNodeWithText(useUnmergedTree = true, text = "Test Item").assertExists()
            onNodeWithText(useUnmergedTree = true, text = "0 / 2 uses · 2 to go").assertExists()

            onNodeWithText(useUnmergedTree = true, text = "+ I used it").performClick()
            waitForText("1 / 2 uses · 1 to go")

            onNodeWithText(useUnmergedTree = true, text = "+ I used it").performClick()
            waitForText("2 / 2 uses · 0 to go")
        }
    }
}
