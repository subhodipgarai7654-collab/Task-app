package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.TaskEarnViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PremiumScreen(
    viewModel: TaskEarnViewModel,
    user: UserEntity,
    onBackClick: () -> Unit
) {
    val settings by viewModel.appSettings.collectAsState()
    val membership by viewModel.userMembership.collectAsState()
    val price = settings.premiumPriceRupees

    var showActivationDialog by remember { mutableStateOf(false) }
    var transactionRefInput by remember { mutableStateOf("UPI-VIP-" + (100000..999999).random()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("premium_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "VIP Premium Membership",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Unlock high-reward tasks & expedited benefits",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Hero Plan Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth().testTag("premium_plan_card")
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TASK EARN VIP",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = Gold400,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${price.toInt()}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Surface(
                            color = if (user.isPremium) Emerald500.copy(alpha = 0.2f) else Gold500.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (user.isPremium) "ACTIVE VIP" else "ONE-TIME PASS",
                                color = if (user.isPremium) Emerald500 else Gold500,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = Color(0xFF334155))

                    Spacer(modifier = Modifier.height(16.dp))

                    // Benefits List
                    val benefits = listOf(
                        "Access to Exclusive High-Reward Premium Tasks (up to 2,500 coins/task)",
                        "1.5x Multiplier on all task rewards automatically credited",
                        "Higher daily task completion limits",
                        "Priority Review for task submissions and fast-track payouts",
                        "Exclusive Golden VIP Badge on your profile",
                        "1,000 Welcome Bonus Coins credited immediately"
                    )

                    benefits.forEach { benefit ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Emerald500,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = benefit,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (user.isPremium) {
                        Button(
                            onClick = { /* already active */ },
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(disabledContainerColor = Emerald500.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Your VIP Status is Active", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { showActivationDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("activate_premium_button")
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upgrade to VIP (₹${price.toInt()})", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Explicit Non-Guarantee Disclaimer (Mandatory Requirement #7)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Gold500)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Transparent Membership Disclosure",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "VIP Premium Membership provides enhanced platform capabilities, priority verification, higher daily limits, and access to premium sponsor research. It does NOT guarantee fixed daily income or passive interest.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Membership Status Record if present
        membership?.let { m ->
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Membership Record",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Plan: ${m.planName}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Status: ${m.status}", style = MaterialTheme.typography.bodySmall, color = Emerald500, fontWeight = FontWeight.Bold)
                        Text(text = "Expires: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(m.expiryDate))}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Reference: ${m.transactionRef}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (showActivationDialog) {
        AlertDialog(
            onDismissRequest = { showActivationDialog = false },
            title = { Text("Activate VIP Membership") },
            text = {
                Column {
                    Text(
                        text = "Demo Activation Flow:\nSimulating verified UPI payment of ₹${price.toInt()} to ${settings.companyUpiId}.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = transactionRefInput,
                        onValueChange = { transactionRefInput = it },
                        label = { Text("Transaction Reference ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.activatePremium(transactionRefInput) {
                            showActivationDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                ) {
                    Text("Confirm & Activate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showActivationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
