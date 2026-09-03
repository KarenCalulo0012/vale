package com.kcalulo.vale.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kcalulo.vale.core.design.ShowcaseScreen
import com.kcalulo.vale.feature.calculate.CalculateScreen
import com.kcalulo.vale.feature.home.HomeScreen
import com.kcalulo.vale.feature.onboarding.OnboardingScreen
import com.kcalulo.vale.feature.progress.ProgressScreen
import com.kcalulo.vale.feature.realitycheck.RealityCheckScreen
import com.kcalulo.vale.feature.things.ThingsScreen
import com.kcalulo.vale.feature.track.TrackScreen

@Composable
fun ValeNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
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
        composable(ValeRoutes.HOME) { HomeScreen() }
        composable(ValeRoutes.THINGS) { ThingsScreen() }
        composable(ValeRoutes.CALCULATE) { CalculateScreen() }
        composable(ValeRoutes.TRACK) { TrackScreen() }
        composable(ValeRoutes.PROGRESS) { ProgressScreen() }
        composable(ValeRoutes.REALITY_CHECK) { RealityCheckScreen() }
        composable(ValeRoutes.SHOWCASE) { ShowcaseScreen() }
    }
}
