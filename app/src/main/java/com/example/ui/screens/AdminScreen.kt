package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.TaskEarnViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminScreen(
    viewModel: TaskEarnViewModel,
    onExitAdmin: () -> Unit
) {
    val currentAdmin by viewModel.currentAdmin.collectAsState()

    if (currentAdmin == null) {
        AdminLoginView(
            onLogin = { user, pass ->
                viewModel.loginAdmin(user, pass) { /* handled */ }
            },
            onBack = onExitAdmin
        )
    } else {
        AdminDashboardView(
            viewModel = viewModel,
            admin = currentAdmin!!,
            onLogout = { viewModel.logoutAdmin() },
            onBack = onExitAdmin
        )
    }
}

@Composable
private fun AdminLoginView(
    onLogin: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var username by remember { mutableStateOf("subhodip@2007") }
    var password by remember { mutableStateOf("subhodip@7") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
            .testTag("admin_login_screen"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Admin Portal Login",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Authorized Personnel Only",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "Default Credentials Configured:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Username: subhodip@2007\nPassword: subhodip@7", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Admin Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("admin_username_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Admin Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("admin_password_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { onLogin(username.trim(), password.trim()) },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("admin_sign_in_button")
                ) {
                    Text("Authenticate & Enter", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardView(
    viewModel: TaskEarnViewModel,
    admin: AdminUserEntity,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    // 0: Overview, 1: Users, 2: Tasks, 3: Withdrawals, 4: Settings, 5: Logs

    val tabs = listOf(
        "Overview", "Users", "Tasks", "Withdrawals", "Settings", "Audit Logs"
    )
    var showAppInstallCreator by remember { mutableStateOf(false) }

    if (showAppInstallCreator) {
        AppInstallCreateScreen(
            viewModel = viewModel,
            onBackClick = { showAppInstallCreator = false },
            onTaskCreated = { showAppInstallCreator = false }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .testTag("admin_dashboard_screen")
    ) {
        // Admin Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Exit to App")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Admin Panel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Signed in as ${admin.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Emerald500,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            IconButton(onClick = onLogout) {
                Icon(Icons.Default.Logout, contentDescription = "Logout Admin", tint = Red500)
            }
        }

        // Horizontal Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTab = index },
                    label = { Text(title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald500,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("admin_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            when (selectedTab) {
                0 -> AdminOverviewTab(viewModel)
                1 -> AdminUsersTab(viewModel)
                2 -> AdminTasksTab(
                    viewModel = viewModel,
                    onCreateAppInstallClick = { showAppInstallCreator = true }
                )
                3 -> AdminWithdrawalsTab(viewModel)
                4 -> AdminSettingsTab(viewModel)
                5 -> AdminLogsTab(viewModel)
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 0: OVERVIEW (Metrics Dashboard - Requirement #16)
// -------------------------------------------------------------
@Composable
private fun AdminOverviewTab(viewModel: TaskEarnViewModel) {
    val totalUsers by viewModel.totalUsersCount.collectAsState()
    val activeUsers by viewModel.activeUsersCount.collectAsState()
    val premiumUsers by viewModel.premiumUsersCount.collectAsState()
    val totalTasks by viewModel.totalTasksCount.collectAsState()
    val verifiedTasks by viewModel.verifiedCompletionsCount.collectAsState()
    val pendingTasks by viewModel.pendingCompletionsCount.collectAsState()
    val coinsIssued by viewModel.totalCoinsIssued.collectAsState()
    val totalWithdrawals by viewModel.totalWithdrawalsCount.collectAsState()
    val pendingWithdrawals by viewModel.pendingWithdrawalsCount.collectAsState()
    val paidWithdrawals by viewModel.paidWithdrawalsCount.collectAsState()
    val totalPaidAmt by viewModel.totalPaidAmount.collectAsState()
    val premiumRevenue by viewModel.totalPremiumRevenue.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text(
                text = "Platform Analytics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Total Users",
                    value = "$totalUsers",
                    subtitle = "$activeUsers Active",
                    icon = Icons.Default.People,
                    iconColor = Emerald500,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "VIP Members",
                    value = "$premiumUsers",
                    subtitle = "₹${(premiumRevenue ?: 0.0).toInt()} Revenue",
                    icon = Icons.Default.WorkspacePremium,
                    iconColor = Gold500,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Active Tasks",
                    value = "$totalTasks",
                    subtitle = "$verifiedTasks Verified",
                    icon = Icons.Default.Task,
                    iconColor = Blue500,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Pending Verifications",
                    value = "$pendingTasks",
                    subtitle = "Task submissions",
                    icon = Icons.Default.PendingActions,
                    iconColor = Amber500,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Coins Issued",
                    value = "$coinsIssued",
                    subtitle = "All-time rewards",
                    icon = Icons.Default.MonetizationOn,
                    iconColor = Gold500,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Pending Payouts",
                    value = "$pendingWithdrawals",
                    subtitle = "Of $totalWithdrawals total",
                    icon = Icons.Default.HourglassBottom,
                    iconColor = Red500,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            StatCard(
                title = "Total Paid Withdrawals",
                value = "₹${String.format("%.2f", totalPaidAmt)}",
                subtitle = "$paidWithdrawals requests processed with valid UTR",
                icon = Icons.Default.CheckCircle,
                iconColor = Emerald500,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// -------------------------------------------------------------
// TAB 1: USERS (Requirement #17)
// -------------------------------------------------------------
@Composable
private fun AdminUsersTab(viewModel: TaskEarnViewModel) {
    val users by viewModel.allUsers.collectAsState()
    var search by remember { mutableStateOf("") }

    val filtered = remember(users, search) {
        users.filter {
            search.isBlank() ||
                    it.name.contains(search, ignoreCase = true) ||
                    it.userId.contains(search, ignoreCase = true) ||
                    it.email.contains(search, ignoreCase = true) ||
                    it.phoneNumber.contains(search, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search users by name, UID, mobile...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("admin_user_search")
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(filtered) { u ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().testTag("admin_user_item_${u.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = u.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = "UID: ${u.userId} • Ref: ${u.referralCode}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            StatusBadge(status = u.accountStatus)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Coins: ${u.coins} • Lifetime: ${u.lifetimeCoins} • Contact: ${if (u.email.isNotBlank()) u.email else u.phoneNumber}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { viewModel.adminToggleBlock(u.id, u.accountStatus) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (u.accountStatus == "BLOCKED") Emerald500 else Red500
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp).testTag("toggle_block_user_${u.id}")
                            ) {
                                Text(if (u.accountStatus == "BLOCKED") "Unblock User" else "Block User", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: TASKS (Requirement #18)
// -------------------------------------------------------------
@Composable
private fun AdminTasksTab(
    viewModel: TaskEarnViewModel,
    onCreateAppInstallClick: () -> Unit = {}
) {
    val tasks by viewModel.allTasks.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Total Tasks (${tasks.size})", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onCreateAppInstallClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("admin_create_app_install_btn")
                ) {
                    Icon(Icons.Default.InstallMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("App Install", fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        taskToEdit = null
                        showEditDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("admin_add_task_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(tasks) { task ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = task.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "+${task.rewardCoins} Coins", color = Gold500, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "Category: ${task.category} • Est: ${task.estimatedTime}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "URL: ${task.targetUrl}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, maxLines = 1)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    taskToEdit = task
                                    showEditDialog = true
                                }
                            ) {
                                Text("Edit")
                            }
                            IconButton(onClick = { viewModel.adminDeleteTask(task) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Red500)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        AdminTaskEditDialog(
            task = taskToEdit,
            onDismiss = { showEditDialog = false },
            onSave = { saved ->
                viewModel.adminSaveTask(saved) {
                    showEditDialog = false
                }
            }
        )
    }
}

@Composable
private fun AdminTaskEditDialog(
    task: TaskEntity?,
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var category by remember { mutableStateOf(task?.category ?: "WATCH_VIDEO") }
    var targetUrl by remember { mutableStateOf(task?.targetUrl ?: "https://youtube.com") }
    var rewardCoinsStr by remember { mutableStateOf((task?.rewardCoins ?: 250).toString()) }
    var estimatedTime by remember { mutableStateOf(task?.estimatedTime ?: "1 min") }
    var question by remember { mutableStateOf(task?.verificationQuestion ?: "What was the main topic?") }
    var answer by remember { mutableStateOf(task?.verificationAnswer ?: "Confirmed") }
    var isPremiumOnly by remember { mutableStateOf(task?.isPremiumOnly ?: false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (task == null) "Add New Task" else "Edit Task",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, maxLines = 2, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (e.g. APP_INSTALL, APP_DISCOVERY)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("APP_INSTALL", "APP_DISCOVERY", "WATCH_VIDEO", "DAILY", "PREMIUM").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = {
                                category = cat
                                if (cat == "APP_INSTALL" && (targetUrl.isBlank() || targetUrl == "https://youtube.com")) {
                                    targetUrl = "https://play.google.com/store/apps/details?id=com.example.app"
                                }
                            },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
                OutlinedTextField(value = targetUrl, onValueChange = { targetUrl = it }, label = { Text("Target URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rewardCoinsStr, onValueChange = { rewardCoinsStr = it }, label = { Text("Reward Coins") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = question, onValueChange = { question = it }, label = { Text("Verification Question") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = answer, onValueChange = { answer = it }, label = { Text("Verification Answer") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = isPremiumOnly, onCheckedChange = { isPremiumOnly = it })
                    Text("VIP Premium Only")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            val saved = task?.copy(
                                title = title,
                                description = description,
                                category = category,
                                targetUrl = targetUrl,
                                rewardCoins = rewardCoinsStr.toLongOrNull() ?: 200L,
                                estimatedTime = estimatedTime,
                                verificationQuestion = question,
                                verificationAnswer = answer,
                                isPremiumOnly = isPremiumOnly
                            ) ?: TaskEntity(
                                title = title,
                                description = description,
                                category = category,
                                targetUrl = targetUrl,
                                requirements = "Complete the task completely and answer the question.",
                                rewardCoins = rewardCoinsStr.toLongOrNull() ?: 200L,
                                estimatedTime = estimatedTime,
                                verificationQuestion = question,
                                verificationAnswer = answer,
                                isPremiumOnly = isPremiumOnly
                            )
                            onSave(saved)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Task")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: WITHDRAWALS (Requirement #19)
// -------------------------------------------------------------
@Composable
private fun AdminWithdrawalsTab(viewModel: TaskEarnViewModel) {
    val withdrawals by viewModel.allWithdrawals.collectAsState()
    var activeWithdrawalToProcess by remember { mutableStateOf<WithdrawalEntity?>(null) }
    var filterStatus by remember { mutableStateOf("ALL") }

    val filtered = remember(withdrawals, filterStatus) {
        if (filterStatus == "ALL") withdrawals else withdrawals.filter { it.status == filterStatus }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "PENDING", "APPROVED", "PAID", "REJECTED").forEach { st ->
                FilterChip(
                    selected = filterStatus == st,
                    onClick = { filterStatus = st },
                    label = { Text(st) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(filtered) { w ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().testTag("admin_payout_item_${w.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Payout #${w.id} • User ${w.userId}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "₹${String.format("%.2f", w.amountInRupees)} (${w.coinsDeducted} Coins)", color = Emerald500, fontWeight = FontWeight.ExtraBold)
                            }
                            StatusBadge(status = w.status)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(text = "${w.method} • ${w.accountDetailsMasked} (${w.accountHolderName})", fontSize = 12.sp)

                        if (w.utrTransactionId.isNotBlank()) {
                            Text(text = "UTR: ${w.utrTransactionId}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (w.status == "PENDING" || w.status == "APPROVED") {
                            Button(
                                onClick = { activeWithdrawalToProcess = w },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp).testTag("process_payout_btn_${w.id}")
                            ) {
                                Text("Process / Update Status", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    activeWithdrawalToProcess?.let { w ->
        var selectedStatus by remember { mutableStateOf("PAID") }
        var utr by remember { mutableStateOf("UTR-" + (1000000..9999999).random()) }
        var note by remember { mutableStateOf("Settled via Bank API") }

        Dialog(onDismissRequest = { activeWithdrawalToProcess = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(10.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Process Payout #${w.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Amount: ₹${w.amountInRupees} to ${w.accountDetailsMasked}", style = MaterialTheme.typography.bodySmall)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = selectedStatus == "PAID", onClick = { selectedStatus = "PAID" }, label = { Text("Mark PAID") })
                        FilterChip(selected = selectedStatus == "APPROVED", onClick = { selectedStatus = "APPROVED" }, label = { Text("APPROVE") })
                        FilterChip(selected = selectedStatus == "REJECTED", onClick = { selectedStatus = "REJECTED" }, label = { Text("REJECT") })
                    }

                    if (selectedStatus == "PAID") {
                        OutlinedTextField(value = utr, onValueChange = { utr = it }, label = { Text("Official UTR / Ref ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }

                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Admin Note / Reason") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { activeWithdrawalToProcess = null }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        Button(
                            onClick = {
                                viewModel.adminProcessWithdrawal(w.id, selectedStatus, utr, note) {
                                    activeWithdrawalToProcess = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 4: APP SETTINGS (Requirement #21)
// -------------------------------------------------------------
@Composable
private fun AdminSettingsTab(viewModel: TaskEarnViewModel) {
    val settings by viewModel.appSettings.collectAsState()

    var coinRateStr: String by remember { mutableStateOf(settings.coinRatePerRupee.toString()) }
    var minWithdrawalStr: String by remember { mutableStateOf(settings.minWithdrawalRupees.toString()) }
    var dailyTargetStr: String by remember { mutableStateOf(settings.dailyTargetRupees.toString()) }
    var premiumPriceStr: String by remember { mutableStateOf(settings.premiumPriceRupees.toString()) }
    var referralBonusStr: String by remember { mutableStateOf(settings.referralBonusCoins.toString()) }
    var supportEmail: String by remember { mutableStateOf(settings.supportEmail) }
    var supportTelegram: String by remember { mutableStateOf(settings.supportTelegram) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text(text = "App Financial & Operational Parameters", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }

        item {
            OutlinedTextField(
                value = coinRateStr,
                onValueChange = { coinRateStr = it },
                label = { Text("Coin Rate (e.g. 100 = 100 coins/₹1)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("setting_coin_rate")
            )
        }

        item {
            OutlinedTextField(
                value = minWithdrawalStr,
                onValueChange = { minWithdrawalStr = it },
                label = { Text("Minimum Withdrawal (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("setting_min_withdrawal")
            )
        }

        item {
            OutlinedTextField(
                value = dailyTargetStr,
                onValueChange = { dailyTargetStr = it },
                label = { Text("Daily Target Display (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = premiumPriceStr,
                onValueChange = { premiumPriceStr = it },
                label = { Text("VIP Premium Price (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = referralBonusStr,
                onValueChange = { referralBonusStr = it },
                label = { Text("Referral Bonus (Coins)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = supportEmail,
                onValueChange = { supportEmail = it },
                label = { Text("Support Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = supportTelegram,
                onValueChange = { supportTelegram = it },
                label = { Text("Support Telegram URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Button(
                onClick = {
                    val updated = settings.copy(
                        coinRatePerRupee = coinRateStr.toIntOrNull() ?: 100,
                        minWithdrawalRupees = minWithdrawalStr.toDoubleOrNull() ?: 50.0,
                        dailyTargetRupees = dailyTargetStr.toDoubleOrNull() ?: 166.0,
                        premiumPriceRupees = premiumPriceStr.toDoubleOrNull() ?: 300.0,
                        referralBonusCoins = referralBonusStr.toLongOrNull() ?: 200L,
                        supportEmail = supportEmail,
                        supportTelegram = supportTelegram
                    )
                    viewModel.adminUpdateSettings(updated) { }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_admin_settings_btn")
            ) {
                Text("Save Configuration", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 5: AUDIT LOGS (Requirement #22)
// -------------------------------------------------------------
@Composable
private fun AdminLogsTab(viewModel: TaskEarnViewModel) {
    val logs by viewModel.adminLogs.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        items(logs) { log ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = log.action, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = log.details, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "By ${log.adminUsername} • ${SimpleDateFormat("dd MMM, hh:mm:ss a", Locale.getDefault()).format(Date(log.timestamp))}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
