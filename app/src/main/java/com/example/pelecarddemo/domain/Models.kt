package com.example.pelecarddemo.domain

import java.math.BigDecimal
import java.util.Locale

enum class Currency(val code: String) {
    USD("USD"),
    ILS("ILS"),
}

/** Which optional inputs are shown on the main screen. Edited on the Settings screen. */
data class AppSettings(
    val installmentsAllowed: Boolean = true,
    val currencyAllowed: Boolean = true,
    val signatureAllowed: Boolean = true,
)

/** Raw, editable state of the main screen form. */
data class PaymentForm(
    val amountInput: String = "",
    val installmentsOn: Boolean = false,
    val installments: Int = PaymentRules.MIN_INSTALLMENTS,
    val currency: Currency = PaymentRules.DEFAULT_CURRENCY,
    val signatureOn: Boolean = false,
)

/** A point of a signature stroke, normalized to 0..1 so it can be drawn at any size. */
data class SignaturePoint(val x: Float, val y: Float)

data class Signature(val strokes: List<List<SignaturePoint>>)

/**
 * A finished transaction as shown on the receipt.
 * Optional fields are null when they are not relevant to the transaction.
 */
data class Transaction(
    val amount: BigDecimal,
    val installments: Int?,
    val currency: Currency?,
    val signature: Signature?,
)

data class ClockTime(val hour: Int, val minute: Int, val second: Int) {
    fun formatted(): String = String.format(Locale.US, "%02d:%02d:%02d", hour, minute, second)
}

enum class AmountError {
    REQUIRED,
    INVALID,
    MUST_BE_POSITIVE,
}

sealed interface AmountResult {
    data class Valid(val value: BigDecimal) : AmountResult
    data class Invalid(val error: AmountError) : AmountResult
}
