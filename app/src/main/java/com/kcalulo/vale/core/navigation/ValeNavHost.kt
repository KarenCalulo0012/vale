package com.kcalulo.vale.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kcalulo.vale.core.design.ShowcaseScreen
import com.kcalulo.vale.feature.calculate.CalculateScreen
import com.kcalulo.vale.feature.calculate.CalculateViewModel
import com.kcalulo.vale.feature.calculate.ResultScreen
import com.kcalulo.vale.feature.home.HomeScreen
import com.kcalulo.vale.feature.itemdetails.ItemDetailsScreen
import com.kcalulo.vale.feature.onboarding.OnboardingScreen
import com.kcalulo.vale.feature.progress.ProgressScreen
import com.kcalulo.vale.feature.realitycheck.RealityCheckScreen
import com.kcalulo.vale.feature.things.ThingsScreen
import com.kcalulo.vale.feature.track.TrackScreen

/** Bottom-nav tabs are siblings, not a stack — crossfade between them rather than sliding. */
private val TOP_LEVEL_ROUTES = setOf(
    ValeRoutes.HOME,
    ValeRoutes.THINGS,
    ValeRoutes.CALCULATE,
    ValeRoutes.TRACK,
    ValeRoutes.PROGRESS,
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTopLevelHop(): Boolean =
    initialState.destination.route in TOP_LEVEL_ROUTES && targetState.destination.route in TOP_LEVEL_ROUTES

@Composable
fun ValeNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            if (isTopLevelHop()) {
                fadeIn(tween(220))
            } else {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(280)) +
                    fadeIn(tween(280))
            }
        },
        exitTransition = {
            if (isTopLevelHop()) {
                fadeOut(tween(220))
            } else {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(280)) +
                    fadeOut(tween(280))
            }
        },
        popEnterTransition = {
            if (isTopLevelHop()) {
                fadeIn(tween(220))
            } else {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(280)) +
                    fadeIn(tween(280))
            }
        },
        popExitTransition = {
            if (isTopLevelHop()) {
                fadeOut(tween(220))
            } else {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(280)) +
                    fadeOut(tween(280))
            }
        }
    ) {
        composable(ValeRoutes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(ValeRoutes.HOME) {
                        popUpTo(ValeRoutes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(ValeRoutes.HOME) {
            HomeScreen(
                onCalculateClick = {
                    navController.navigate(ValeRoutes.CALCULATE) { launchSingleTop = true }
                },
                onItemClick = { id -> navController.navigate(ValeRoutes.itemDetails(id)) },
                onAttentionSeeAll = { reason ->
                    // Same popUpTo/saveState/restoreState contract the bottom-nav tabs use
                    // (see ValeAppScaffold) — a raw navigate() here left this entry outside
                    // that bookkeeping and corrupted the *next* tab switch's restoreState.
                    navController.navigate(ValeRoutes.trackFiltered(reason)) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(ValeRoutes.THINGS) {
            ThingsScreen(
                onItemClick = { id -> navController.navigate(ValeRoutes.itemDetails(id)) }
            )
        }
        composable(ValeRoutes.CALCULATE) {
            CalculateScreen(
                onCalculated = { navController.navigate(ValeRoutes.RESULT) }
            )
        }
        composable(ValeRoutes.RESULT) { entry ->
            // Share the CalculateViewModel so the draft survives Calculate → Result.
            val parentEntry = remember(entry) {
                navController.getBackStackEntry(ValeRoutes.CALCULATE)
            }
            val viewModel: CalculateViewModel = hiltViewModel(parentEntry)
            ResultScreen(
                viewModel = viewModel,
                onDecided = {
                    navController.navigate(ValeRoutes.HOME) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                onViewItem = { id ->
                    navController.navigate(ValeRoutes.itemDetails(id)) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                    }
                }
            )
        }
        composable(ValeRoutes.TRACK) {
            TrackScreen(
                onItemClick = { id -> navController.navigate(ValeRoutes.itemDetails(id)) }
            )
        }
        composable(
            route = ValeRoutes.TRACK_FILTERED_PATTERN,
            arguments = listOf(navArgument("reason") { type = NavType.StringType })
        ) {
            TrackScreen(
                onItemClick = { id -> navController.navigate(ValeRoutes.itemDetails(id)) }
            )
        }
        composable(ValeRoutes.PROGRESS) { ProgressScreen() }
        composable(
            route = ValeRoutes.ITEM_DETAILS,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) {
            ItemDetailsScreen(
                onBack = { navController.popBackStack() },
                onRealityCheck = { id -> navController.navigate(ValeRoutes.realityCheck(id)) }
            )
        }
        composable(
            route = ValeRoutes.REALITY_CHECK,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) {
            RealityCheckScreen(onBack = { navController.popBackStack() })
        }
        composable(ValeRoutes.SHOWCASE) { ShowcaseScreen() }
    }
}
