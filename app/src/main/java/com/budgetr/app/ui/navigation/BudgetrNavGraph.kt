package com.budgetr.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.budgetr.app.ui.screens.balances.AccountBalancesScreen
import com.budgetr.app.ui.screens.home.HomeScreen
import com.budgetr.app.ui.screens.login.LoginScreen
import com.budgetr.app.ui.screens.settings.SettingsScreen
import com.budgetr.app.ui.screens.splash.SplashScreen
import com.budgetr.app.ui.screens.transactions.TransactionsScreen

@Composable
fun BudgetrNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateToTransactions = { navController.navigate(NavRoutes.TRANSACTIONS) },
                onNavigateToBalances = { navController.navigate(NavRoutes.ACCOUNT_BALANCES) },
                onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) }
            )
        }

        composable(NavRoutes.TRANSACTIONS) {
            TransactionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.ACCOUNT_BALANCES) {
            AccountBalancesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
