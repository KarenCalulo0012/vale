package com.kcalulo.vale.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

/** All navigation routes. Full screens only where sustained focus is needed (spec §4). */
object ValeRoutes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val THINGS = "things"
    const val CALCULATE = "calculate"
    const val TRACK = "track"
    const val PROGRESS = "progress"
    const val RESULT = "result/{itemId}"
    const val ITEM_DETAILS = "item/{itemId}"
    const val REALITY_CHECK = "realitycheck/{itemId}"
    const val SHOWCASE = "showcase"

    fun result(itemId: Long) = "result/$itemId"
    fun itemDetails(itemId: Long) = "item/$itemId"
    fun realityCheck(itemId: Long) = "realitycheck/$itemId"
}

/** Bottom navigation — Calculate is the visually dominant center action. */
data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val isCenterAction: Boolean = false,
)

val TopLevelDestinations = listOf(
    TopLevelDestination(ValeRoutes.HOME, "Home", Icons.Default.Home),
    TopLevelDestination(ValeRoutes.THINGS, "Things", Icons.Default.ShoppingCart),
    TopLevelDestination(ValeRoutes.CALCULATE, "Calculate", Icons.Default.Add, isCenterAction = true),
    TopLevelDestination(ValeRoutes.TRACK, "Track", Icons.Default.CheckCircle),
    TopLevelDestination(ValeRoutes.PROGRESS, "Progress", Icons.Default.Star),
)
