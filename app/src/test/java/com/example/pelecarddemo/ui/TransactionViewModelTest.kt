package com.example.pelecarddemo.ui

import com.example.pelecarddemo.data.SettingsRepository
import com.example.pelecarddemo.domain.AmountError
import com.example.pelecarddemo.domain.Currency
import com.example.pelecarddemo.domain.SignaturePoint
import com.example.pelecarddemo.ui.transaction.TransactionEvent
import com.example.pelecarddemo.ui.transaction.TransactionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settingsRepository = SettingsRepository()
    private lateinit var viewModel: TransactionViewModel

    // Created in @Before (not in the field initializer): viewModelScope needs Dispatchers.Main,
    // which the rule installs only right before the test body runs.
    @Before
    fun setUp() {
        viewModel = TransactionViewModel(settingsRepository)
    }

    @Test
    fun submit_withEmptyAmount_showsRequiredError_andDoesNotNavigate() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.mainState.collect {} }

        viewModel.onSubmit()

        assertEquals(AmountError.REQUIRED, viewModel.mainState.value.amountError)
        assertNull(viewModel.receipt.value)
    }

    @Test
    fun submit_withZero_showsPositiveError() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.mainState.collect {} }

        viewModel.onAmountChange("0")
        viewModel.onSubmit()

        assertEquals(AmountError.MUST_BE_POSITIVE, viewModel.mainState.value.amountError)
    }

    @Test
    fun editingAmount_clearsError() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.mainState.collect {} }

        viewModel.onSubmit()
        viewModel.onAmountChange("5")

        assertNull(viewModel.mainState.value.amountError)
    }

    @Test
    fun submit_withoutSignature_goesToReceipt() = runTest {
        viewModel.onAmountChange("200")
        viewModel.onInstallmentsToggle(true)
        viewModel.onInstallmentsChange(12)
        viewModel.onCurrencyChange(Currency.USD)

        viewModel.onSubmit()

        assertEquals(TransactionEvent.NavigateToReceipt, viewModel.events.first())
        val receipt = viewModel.receipt.value
        assertNotNull(receipt)
        receipt!!
        assertEquals("200.00", receipt.amount.toPlainString())
        assertEquals(12, receipt.installments)
        assertEquals(Currency.USD, receipt.currency)
        assertNull(receipt.signature)
    }

    @Test
    fun submit_withSignature_goesToSignatureThenReceipt() = runTest {
        viewModel.onAmountChange("50")
        viewModel.onSignatureToggle(true)

        viewModel.onSubmit()
        assertEquals(TransactionEvent.NavigateToSignature, viewModel.events.first())
        assertNull(viewModel.receipt.value)

        viewModel.onStrokeStart(SignaturePoint(0.1f, 0.1f))
        viewModel.onStrokePoint(SignaturePoint(0.5f, 0.5f))
        viewModel.onSignatureSubmit()

        assertEquals(TransactionEvent.NavigateToReceipt, viewModel.events.first())
        assertNotNull(viewModel.receipt.value?.signature)
    }

    @Test
    fun hidingAnOption_resetsItsValue() = runTest {
        viewModel.onInstallmentsToggle(true)
        viewModel.onSignatureToggle(true)

        settingsRepository.update { it.copy(installmentsAllowed = false, signatureAllowed = false) }
        viewModel.onAmountChange("10")
        viewModel.onSubmit()

        // Signature is hidden => straight to the receipt, without installments.
        assertEquals(TransactionEvent.NavigateToReceipt, viewModel.events.first())
        assertNull(viewModel.receipt.value?.installments)
        assertNull(viewModel.receipt.value?.signature)
    }

    @Test
    fun cancel_restoresInitialState() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.mainState.collect {} }

        viewModel.onAmountChange("99")
        viewModel.onInstallmentsToggle(true)
        viewModel.onSignatureToggle(true)
        viewModel.onCancel()

        val state = viewModel.mainState.value
        assertEquals("", state.amount)
        assertFalse(state.installmentsOn)
        assertFalse(state.signatureOn)
        assertEquals(Currency.ILS, state.currency)
        assertTrue(state.showInstallments)
    }
}
