package com.accpro.teller.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.accpro.teller.data.AccountItem
import com.accpro.teller.data.ApiRepository
import com.accpro.teller.data.ApiResult
import com.accpro.teller.data.LocalStore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherFormScreen(
    voucherType: String,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val localStore = remember { LocalStore(context) }
    val repository = remember { ApiRepository(localStore) }
    val scope = rememberCoroutineScope()

    val typeLabel = when (voucherType) {
        "payment" -> "Payment"
        "receipt" -> "Receipt"
        "contra" -> "Contra"
        else -> "Voucher"
    }

    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var amount by remember { mutableStateOf("") }
    var drAccountId by remember { mutableStateOf("") }
    var crAccountId by remember { mutableStateOf("") }
    var drName by remember { mutableStateOf("") }
    var crName by remember { mutableStateOf("") }
    var narration by remember { mutableStateOf("") }
    var refNo by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    var accounts by remember { mutableStateOf<List<AccountItem>>(emptyList()) }
    var accountsLoading by remember { mutableStateOf(true) }

    // Load accounts for dropdown
    LaunchedEffect(Unit) {
        when (val result = repository.getAccounts()) {
            is ApiResult.Success -> {
                accounts = result.data.accounts ?: emptyList()
            }
            is ApiResult.Error -> {
                errorMsg = "Could not load accounts: ${result.message}"
            }
        }
        accountsLoading = false
    }

    // Determine DR/CR labels based on voucher type
    val drLabel = when (voucherType) {
        "payment" -> "Debit (Party/Expense)"
        "receipt" -> "Debit (Cash/Bank)"
        "contra" -> "Debit Account"
        else -> "Debit"
    }
    val crLabel = when (voucherType) {
        "payment" -> "Credit (Cash/Bank)"
        "receipt" -> "Credit (Party/Income)"
        "contra" -> "Credit Account"
        else -> "Credit"
    }

    // Filter: for payment receipt, DR side might be a different account type
    // For simplicity, show all accounts and let user pick
    val accountOptions = accounts.map { a ->
        "${a.name}${if (a.type != null) " (${a.type})" else ""}"
    }
    val accountIdMap = accounts.associate { a ->
        "${a.name}${if (a.type != null) " (${a.type})" else ""}" to a.id
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$typeLabel Voucher") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Date
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            // DR Account Dropdown
            if (accountsLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                var drExpanded by remember { mutableStateOf(false) }
                var drSearch by remember { mutableStateOf("") }
                val filteredDr = if (drSearch.isBlank()) accountOptions
                    else accountOptions.filter { it.contains(drSearch, ignoreCase = true) }

                ExposedDropdownMenuBox(
                    expanded = drExpanded,
                    onExpandedChange = { drExpanded = it }
                ) {
                    OutlinedTextField(
                        value = drName,
                        onValueChange = {
                            drName = it
                            drSearch = it
                            drExpanded = true
                            drAccountId = accountIdMap[it] ?: ""
                        },
                        label = { Text(drLabel) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = drExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = drExpanded,
                        onDismissRequest = { drExpanded = false }
                    ) {
                        filteredDr.take(50).forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontSize = MaterialTheme.typography.bodySmall.fontSize) },
                                onClick = {
                                    drName = option
                                    drAccountId = accountIdMap[option] ?: ""
                                    drExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // CR Account Dropdown
            if (accountsLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                var crExpanded by remember { mutableStateOf(false) }
                var crSearch by remember { mutableStateOf("") }
                val filteredCr = if (crSearch.isBlank()) accountOptions
                    else accountOptions.filter { it.contains(crSearch, ignoreCase = true) }

                ExposedDropdownMenuBox(
                    expanded = crExpanded,
                    onExpandedChange = { crExpanded = it }
                ) {
                    OutlinedTextField(
                        value = crName,
                        onValueChange = {
                            crName = it
                            crSearch = it
                            crExpanded = true
                            crAccountId = accountIdMap[it] ?: ""
                        },
                        label = { Text(crLabel) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = crExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = crExpanded,
                        onDismissRequest = { crExpanded = false }
                    ) {
                        filteredCr.take(50).forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontSize = MaterialTheme.typography.bodySmall.fontSize) },
                                onClick = {
                                    crName = option
                                    crAccountId = accountIdMap[option] ?: ""
                                    crExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Narration
            OutlinedTextField(
                value = narration,
                onValueChange = { narration = it },
                label = { Text("Narration (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(Modifier.height(12.dp))

            // Ref No (optional)
            OutlinedTextField(
                value = refNo,
                onValueChange = { refNo = it },
                label = { Text("Ref No (optional, auto if empty)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (errorMsg != null) {
                Spacer(Modifier.height(12.dp))
                Text(errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            if (successMsg != null) {
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        successMsg!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull()
                    if (amountVal == null || amountVal <= 0) {
                        errorMsg = "Enter a valid amount"
                        return@Button
                    }
                    if (drAccountId.isBlank() || crAccountId.isBlank()) {
                        errorMsg = "Select both DR and CR accounts"
                        return@Button
                    }
                    if (drAccountId == crAccountId && voucherType != "contra") {
                        // For contra it's allowed (same bank/cash diff)
                    }

                    loading = true
                    errorMsg = null
                    successMsg = null
                    scope.launch {
                        when (val result = repository.createVoucher(
                            type = voucherType,
                            date = date,
                            amount = amountVal,
                            drAccountId = drAccountId,
                            crAccountId = crAccountId,
                            drName = drName,
                            crName = crName,
                            narration = narration.ifBlank { null },
                            refNo = refNo.ifBlank { null }
                        )) {
                            is ApiResult.Success -> {
                                if (result.data.success) {
                                    successMsg = "✅ Voucher created! Ref: ${result.data.refNo ?: result.data.voucherId}"
                                    // Clear form after short delay
                                    // After showing success, navigate back
                                    kotlinx.coroutines.delay(1500)
                                    onSaved()
                                } else {
                                    errorMsg = result.data.message ?: "Failed to create voucher"
                                }
                            }
                            is ApiResult.Error -> {
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
                else Text("Save $typeLabel Voucher", fontWeight = FontWeight.Bold)
            }
        }
    }
}
