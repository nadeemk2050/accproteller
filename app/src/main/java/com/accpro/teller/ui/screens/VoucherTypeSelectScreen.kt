package com.accpro.teller.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class VoucherType(
    val type: String,
    val label: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

private val voucherTypes = listOf(
    VoucherType("payment", "Payment", "Cash/Bank payment to party or expense", Icons.Default.ArrowUpward, Color(0xFFE53935)),
    VoucherType("receipt", "Receipt", "Cash/Bank receipt from party or income", Icons.Default.ArrowDownward, Color(0xFF43A047)),
    VoucherType("contra", "Contra", "Transfer between cash and bank accounts", Icons.Default.SwapHoriz, Color(0xFFFB8C00))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherTypeSelectScreen(
    onVoucherSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Voucher Type") },
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
            Spacer(Modifier.height(8.dp))

            voucherTypes.forEach { vt ->
                Card(
                    onClick = { onVoucherSelected(vt.type) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = vt.color.copy(alpha = 0.15f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(vt.icon, contentDescription = null, tint = vt.color, modifier = Modifier.size(28.dp))
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(vt.label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(vt.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
