package com.example.pelecarddemo.ui.conversion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pelecarddemo.data.ExchangeRateRepository
import com.example.pelecarddemo.domain.ConversionRate
import com.example.pelecarddemo.domain.ConversionRules
import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ConversionUiState {
    data object Loading : ConversionUiState
    data object Error : ConversionUiState
    data class Success(
        val amount: BigDecimal,
        val baseCurrencyCode: String,
        val rates: List<ConversionRate>,
        val fetchedAtMillis: Long,
    ) : ConversionUiState
}

/**
 * Owns the Convert screen's network call. Scoped to the Conversion back-stack entry (via
 * `viewModel()` in [ConversionRoute]), so it is created once per visit to the screen and survives
 * rotation: [load] only runs again if the caller explicitly asks for a [retry].
 */
class ConversionViewModel(
    private val repository: ExchangeRateRepository,
    private val amount: BigDecimal,
    private val baseCurrencyCode: String,
) : ViewModel() {

    private val _state = MutableStateFlow<ConversionUiState>(ConversionUiState.Loading)
    val state: StateFlow<ConversionUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        _state.value = ConversionUiState.Loading
        viewModelScope.launch {
            repository.fetchRates(baseCurrencyCode)
                .onSuccess { rates ->
                    _state.value = ConversionUiState.Success(
                        amount = amount,
                        baseCurrencyCode = baseCurrencyCode,
                        rates = ConversionRules.buildConversionRates(amount, baseCurrencyCode, rates),
                        fetchedAtMillis = System.currentTimeMillis(),
                    )
                }
                .onFailure { _state.value = ConversionUiState.Error }
        }
    }
}
