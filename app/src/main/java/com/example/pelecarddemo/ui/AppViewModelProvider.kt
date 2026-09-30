package com.example.pelecarddemo.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pelecarddemo.PeleApplication
import com.example.pelecarddemo.ui.clock.ClockViewModel
import com.example.pelecarddemo.ui.settings.SettingsViewModel
import com.example.pelecarddemo.ui.transaction.TransactionViewModel

/** Creates ViewModels with their dependencies taken from the application's AppContainer. */
object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer { SettingsViewModel(peleApplication().container.settingsRepository) }
        initializer { TransactionViewModel(peleApplication().container.settingsRepository) }
        initializer { ClockViewModel(peleApplication().container.clockSource) }
    }
}

private fun CreationExtras.peleApplication(): PeleApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PeleApplication
