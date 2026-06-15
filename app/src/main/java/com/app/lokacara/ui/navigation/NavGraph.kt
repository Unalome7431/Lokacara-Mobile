package com.app.lokacara.ui.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.lokacara.ui.components.BottomNavbar
import com.app.lokacara.ui.screens.*

private val screenEnter: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
    slideInHorizontally(initialOffsetX = { it / 4 }) + fadeIn(tween(250))
}
private val screenExit: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
    slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut(tween(150))
}
private val screenPopEnter: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
    slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn(tween(250))
}
private val screenPopExit: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
    slideOutHorizontally(targetOffsetX = { it / 4 }) + fadeOut(tween(150))
}

@Composable
fun NavGraph(isLoggedIn: Boolean, isOnboardingCompleted: Boolean) {
    val rootNavController = rememberNavController()
    val startDestination = when {
        !isOnboardingCompleted -> Screen.Onboarding.route
        isLoggedIn -> "main_container"
        else -> Screen.Login.route
    }

    NavHost(navController = rootNavController, startDestination = startDestination) {
        composable(
            Screen.Onboarding.route,
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(200)) }
        ) {
            OnboardingScreen(onFinish = {
                val nextRoute = when {
                    isLoggedIn -> "main_container"
                    else -> Screen.Register.route
                }
                rootNavController.navigate(nextRoute) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }
        composable(
            Screen.Register.route,
            enterTransition = screenEnter,
            exitTransition = screenExit,
            popEnterTransition = screenPopEnter,
            popExitTransition = screenPopExit
        ) {
            RegisterScreen(
                onNavigateToLogin = {
                    rootNavController.navigate(Screen.Login.route) {
                        launchSingleTop = true
                    }
                },
                onLoginSuccess = {
                    rootNavController.navigate("main_container") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(
            Screen.Login.route,
            enterTransition = screenEnter,
            exitTransition = screenExit,
            popEnterTransition = screenPopEnter,
            popExitTransition = screenPopExit
        ) {
            LoginScreen(
                onNavigateToRegister = {
                    rootNavController.navigate(Screen.Register.route) {
                        launchSingleTop = true
                    }
                },
                onLoginSuccess = {
                    rootNavController.navigate("main_container") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            "main_container",
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(200)) }
        ) {
            MainContainer(rootNavController)
        }
    }
}

@Composable
fun MainContainer(rootNavController: androidx.navigation.NavController) {
    val internalNavController = rememberNavController()
    val navBackStackEntry by internalNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val layoutDirection = LocalLayoutDirection.current

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (currentRoute.isMainTabRoute()) {
                BottomNavbar(navController = internalNavController)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                    bottom = 0.dp
                )
                .fillMaxSize()
        ) {
            NavHost(
                navController = internalNavController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) { HomeScreen(navController = internalNavController) }
                composable(
                    Screen.EventDetail.route,
                    arguments = listOf(navArgument("eventId") { type = NavType.LongType; defaultValue = 0L }),
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(250)) },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut(tween(150)) },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn(tween(250)) },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut(tween(150)) }
                ) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
                    EventDetailScreen(navController = internalNavController, eventId = eventId)
                }
                composable(
                    Screen.Explore.route,
                    arguments = listOf(navArgument("category") { type = NavType.StringType; defaultValue = "" }),
                    enterTransition = screenEnter,
                    exitTransition = screenExit,
                    popEnterTransition = screenPopEnter,
                    popExitTransition = screenPopExit
                ) { backStackEntry ->
                    val initialCategory = Uri.decode(backStackEntry.arguments?.getString("category").orEmpty())
                    ExploreScreen(navController = internalNavController, initialCategory = initialCategory)
                }
                composable(
                    Screen.Tickets.route,
                    enterTransition = screenEnter,
                    exitTransition = screenExit,
                    popEnterTransition = screenPopEnter,
                    popExitTransition = screenPopExit
                ) {
                    TicketsScreen(
                        navController = internalNavController,
                        rootNavController = rootNavController
                    )
                }
                composable(
                    Screen.Profile.route,
                    enterTransition = screenEnter,
                    exitTransition = screenExit,
                    popEnterTransition = screenPopEnter,
                    popExitTransition = screenPopExit
                ) {
                    ProfileScreen(
                        navController = internalNavController,
                        rootNavController = rootNavController
                    )
                }
                composable(
                    Screen.CreateEvent.route,
                    enterTransition = { fadeIn(tween(300)) },
                    exitTransition = { fadeOut(tween(200)) },
                    popEnterTransition = { fadeIn(tween(300)) },
                    popExitTransition = { fadeOut(tween(200)) }
                ) {
                    CreateEventScreen(
                        onBack = { internalNavController.navigateBackOrHome() },
                        onPublish = {
                            internalNavController.navigate(Screen.MyEvents.route) {
                                popUpTo(Screen.CreateEvent.route) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(
                    Screen.Notification.route,
                    enterTransition = screenEnter,
                    exitTransition = screenExit,
                    popEnterTransition = screenPopEnter,
                    popExitTransition = screenPopExit
                ) {
                    NotificationScreen(navController = internalNavController)
                }
                composable(
                    Screen.EditProfile.route,
                    enterTransition = screenEnter,
                    exitTransition = screenExit,
                    popEnterTransition = screenPopEnter,
                    popExitTransition = screenPopExit
                ) { EditProfileScreen(navController = internalNavController) }
                composable(
                    Screen.MyEvents.route,
                    enterTransition = screenEnter,
                    exitTransition = screenExit,
                    popEnterTransition = screenPopEnter,
                    popExitTransition = screenPopExit
                ) { MyEventsScreen(navController = internalNavController) }
                composable(
                    Screen.SavedEvents.route,
                    enterTransition = screenEnter,
                    exitTransition = screenExit,
                    popEnterTransition = screenPopEnter,
                    popExitTransition = screenPopExit
                ) { SavedEventsScreen(navController = internalNavController) }
                composable(
                    Screen.Certificates.route,
                    enterTransition = screenEnter,
                    exitTransition = screenExit,
                    popEnterTransition = screenPopEnter,
                    popExitTransition = screenPopExit
                ) { CertificatesScreen(navController = internalNavController) }
                composable(
                    Screen.Settings.route,
                    enterTransition = screenEnter,
                    exitTransition = screenExit,
                    popEnterTransition = screenPopEnter,
                    popExitTransition = screenPopExit
                ) {
                    SettingsScreen(
                        navController = internalNavController,
                        rootNavController = rootNavController
                    )
                }
                composable(
                    Screen.About.route,
                    enterTransition = screenEnter,
                    exitTransition = screenExit,
                    popEnterTransition = screenPopEnter,
                    popExitTransition = screenPopExit
                ) { AboutScreen(navController = internalNavController) }
                composable(
                    Screen.Bookmark.route,
                    enterTransition = screenEnter,
                    exitTransition = screenExit,
                    popEnterTransition = screenPopEnter,
                    popExitTransition = screenPopExit
                ) {
                    BookmarkScreen(navController = internalNavController)
                }
                composable(
                    Screen.ChangePassword.route,
                    enterTransition = screenEnter,
                    exitTransition = screenExit,
                    popEnterTransition = screenPopEnter,
                    popExitTransition = screenPopExit
                ) { ChangePasswordScreen(navController = internalNavController) }
                composable(
                    Screen.HelpCenter.route,
                    enterTransition = screenEnter,
                    exitTransition = screenExit,
                    popEnterTransition = screenPopEnter,
                    popExitTransition = screenPopExit
                ) { HelpCenterScreen(navController = internalNavController) }
                composable(
                    Screen.TermsConditions.route,
                    enterTransition = screenEnter,
                    exitTransition = screenExit,
                    popEnterTransition = screenPopEnter,
                    popExitTransition = screenPopExit
                ) { TermsConditionsScreen(navController = internalNavController) }
                composable(
                    Screen.PrivacyPolicy.route,
                    enterTransition = screenEnter,
                    exitTransition = screenExit,
                    popEnterTransition = screenPopEnter,
                    popExitTransition = screenPopExit
                ) { PrivacyPolicyScreen(navController = internalNavController) }
                composable(
                    Screen.Attendees.route,
                    enterTransition = screenEnter,
                    exitTransition = screenExit,
                    popEnterTransition = screenPopEnter,
                    popExitTransition = screenPopExit
                ) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId")?.toLongOrNull() ?: return@composable
                    AttendeesScreen(navController = internalNavController, eventId = eventId)
                }
                composable(
                    Screen.QrScan.route,
                    enterTransition = screenEnter,
                    exitTransition = screenExit,
                    popEnterTransition = screenPopEnter,
                    popExitTransition = screenPopExit
                ) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId")?.toLongOrNull() ?: return@composable
                    QrScanScreen(navController = internalNavController, eventId = eventId)
                }
            }
        }
    }
}
