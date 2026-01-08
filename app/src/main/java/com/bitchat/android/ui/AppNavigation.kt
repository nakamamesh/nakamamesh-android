package com.NakamaMesh.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.NakamaMesh.android.ui.wallet.WalletScreen

/**
 * Navigation routes for the app
 */
object AppRoutes {
    const val CHAT = "chat"
    const val WALLET = "wallet"
}

/**
 * Main navigation host for the app
 */
@Composable
fun AppNavigation(
    chatViewModel: ChatViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.CHAT,
        modifier = modifier
    ) {
        // Chat Screen (main screen)
        composable(AppRoutes.CHAT) {
            ChatScreen(
                viewModel = chatViewModel,
                onNavigateToWallet = {
                    navController.navigate(AppRoutes.WALLET)
                }
            )
        }

        // Wallet Screen (with back button)
        composable(AppRoutes.WALLET) {
            WalletScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}