package com.accpro.teller.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.accpro.teller.data.ApiRepository
import com.accpro.teller.data.LocalStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val localStore = remember { LocalStore(context) }
    val repository = remember { ApiRepository(localStore) }
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var companyName by remember { mutableStateOf<String?>(null) }

    // Load company name
    LaunchedEffect(Unit) {
        companyName = localStore.getCompanyName()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AccPro Teller Login") },
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
            Spacer(Modifier.height(40.dp))

            Text(
                "Team Login",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            if (companyName != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Company: $companyName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Only team members (cashiers/bankers) can log in here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            if (errorMsg != null) {
                Spacer(Modifier.height(12.dp))
                Text(errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        errorMsg = "Please enter username and password"
                        return@Button
                    }
                    loading = true
                    errorMsg = null
                    scope.launch {
                        when (val result = repository.login(username.trim(), password.trim())) {
                            is com.accpro.teller.data.ApiResult.Success -> {
                                if (result.data.success) {
                                    onLoginSuccess()
                                } else {
                                    errorMsg = result.data.message ?: "Login failed"
                                }
                            }
                            is com.accpro.teller.data.ApiResult.Error -> {
                                errorMsg = result.message
                            }
                        }
                        loading = false
                    }
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Login", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = {
                scope.launch {
                    localStore.clearSession()
                    localStore.saveApiKey("")
                }
            }) {
                Text("Reset API Key", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
