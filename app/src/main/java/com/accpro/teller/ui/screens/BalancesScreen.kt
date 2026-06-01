package com.accpro.teller.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.accpro.teller.data.ApiRepository
import com.accpro.teller.data.ApiResult
import com.accpro.teller.data.BalanceItem
import com.accpro.teller.data.LocalStore
import com.accpro.teller.ui.components.SafeCircularProgressIndicator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalancesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val localStore = remember { LocalStore(context) }
    val repository = remember { ApiRepository(localStore) }
    val scope = rememberCoroutineScope()

    var balances by remember { mutableStateOf<List<BalanceItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        when (val result = repository.getBalances()) {
            is ApiResult.Success -> {
                balances = result.data.balances ?: emptyList()
                loading = false
            }
            is ApiResult.Error -> {
                errorMsg = result.message
                loading = false
            }
        }
    }

    val cashBalances = balances.filter { it.accountType == "cash" }
    val bankBalances = balances.filter { it.accountType == "bank" || it.accountType != "cash" }
    val totalCash = cashBalances.sumOf { it.balance }
    val totalBank = bankBalances.sumOf { it.balance }
    val grandTotal = totalCash + totalBank

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cashier / Bank Balances") },
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
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SafeCircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Loading balances...")
                }
            }
        } else if (errorMsg != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: $errorMsg", color = MaterialTheme.colorScheme.error)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Summary cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Total Cash", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                                Text(
                                    String.format("%,.2f", totalCash),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Total Bank", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1565C0))
                                Text(
                                    String.format("%,.2f", totalBank),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color(0xFF0D47A1)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Grand Total", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                String.format("%,.2f", grandTotal),
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Cash Accounts",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                }

                // Cash items
                items(cashBalances) { item ->
                    BalanceRow(item)
                }

                // Bank header
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Bank Accounts",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.height(4.dp))
                }

                // Bank items
                items(bankBalances) { item ->
                    BalanceRow(item)
                }

                // Empty state
                if (balances.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No accounts found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceRow(item: BalanceItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (item.accountType == "cash") Icons.Default.Money else Icons.Default.AccountBalance,
                contentDescription = null,
                tint = if (item.accountType == "cash") Color(0xFF2E7D32) else Color(0xFF1565C0),
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.accountName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    item.accountType.replaceFirstChar { it.uppercase() },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                String.format("%,.2f", item.balance),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (item.balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}
