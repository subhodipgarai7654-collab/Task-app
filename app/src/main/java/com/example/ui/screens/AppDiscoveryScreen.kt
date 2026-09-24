package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TaskEntity
import com.example.ui.components.AntiBotCaptchaDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.Blue500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Gold500
import com.example.ui.theme.Red500
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.TaskEarnViewModel

@Composable
fun AppDiscoveryScreen(
    viewModel: TaskEarnViewModel,
    onBackClick: () -> Unit,
    onCreateAppInstallClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val allTasks by viewModel.activeTasks.collectAsState()
    val completions by viewModel.userCompletions.collectAsState()

    val discoveryTasks = remember(allTasks) {
        allTasks.filter {
            it.category == "APP_DISCOVERY" || it.category == "PLAY_STORE" || it.category == "APP_INSTALL"
        }
    }

    val completedIds = remember(completions) {
        completions.filter { it.status == "VERIFIED" }.map { it.taskId }.toSet()
    }

    var selectedTaskForVerify by remember { mutableStateOf<TaskEntity?>(null) }
    var answerInput by remember { mutableStateOf("") }
    var answerError by remember { mutableStateOf<String?>(null) }
    var showCaptcha by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("app_discovery_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Discover & Install Apps",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Explore official applications on Google Play Store",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onCreateAppInstallClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("create_app_install_top_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Quick Banner: Promote App / Create App Install Campaign
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("promote_app_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Emerald500.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Emerald500,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Have an App to Promote?",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Create an App Install campaign with coin rewards",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Button(
                        onClick = onCreateAppInstallClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("create_app_campaign_btn")
                    ) {
                        Text("Create", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Transparency Disclaimer
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Blue500,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Verified App Discovery Guarantee",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "We respect user privacy and do not run intrusive background monitoring or simulate app installations. Rewards are granted only after reviewing the official app store details and successfully answering the verification inquiry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        item {
            Text(
                text = "Available App Install & Discovery Tasks (${discoveryTasks.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(discoveryTasks) { task ->
            val isDone = completedIds.contains(task.id)
            val isInstallTask = task.category == "APP_INSTALL"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("discovery_card_${task.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (isInstallTask) Emerald500.copy(alpha = 0.15f) else Blue500.copy(alpha = 0.15f),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isInstallTask) Icons.Default.InstallMobile else Icons.Default.Apps,
                                    contentDescription = null,
                                    tint = if (isInstallTask) Emerald500 else Blue500,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${if (isInstallTask) "App Install" else "Play Store"} • +${task.rewardCoins} Coins",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gold500,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (isDone) {
                            StatusBadge(status = "VERIFIED")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(task.targetUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // fallback
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("open_playstore_btn_${task.id}")
                        ) {
                            Icon(Icons.Default.Shop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open in Store", fontSize = 13.sp)
                        }

                        if (!isDone) {
                            Button(
                                onClick = {
                                    selectedTaskForVerify = task
                                    answerInput = ""
                                    answerError = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("verify_app_btn_${task.id}")
                            ) {
                                Text("Verify & Earn", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Verification Dialog
    selectedTaskForVerify?.let { task ->
        Dialog(onDismissRequest = { selectedTaskForVerify = null }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Discovery Verification",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Task: ${task.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Question:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = task.verificationQuestion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = answerInput,
                        onValueChange = { answerInput = it; answerError = null },
                        label = { Text("Your Answer") },
                        placeholder = { Text("e.g. ${task.verificationAnswer}") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("app_verify_input")
                    )

                    if (answerError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = answerError ?: "",
                            color = Red500,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedTaskForVerify = null },
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (answerInput.isBlank()) {
                                    answerError = "Please enter the answer."
                                    return@Button
                                }
                                showCaptcha = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            modifier = Modifier.weight(1f).height(46.dp).testTag("app_verify_submit")
                        ) {
                            Text("Confirm & Claim", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showCaptcha && selectedTaskForVerify != null) {
        val task = selectedTaskForVerify!!
        AntiBotCaptchaDialog(
            purpose = "App Discovery Verification",
            onDismiss = { showCaptcha = false },
            onVerified = {
                showCaptcha = false
                viewModel.completeTask(task.id, answerInput, true) { success ->
                    if (success) {
                        selectedTaskForVerify = null
                    } else {
                        answerError = "Verification answer is incorrect. Please recheck."
                    }
                }
            }
        )
    }
}
