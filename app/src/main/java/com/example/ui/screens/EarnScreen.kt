package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.components.ResponsibleEarningDisclaimer
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Gold500
import com.example.ui.theme.Purple500
import com.example.ui.theme.Blue500
import com.example.ui.theme.Amber500
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.TaskEarnViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EarnScreen(
    viewModel: TaskEarnViewModel,
    user: UserEntity,
    onNavigateToTasks: (category: String) -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {}
) {
    val context = LocalContext.current
    val settings by viewModel.appSettings.collectAsState()
    val referrals by viewModel.userReferrals.collectAsState()

    val rate = if (settings.coinRatePerRupee <= 0) 100 else settings.coinRatePerRupee
    val todayRupees = user.todayCoins.toDouble() / rate
    val targetRupees = settings.dailyTargetRupees
    val progressFraction = (todayRupees / targetRupees).coerceIn(0.0, 1.0).toFloat()
    val progressPercent = (progressFraction * 100).toInt()

    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val isAlreadyCheckedIn = user.lastCheckinDate == todayDateStr

    var copiedSnackbar by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("earn_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Daily Goals & Referrals",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Track your daily progress and earn referral bonuses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Feature #6: DAILY EARNING SECTION
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth().testTag("daily_earning_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Earning Goal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Target: ₹${String.format("%.0f", targetRupees)}/day",
                                style = MaterialTheme.typography.labelMedium,
                                color = Emerald500,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Surface(
                            color = Emerald500.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "$progressPercent%",
                                color = Emerald500,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Emerald500,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Earned today: ₹${String.format("%.2f", todayRupees)} (${user.todayCoins} Coins)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Target: ₹${String.format("%.2f", targetRupees)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notice mandated in prompt: "Display ₹166 as a configurable target/example, not as a guaranteed earning."
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "Note: ₹${String.format("%.0f", targetRupees)} is a configurable daily target example. Rewards are based on verified task completion, not guaranteed automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // Earning Hub Categories (Video, App Discovery, Daily Tasks, Bonus, VIP)
        item {
            Text(
                text = "Earn Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EarnCategoryTile(
                    title = "Video Tasks",
                    subtitle = "Watch & Earn",
                    icon = Icons.Default.PlayCircle,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTasks("WATCH_VIDEO") }
                )
                EarnCategoryTile(
                    title = "App Discovery",
                    subtitle = "Discover & Test",
                    icon = Icons.Default.Apps,
                    color = Blue500,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTasks("APP_DISCOVERY") }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EarnCategoryTile(
                    title = "Daily Tasks",
                    subtitle = "Daily Quests",
                    icon = Icons.Default.Today,
                    color = Amber500,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTasks("DAILY") }
                )
                EarnCategoryTile(
                    title = "VIP Tasks",
                    subtitle = "Exclusive Rewards",
                    icon = Icons.Default.WorkspacePremium,
                    color = Purple500,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToPremium() }
                )
            }
        }

        // Daily Checkin Streak Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("daily_checkin_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Gold500.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = Gold500,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Daily Streak Bonus",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Current Streak: ${user.checkinStreak} Days",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.claimDailyCheckin() },
                            enabled = !isAlreadyCheckedIn,
                            colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("claim_checkin_button")
                        ) {
                            Text(
                                text = if (isAlreadyCheckedIn) "Claimed" else "Claim",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 7-day indicator dots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        (1..7).forEach { day ->
                            val isCompletedDay = day <= (user.checkinStreak % 7).let { if (it == 0 && user.checkinStreak > 0) 7 else it }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(
                                            if (isCompletedDay) Emerald500 else MaterialTheme.colorScheme.surfaceVariant,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompletedDay) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    } else {
                                        Text(text = "D$day", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "+${100 + (day * 25)}",
                                    fontSize = 10.sp,
                                    color = Gold500,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Feature #12: USER REFERRAL SECTION
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier.fillMaxWidth().testTag("referral_program_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Purple500.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Purple500, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Refer & Earn Bonus",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Earn +${settings.referralBonusCoins} Coins per friend registered",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Referral Code Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Your Referral Code", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                            Text(text = user.referralCode, fontWeight = FontWeight.Black, fontSize = 20.sp, color = Gold500)
                        }

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Referral Code", user.referralCode))
                                copiedSnackbar = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Purple500),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("copy_referral_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Share Link Button
                    val shareUrl = "https://taskearnpro.app/ref/${user.referralCode}"
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Join TaskEarn Pro")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Join me on TaskEarn Pro! Complete transparent daily tasks, discover apps, and earn real rewards. Use my referral code ${user.referralCode} or click: $shareUrl"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share TaskEarn Pro"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("share_referral_link_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Referral Link", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Referral Stats & History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Referral History (${referrals.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                val totalRefCoins = referrals.sumOf { it.rewardCoins }
                Text(
                    text = "Total Earned: +$totalRefCoins Coins",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gold500
                )
            }
        }

        if (referrals.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No referrals yet. Share your code to start earning referral bonuses!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(referrals) { ref ->
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
                        Column {
                            Text(
                                text = ref.referredUserName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(ref.createdAt)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "+${ref.rewardCoins} Coins",
                            color = Emerald500,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        item {
            ResponsibleEarningDisclaimer()
        }
    }
}

@Composable
private fun EarnCategoryTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .clickable { onClick() }
            .testTag("earn_tile_${title.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
