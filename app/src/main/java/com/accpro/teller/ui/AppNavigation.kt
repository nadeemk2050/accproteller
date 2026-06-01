package com.accpro.teller.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.accpro.teller.data.LocalStore
import com.accpro.teller.data.ApiRepository
import com.accpro.teller.ui.screens.*

object Routes {
    const val API_KEY_SETUP = "api_key_setup"
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val VOUCHER_TYPE_SELECT = "voucher_type_select"
    const val VOUCHER_FORM = "voucher_form/{voucherType}"
    const val BALANCES = "balances"

    fun voucherForm(voucherType: String) = "voucher_form/$voucherType"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // In a real app, LocalStore would be injected via dependency injection
    // For simplicity, we create them here (context-based)
    // Note: localStore is accessed via LocalContext in each screen
    // We pass navController through the composable tree

    NavHost(navController = navController, startDestination = Routes.API_KEY_SETUP) {
        composable(Routes.API_KEY_SETUP) {
            ApiKeySetupScreen(
                onApiKeySaved = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.API_KEY_SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToVoucher = {
                    navController.navigate(Routes.VOUCHER_TYPE_SELECT)
                },
                onNavigateToBalances = {
                    navController.navigate(Routes.BALANCES)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.VOUCHER_TYPE_SELECT) {
            VoucherTypeSelectScreen(
                onVoucherSelected = { type ->
                    navController.navigate(Routes.voucherForm(type))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.VOUCHER_FORM,
            arguments = listOf(navArgument("voucherType") { type = NavType.StringType })
        ) { backStackEntry ->
            val voucherType = backStackEntry.arguments?.getString("voucherType") ?: "payment"
            VoucherFormScreen(
                voucherType = voucherType,
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.popBackStack(Routes.DASHBOARD, inclusive = false)
                }
            )
        }

        composable(Routes.BALANCES) {
            BalancesScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
