package com.kcalulo.vale.feature.realitycheck

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kcalulo.vale.core.design.components.ValeInputField
import com.kcalulo.vale.core.design.components.ValeMascot
import com.kcalulo.vale.core.design.components.ValePrimaryButton
import com.kcalulo.vale.core.design.components.ValeTextButton

/** Give Away confirmation (spec §19) — closes the lifecycle without pretending it's profit. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiveAwaySheet(
    onConfirm: (note: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var note by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ValeMascot(size = 56.dp)
            Text(
                text = "Give it away?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Space recovered. Someone else gets to use it. 💜",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ValeInputField(
                label = "Note (optional)",
                value = note,
                onValueChange = { note = it },
                placeholder = "e.g. Gave it to my sister"
            )

            ValePrimaryButton(
                text = "Mark as given away",
                onClick = { onConfirm(note) },
                modifier = Modifier.fillMaxWidth()
            )
            ValeTextButton(text = "Cancel", onClick = onDismiss)
        }
    }
}
