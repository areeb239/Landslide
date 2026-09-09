package com.ner.landslide.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.presentation.ui.admin.AdminDashboardScreen
import com.ner.landslide.presentation.ui.admin.BroadcastAlertScreen
import com.ner.landslide.presentation.ui.auth.LoginScreen
import com.ner.landslide.presentation.ui.auth.RegisterScreen
import com.ner.landslide.presentation.ui.auth.SplashScreen
import com.ner.landslide.presentation.ui.home.HomeScreen
import com.ner.landslide.presentation.ui.map.MapScreen
import com.ner.landslide.presentation.ui.prediction.PredictionScreen
import com.ner.landslide.presentation.ui.profile.ProfileScreen
import com.ner.landslide.presentation.ui.report.ReportScreen
import com.ner.landslide.presentation.ui.weather.WeatherScreen

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Alerts", Icons.Default.NotificationsActive),
    BottomNavItem(Screen.Map, "Map", Icons.Default.Map),
    BottomNavItem(Screen.Report, "Report", Icons.Default.AddLocationAlt),
    BottomNavItem(Screen.Profile, "Profile", Icons.Default.Person)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val isSessionActive = auth.currentUser != null || com.ner.landslide.data.repository.UserRepositoryImpl.isSessionActive(context)
    val startDestination = if (isSessionActive) Screen.Home.route else Screen.Splash.route
    val tabRoutes = bottomNavItems.map { it.screen.route }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in tabRoutes

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    color = SurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    shadowElevation = 0.dp
                ) {
                    NavigationBar(
                        containerColor = SurfaceDark,
                        tonalElevation = 0.dp
                    ) {
                        val currentDestination = navBackStackEntry?.destination
                        bottomNavItems.forEach { item ->
                            val isSelected = currentDestination?.hierarchy?.any {
                                it.route == item.screen.route
                            } == true
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                    )
                                },
                                selected = isSelected,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Primary80,
                                    selectedTextColor = Primary80,
                                    indicatorColor = Primary80.copy(alpha = 0.18f),
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextSubtle
                                ),
                                onClick = {
                                    navController.navigate(item.screen.route) {
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
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn() + slideInHorizontally { it / 4 } },
            exitTransition = { fadeOut() + slideOutHorizontally { -it / 4 } },
            popEnterTransition = { fadeIn() + slideInHorizontally { -it / 4 } },
            popExitTransition = { fadeOut() + slideOutHorizontally { it / 4 } }
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) { popUpTo(0) } },
                    onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(0) } }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { navController.navigate(Screen.Home.route) { popUpTo(0) } },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { navController.navigate(Screen.Home.route) { popUpTo(0) } },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToPrediction = { navController.navigate(Screen.Prediction.route) },
                    onNavigateToWeather = { navController.navigate(Screen.Weather.route) },
                    onNavigateToAdmin = { navController.navigate(Screen.AdminDashboard.route) }
                )
            }
            composable(Screen.Map.route) {
                MapScreen()
            }
            composable(Screen.Report.route) {
                ReportScreen(
                    onReportSubmitted = { navController.popBackStack() }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) } },
                    onNavigateToAdmin = { navController.navigate(Screen.AdminDashboard.route) }
                )
            }
            composable(Screen.Prediction.route) {
                PredictionScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Weather.route) {
                WeatherScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBroadcast = { navController.navigate(Screen.BroadcastAlert.route) }
                )
            }
            composable(Screen.BroadcastAlert.route) {
                BroadcastAlertScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onAlertSent = { navController.popBackStack() }
                )
            }
        }
    }
}
