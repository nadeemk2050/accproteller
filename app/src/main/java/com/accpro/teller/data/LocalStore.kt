package com.accpro.teller.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "accpro_teller")

class LocalStore(private val context: Context) {

    companion object {
        private val KEY_API_KEY = stringPreferencesKey("api_key")
        private val KEY_API_BASE_URL = stringPreferencesKey("api_base_url")
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_TEAM_USER_ID = stringPreferencesKey("team_user_id")
        private val KEY_TEAM_USER_NAME = stringPreferencesKey("team_user_name")
        private val KEY_COMPANY_ID = stringPreferencesKey("company_id")
        private val KEY_COMPANY_NAME = stringPreferencesKey("company_name")
        private val KEY_TEAM_LIST = stringPreferencesKey("team_list")
        private val KEY_API_CONNECTED_AT = stringPreferencesKey("api_connected_at")
        private val KEY_API_DATA_SENT = longPreferencesKey("api_data_sent")
        private val KEY_API_DATA_RECEIVED = longPreferencesKey("api_data_received")
    }

    // API key
    val apiKeyFlow: Flow<String?> = context.dataStore.data.map { it[KEY_API_KEY] }
    suspend fun saveApiKey(key: String) = context.dataStore.edit { it[KEY_API_KEY] = key }
    suspend fun getApiKey(): String? = apiKeyFlow.first()

    // Base URL
    val baseUrlFlow: Flow<String?> = context.dataStore.data.map { it[KEY_API_BASE_URL] }
    suspend fun saveBaseUrl(url: String) = context.dataStore.edit { it[KEY_API_BASE_URL] = url }
    suspend fun getBaseUrl(): String? = baseUrlFlow.first()

    // Auth token (returned after login)
    val authTokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_AUTH_TOKEN] }
    suspend fun saveAuthToken(token: String) = context.dataStore.edit { it[KEY_AUTH_TOKEN] = token }
    suspend fun getAuthToken(): String? = authTokenFlow.first()

    // Team user info
    val teamUserIdFlow: Flow<String?> = context.dataStore.data.map { it[KEY_TEAM_USER_ID] }
    suspend fun saveTeamUserId(id: String) = context.dataStore.edit { it[KEY_TEAM_USER_ID] = id }
    suspend fun getTeamUserId(): String? = teamUserIdFlow.first()

    val teamUserNameFlow: Flow<String?> = context.dataStore.data.map { it[KEY_TEAM_USER_NAME] }
    suspend fun saveTeamUserName(name: String) = context.dataStore.edit { it[KEY_TEAM_USER_NAME] = name }
    suspend fun getTeamUserName(): String? = teamUserNameFlow.first()

    // Company info
    val companyIdFlow: Flow<String?> = context.dataStore.data.map { it[KEY_COMPANY_ID] }
    suspend fun saveCompanyId(id: String) = context.dataStore.edit { it[KEY_COMPANY_ID] = id }
    suspend fun getCompanyId(): String? = companyIdFlow.first()

    val companyNameFlow: Flow<String?> = context.dataStore.data.map { it[KEY_COMPANY_NAME] }
    suspend fun saveCompanyName(name: String) = context.dataStore.edit { it[KEY_COMPANY_NAME] = name }
    suspend fun getCompanyName(): String? = companyNameFlow.first()

    val teamListFlow: Flow<String?> = context.dataStore.data.map { it[KEY_TEAM_LIST] }
    suspend fun saveTeamList(json: String) = context.dataStore.edit { it[KEY_TEAM_LIST] = json }
    suspend fun getTeamList(): String? = teamListFlow.first()

    val apiConnectedAtFlow: Flow<String?> = context.dataStore.data.map { it[KEY_API_CONNECTED_AT] }
    suspend fun saveApiConnectedAt(timestamp: String) = context.dataStore.edit { it[KEY_API_CONNECTED_AT] = timestamp }
    suspend fun getApiConnectedAt(): String? = apiConnectedAtFlow.first()

    val apiDataSentFlow: Flow<Long> = context.dataStore.data.map { it[KEY_API_DATA_SENT] ?: 0L }
    suspend fun incrementApiDataSent(bytes: Long) = context.dataStore.edit { 
        val current = it[KEY_API_DATA_SENT] ?: 0L
        it[KEY_API_DATA_SENT] = current + bytes
    }
    suspend fun getApiDataSent(): Long = apiDataSentFlow.first()

    val apiDataReceivedFlow: Flow<Long> = context.dataStore.data.map { it[KEY_API_DATA_RECEIVED] ?: 0L }
    suspend fun incrementApiDataReceived(bytes: Long) = context.dataStore.edit { 
        val current = it[KEY_API_DATA_RECEIVED] ?: 0L
        it[KEY_API_DATA_RECEIVED] = current + bytes
    }
    suspend fun getApiDataReceived(): Long = apiDataReceivedFlow.first()

    // Check if API key is configured
    val isApiKeyConfigured: Flow<Boolean> = apiKeyFlow.map { !it.isNullOrBlank() }

    // Check if logged in
    val isLoggedIn: Flow<Boolean> = authTokenFlow.map { !it.isNullOrBlank() }

    // Clear all session data (logout)
    suspend fun clearSession() = context.dataStore.edit {
        it.remove(KEY_AUTH_TOKEN)
        it.remove(KEY_TEAM_USER_ID)
        it.remove(KEY_TEAM_USER_NAME)
        it.remove(KEY_COMPANY_ID)
        it.remove(KEY_COMPANY_NAME)
        it.remove(KEY_TEAM_LIST)
    }
}
