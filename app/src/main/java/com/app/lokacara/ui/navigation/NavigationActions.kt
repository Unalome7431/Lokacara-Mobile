package com.app.lokacara.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

private val mainTabBaseRoutes = setOf(
    Screen.Home.route.routeBase(),
    Screen.Explore.route.routeBase(),
    Screen.Tickets.route.routeBase(),
    Screen.Profile.route.routeBase()
)

fun String.routeBase(): String = substringBefore("?")

fun String?.matchesRoute(route: String): Boolean {
    return this?.routeBase() == route.routeBase()
}

fun String?.isMainTabRoute(): Boolean {
    return this?.routeBase() in mainTabBaseRoutes
}

fun NavController.navigateToMainTab(route: String) {
    val targetRoute = if (route == Screen.Explore.route) Screen.Explore.createRoute() else route
    navigate(targetRoute) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavController.navigateToExplore(category: String = "") {
    navigate(Screen.Explore.createRoute(category)) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = category.isBlank()
    }
}

fun NavController.navigateToCreateEvent() {
    navigate(Screen.CreateEvent.route) {
        launchSingleTop = true
    }
}

fun NavController.navigateBackOrHome() {
    if (popBackStack()) return

    navigate(Screen.Home.route) {
        popUpTo(graph.findStartDestination().id) {
            inclusive = false
        }
        launchSingleTop = true
    }
}

fun NavController.navigateToLoginAndClearMain() {
    navigate(Screen.Login.route) {
        popUpTo(0) {
            inclusive = true
        }
        launchSingleTop = true
    }
}
