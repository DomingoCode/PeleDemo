package com.example.pelecarddemo

import com.example.pelecarddemo.data.ClockSource
import com.example.pelecarddemo.data.ExchangeRateRepository
import com.example.pelecarddemo.data.SettingsRepository

/** Manual dependency container: enough for this app, no DI framework needed. */
class AppContainer {
    val settingsRepository = SettingsRepository()
    val clockSource = ClockSource()
    val exchangeRateRepository = ExchangeRateRepository()
}
