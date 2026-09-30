package com.example.pelecarddemo.domain

import java.math.BigDecimal
import java.math.RoundingMode

data class ConversionRate(
    val currencyCode: String,
    val rate: BigDecimal,
    val convertedAmount: BigDecimal,
)

/** Turns raw rates from the API into display-ready [ConversionRate]s. Pure, so it is trivial to unit test. */
object ConversionRules {
    private const val RATE_SCALE = 4
    private const val AMOUNT_SCALE = 2

    /** A fixed, well-known set of targets; the base currency is dropped wherever it appears here. */
    val TARGET_CURRENCY_CODES = listOf("USD", "EUR", "GBP", "JPY", "INR", "ILS", "CAD", "AUD", "CHF", "CNY")

    fun buildConversionRates(
        amount: BigDecimal,
        baseCurrencyCode: String,
        rates: Map<String, BigDecimal>,
    ): List<ConversionRate> = TARGET_CURRENCY_CODES
        .filter { it != baseCurrencyCode }
        .mapNotNull { code ->
            val rate = rates[code] ?: return@mapNotNull null
            ConversionRate(
                currencyCode = code,
                rate = rate.setScale(RATE_SCALE, RoundingMode.HALF_UP),
                convertedAmount = (amount * rate).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP),
            )
        }
}
