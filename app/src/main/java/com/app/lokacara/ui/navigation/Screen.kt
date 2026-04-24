package com.app.lokacara.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Register : Screen("register")
    object Login : Screen("login")
    object Home : Screen("home")
    object Explore : Screen("explore")
    object CreateEvent : Screen("create_event")
    object Profile : Screen("profile")
}
