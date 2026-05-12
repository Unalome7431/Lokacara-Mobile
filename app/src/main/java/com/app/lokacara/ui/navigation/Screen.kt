package com.app.lokacara.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Register : Screen("register")
    object Login : Screen("login")
    object Home : Screen("home")
    object Explore : Screen("explore")
    object CreateEvent : Screen("create_event")
    object Tickets : Screen("tickets")
    object Profile : Screen("profile")
    object Notification : Screen("notification")
    
    // Profile Sub-screens
    object EditProfile : Screen("edit_profile")
    object MyEvents : Screen("my_events")
    object SavedEvents : Screen("saved_events")
    object Certificates : Screen("certificates")
    object Settings : Screen("settings")
    object About : Screen("about")
}
