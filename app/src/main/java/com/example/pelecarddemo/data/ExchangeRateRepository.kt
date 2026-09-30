package com.example.pelecarddemo.data

import com.example.pelecarddemo.data.remote.ExchangeRateApi
import java.math.BigDecimal
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Fetches live exchange rates. Failures are wrapped in [Result] so the ViewModel can offer a retry. */
class ExchangeRateRepository(
    private val api: ExchangeRateApi = Retrofit.Builder()
        .baseUrl("https://open.er-api.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ExchangeRateApi::class.java),
) {
    suspend fun fetchRates(baseCurrencyCode: String): Result<Map<String, BigDecimal>> = runCatching {
        api.getLatestRates(baseCurrencyCode).rates.mapValues { (_, rate) -> BigDecimal.valueOf(rate) }
    }
}
