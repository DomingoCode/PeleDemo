package com.example.pelecarddemo.ui.conversion

import com.example.pelecarddemo.data.ExchangeRateRepository
import com.example.pelecarddemo.data.remote.ExchangeRateApi
import com.example.pelecarddemo.data.remote.ExchangeRateResponse
import com.example.pelecarddemo.ui.MainDispatcherRule
import java.io.IOException
import java.math.BigDecimal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConversionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeApi(
        private val rates: Map<String, Double>? = null,
    ) : ExchangeRateApi {
        override suspend fun getLatestRates(baseCurrencyCode: String): ExchangeRateResponse {
            if (rates == null) throw IOException("network down")
            return ExchangeRateResponse(result = "success", baseCode = baseCurrencyCode, rates = rates)
        }
    }

    @Test
    fun load_onSuccess_emitsConversionRates() = runTest {
        val api = FakeApi(rates = mapOf("USD" to 0.28, "EUR" to 0.26, "GBP" to 0.22, "JPY" to 3.07, "INR" to 2.02))
        val viewModel = ConversionViewModel(ExchangeRateRepository(api), BigDecimal("50"), "ILS")

        val state = viewModel.state.value
        assertTrue(state is ConversionUiState.Success)
        state as ConversionUiState.Success
        assertEquals(5, state.rates.size)
        assertEquals(BigDecimal("14.00"), state.rates.first { it.currencyCode == "USD" }.convertedAmount)
    }

    @Test
    fun load_onFailure_emitsError() = runTest {
        val viewModel = ConversionViewModel(ExchangeRateRepository(FakeApi()), BigDecimal("50"), "ILS")

        assertEquals(ConversionUiState.Error, viewModel.state.value)
    }

    @Test
    fun retry_afterFailure_canSucceed() = runTest {
        var succeed = false
        val api = object : ExchangeRateApi {
            override suspend fun getLatestRates(baseCurrencyCode: String): ExchangeRateResponse {
                if (!succeed) throw IOException("network down")
                return ExchangeRateResponse("success", baseCurrencyCode, mapOf("USD" to 0.28))
            }
        }
        val viewModel = ConversionViewModel(ExchangeRateRepository(api), BigDecimal("50"), "ILS")
        assertEquals(ConversionUiState.Error, viewModel.state.value)

        succeed = true
        viewModel.retry()

        assertTrue(viewModel.state.value is ConversionUiState.Success)
    }
}
