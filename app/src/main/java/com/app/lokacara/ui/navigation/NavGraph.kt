package com.app.lokacara.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.lokacara.data.SettingsManager
import com.app.lokacara.ui.components.BottomNavbar
import com.app.lokacara.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun NavGraph(targetDestination: String = Screen.Login.route) {
    val rootNavController = rememberNavController()

    NavHost(navController = rootNavController, startDestination = Screen.Onboarding.route) {

        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinish = {
                rootNavController.navigate(targetDestination) {
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
                composable(Screen.EventDetail.route) {
                    EventDetailScreen(navController = internalNavController)
                }
                composable(Screen.Explore.route) { ExploreScreen(navController = internalNavController) }
                composable(Screen.Tickets.route) { TicketsScreen(navController = internalNavController) }
                composable(Screen.Profile.route) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val settingsManager = remember { SettingsManager(context) }
                    val coroutineScope = rememberCoroutineScope()

                    ProfileScreen(
                        navController = internalNavController,
                        onLogout = {
                            coroutineScope.launch {
                                settingsManager.clearSession()
                                rootNavController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
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
                composable(Screen.EditProfile.route) { EditProfileScreen(navController = internalNavController) }
                composable(Screen.MyEvents.route) { MyEventsScreen(navController = internalNavController) }
                composable(Screen.SavedEvents.route) { SavedEventsScreen(navController = internalNavController) }
                composable(Screen.Certificates.route) { CertificatesScreen(navController = internalNavController) }
                composable(Screen.Settings.route) { SettingsScreen(navController = internalNavController) }
                composable(Screen.About.route) { AboutScreen(navController = internalNavController) }
                composable(Screen.Bookmark.route) {
                    BookmarkScreen(navController = internalNavController)
                }
                composable(Screen.ChangePassword.route) { ChangePasswordScreen(navController = internalNavController) }
                composable(Screen.HelpCenter.route) { HelpCenterScreen(navController = internalNavController) }
                composable(Screen.TermsConditions.route) { TermsConditionsScreen(navController = internalNavController) }
                composable(Screen.PrivacyPolicy.route) { PrivacyPolicyScreen(navController = internalNavController) }
            }
        }
    }
}