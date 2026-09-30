package com.example.pelecarddemo.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsTest {

    // The exact hour depends on the machine's timezone, so only the shape is asserted (not flaky across CI/locale).
    @Test
    fun formatReceiptDate_matchesTheExpectedShape() {
        val formatted = formatReceiptDate(1_700_000_000_000L)

        assertTrue(formatted, formatted.matches(Regex("""[A-Z][a-z]{2} \d{1,2}, \d{2}:\d{2}""")))
    }

    @Test
    fun formatTime_matchesTheExpectedShape() {
        val formatted = formatTime(1_700_000_000_000L)

        assertTrue(formatted, formatted.matches(Regex("""\d{2}:\d{2}""")))
    }
}
