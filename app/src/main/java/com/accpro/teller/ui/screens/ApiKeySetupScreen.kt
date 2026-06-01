package com.accpro.teller.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.accpro.teller.data.LocalStore
import com.accpro.teller.data.ApiRepository
import com.accpro.teller.data.ApiResult
import com.accpro.teller.data.TeamMember
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeySetupScreen(onApiKeySaved: () -> Unit) {
    val context = LocalContext.current
    val localStore = remember { LocalStore(context) }
    val repository = remember { ApiRepository(localStore) }
    val scope = rememberCoroutineScope()

    var apiKey by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var testing by remember { mutableStateOf(false) }
    var validated by remember { mutableStateOf(false) }
    var companyName by remember { mutableStateOf<String?>(null) }
    var teamList by remember { mutableStateOf<List<TeamMember>>(emptyList()) }

    // Check if already configured
    LaunchedEffect(Unit) {
        try {
            val existingKey = localStore.getApiKey()
            val existingUrl = localStore.getBaseUrl()
            if (!existingKey.isNullOrBlank() && !existingUrl.isNullOrBlank()) {
                onApiKeySaved()
            }
        } catch (e: Exception) {
            android.util.Log.e("AccProTeller", "API key check failed", e)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AccPro Teller Setup") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Text(
                "API Configuration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Enter your AccPro server URL and API key to connect.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it; validated = false },
                label = { Text("Server URL") },
                placeholder = { Text("https://cashshams.web.app") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it; validated = false },
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showApiKey = !showApiKey }) {
                        Text(if (showApiKey) "Hide" else "Show", fontSize = 12.sp)
                    }
                }
            )

            if (errorMsg != null) {
                Spacer(Modifier.height(8.dp))
                Text(errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            // Validation success info
            if (validated && companyName != null) {
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "✅ Connected — $companyName",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (teamList.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${teamList.size} team member(s) found:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            teamList.forEach { member ->
                                val memberName = member.name?.takeIf { it.isNotBlank() } ?: "Unknown"
                                val memberRole = member.role?.takeIf { it.isNotBlank() } ?: "member"
                                Text(
                                    "  • $memberName ($memberRole)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (apiKey.isBlank() || baseUrl.isBlank()) {
                        errorMsg = "Please fill in both fields"
                        return@Button
                    }
                    loading = true
                    errorMsg = null
                    scope.launch {
                        try {
                            val url = baseUrl.trim().trimEnd('/')
                            localStore.saveBaseUrl(url)
                            localStore.saveApiKey(apiKey.trim())
                            onApiKeySaved()
                        } catch (e: Exception) {
                            errorMsg = "Error: ${e.message}"
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Save & Continue", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = {
                if (loading || testing) return@TextButton
                if (apiKey.isBlank() || baseUrl.isBlank()) {
                    errorMsg = "Please fill in both fields"
                    return@TextButton
                }
                testing = true
                errorMsg = null
                validated = false
                scope.launch {
                    try {
                        val url = baseUrl.trim().trimEnd('/')
                        localStore.saveBaseUrl(url)
                        localStore.saveApiKey(apiKey.trim())

                        when (val result = repository.validateKey(apiKey.trim())) {
                            is ApiResult.Success -> {
                                if (result.data.success) {
                                    companyName = result.data.companyName ?: "AccountsPro"
                                    teamList = result.data.team ?: emptyList()
                                    validated = true
                                    errorMsg = null
                                } else {
                                    errorMsg = result.data.message ?: "Invalid API key"
                                }
                            }
                            is ApiResult.Error -> {
                                errorMsg = "Connection failed: ${result.message}"
                            }
                        }
                    } catch (e: Throwable) {
                        android.util.Log.e("AccProTeller", "Test connection crash prevented", e)
                        errorMsg = "Error: ${e.message}"
                    } finally {
                        testing = false
                    }
                }
            }) {
                Text(if (testing) "Testing..." else if (validated) "✓ Verified - Test Again" else "Test Connection", fontSize = 12.sp)
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = {
                scope.launch {
                    localStore.clearSession()
                    localStore.saveApiKey("")
                    localStore.saveBaseUrl("")
                }
            }) {
                Text("Reset All Data", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        }
    }
}
