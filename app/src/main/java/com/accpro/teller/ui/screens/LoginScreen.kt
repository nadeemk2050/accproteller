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
import androidx.compose.ui.unit.sp
import com.accpro.teller.data.ApiRepository
import com.accpro.teller.data.LocalStore
import com.accpro.teller.ui.components.SafeCircularProgressIndicator
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person

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
    var teamList by remember { mutableStateOf<List<com.accpro.teller.data.TeamMember>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }

    // Load company name and team list
    LaunchedEffect(Unit) {
        try {
            companyName = localStore.getCompanyName()
            val teamJson = localStore.getTeamList()
            if (!teamJson.isNullOrBlank()) {
                val type = object : com.google.gson.reflect.TypeToken<List<com.accpro.teller.data.TeamMember>>() {}.type
                teamList = com.google.gson.Gson().fromJson(teamJson, type)
            }
            
            // If the team list is empty, fetch it silently using the saved API key!
            if (teamList.isEmpty()) {
                val apiKey = localStore.getApiKey()
                if (!apiKey.isNullOrBlank()) {
                    when (val result = repository.validateKey(apiKey)) {
                        is com.accpro.teller.data.ApiResult.Success -> {
                            if (result.data.success) {
                                val fetchedTeam = result.data.team ?: emptyList()
                                teamList = fetchedTeam
                                companyName = result.data.companyName ?: companyName
                                localStore.saveTeamList(com.google.gson.Gson().toJson(fetchedTeam))
                                if (result.data.companyName != null) {
                                    localStore.saveCompanyName(result.data.companyName)
                                }
                            }
                        }
                        else -> { /* Ignore error, user can still type manually */ }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AccProTeller", "Failed to load cached data or fetch team", e)
        }
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
                "✅ API Connected — You can login",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            if (teamList.isNotEmpty()) {
                Text(
                    "Quick Select User:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(teamList) { member ->
                        val memberName = member.name ?: "Unknown"
                        val isSelected = username.equals(memberName, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { username = memberName },
                            label = { Text(memberName, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        expanded = true 
                    },
                    label = { Text("Username") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )

                val filteredMembers = teamList.filter { 
                    val name = it.name ?: ""
                    name.contains(username, ignoreCase = true)
                }

                if (filteredMembers.isNotEmpty() || teamList.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        val displayList = if (username.isBlank()) teamList else filteredMembers
                        displayList.forEach { member ->
                            val memberName = member.name ?: ""
                            val roleLabel = member.role?.takeIf { it.isNotBlank() } ?: "member"
                            DropdownMenuItem(
                                text = { Text("$memberName ($roleLabel)") },
                                onClick = {
                                    username = memberName
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

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
                if (loading) SafeCircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
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
