package com.kcalulo.vale.core.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kcalulo.vale.core.design.components.ValeBottomSheet
import com.kcalulo.vale.core.design.components.ValeDialog
import com.kcalulo.vale.core.design.components.ValeInputField
import com.kcalulo.vale.core.design.components.ValeItemCard
import com.kcalulo.vale.core.design.components.ValeMascot
import com.kcalulo.vale.core.design.components.ValeMascotMessage
import com.kcalulo.vale.core.design.components.ValePrimaryButton
import com.kcalulo.vale.core.design.components.ValeSecondaryButton
import com.kcalulo.vale.core.design.components.ValeSheetOption
import com.kcalulo.vale.core.design.components.ValeStatus
import com.kcalulo.vale.core.design.components.ValeStatusChip
import com.kcalulo.vale.core.design.components.ValeStepperField
import com.kcalulo.vale.core.design.components.ValeTextButton
import com.kcalulo.vale.core.design.theme.MintSoft
import com.kcalulo.vale.core.design.theme.PinkSoft
import com.kcalulo.vale.core.design.theme.SoftLavender
import com.kcalulo.vale.core.design.theme.StatusNotWorthIt
import com.kcalulo.vale.core.design.theme.StatusOnTrack
import com.kcalulo.vale.core.design.theme.ValeTheme

/** Living catalog of the Vale design system, reachable at the "showcase" route. */
@Composable
fun ShowcaseScreen(modifier: Modifier = Modifier) {
    var amount by remember { mutableStateOf("1,200") }
    var uses by remember { mutableIntStateOf(30) }
    var showSheet by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ValeMascot(size = 96.dp)
            Text(
                text = "VALE",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Prove your math.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ValeMascotMessage(
            message = "Great choice! You just avoided a bad purchase.",
            positive = true,
            modifier = Modifier.fillMaxWidth()
        )
        ValeMascotMessage(
            message = "Hmm… this might not be worth it.",
            positive = false,
            modifier = Modifier.fillMaxWidth()
        )

        // Buttons
        ValePrimaryButton(
            text = "Primary Button",
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        )
        ValeSecondaryButton(
            text = "Secondary Button",
            onClick = { showSheet = true },
            modifier = Modifier.fillMaxWidth()
        )
        ValeTextButton(text = "Tertiary / Text Button", onClick = {})

        // Chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ValeStatusChip(ValeStatus.Bought)
            ValeStatusChip(ValeStatus.Considering)
            ValeStatusChip(ValeStatus.Skipped)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ValeStatusChip(ValeStatus.OnTrack)
            ValeStatusChip(ValeStatus.StillProvingIt)
            ValeStatusChip(ValeStatus.NotWorthIt)
        }

        // Card
        ValeItemCard(
            title = "Canvas Tote Bag",
            price = "₱1,200",
            perUse = "₱40.00 per use",
            usesText = "12 / 30 uses",
            status = ValeStatus.StillProvingIt,
            thumbnail = { Text("👜", style = MaterialTheme.typography.headlineSmall) },
            onClick = { showSheet = true }
        )

        // Inputs
        ValeInputField(
            label = "How much is it?",
            value = amount,
            onValueChange = { amount = it },
            placeholder = "₱0",
            keyboardType = KeyboardType.Number
        )
        ValeStepperField(
            label = "How many times will you use it?",
            value = uses,
            onValueChange = { uses = it }
        )
    }

    if (showSheet) {
        ValeBottomSheet(
            title = "Let's make it worth it.",
            subtitle = "What do you want to do with this item?",
            options = listOf(
                ValeSheetOption("Keep using it", Icons.Default.Check, StatusOnTrack, MintSoft),
                ValeSheetOption("Sell it", Icons.Default.ShoppingCart, MaterialTheme.colorScheme.primary, SoftLavender),
                ValeSheetOption("Give it away", Icons.Default.Favorite, StatusNotWorthIt, PinkSoft),
            ),
            onOptionSelected = { showSheet = false },
            onDismiss = { showSheet = false }
        )
    }

    if (showDialog) {
        ValeDialog(
            title = "Are you sure?",
            message = "This action cannot be undone.",
            confirmText = "Yes, continue",
            onConfirm = { showDialog = false },
            onDismiss = { showDialog = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ShowcasePreview() {
    ValeTheme {
        ShowcaseScreen()
    }
}
