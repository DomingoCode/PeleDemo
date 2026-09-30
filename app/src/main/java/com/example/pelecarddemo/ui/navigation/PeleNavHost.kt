package com.example.pelecarddemo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pelecarddemo.PeleApplication
import com.example.pelecarddemo.domain.PaymentRules
import com.example.pelecarddemo.ui.AppViewModelProvider
import com.example.pelecarddemo.ui.clock.ClockViewModel
import com.example.pelecarddemo.ui.conversion.ConversionRoute
import com.example.pelecarddemo.ui.main.MainRoute
import com.example.pelecarddemo.ui.receipt.ReceiptRoute
import com.example.pelecarddemo.ui.settings.SettingsRoute
import com.example.pelecarddemo.ui.settings.SettingsViewModel
import com.example.pelecarddemo.ui.signature.SignatureRoute
import com.example.pelecarddemo.ui.transaction.TransactionEvent
import com.example.pelecarddemo.ui.transaction.TransactionViewModel

@Composable
fun PeleNavHost(navController: NavHostController = rememberNavController()) {
    // Created here (outside the NavHost) => scoped to the Activity and shared by all destinations.
    val transactionViewModel: TransactionViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val clockViewModel: ClockViewModel = viewModel(factory = AppViewModelProvider.Factory)

    val context = LocalContext.current
    val exchangeRateRepository = remember {
        (context.applicationContext as PeleApplication).container.exchangeRateRepository
    }

    LaunchedEffect(navController, transactionViewModel) {
        transactionViewModel.events.collect { event ->
            // The current-destination checks make a double tap on Submit harmless.
            val current = navController.currentDestination?.route
            when (event) {
                TransactionEvent.NavigateToSignature ->
                    if (current == Route.MAIN) navController.navigate(Route.SIGNATURE)

                TransactionEvent.NavigateToReceipt ->
                    if (current == Route.MAIN || current == Route.SIGNATURE) {
                        // Pops the signature screen so it cannot be reached again with Back.
                        navController.navigate(Route.RECEIPT) { popUpTo(Route.MAIN) }
                    }

                TransactionEvent.ReturnToMain ->
                    navController.popBackStack(Route.MAIN, inclusive = false)
            }
        }
    }

    NavHost(navController = navController, startDestination = Route.MAIN) {
        composable(Route.MAIN) {
            MainRoute(
                transactionViewModel = transactionViewModel,
                clockViewModel = clockViewModel,
                onOpenSettings = { navController.navigate(Route.SETTINGS) },
            )
        }
        composable(Route.SETTINGS) {
            SettingsRoute(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Route.SIGNATURE) {
            SignatureRoute(
                viewModel = transactionViewModel,
                onCancel = { navController.popBackStack() },
            )
        }
        composable(Route.RECEIPT) {
            ReceiptRoute(
                viewModel = transactionViewModel,
                onFinish = { navController.popBackStack(Route.MAIN, inclusive = false) },
                onConvert = { navController.navigate(Route.CONVERSION) },
            )
        }
        composable(Route.CONVERSION) {
            val transaction by transactionViewModel.receipt.collectAsStateWithLifecycle()
            val current = transaction
            if (current == null) {
                // No receipt to convert (e.g. the process was recreated): back out to the receipt/main flow.
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                ConversionRoute(
                    repository = exchangeRateRepository,
                    amount = current.amount,
                    baseCurrencyCode = (current.currency ?: PaymentRules.DEFAULT_CURRENCY).code,
                    receiptNumber = current.receiptNumber,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
