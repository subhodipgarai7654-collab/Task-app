package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserEntity
import com.example.ui.components.ResponsibleEarningDisclaimer
import com.example.ui.components.StatusBadge
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Gold500
import com.example.ui.theme.Red500
import com.example.ui.viewmodel.TaskEarnViewModel

@Composable
fun ProfileScreen(
    viewModel: TaskEarnViewModel,
    user: UserEntity,
    onAdminClick: () -> Unit,
    onNavigateToPremium: () -> Unit = {},
    onNavigateToEarn: () -> Unit = {}
) {
    val context = LocalContext.current
    val settings by viewModel.appSettings.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    var editName by remember { mutableStateOf(user.name) }
    var selectedAvatarIndex by remember { mutableStateOf(user.avatarId) }

    val avatarColors = listOf(
        Emerald500, Color(0xFF3B82F6), Color(0xFF8B5CF6),
        Color(0xFFEC4899), Color(0xFFF59E0B), Color(0xFF14B8A6)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("profile_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Top Header
        item {
            Column {
                Text(
                    text = "My Profile & Support",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Manage account, verification & support resources",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Profile Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("user_profile_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(avatarColors[user.avatarId % avatarColors.size]),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (user.isPremium) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            Icons.Default.Verified,
                                            contentDescription = "VIP",
                                            tint = Gold500,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "UID: ${user.userId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (user.email.isNotBlank()) user.email else user.phoneNumber,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        StatusBadge(status = user.accountStatus)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                editName = user.name
                                selectedAvatarIndex = user.avatarId
                                showEditProfileDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(40.dp).testTag("edit_profile_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Profile", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.logout() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Red500),
                            modifier = Modifier.weight(1f).height(40.dp).testTag("logout_button")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Logout", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Account & Membership Shortcuts
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("profile_membership_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPremium() }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Gold500.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Gold500, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Premium VIP Membership",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (user.isPremium) "Active Member - Tap for Details" else "₹${String.format("%.0f", settings.premiumPriceRupees)} • Unlock VIP tasks & perks",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (user.isPremium) Emerald500 else Gold500
                                )
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToEarn() }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Emerald500.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Emerald500, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Referral Program & Bonuses",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Code: ${user.referralCode} • Tap to view invites",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Support & Help Center (Requirement #14)
        item {
            Text(
                text = "Support & Help Center",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Email Support
                    ListItem(
                        headlineContent = { Text("Email Support", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(settings.supportEmail) },
                        leadingContent = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Emerald500)
                        },
                        trailingContent = {
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${settings.supportEmail}")
                                    putExtra(Intent.EXTRA_SUBJECT, "TaskEarn Pro Inquiry - User ${user.userId}")
                                }
                                try { context.startActivity(intent) } catch (e: Exception) { }
                            }) {
                                Icon(Icons.Default.Send, contentDescription = "Send Email")
                            }
                        }
                    )

                    Divider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Telegram Support
                    ListItem(
                        headlineContent = { Text("Telegram Official Community", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(settings.supportTelegram) },
                        leadingContent = {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF0088CC))
                        },
                        trailingContent = {
                            IconButton(onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(settings.supportTelegram))
                                    context.startActivity(intent)
                                } catch (e: Exception) { }
                            }) {
                                Icon(Icons.Default.OpenInNew, contentDescription = "Open")
                            }
                        }
                    )

                    Divider(modifier = Modifier.padding(horizontal = 16.dp))

                    // FAQs
                    ListItem(
                        headlineContent = { Text("Frequently Asked Questions", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Coin rates, payout timelines, and task guidelines") },
                        leadingContent = {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Gold500)
                        },
                        modifier = Modifier.clickable { showFaqDialog = true }
                    )

                    Divider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Terms & Conditions
                    ListItem(
                        headlineContent = { Text("Terms & Conditions", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Transparent user policy & fair-play rules") },
                        leadingContent = {
                            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier.clickable { showTermsDialog = true }
                    )

                    Divider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Privacy Policy
                    ListItem(
                        headlineContent = { Text("Privacy Policy", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Local storage & encrypted sensitive data") },
                        leadingContent = {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Emerald500)
                        },
                        modifier = Modifier.clickable { showPrivacyDialog = true }
                    )
                }
            }
        }

        // Admin Portal Quick Action
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Admin Control Panel",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Manage users, verify payouts, edit coin rates & tasks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onAdminClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("profile_admin_btn")
                    ) {
                        Text("Open Admin")
                    }
                }
            }
        }

        item {
            ResponsibleEarningDisclaimer()
        }
    }

    // Dialog: Edit Profile
    if (showEditProfileDialog) {
        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "Choose Avatar Color:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        avatarColors.forEachIndexed { index, color ->
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedAvatarIndex = index },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedAvatarIndex == index) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showEditProfileDialog = false },
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                viewModel.updateProfile(editName, selectedAvatarIndex)
                                showEditProfileDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    // Dialog: FAQs
    if (showFaqDialog) {
        AlertDialog(
            onDismissRequest = { showFaqDialog = false },
            title = { Text("Frequently Asked Questions") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Q: What is the coin conversion rate?\nA: By default, 1,000 Coins = ₹10 (100 Coins = ₹1). Rates are transparently set by admin.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Q: When will my withdrawal be paid?\nA: Payout requests are verified by our admin team against task completion records and settled via UPI with reference UTR.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Q: Why is anti-bot CAPTCHA required?\nA: To ensure fair reward distribution and protect real user payouts against automated bots.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showFaqDialog = false }) { Text("Close") }
            }
        )
    }

    // Dialog: Terms
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms & Conditions") },
            text = {
                Text(
                    text = "1. TaskEarn Pro is a transparent reward management application.\n" +
                            "2. Rewards are issued solely for authentic, verified task actions.\n" +
                            "3. Users must not use bots, scripts, or multiple unauthorized accounts.\n" +
                            "4. Payouts require administrative verification and valid UPI/Bank details.\n" +
                            "5. Marketing statistics represent benchmark platform milestones.",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) { Text("Understood") }
            }
        )
    }

    // Dialog: Privacy Policy
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Text(
                    text = "We prioritize user privacy. Personal credentials, UPI IDs, and account numbers are stored locally within an encrypted Room database. We do not track external browsing or install hidden monitoring services.",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Close") }
            }
        )
    }
}
