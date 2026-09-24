package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.data.model.UserEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.TaskEarnViewModel

@Composable
fun HomeScreen(
    viewModel: TaskEarnViewModel,
    user: UserEntity,
    onNavigateToTasks: () -> Unit,
    onNavigateToEarn: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onStartTask: (TaskEntity) -> Unit
) {
    val settings by viewModel.appSettings.collectAsState()
    val activeTasks by viewModel.activeTasks.collectAsState()
    val completions by viewModel.userCompletions.collectAsState()

    var showBonusDialog by remember { mutableStateOf(false) }

    val completedTaskIds = remember(completions) {
        completions.filter { it.status == "VERIFIED" }.map { it.taskId }.toSet()
    }
    val pendingTaskCount = remember(completions) {
        completions.count { it.status == "PENDING" }
    }
    val verifiedTaskCount = completedTaskIds.size

    val rate = if (settings.coinRatePerRupee <= 0) 100 else settings.coinRatePerRupee
    val balanceRupees = String.format("%.2f", user.coins.toDouble() / rate)
    val todayRupees = String.format("%.2f", user.todayCoins.toDouble() / rate)
    val lifetimeRupees = String.format("%.2f", user.lifetimeCoins.toDouble() / rate)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("home_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header: User info & Premium Badge
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Emerald500),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Hello, ${user.name.split(" ").firstOrNull() ?: user.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "ID: ${user.userId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (user.isPremium) {
                    Surface(
                        color = Gold500.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("vip_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Gold500,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VIP PRO",
                                color = Gold500,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onNavigateToPremium,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("get_vip_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Gold500,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Go VIP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold500
                        )
                    }
                }
            }
        }

        // 2. Hero Balance Card (Modern Fintech Card with Gradient)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("balance_hero_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Slate900, Slate850, Slate800)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Available Balance",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Slate400
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "₹$balanceRupees",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }

                            Surface(
                                color = Emerald500.copy(alpha = 0.2f),
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
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Gold400,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Configured Rate: ${rate * 10} Coins = ₹10 (100 Coins = ₹1.00)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onNavigateToWallet,
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("hero_withdraw_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Withdraw", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = { showBonusDialog = true },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Slate700),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("hero_checkin_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Emerald500,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Daily Bonus", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Quick Navigation Shortcuts (Home, Tasks, Earn, Wallet, VIP)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("home_quick_shortcuts")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HomeQuickAction(
                        icon = Icons.Default.TaskAlt,
                        label = "Tasks",
                        color = Blue500,
                        onClick = onNavigateToTasks
                    )
                    HomeQuickAction(
                        icon = Icons.Default.TrendingUp,
                        label = "Earn Goal",
                        color = Emerald500,
                        onClick = onNavigateToEarn
                    )
                    HomeQuickAction(
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "Wallet",
                        color = Gold500,
                        onClick = onNavigateToWallet
                    )
                    HomeQuickAction(
                        icon = Icons.Default.WorkspacePremium,
                        label = "Go VIP",
                        color = Purple500,
                        onClick = onNavigateToPremium
                    )
                }
            }
        }

        // 3. Stats Grid: Today's Earnings, Lifetime Earnings, Completed Tasks, Pending Rewards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Today's Earnings",
                        value = "₹$todayRupees",
                        subtitle = "${user.todayCoins} Coins",
                        icon = Icons.Default.TrendingUp,
                        iconColor = Emerald500,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToEarn
                    )
                    StatCard(
                        title = "Lifetime Earnings",
                        value = "₹$lifetimeRupees",
                        subtitle = "${user.lifetimeCoins} Coins",
                        icon = Icons.Default.Paid,
                        iconColor = Gold500,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWallet
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Completed Tasks",
                        value = "$verifiedTaskCount Tasks",
                        subtitle = "Verified & Rewarded",
                        icon = Icons.Default.TaskAlt,
                        iconColor = Blue500,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTasks
                    )
                    StatCard(
                        title = "Pending Rewards",
                        value = "$pendingTaskCount Pending",
                        subtitle = "Under Admin Review",
                        icon = Icons.Default.HourglassTop,
                        iconColor = Amber500,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTasks
                    )
                }
            }
        }

        // 4. Marketing Benchmark Card (Requirement #23: clearly marked demo statistics)
        item {
            DemoMarketingStatsCard()
        }

        // 5. Section Header for Available Tasks
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Recommended Tasks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onNavigateToTasks) {
                    Text("View All", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 6. Preview of first 4 tasks
        val previewTasks = activeTasks.take(4)
        if (previewTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No active tasks currently. Check back soon!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(previewTasks) { task ->
                val isDone = completedTaskIds.contains(task.id)
                TaskCardItem(
                    task = task,
                    isCompleted = isDone,
                    conversionRate = rate,
                    onStartClick = { onStartTask(task) }
                )
            }
        }

        // 7. Responsible Earning Disclaimer
        item {
            ResponsibleEarningDisclaimer()
        }
    }

    if (showBonusDialog) {
        DailyBonusDialog(
            user = user,
            viewModel = viewModel,
            onDismiss = { showBonusDialog = false },
            onGoToEarn = {
                showBonusDialog = false
                onNavigateToEarn()
            }
        )
    }
}

@Composable
private fun HomeQuickAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("quick_action_${label.lowercase().replace(" ", "_")}")
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun DailyBonusDialog(
    user: UserEntity,
    viewModel: TaskEarnViewModel,
    onDismiss: () -> Unit,
    onGoToEarn: () -> Unit
) {
    val todayDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    val isAlreadyClaimed = user.lastCheckinDate == todayDateStr

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("daily_bonus_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Gold500.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = Gold500,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Daily Check-in Bonus",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Current Streak: ${user.checkinStreak} Days in a row!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (1..7).forEach { day ->
                        val isCompletedDay = day <= (user.checkinStreak % 7).let { if (it == 0 && user.checkinStreak > 0) 7 else it }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCompletedDay) Emerald500 else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompletedDay) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                } else {
                                    Text(
                                        text = "D$day",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "+${100 + (day * 25)}",
                                fontSize = 9.sp,
                                color = Gold500,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isAlreadyClaimed) {
                    Surface(
                        color = Emerald500.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald500, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Today's Bonus Claimed!",
                                color = Emerald500,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.claimDailyCheckin()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("dialog_claim_bonus_button")
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Claim Today's Bonus", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onGoToEarn,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("View Earn Goals", fontSize = 12.sp)
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
