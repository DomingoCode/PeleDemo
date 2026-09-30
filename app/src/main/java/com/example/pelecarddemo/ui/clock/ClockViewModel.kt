package com.example.pelecarddemo.ui.clock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pelecarddemo.data.ClockSource
import com.example.pelecarddemo.domain.ClockTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ClockViewModel(clockSource: ClockSource) : ViewModel() {
    // Ticks only while the clock is on screen (WhileSubscribed), so nothing runs in the background.
    val time: StateFlow<ClockTime> = clockSource.ticks().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = clockSource.now(),
    )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
