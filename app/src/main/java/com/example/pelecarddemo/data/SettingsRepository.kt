package com.example.pelecarddemo.data

import com.example.pelecarddemo.domain.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory settings holder shared by the Settings and Main screens.
 * Settings live for the lifetime of the process. To persist them, replace the
 * MutableStateFlow with a DataStore-backed implementation; nothing else has to change.
 */
class SettingsRepository {
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun update(transform: (AppSettings) -> AppSettings) {
        _settings.update(transform)
    }
}
