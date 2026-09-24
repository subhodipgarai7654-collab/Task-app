package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserEntity
import com.example.data.model.WithdrawalEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.TaskEarnViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalHistoryScreen(
    viewModel: TaskEarnViewModel,
    user: UserEntity,
    onBackClick: () -> Unit,
    onRequestPayoutClick: () -> Unit
) {
    val context = LocalContext.current
    val withdrawals by viewModel.userWithdrawals.collectAsState()
    val settings by viewModel.appSettings.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, PAID, REJECTED
    var selectedSortByNewest by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedWithdrawalForDetails by remember { mutableStateOf<WithdrawalEntity?>(null) }

    // Filter & Sort Logic
    val filteredWithdrawals = remember(withdrawals, selectedFilter, searchQuery, selectedSortByNewest) {
        withdrawals
            .filter { w ->
                val matchesFilter = when (selectedFilter) {
                    "PENDING" -> w.status == "PENDING" || w.status == "APPROVED"
                    "PAID" -> w.status == "PAID"
                    "REJECTED" -> w.status == "REJECTED"
                    else -> true
                }
                val matchesSearch = if (searchQuery.isBlank()) {
                    true
                } else {
                    val q = searchQuery.trim().lowercase()
                    w.method.lowercase().contains(q) ||
                            w.accountDetailsMasked.lowercase().contains(q) ||
                            w.accountHolderName.lowercase().contains(q) ||
                            w.utrTransactionId.lowercase().contains(q) ||
                            w.id.toString().contains(q) ||
                            String.format("%.2f", w.amountInRupees).contains(q)
                }
                matchesFilter && matchesSearch
            }
            .sortedWith(
                if (selectedSortByNewest) {
                    compareByDescending { it.requestedAt }
                } else {
                    compareBy { it.requestedAt }
                }
            )
    }

    // Status Counts & Totals
    val totalPaidRupees = remember(withdrawals) {
        withdrawals.filter { it.status == "PAID" }.sumOf { it.amountInRupees }
    }
    val pendingRupees = remember(withdrawals) {
        withdrawals.filter { it.status == "PENDING" || it.status == "APPROVED" }.sumOf { it.amountInRupees }
    }
    val pendingCount = remember(withdrawals) {
        withdrawals.count { it.status == "PENDING" || it.status == "APPROVED" }
    }
    val paidCount = remember(withdrawals) {
        withdrawals.count { it.status == "PAID" }
    }
    val rejectedCount = remember(withdrawals) {
        withdrawals.count { it.status == "REJECTED" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Withdrawal History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${withdrawals.size} Total Transaction${if (withdrawals.size == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("withdrawal_history_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { selectedSortByNewest = !selectedSortByNewest },
                        modifier = Modifier.testTag("toggle_sort_button")
                    ) {
                        Icon(
                            imageVector = if (selectedSortByNewest) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = if (selectedSortByNewest) "Sort Newest First" else "Sort Oldest First",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.testTag("withdrawal_history_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
        ) {
            // Summary KPI Cards (Total Disbursed vs Pending in escrow)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WithdrawalSummaryCard(
                        title = "Total Received",
                        amountText = "₹${String.format("%.2f", totalPaidRupees)}",
                        countText = "$paidCount Paid",
                        icon = Icons.Default.CheckCircle,
                        color = Emerald500,
                        modifier = Modifier.weight(1f)
                    )
                    WithdrawalSummaryCard(
                        title = "In Processing",
                        amountText = "₹${String.format("%.2f", pendingRupees)}",
                        countText = "$pendingCount Pending",
                        icon = Icons.Default.HourglassTop,
                        color = Gold500,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search Filter Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                            }
                        }
                    },
                    placeholder = { Text("Search by ID, UTR, UPI, or Bank...", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Emerald500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdrawal_history_search")
                )
            }

            // Status Filter Chips (ALL, PENDING, PAID, REJECTED)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusFilterChip(
                        label = "All (${withdrawals.size})",
                        selected = selectedFilter == "ALL",
                        color = Slate400,
                        onClick = { selectedFilter = "ALL" },
                        testTag = "filter_chip_all"
                    )
                    StatusFilterChip(
                        label = "Pending ($pendingCount)",
                        selected = selectedFilter == "PENDING",
                        color = Gold500,
                        onClick = { selectedFilter = "PENDING" },
                        testTag = "filter_chip_pending"
                    )
                    StatusFilterChip(
                        label = "Paid ($paidCount)",
                        selected = selectedFilter == "PAID",
                        color = Emerald500,
                        onClick = { selectedFilter = "PAID" },
                        testTag = "filter_chip_paid"
                    )
                    StatusFilterChip(
                        label = "Rejected ($rejectedCount)",
                        selected = selectedFilter == "REJECTED",
                        color = Red500,
                        onClick = { selectedFilter = "REJECTED" },
                        testTag = "filter_chip_rejected"
                    )
                }
            }

            // Transaction Cards List
            if (filteredWithdrawals.isEmpty()) {
                item {
                    EmptyWithdrawalState(
                        filter = selectedFilter,
                        searchQuery = searchQuery,
                        onRequestPayout = onRequestPayoutClick
                    )
                }
            } else {
                items(filteredWithdrawals, key = { it.id }) { withdrawal ->
                    WithdrawalTransactionCard(
                        withdrawal = withdrawal,
                        onViewDetails = { selectedWithdrawalForDetails = withdrawal },
                        onCopyUtr = { utr ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("UTR / Transaction ID", utr))
                            Toast.makeText(context, "Copied UTR: $utr", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // Trust & Policy Notice
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Emerald500,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "All payouts are reviewed within 24-48 business hours by admin team. Coins are automatically refunded if a transaction fails.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Detailed Modal Dialog for single withdrawal
    selectedWithdrawalForDetails?.let { item ->
        WithdrawalDetailDialog(
            withdrawal = item,
            onDismiss = { selectedWithdrawalForDetails = null },
            onCopy = { label, text ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
                Toast.makeText(context, "Copied $label", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun WithdrawalSummaryCard(
    title: String,
    amountText: String,
    countText: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = amountText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = countText,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun StatusFilterChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    testTag: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = if (color == Slate400) MaterialTheme.colorScheme.primary else color,
            selectedLabelColor = Color.White
        ),
        modifier = Modifier.testTag(testTag)
    )
}

@Composable
fun WithdrawalTransactionCard(
    withdrawal: WithdrawalEntity,
    onViewDetails: () -> Unit,
    onCopyUtr: (String) -> Unit
) {
    val statusInfo = getWithdrawalStatusInfo(withdrawal.status)
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(withdrawal.requestedAt) { dateFormat.format(Date(withdrawal.requestedAt)) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
            .testTag("withdrawal_card_${withdrawal.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Amount & Status Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status Icon Avatar
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(statusInfo.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = statusInfo.icon,
                            contentDescription = null,
                            tint = statusInfo.color,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "₹${String.format("%.2f", withdrawal.amountInRupees)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${withdrawal.coinsDeducted} Coins deducted",
                            style = MaterialTheme.typography.labelSmall,
                            color = Gold500,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Visual Status Indicator Pill
                WithdrawalStatusIndicator(status = withdrawal.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Method & Destination
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (withdrawal.method.uppercase() == "UPI") Icons.Default.QrCode else Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${withdrawal.method}: ${withdrawal.accountDetailsMasked}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "#${withdrawal.id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Date & Account Holder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Name: ${withdrawal.accountHolderName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // UTR / Transaction Proof Section (if Paid / Approved)
            if (withdrawal.utrTransactionId.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Emerald500.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = null,
                                tint = Emerald500,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "UTR: ${withdrawal.utrTransactionId}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald500
                            )
                        }

                        IconButton(
                            onClick = { onCopyUtr(withdrawal.utrTransactionId) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy UTR",
                                tint = Emerald500,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Admin Note / Reason (especially useful for Rejected or Special Notes)
            if (withdrawal.adminNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (withdrawal.status == "REJECTED") Icons.Default.ErrorOutline else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (withdrawal.status == "REJECTED") Red500 else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Note: ${withdrawal.adminNote}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = if (withdrawal.status == "REJECTED") Red500 else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Status indicator pill with distinctive color, icon, and label
 * Handles Pending, Paid, Rejected (and Approved)
 */
@Composable
fun WithdrawalStatusIndicator(
    status: String,
    modifier: Modifier = Modifier
) {
    val info = getWithdrawalStatusInfo(status)

    Surface(
        color = info.color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.testTag("withdrawal_status_indicator_${status.lowercase()}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(info.color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = info.label,
                color = info.color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class WithdrawalStatusInfo(
    val label: String,
    val color: Color,
    val icon: ImageVector,
    val description: String
)

private fun getWithdrawalStatusInfo(status: String): WithdrawalStatusInfo {
    return when (status.uppercase()) {
        "PAID" -> WithdrawalStatusInfo(
            label = "Paid",
            color = Emerald500,
            icon = Icons.Default.CheckCircle,
            description = "Payment successfully transferred and settled."
        )
        "PENDING" -> WithdrawalStatusInfo(
            label = "Pending",
            color = Gold500,
            icon = Icons.Default.HourglassTop,
            description = "Awaiting verification and manual bank processing."
        )
        "APPROVED" -> WithdrawalStatusInfo(
            label = "Approved",
            color = Blue500,
            icon = Icons.Default.Check,
            description = "Approved by admin, payout transfer in progress."
        )
        "REJECTED" -> WithdrawalStatusInfo(
            label = "Rejected",
            color = Red500,
            icon = Icons.Default.Cancel,
            description = "Request was rejected. Coins have been refunded."
        )
        else -> WithdrawalStatusInfo(
            label = status,
            color = Slate400,
            icon = Icons.Default.Info,
            description = "Status: $status"
        )
    }
}

@Composable
private fun EmptyWithdrawalState(
    filter: String,
    searchQuery: String,
    onRequestPayout: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("empty_withdrawals_card")
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (searchQuery.isNotBlank()) "No Matching Records" else if (filter != "ALL") "No $filter Withdrawals" else "No Withdrawal Requests Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (searchQuery.isNotBlank()) {
                    "Try searching with another keyword, UTR, or reset filters."
                } else {
                    "Your completed task earnings can be withdrawn via UPI or Direct Bank Transfer once you meet the threshold."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onRequestPayout,
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("empty_request_payout_btn")
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Request New Payout", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Detailed Transaction Info Dialog
 */
@Composable
private fun WithdrawalDetailDialog(
    withdrawal: WithdrawalEntity,
    onDismiss: () -> Unit,
    onCopy: (String, String) -> Unit
) {
    val statusInfo = getWithdrawalStatusInfo(withdrawal.status)
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy, hh:mm:ss a", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("withdrawal_detail_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    WithdrawalStatusIndicator(status = withdrawal.status)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Header Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "₹${String.format("%.2f", withdrawal.amountInRupees)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${withdrawal.coinsDeducted} Coins Deducted",
                            style = MaterialTheme.typography.labelMedium,
                            color = Gold500,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = statusInfo.description,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = statusInfo.color
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                DetailItem(label = "Reference ID", value = "#${withdrawal.id}") {
                    onCopy("Reference ID", "#${withdrawal.id}")
                }
                DetailItem(label = "Payment Mode", value = withdrawal.method)
                DetailItem(label = "Account / VPA", value = withdrawal.accountDetailsMasked) {
                    onCopy("Account Details", withdrawal.accountDetailsMasked)
                }
                DetailItem(label = "Beneficiary Name", value = withdrawal.accountHolderName)
                DetailItem(
                    label = "Requested At",
                    value = dateFormat.format(Date(withdrawal.requestedAt))
                )

                if (withdrawal.processedAt != null) {
                    DetailItem(
                        label = "Processed At",
                        value = dateFormat.format(Date(withdrawal.processedAt))
                    )
                }

                if (withdrawal.utrTransactionId.isNotBlank()) {
                    DetailItem(label = "Bank UTR / Ref", value = withdrawal.utrTransactionId) {
                        onCopy("UTR", withdrawal.utrTransactionId)
                    }
                }

                if (withdrawal.adminNote.isNotBlank()) {
                    DetailItem(label = "Admin Remark", value = withdrawal.adminNote)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    onCopyClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (onCopyClick != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onCopyClick() }
                )
            }
        }
    }
}
