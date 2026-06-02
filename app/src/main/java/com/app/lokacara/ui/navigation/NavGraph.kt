package com.app.lokacara.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
fun NavGraph(isLoggedIn: Boolean, isOnboardingCompleted: Boolean) {
    val rootNavController = rememberNavController()

    NavHost(navController = rootNavController, startDestination = Screen.Onboarding.route) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinish = {
                val nextRoute = when {
                    isLoggedIn -> "main_container"
                    isOnboardingCompleted -> Screen.Login.route
                    else -> Screen.Register.route
                }
                rootNavController.navigate(nextRoute) {
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

    val navActions = remember(internalNavController) {
        NavigationActions(
            navigateTo = { route ->
                internalNavController.navigate(route) {
                    popUpTo(internalNavController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            goBack = { internalNavController.popBackStack() }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (currentRoute != Screen.Notification.route && currentRoute != Screen.CreateEvent.route) {
                BottomNavbar(navActions = navActions, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            NavHost(
                navController = internalNavController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) { HomeScreen(navActions = navActions) }
                composable(Screen.EventDetail.route) {
                    EventDetailScreen(navActions = navActions)
                }
                composable(Screen.Explore.route) { ExploreScreen(navActions = navActions) }
                composable(Screen.Tickets.route) { TicketsScreen(navActions = navActions) }
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        navActions = navActions,
                        onLogout = {
                            rootNavController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.CreateEvent.route) {
                    CreateEventScreen(
                        onBack = navActions.goBack,
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
                    NotificationScreen(navActions = navActions)
                }
                composable(Screen.EditProfile.route) { EditProfileScreen(navActions = navActions) }
                composable(Screen.MyEvents.route) { MyEventsScreen(navActions = navActions) }
                composable(Screen.SavedEvents.route) { SavedEventsScreen(navActions = navActions) }
                composable(Screen.Certificates.route) { CertificatesScreen(navActions = navActions) }
                composable(Screen.Settings.route) { SettingsScreen(navActions = navActions) }
                composable(Screen.About.route) { AboutScreen(navActions = navActions) }
                composable(Screen.Bookmark.route) {
                    BookmarkScreen(navActions = navActions)
                }
                composable(Screen.ChangePassword.route) { ChangePasswordScreen(navActions = navActions) }
                composable(Screen.HelpCenter.route) { HelpCenterScreen(navActions = navActions) }
                composable(Screen.TermsConditions.route) { TermsConditionsScreen(navActions = navActions) }
                composable(Screen.PrivacyPolicy.route) { PrivacyPolicyScreen(navActions = navActions) }
            }
        }
    }
}
