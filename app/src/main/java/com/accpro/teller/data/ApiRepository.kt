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

    suspend fun validateKey(apiKey: String): ApiResult<ValidateKeyResponse> =
        withContext(Dispatchers.IO) {
            try {
                val api = getApi() ?: return@withContext ApiResult.Error("API not configured.")
                localStore.incrementApiDataSent(150L)
                val response = api.validateKey(ValidateKeyRequest(apiKey = apiKey))
                localStore.incrementApiDataReceived(1000L)
                
                if (response.success) {
                    val currentConnected = localStore.getApiConnectedAt()
                    if (currentConnected.isNullOrBlank()) {
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                        localStore.saveApiConnectedAt(sdf.format(java.util.Date()))
                    }
                }

                // Normalize nullable payload values so UI rendering remains crash-safe.
                val safeTeam = (response.team ?: emptyList()).map { member ->
                    TeamMember(
                        id = member.id ?: "",
                        name = member.name ?: "Unknown",
                        email = member.email ?: "",
                        role = member.role ?: "member"
                    )
                }

                val safeResponse = response.copy(
                    companyName = response.companyName ?: "AccountsPro",
                    team = safeTeam,
                    teamCount = response.teamCount ?: safeTeam.size,
                    message = response.message ?: if (response.success) "Connection successful" else "Validation failed"
                )

                ApiResult.Success(safeResponse)
            } catch (e: Exception) {
                android.util.Log.e("AccProTeller", "Validate key failed", e)
                ApiResult.Error(e.message ?: "Failed to validate API key")
            }
        }

    suspend fun login(username: String, password: String): ApiResult<LoginResponse> =
        withContext(Dispatchers.IO) {
            try {
                val api = getApi() ?: return@withContext ApiResult.Error("API not configured. Set API key first.")
                val apiKey = localStore.getApiKey() ?: return@withContext ApiResult.Error("API key not found.")
                localStore.incrementApiDataSent(200L)
                val response = api.login(LoginRequest(apiKey = apiKey, username = username, password = password))
                localStore.incrementApiDataReceived(500L)
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

    suspend fun registerDevice(): ApiResult<RegisterDeviceResponse> =
        withContext(Dispatchers.IO) {
            try {
                val api = getApi() ?: return@withContext ApiResult.Error("API not configured.")
                val apiKey = localStore.getApiKey() ?: return@withContext ApiResult.Error("API key not found.")
                val deviceName = android.os.Build.MODEL
                val deviceInfo = "Android ${android.os.Build.VERSION.RELEASE} / ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
                localStore.incrementApiDataSent(200L)
                val response = api.registerDevice(RegisterDeviceRequest(
                    apiKey = apiKey,
                    deviceName = deviceName,
                    deviceInfo = deviceInfo
                ))
                localStore.incrementApiDataReceived(300L)

                // Save connected timestamp locally
                val currentConnected = localStore.getApiConnectedAt()
                if (currentConnected.isNullOrBlank()) {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                    localStore.saveApiConnectedAt(sdf.format(java.util.Date()))
                }

                ApiResult.Success(response)
            } catch (e: Exception) {
                Log.e("ApiRepository", "Register device failed", e)
                // Non-critical - validation already succeeded, this is just for tracking
                ApiResult.Error(e.message ?: "Failed to register device")
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
            localStore.incrementApiDataSent(600L)
            val response = api.createVoucher("Bearer $token", request)
            localStore.incrementApiDataReceived(250L)
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
            localStore.incrementApiDataSent(50L)
            val response = api.getBalances("Bearer $token")
            localStore.incrementApiDataReceived(1500L)
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
            localStore.incrementApiDataSent(50L)
            val response = api.getAccounts("Bearer $token")
            localStore.incrementApiDataReceived(3000L)
            ApiResult.Success(response)
        } catch (e: Exception) {
            Log.e("ApiRepository", "Get accounts failed", e)
            ApiResult.Error(e.message ?: "Failed to fetch accounts")
        }
    }
}
