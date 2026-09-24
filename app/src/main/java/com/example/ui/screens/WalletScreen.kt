package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PaymentMethodEntity
import com.example.data.model.UserEntity
import com.example.data.model.WithdrawalEntity
import com.example.ui.components.AntiBotCaptchaDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.TaskEarnViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WalletScreen(
    viewModel: TaskEarnViewModel,
    user: UserEntity,
    onNavigateToWithdrawalHistory: () -> Unit = {}
) {
    val settings by viewModel.appSettings.collectAsState()
    val withdrawals by viewModel.userWithdrawals.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()
    val transactions by viewModel.userTransactions.collectAsState()

    val rate = if (settings.coinRatePerRupee <= 0) 100 else settings.coinRatePerRupee
    val balanceRupees = user.coins.toDouble() / rate

    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var showCaptchaForWithdrawal by remember { mutableStateOf(false) }

    // Withdrawal Form State
    var withdrawAmountStr by remember { mutableStateOf("50") }
    var withdrawMethod by remember { mutableStateOf("UPI") } // "UPI" or "BANK"
    var selectedMethodEntity by remember { mutableStateOf<PaymentMethodEntity?>(null) }
    var customIdentifier by remember { mutableStateOf("") }
    var customHolderName by remember { mutableStateOf(user.name) }
    var withdrawError by remember { mutableStateOf<String?>(null) }

    // Add Payment Form State
    var newPayType by remember { mutableStateOf("UPI") }
    var newIdentifier by remember { mutableStateOf("") }
    var newHolderName by remember { mutableStateOf(user.name) }
    var newIfsc by remember { mutableStateOf("") }
    var newBankName by remember { mutableStateOf("") }
    var addPayError by remember { mutableStateOf<String?>(null) }

    var selectedTab by remember { mutableStateOf(0) } // 0: Withdrawals, 1: Ledger, 2: Payment Methods

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("wallet_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Top Header
        item {
            Column {
                Text(
                    text = "My Wallet & Payouts",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Transparent coin conversion & verified payouts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Wallet Balance Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth().testTag("wallet_balance_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Total Balance", style = MaterialTheme.typography.labelMedium, color = Slate400)
                            Text(
                                text = "₹${String.format("%.2f", balanceRupees)}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Surface(
                            color = Gold500.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = Gold400,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${user.coins} Coins",
                                    color = Gold400,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Conversion Info Card
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Conversion Rate: 1000 Coins = ₹${(1000.0 / rate).toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFCBD5E1),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Min. ₹${settings.minWithdrawalRupees.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Emerald500,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                withdrawError = null
                                showWithdrawDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(46.dp).testTag("open_withdrawal_btn")
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Request Payout", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                addPayError = null
                                showAddPaymentDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(46.dp).testTag("add_payment_method_btn")
                        ) {
                            Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Method", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dedicated Quick Shortcut to Detailed Withdrawal History
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToWithdrawalHistory() }
                            .testTag("open_detailed_withdrawal_history_btn")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HistoryEdu,
                                    contentDescription = null,
                                    tint = Gold400,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Detailed Withdrawal History",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${withdrawals.size} records",
                                    fontSize = 11.sp,
                                    color = Slate400
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sub-tabs: Withdrawals / Ledger / Payment Methods
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Payouts (${withdrawals.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Ledger (${transactions.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Methods (${paymentMethods.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }
        }

        // TAB 0: WITHDRAWAL HISTORY
        if (selectedTab == 0) {
            // Header banner with quick link to full Detailed History screen
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Withdrawals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(
                        onClick = { onNavigateToWithdrawalHistory() },
                        modifier = Modifier.testTag("view_all_withdrawals_btn")
                    ) {
                        Text(
                            text = "View Detailed History",
                            color = Emerald500,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Emerald500,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (withdrawals.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No withdrawal requests submitted yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(withdrawals) { w ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToWithdrawalHistory() }
                            .testTag("withdrawal_item_${w.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "₹${String.format("%.2f", w.amountInRupees)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${w.coinsDeducted} Coins used",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Gold500
                                    )
                                }
                                StatusBadge(status = w.status)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "To: ${w.method} • ${w.accountDetailsMasked}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(w.requestedAt)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (w.utrTransactionId.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "UTR / TXN ID: ${w.utrTransactionId}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald500,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (w.adminNote.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Note: ${w.adminNote}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // TAB 1: COIN TRANSACTION LEDGER
        if (selectedTab == 1) {
            if (transactions.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No transactions recorded yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(transactions) { tx ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (tx.amount > 0) Emerald500.copy(alpha = 0.15f) else Red500.copy(alpha = 0.15f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (tx.amount > 0) Icons.Default.Add else Icons.Default.Remove,
                                        contentDescription = null,
                                        tint = if (tx.amount > 0) Emerald500 else Red500,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = tx.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = "${if (tx.amount > 0) "+" else ""}${tx.amount} Coins",
                                fontWeight = FontWeight.Bold,
                                color = if (tx.amount > 0) Emerald500 else Red500,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // TAB 2: SAVED PAYMENT METHODS
        if (selectedTab == 2) {
            if (paymentMethods.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No payment methods saved. Tap 'Add Method' above.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(paymentMethods) { pm ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("payment_method_${pm.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (pm.type == "UPI") Icons.Default.QrCode else Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${pm.type}: ${pm.maskedIdentifier}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Holder: ${pm.holderName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (pm.isDefault) {
                                Surface(
                                    color = Emerald500.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "DEFAULT",
                                        color = Emerald500,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Request Withdrawal Dialog
    if (showWithdrawDialog) {
        Dialog(onDismissRequest = { showWithdrawDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(12.dp).testTag("withdrawal_dialog")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Request Payout",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Min ₹${settings.minWithdrawalRupees.toInt()} (Available: ₹${String.format("%.2f", balanceRupees)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Method Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = withdrawMethod == "UPI",
                            onClick = { withdrawMethod = "UPI" },
                            label = { Text("UPI Payout") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = withdrawMethod == "BANK",
                            onClick = { withdrawMethod = "BANK" },
                            label = { Text("Bank Transfer") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = withdrawAmountStr,
                        onValueChange = { withdrawAmountStr = it; withdrawError = null },
                        label = { Text("Withdrawal Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("withdraw_amount_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customIdentifier,
                        onValueChange = { customIdentifier = it; withdrawError = null },
                        label = { Text(if (withdrawMethod == "UPI") "UPI ID (e.g. name@oksbi)" else "Bank Account Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("withdraw_identifier_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customHolderName,
                        onValueChange = { customHolderName = it; withdrawError = null },
                        label = { Text("Account Holder Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("withdraw_holder_input")
                    )

                    if (withdrawError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = withdrawError ?: "", color = Red500, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showWithdrawDialog = false },
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val amt = withdrawAmountStr.toDoubleOrNull()
                                if (amt == null || amt < settings.minWithdrawalRupees) {
                                    withdrawError = "Minimum withdrawal is ₹${settings.minWithdrawalRupees}"
                                    return@Button
                                }
                                if (amt > balanceRupees) {
                                    withdrawError = "Insufficient balance."
                                    return@Button
                                }
                                if (customIdentifier.isBlank()) {
                                    withdrawError = "Please enter valid payment details."
                                    return@Button
                                }
                                // Trigger anti-bot CAPTCHA before final submission
                                showCaptchaForWithdrawal = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            modifier = Modifier.weight(1f).height(46.dp).testTag("submit_withdrawal_button")
                        ) {
                            Text("Verify & Submit", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal: Add Payment Method Dialog
    if (showAddPaymentDialog) {
        Dialog(onDismissRequest = { showAddPaymentDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Add Payment Method",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Secure local storage for streamlined withdrawals",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = newPayType == "UPI",
                            onClick = { newPayType = "UPI" },
                            label = { Text("UPI ID") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = newPayType == "BANK",
                            onClick = { newPayType = "BANK" },
                            label = { Text("Bank Account") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newIdentifier,
                        onValueChange = { newIdentifier = it; addPayError = null },
                        label = { Text(if (newPayType == "UPI") "UPI ID (e.g. mobile@upi)" else "Bank Account Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_method_identifier_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newHolderName,
                        onValueChange = { newHolderName = it },
                        label = { Text("Account Holder Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_method_holder_input")
                    )

                    if (newPayType == "BANK") {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newIfsc,
                            onValueChange = { newIfsc = it.uppercase() },
                            label = { Text("IFSC Code") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newBankName,
                            onValueChange = { newBankName = it },
                            label = { Text("Bank Name (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (addPayError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = addPayError ?: "", color = Red500, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddPaymentDialog = false },
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (newIdentifier.isBlank()) {
                                    addPayError = "Identifier cannot be empty."
                                    return@Button
                                }
                                viewModel.addPaymentMethod(
                                    type = newPayType,
                                    identifier = newIdentifier,
                                    holderName = newHolderName,
                                    ifsc = newIfsc,
                                    bankName = newBankName
                                ) {
                                    showAddPaymentDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            modifier = Modifier.weight(1f).height(46.dp).testTag("save_payment_method_button")
                        ) {
                            Text("Save Method", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showCaptchaForWithdrawal) {
        AntiBotCaptchaDialog(
            purpose = "Withdrawal Submission",
            onDismiss = { showCaptchaForWithdrawal = false },
            onVerified = {
                showCaptchaForWithdrawal = false
                val amt = withdrawAmountStr.toDoubleOrNull() ?: 50.0
                viewModel.submitWithdrawal(
                    amountRupees = amt,
                    method = withdrawMethod,
                    identifier = customIdentifier,
                    holderName = customHolderName,
                    captchaPassed = true
                ) { success ->
                    if (success) {
                        showWithdrawDialog = false
                    }
                }
            }
        )
    }
}
