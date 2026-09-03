package com.kcalulo.vale.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kcalulo.vale.core.design.components.ValeMascot
import com.kcalulo.vale.core.design.components.ValePrimaryButton
import com.kcalulo.vale.core.design.components.ValeTextButton

/**
 * Phase 0 shell — real 2–3 panel onboarding with currency selection lands in Phase 1.
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ValeMascot(size = 128.dp)
        Text(
            text = "VALE",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "Prove your math.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Calculate before buying. Track what you actually use. See if it was really worth it.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, bottom = 32.dp)
        )
        ValePrimaryButton(
            text = "Get Started",
            onClick = {
                viewModel.completeOnboarding()
                onFinished()
            },
            modifier = Modifier.fillMaxWidth()
        )
        ValeTextButton(
            text = "Skip",
            onClick = {
                viewModel.completeOnboarding()
                onFinished()
            }
        )
    }
}
