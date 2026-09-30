package com.example.pelecarddemo.domain

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentRulesTest {

    // region sanitizeAmountInput

    @Test
    fun sanitize_removesLettersAndSymbols() {
        assertEquals("12", PaymentRules.sanitizeAmountInput("1a2-+ "))
    }

    @Test
    fun sanitize_keepsOnlyFirstSeparator_andNormalizesComma() {
        assertEquals("1.5", PaymentRules.sanitizeAmountInput("1,5"))
        assertEquals("1.25", PaymentRules.sanitizeAmountInput("1.2.5"))
    }

    @Test
    fun sanitize_limitsDecimalDigits() {
        assertEquals("10.99", PaymentRules.sanitizeAmountInput("10.9999"))
    }

    @Test
    fun sanitize_limitsIntegerDigits() {
        assertEquals("123456789", PaymentRules.sanitizeAmountInput("1234567890"))
    }

    // endregion

    // region validateAmount

    @Test
    fun validate_emptyIsRequired() {
        assertEquals(AmountResult.Invalid(AmountError.REQUIRED), PaymentRules.validateAmount(""))
        assertEquals(AmountResult.Invalid(AmountError.REQUIRED), PaymentRules.validateAmount("   "))
    }

    @Test
    fun validate_lonelySeparatorIsInvalid() {
        assertEquals(AmountResult.Invalid(AmountError.INVALID), PaymentRules.validateAmount("."))
    }

    @Test
    fun validate_zeroIsNotPositive() {
        assertEquals(AmountResult.Invalid(AmountError.MUST_BE_POSITIVE), PaymentRules.validateAmount("0"))
        assertEquals(AmountResult.Invalid(AmountError.MUST_BE_POSITIVE), PaymentRules.validateAmount("0.00"))
    }

    @Test
    fun validate_positiveValues() {
        assertEquals(AmountResult.Valid(BigDecimal("200")), PaymentRules.validateAmount("200"))
        assertEquals(AmountResult.Valid(BigDecimal("0.01")), PaymentRules.validateAmount("0.01"))
        assertEquals(AmountResult.Valid(BigDecimal("5.5")), PaymentRules.validateAmount("5,5"))
    }

    // endregion

    // region settings

    @Test
    fun requiresSignature_needsBothSettingAndSwitch() {
        val on = PaymentForm(signatureOn = true)
        assertTrue(PaymentRules.requiresSignature(on, AppSettings(signatureAllowed = true)))
        assertFalse(PaymentRules.requiresSignature(on, AppSettings(signatureAllowed = false)))
        assertFalse(PaymentRules.requiresSignature(PaymentForm(), AppSettings()))
    }

    @Test
    fun applySettings_resetsHiddenOptions() {
        val form = PaymentForm(
            installmentsOn = true,
            installments = 6,
            currency = Currency.USD,
            signatureOn = true,
        )
        val result = PaymentRules.applySettings(
            form,
            AppSettings(installmentsAllowed = false, currencyAllowed = false, signatureAllowed = false),
        )
        assertFalse(result.installmentsOn)
        assertEquals(PaymentRules.MIN_INSTALLMENTS, result.installments)
        assertEquals(PaymentRules.DEFAULT_CURRENCY, result.currency)
        assertFalse(result.signatureOn)
    }

    @Test
    fun applySettings_keepsVisibleOptions() {
        val form = PaymentForm(installmentsOn = true, installments = 6, currency = Currency.USD, signatureOn = true)
        assertEquals(form, PaymentRules.applySettings(form, AppSettings()))
    }

    // endregion

    // region installmentAmount

    @Test
    fun installmentAmount_dividesAndRoundsHalfUp() {
        assertEquals(BigDecimal("413.33"), PaymentRules.installmentAmount(BigDecimal("1240"), 3))
        assertEquals(BigDecimal("620.00"), PaymentRules.installmentAmount(BigDecimal("1240"), 2))
    }

    // endregion

    // region buildTransaction

    @Test
    fun buildTransaction_includesRelevantFields() {
        val signature = Signature(listOf(listOf(SignaturePoint(0f, 0f), SignaturePoint(1f, 1f))))
        val form = PaymentForm(installmentsOn = true, installments = 12, currency = Currency.USD, signatureOn = true)

        val transaction = PaymentRules.buildTransaction(
            BigDecimal("200"), form, AppSettings(), signature, receiptNumber = 7, timestampMillis = 1_000L,
        )

        assertEquals(BigDecimal("200.00"), transaction.amount)
        assertEquals(12, transaction.installments)
        assertEquals(Currency.USD, transaction.currency)
        assertEquals(signature, transaction.signature)
        assertEquals(7, transaction.receiptNumber)
        assertEquals(1_000L, transaction.timestampMillis)
    }

    @Test
    fun buildTransaction_omitsFieldsThatAreOffOrHidden() {
        val form = PaymentForm(installmentsOn = false, currency = Currency.ILS, signatureOn = true)
        val settings = AppSettings(currencyAllowed = false, signatureAllowed = false)

        val transaction = PaymentRules.buildTransaction(
            BigDecimal("50"), form, settings, Signature(emptyList()), receiptNumber = 1, timestampMillis = 0L,
        )

        assertNull(transaction.installments)
        assertNull(transaction.currency)
        assertNull(transaction.signature)
    }

    // endregion
}
