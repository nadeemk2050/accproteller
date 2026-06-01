package com.accpro.teller.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}

class ApiRepository(private val localStore: LocalStore) {

    private suspend fun getApi(): AccProApi? {
        val baseUrl = localStore.getBaseUrl() ?: return null
        return AccProApi.create(baseUrl)
    }

    suspend fun login(username: String, password: String): ApiResult<LoginResponse> =
        withContext(Dispatchers.IO) {
            try {
                val api = getApi() ?: return@withContext ApiResult.Error("API not configured. Set API key first.")
                val apiKey = localStore.getApiKey() ?: return@withContext ApiResult.Error("API key not found.")
                val response = api.login(LoginRequest(apiKey, username, password))
                if (response.success && response.token != null) {
                    localStore.saveAuthToken(response.token)
                    localStore.saveTeamUserId(response.userId ?: "")
                    localStore.saveTeamUserName(response.userName ?: "")
                    localStore.saveCompanyId(response.companyId ?: "")
                    localStore.saveCompanyName(response.companyName ?: "")
                }
                ApiResult.Success(response)
            } catch (e: Exception) {
                Log.e("ApiRepository", "Login failed", e)
                ApiResult.Error(e.message ?: "Login failed")
            }
        }

    suspend fun createVoucher(
        type: String,
        date: String,
        amount: Double,
        drAccountId: String?,
        crAccountId: String?,
        drName: String?,
        crName: String?,
        narration: String?,
        refNo: String?
    ): ApiResult<VoucherResponse> = withContext(Dispatchers.IO) {
        try {
            val api = getApi() ?: return@withContext ApiResult.Error("API not configured.")
            val token = localStore.getAuthToken() ?: return@withContext ApiResult.Error("Not logged in.")
            val request = VoucherRequest(
                type = type,
                date = date,
                amount = amount,
                drAccountId = drAccountId,
                crAccountId = crAccountId,
                drName = drName,
                crName = crName,
                narration = narration,
                refNo = refNo
            )
            val response = api.createVoucher("Bearer $token", request)
            ApiResult.Success(response)
        } catch (e: Exception) {
            Log.e("ApiRepository", "Create voucher failed", e)
            ApiResult.Error(e.message ?: "Failed to create voucher")
        }
    }

    suspend fun getBalances(): ApiResult<BalanceResponse> = withContext(Dispatchers.IO) {
        try {
            val api = getApi() ?: return@withContext ApiResult.Error("API not configured.")
            val token = localStore.getAuthToken() ?: return@withContext ApiResult.Error("Not logged in.")
            val response = api.getBalances("Bearer $token")
            ApiResult.Success(response)
        } catch (e: Exception) {
            Log.e("ApiRepository", "Get balances failed", e)
            ApiResult.Error(e.message ?: "Failed to fetch balances")
        }
    }

    suspend fun getAccounts(): ApiResult<AccountsResponse> = withContext(Dispatchers.IO) {
        try {
            val api = getApi() ?: return@withContext ApiResult.Error("API not configured.")
            val token = localStore.getAuthToken() ?: return@withContext ApiResult.Error("Not logged in.")
            val response = api.getAccounts("Bearer $token")
            ApiResult.Success(response)
        } catch (e: Exception) {
            Log.e("ApiRepository", "Get accounts failed", e)
            ApiResult.Error(e.message ?: "Failed to fetch accounts")
        }
    }
}
