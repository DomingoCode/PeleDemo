package com.example.pelecarddemo.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pelecarddemo.data.SettingsRepository
import com.example.pelecarddemo.domain.AmountError
import com.example.pelecarddemo.domain.AmountResult
import com.example.pelecarddemo.domain.AppSettings
import com.example.pelecarddemo.domain.Currency
import com.example.pelecarddemo.domain.PaymentForm
import com.example.pelecarddemo.domain.PaymentRules
import com.example.pelecarddemo.domain.Signature
import com.example.pelecarddemo.domain.SignaturePoint
import com.example.pelecarddemo.domain.Transaction
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** What the main screen needs to render. Hidden options are simply not shown. */
data class MainUiState(
    val amount: String,
    val amountError: AmountError?,
    val showInstallments: Boolean,
    val installmentsOn: Boolean,
    val installments: Int,
    val showCurrency: Boolean,
    val currency: Currency,
    val showSignature: Boolean,
    val signatureOn: Boolean,
)

data class SignatureUiState(val strokes: List<List<SignaturePoint>> = emptyList()) {
    val canSubmit: Boolean get() = strokes.isNotEmpty()
}

/** One-off navigation requests. The ViewModel decides WHEN to go; the NavHost decides HOW. */
sealed interface TransactionEvent {
    data object NavigateToSignature : TransactionEvent
    data object NavigateToReceipt : TransactionEvent
    data object ReturnToMain : TransactionEvent
}

/**
 * Owns the whole "current transaction" flow (Main -> Signature -> Receipt).
 * Scoped to the Activity, so the state survives rotation and is shared by the three screens.
 */
class TransactionViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val form = MutableStateFlow(PaymentForm())
    private val amountError = MutableStateFlow<AmountError?>(null)
    private val strokes = MutableStateFlow<List<List<SignaturePoint>>>(emptyList())
    private val _receipt = MutableStateFlow<Transaction?>(null)
    private val _events = Channel<TransactionEvent>(Channel.BUFFERED)

    val events: Flow<TransactionEvent> = _events.receiveAsFlow()

    val receipt: StateFlow<Transaction?> = _receipt.asStateFlow()

    val mainState: StateFlow<MainUiState> = combine(
        form,
        amountError,
        settingsRepository.settings,
    ) { currentForm, error, settings -> toMainState(currentForm, error, settings) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = toMainState(form.value, amountError.value, settingsRepository.settings.value),
        )

    val signatureState: StateFlow<SignatureUiState> = strokes
        .map { SignatureUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = SignatureUiState(strokes.value),
        )

    init {
        // An option hidden in Settings is treated as off, so reset its value.
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                form.update { PaymentRules.applySettings(it, settings) }
            }
        }
    }

    // region Main screen

    fun onAmountChange(raw: String) {
        form.update { it.copy(amountInput = PaymentRules.sanitizeAmountInput(raw)) }
        amountError.value = null
    }

    fun onInstallmentsToggle(enabled: Boolean) = form.update { it.copy(installmentsOn = enabled) }

    fun onInstallmentsChange(count: Int) = form.update {
        it.copy(installments = count.coerceIn(PaymentRules.MIN_INSTALLMENTS, PaymentRules.MAX_INSTALLMENTS))
    }

    fun onCurrencyChange(currency: Currency) = form.update { it.copy(currency = currency) }

    fun onSignatureToggle(enabled: Boolean) = form.update { it.copy(signatureOn = enabled) }

    fun onSubmit() {
        val currentForm = form.value
        val settings = settingsRepository.settings.value
        when (val result = PaymentRules.validateAmount(currentForm.amountInput)) {
            is AmountResult.Invalid -> amountError.value = result.error
            is AmountResult.Valid -> {
                amountError.value = null
                if (PaymentRules.requiresSignature(currentForm, settings)) {
                    strokes.value = emptyList() // always start with a blank pad
                    _events.trySend(TransactionEvent.NavigateToSignature)
                } else {
                    openReceipt(result, currentForm, settings, signature = null)
                }
            }
        }
    }

    /** Clears the current transaction and returns the main screen to its initial state. */
    fun onCancel() {
        resetForm()
        _receipt.value = null
    }

    // endregion

    // region Signature screen

    fun onStrokeStart(point: SignaturePoint) = strokes.update { it + listOf(listOf(point)) }

    fun onStrokePoint(point: SignaturePoint) = strokes.update { current ->
        if (current.isEmpty()) current else current.dropLast(1) + listOf(current.last() + point)
    }

    fun onSignatureClear() {
        strokes.value = emptyList()
    }

    fun onSignatureSubmit() {
        val drawn = strokes.value
        if (drawn.isEmpty()) return
        val result = PaymentRules.validateAmount(form.value.amountInput)
        if (result is AmountResult.Valid) {
            openReceipt(result, form.value, settingsRepository.settings.value, Signature(drawn))
        } else {
            // The form is gone (e.g. the process was recreated): go back and start over.
            _events.trySend(TransactionEvent.ReturnToMain)
        }
    }

    // endregion

    /**
     * Starts a new transaction. The last receipt is kept until the next one replaces it,
     * so the receipt screen does not go blank while it animates away.
     */
    fun onFinish() = resetForm()

    private fun openReceipt(
        amount: AmountResult.Valid,
        currentForm: PaymentForm,
        settings: AppSettings,
        signature: Signature?,
    ) {
        _receipt.value = PaymentRules.buildTransaction(amount.value, currentForm, settings, signature)
        _events.trySend(TransactionEvent.NavigateToReceipt)
    }

    private fun resetForm() {
        form.value = PaymentForm()
        amountError.value = null
        strokes.value = emptyList()
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun toMainState(form: PaymentForm, error: AmountError?, settings: AppSettings) = MainUiState(
    amount = form.amountInput,
    amountError = error,
    showInstallments = settings.installmentsAllowed,
    installmentsOn = form.installmentsOn,
    installments = form.installments,
    showCurrency = settings.currencyAllowed,
    currency = form.currency,
    showSignature = settings.signatureAllowed,
    signatureOn = form.signatureOn,
)
