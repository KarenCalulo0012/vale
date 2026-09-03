package com.kcalulo.vale.core.design.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class ValeNavItem(
    val label: String,
    val icon: ImageVector,
    val isCenterAction: Boolean = false,
)

/**
 * Bottom navigation — Home / Things / Calculate (center, pink) / Track / Progress.
 * The center item renders as a highlighted pink action, per the style board.
 */
@Composable
fun ValeBottomNav(
    items: List<ValeNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        items.forEachIndexed { index, item ->
            if (item.isCenterAction) {
                NavigationBarItem(
                    selected = false,
                    onClick = { onItemSelected(index) },
                    icon = {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier
                                    .size(44.dp)
                                    .padding(10.dp)
                            )
                        }
                    },
                    label = {
                        Text(item.label, style = MaterialTheme.typography.labelSmall)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.surface,
                        unselectedTextColor = MaterialTheme.colorScheme.secondary
                    )
                )
            } else {
                NavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { onItemSelected(index) },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = {
                        Text(item.label, style = MaterialTheme.typography.labelSmall)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
