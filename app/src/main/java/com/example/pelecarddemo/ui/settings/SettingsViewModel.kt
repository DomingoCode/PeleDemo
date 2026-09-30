package com.example.pelecarddemo.ui.settings

import androidx.lifecycle.ViewModel
import com.example.pelecarddemo.data.SettingsRepository
import com.example.pelecarddemo.domain.AppSettings
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val settings: StateFlow<AppSettings> = repository.settings

    fun onInstallmentsAllowedChange(allowed: Boolean) =
        repository.update { it.copy(installmentsAllowed = allowed) }

    fun onCurrencyAllowedChange(allowed: Boolean) =
        repository.update { it.copy(currencyAllowed = allowed) }

    fun onSignatureAllowedChange(allowed: Boolean) =
        repository.update { it.copy(signatureAllowed = allowed) }
}
