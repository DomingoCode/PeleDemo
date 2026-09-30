package com.example.pelecarddemo.domain

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ConversionRulesTest {

    private val rates = mapOf(
        "USD" to BigDecimal("0.28"),
        "EUR" to BigDecimal("0.26"),
        "GBP" to BigDecimal("0.22"),
        "JPY" to BigDecimal("3.07"),
        "INR" to BigDecimal("2.02"),
        "ILS" to BigDecimal("1"),
    )

    @Test
    fun buildConversionRates_excludesTheBaseCurrency() {
        val result = ConversionRules.buildConversionRates(BigDecimal("50"), "ILS", rates)

        assertFalse(result.any { it.currencyCode == "ILS" })
    }

    @Test
    fun buildConversionRates_computesRateAndConvertedAmount() {
        val result = ConversionRules.buildConversionRates(BigDecimal("50"), "ILS", rates)

        val usd = result.first { it.currencyCode == "USD" }
        assertEquals(BigDecimal("0.2800"), usd.rate)
        assertEquals(BigDecimal("14.00"), usd.convertedAmount)
    }

    @Test
    fun buildConversionRates_skipsCurrenciesMissingFromTheApiResponse() {
        val result = ConversionRules.buildConversionRates(BigDecimal("50"), "ILS", rates - "CHF" - "CAD" - "AUD" - "CNY")

        assertNull(result.find { it.currencyCode == "CHF" })
    }

    @Test
    fun buildConversionRates_returnsAtLeastFiveTargets() {
        val result = ConversionRules.buildConversionRates(BigDecimal("50"), "ILS", rates)

        assertEquals(5, result.size)
    }
}
