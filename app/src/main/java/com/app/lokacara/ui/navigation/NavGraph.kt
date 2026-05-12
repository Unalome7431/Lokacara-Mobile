package com.app.lokacara.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.ui.components.BottomNavbar
import com.app.lokacara.ui.screens.*

@Composable
fun NavGraph(startDestination: String = Screen.Onboarding.route) {
    val rootNavController = rememberNavController()

    NavHost(navController = rootNavController, startDestination = startDestination) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinish = {
                rootNavController.navigate(Screen.Register.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Register.route) {
            RegisterScreen(onNavigateToLogin = {
                rootNavController.navigate(Screen.Login.route)
            })
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { rootNavController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    rootNavController.navigate("main_container") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("main_container") {
            MainContainer(rootNavController)
        }
    }
}

@Composable
fun MainContainer(rootNavController: androidx.navigation.NavController) {
    val internalNavController = rememberNavController()
    val navBackStackEntry by internalNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            // Navbar disembunyikan di halaman Notifikasi dan Create Event
            if (currentRoute != Screen.Notification.route && currentRoute != Screen.CreateEvent.route) {
                BottomNavbar(navController = internalNavController)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            NavHost(
                navController = internalNavController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) { HomeScreen(navController = internalNavController) }
                composable(Screen.Explore.route) { ExploreScreen(navController = internalNavController) }
                composable(Screen.Tickets.route) { TicketsScreen(navController = internalNavController) }
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        navController = internalNavController,
                        onLogout = {
                            rootNavController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.CreateEvent.route) {
                    CreateEventScreen(
                        onBack = { internalNavController.popBackStack() },
                        onPublish = {
                            internalNavController.navigate(Screen.Home.route) {
                                popUpTo(internalNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(Screen.Notification.route) {
                    NotificationScreen(navController = internalNavController)
                }
                composable(Screen.Bookmark.route) {
                    BookmarkScreen(navController = internalNavController)
                }
            }
        }
    }
}