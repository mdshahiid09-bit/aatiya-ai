package com.aatiya.voiceassistant.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aatiya.voiceassistant.di.AppContainer
import com.aatiya.voiceassistant.ui.screens.main.MainAssistantScreen
import com.aatiya.voiceassistant.ui.screens.settings.SettingsScreen
import com.aatiya.voiceassistant.ui.screens.setup.ApiSetupScreen
import com.aatiya.voiceassistant.ui.screens.splash.SplashScreen
import com.aatiya.voiceassistant.viewmodel.MainViewModel
import com.aatiya.voiceassistant.viewmodel.SettingsViewModel
import com.aatiya.voiceassistant.viewmodel.SetupViewModel

@Composable
fun NavGraph(
    container: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                hasApiKey = container.settingsRepository.hasApiKey(),
                onNavigateNext = { destination ->
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Setup.route) {
            val setupViewModel: SetupViewModel = viewModel(
                factory = SetupViewModel.Factory(
                    settingsRepository = container.settingsRepository,
                    restClient = container.geminiRestClient
                )
            )
            ApiSetupScreen(
                viewModel = setupViewModel,
                onSetupComplete = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModel.Factory(
                    assistantRepository = container.liveAssistantRepository,
                    settingsRepository = container.settingsRepository
                )
            )
            mainViewModel.refreshConfigState()

            MainAssistantScreen(
                viewModel = mainViewModel,
                onNavigateSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(
                    settingsRepository = container.settingsRepository
                )
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
