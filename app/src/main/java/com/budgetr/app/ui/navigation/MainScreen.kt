package com.budgetr.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.budgetr.app.ui.screens.balances.AccountBalancesScreen
import com.budgetr.app.ui.screens.home.HomeScreen
import com.budgetr.app.ui.screens.settings.SettingsScreen
import com.budgetr.app.ui.screens.transactions.TransactionsScreen

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(NavRoutes.HOME, "Home", Icons.Default.Home),
    BottomNavItem(NavRoutes.TRANSACTIONS, "Transactions", Icons.Default.List),
    BottomNavItem(NavRoutes.ACCOUNT_BALANCES, "Accounts", Icons.Default.AccountBalance),
    BottomNavItem(NavRoutes.SETTINGS, "Settings", Icons.Default.Settings)
)

@Composable
fun MainScreen(onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.HOME) {
                HomeScreen(
                    onNavigateToAddTransaction = {
                        navController.navigate(NavRoutes.TRANSACTIONS) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(NavRoutes.TRANSACTIONS) {
                TransactionsScreen()
            }
            composable(NavRoutes.ACCOUNT_BALANCES) {
                AccountBalancesScreen()
            }
            composable(NavRoutes.SETTINGS) {
                SettingsScreen(onSignOut = onSignOut)
            }
        }
    }
}
