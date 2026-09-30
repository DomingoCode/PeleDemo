package com.example.pelecarddemo.data

import com.example.pelecarddemo.domain.ClockTime
import java.util.Calendar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Emits the current wall-clock time once per second, aligned to the second boundary. */
class ClockSource {

    fun now(): ClockTime {
        val calendar = Calendar.getInstance()
        return ClockTime(
            hour = calendar.get(Calendar.HOUR_OF_DAY),
            minute = calendar.get(Calendar.MINUTE),
            second = calendar.get(Calendar.SECOND),
        )
    }

    fun ticks(): Flow<ClockTime> = flow {
        while (true) {
            emit(now())
            delay(1_000L - System.currentTimeMillis() % 1_000L)
        }
    }
}
