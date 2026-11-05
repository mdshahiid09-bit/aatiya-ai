package com.aatiya.voiceassistant.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Setup : Screen("setup")
    object Main : Screen("main")
    object Settings : Screen("settings")
}
