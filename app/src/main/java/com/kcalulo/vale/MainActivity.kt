package com.kcalulo.vale

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kcalulo.vale.core.design.components.ValeBottomNav
import com.kcalulo.vale.core.design.components.ValeDialog
import com.kcalulo.vale.core.design.components.ValeNavItem
import com.kcalulo.vale.core.design.theme.ValeTheme
import com.kcalulo.vale.core.navigation.TopLevelDestinations
import com.kcalulo.vale.core.navigation.ValeNavHost
import com.kcalulo.vale.core.navigation.ValeRoutes
import com.kcalulo.vale.data.preferences.ThemePreference
import com.kcalulo.vale.feature.progress.AchievementPopupViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        val viewModel: MainViewModel by viewModels()
        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value is MainUiState.Loading
        }
        enableEdgeToEdge()
        setContent {
            ValeApp()
        }
    }
}

@Composable
fun ValeApp(viewModel: MainViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val state = uiState

    if (state is MainUiState.Ready) {
        val darkTheme = when (state.preferences.theme) {
            ThemePreference.SYSTEM -> isSystemInDarkTheme()
            ThemePreference.LIGHT -> false
            ThemePreference.DARK -> true
        }
        ValeTheme(darkTheme = darkTheme) {
            ValeAppScaffold(
                startDestination = if (state.preferences.hasCompletedOnboarding) {
                    ValeRoutes.HOME
                } else {
                    ValeRoutes.ONBOARDING
                }
            )
        }
    }
}

@Composable
private fun ValeAppScaffold(
    startDestination: String,
    achievementPopupViewModel: AchievementPopupViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val topLevelIndex = TopLevelDestinations.indexOfFirst { dest ->
        currentDestination?.hierarchy?.any { it.route == dest.route } == true
    }
    val showBottomBar = topLevelIndex >= 0

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                ValeBottomNav(
                    items = TopLevelDestinations.map {
                        ValeNavItem(it.label, it.icon, it.isCenterAction)
                    },
                    selectedIndex = topLevelIndex,
                    onItemSelected = { index ->
                        navController.navigate(TopLevelDestinations[index].route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        ValeNavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        )
    }

    val unlocked by achievementPopupViewModel.current.collectAsStateWithLifecycle()
    unlocked?.let { achievement ->
        ValeDialog(
            title = "${achievement.emoji} ${achievement.title}",
            message = achievement.shortCopy,
            confirmText = "Continue",
            onConfirm = achievementPopupViewModel::dismiss,
            onDismiss = achievementPopupViewModel::dismiss,
            cancelText = "",
        )
    }
}
