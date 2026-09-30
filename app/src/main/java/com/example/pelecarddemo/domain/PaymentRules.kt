package com.example.pelecarddemo.domain

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * All business rules of the payment flow as pure functions (no Android, no Compose),
 * so they are trivial to unit test and can never leak into Composables.
 */
object PaymentRules {
    const val MIN_INSTALLMENTS = 1
    const val MAX_INSTALLMENTS = 12
    const val MAX_INTEGER_DIGITS = 9
    const val MAX_DECIMAL_DIGITS = 2

    val DEFAULT_CURRENCY = Currency.ILS
    val INSTALLMENT_OPTIONS: IntRange = MIN_INSTALLMENTS..MAX_INSTALLMENTS

    /**
     * Keeps only what can be part of a money amount: digits and a single decimal separator
     * (both '.' and ',' are accepted and normalized to '.'), with limited length.
     */
    fun sanitizeAmountInput(raw: String): String {
        val result = StringBuilder()
        var separatorSeen = false
        var integerDigits = 0
        var decimalDigits = 0
        for (ch in raw) {
            when {
                ch in '0'..'9' -> {
                    if (separatorSeen) {
                        if (decimalDigits < MAX_DECIMAL_DIGITS) {
                            result.append(ch)
                            decimalDigits++
                        }
                    } else if (integerDigits < MAX_INTEGER_DIGITS) {
                        result.append(ch)
                        integerDigits++
                    }
                }
                (ch == '.' || ch == ',') && !separatorSeen -> {
                    separatorSeen = true
                    result.append('.')
                }
            }
        }
        return result.toString()
    }

    /** The amount is required and must be greater than zero. */
    fun validateAmount(input: String): AmountResult {
        val text = input.trim().replace(',', '.')
        if (text.isEmpty()) return AmountResult.Invalid(AmountError.REQUIRED)
        val value = text.toBigDecimalOrNull() ?: return AmountResult.Invalid(AmountError.INVALID)
        return if (value.signum() <= 0) {
            AmountResult.Invalid(AmountError.MUST_BE_POSITIVE)
        } else {
            AmountResult.Valid(value)
        }
    }

    /** A signature is collected only if the option is allowed in Settings AND switched on. */
    fun requiresSignature(form: PaymentForm, settings: AppSettings): Boolean =
        settings.signatureAllowed && form.signatureOn

    /** Options hidden in Settings are treated as off: their values go back to the defaults. */
    fun applySettings(form: PaymentForm, settings: AppSettings): PaymentForm {
        var result = form
        if (!settings.installmentsAllowed) {
            result = result.copy(installmentsOn = false, installments = MIN_INSTALLMENTS)
        }
        if (!settings.currencyAllowed) {
            result = result.copy(currency = DEFAULT_CURRENCY)
        }
        if (!settings.signatureAllowed) {
            result = result.copy(signatureOn = false)
        }
        return result
    }

    /** The amount of a single installment, rounded the same way as the total. */
    fun installmentAmount(total: BigDecimal, installments: Int): BigDecimal =
        total.divide(BigDecimal(installments), MAX_DECIMAL_DIGITS, RoundingMode.HALF_UP)

    /** Builds the receipt data, including only what is relevant to this transaction. */
    fun buildTransaction(
        amount: BigDecimal,
        form: PaymentForm,
        settings: AppSettings,
        signature: Signature?,
        receiptNumber: Int,
        timestampMillis: Long,
    ): Transaction = Transaction(
        amount = amount.setScale(MAX_DECIMAL_DIGITS, RoundingMode.HALF_UP),
        installments = if (settings.installmentsAllowed && form.installmentsOn) form.installments else null,
        currency = if (settings.currencyAllowed) form.currency else null,
        signature = if (requiresSignature(form, settings)) signature else null,
        receiptNumber = receiptNumber,
        timestampMillis = timestampMillis,
    )
}
