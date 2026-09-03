package com.kcalulo.vale.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.kcalulo.vale.core.common.AttentionReason

/** All navigation routes. Full screens only where sustained focus is needed (spec §4). */
object ValeRoutes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val THINGS = "things"
    const val CALCULATE = "calculate"

    /** Bottom-nav Track tab — plain, argument-free. */
    const val TRACK = "track"

    /**
     * A *separate* registered destination for Home's Attention "+N more" deep link (spec §6),
     * sharing [TrackScreen][com.kcalulo.vale.feature.track.TrackScreen] but never the same nav
     * graph node as [TRACK]. Giving it its own route (rather than an optional query arg on
     * [TRACK]) matters: Navigation Compose's bottom-tab `popUpTo`/`saveState`/`restoreState`
     * dance saves/restores back-stack state keyed by node id, and one node visited under two
     * different argument sets corrupted that bookkeeping for every tab, not just Track —
     * tapping Home stopped navigating anywhere after visiting the filtered route once.
     */
    const val TRACK_FILTERED_PATTERN = "trackFiltered/{reason}"

    const val PROGRESS = "progress"
    const val RESULT = "result"
    const val ITEM_DETAILS = "item/{itemId}"
    const val REALITY_CHECK = "realitycheck/{itemId}"
    const val SHOWCASE = "showcase"

    fun itemDetails(itemId: Long) = "item/$itemId"
    fun realityCheck(itemId: Long) = "realitycheck/$itemId"

    /** Deep link from Home's Attention "+N more" into Track, pre-filtered to that group. */
    fun trackFiltered(reason: AttentionReason) = "trackFiltered/${reason.name}"
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
