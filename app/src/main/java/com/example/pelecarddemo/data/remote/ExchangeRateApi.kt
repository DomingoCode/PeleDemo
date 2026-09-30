package com.example.pelecarddemo.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path

/** https://www.exchangerate-api.com/docs/free — free tier, no API key required. */
interface ExchangeRateApi {
    @GET("v6/latest/{base}")
    suspend fun getLatestRates(@Path("base") baseCurrencyCode: String): ExchangeRateResponse
}

data class ExchangeRateResponse(
    @SerializedName("result") val result: String,
    @SerializedName("base_code") val baseCode: String,
    @SerializedName("rates") val rates: Map<String, Double>,
)
