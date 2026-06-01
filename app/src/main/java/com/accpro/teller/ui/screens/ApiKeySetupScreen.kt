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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeySetupScreen(onApiKeySaved: () -> Unit) {
    val context = LocalContext.current
    val localStore = remember { LocalStore(context) }
    val scope = rememberCoroutineScope()

    var apiKey by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    // Check if already configured
    LaunchedEffect(Unit) {
        val existingKey = localStore.getApiKey()
        val existingUrl = localStore.getBaseUrl()
        if (!existingKey.isNullOrBlank() && !existingUrl.isNullOrBlank()) {
            onApiKeySaved()
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
            Spacer(Modifier.height(32.dp))

            Text(
                "API Configuration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Enter the API key issued from AccPro and the server URL.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("Server URL") },
                placeholder = { Text("https://cashshams.web.app") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
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

            Spacer(Modifier.height(24.dp))

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
                            localStore.saveApiKey(apiKey.trim())
                            val url = baseUrl.trim().trimEnd('/')
                            localStore.saveBaseUrl(url)
                            onApiKeySaved()
                        } catch (e: Exception) {
                            errorMsg = "Failed to save: ${e.message}"
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
        }
    }
}
