package com.accpro.teller.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface AccProApi {
    // All teller endpoints go through one cloud function

    @POST("")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("")
    suspend fun createVoucher(
        @Header("Authorization") token: String,
        @Body request: VoucherRequest
    ): VoucherResponse

    @GET("")
    suspend fun getBalances(
        @Header("Authorization") token: String,
        @Query("action") action: String = "balances"
    ): BalanceResponse

    @GET("")
    suspend fun getAccounts(
        @Header("Authorization") token: String,
        @Query("action") action: String = "accounts"
    ): AccountsResponse

    companion object {
        fun create(baseUrl: String): AccProApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            // Build the full URL to the teller cloud function
            val tellerUrl = baseUrl.trimEnd('/').let {
                // If it's a Firebase Hosting URL, use the hosting proxy path
                if (it.contains("web.app") || it.contains("firebase")) {
                    "$it/tellerApi"
                } else {
                    "$it/tellerApi" // Cloud Function path
                }
            }
            val url = if (tellerUrl.endsWith("/")) tellerUrl else "$tellerUrl/"

            return Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AccProApi::class.java)
        }
    }
}
