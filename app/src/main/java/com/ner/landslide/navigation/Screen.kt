package com.ner.landslide.navigation

sealed class Screen(val route: String) {
    // Auth
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")

    // Main tabs
    object Home : Screen("home")
    object Map : Screen("map")
    object Report : Screen("report")
    object Profile : Screen("profile")

    // Sub-screens (not in bottom nav)
    object Prediction : Screen("prediction")
    object Weather : Screen("weather")
    object AdminDashboard : Screen("admin_dashboard")
    object BroadcastAlert : Screen("broadcast_alert")
    object SOSAlertList : Screen("sos_alert_list")
}
