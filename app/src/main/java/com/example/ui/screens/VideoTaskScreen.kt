package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.TaskEntity
import com.example.ui.components.AntiBotCaptchaDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Gold500
import com.example.ui.theme.Red500
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.TaskEarnViewModel
import kotlinx.coroutines.delay

@Composable
fun VideoTaskScreen(
    viewModel: TaskEarnViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val allTasks by viewModel.activeTasks.collectAsState()
    val completions by viewModel.userCompletions.collectAsState()

    val videoTasks = remember(allTasks) {
        allTasks.filter { it.category == "WATCH_VIDEO" || it.category == "YOUTUBE" }
    }

    val completedIds = remember(completions) {
        completions.filter { it.status == "VERIFIED" }.map { it.taskId }.toSet()
    }

    var selectedVideoTask by remember { mutableStateOf(videoTasks.firstOrNull()) }
    var inAppTimerSeconds by remember { mutableStateOf(0) }
    var timerRunning by remember { mutableStateOf(false) }
    var showCaptcha by remember { mutableStateOf(false) }
    var verificationAnswer by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Countdown effect for in-app demo timer
    LaunchedEffect(timerRunning, inAppTimerSeconds) {
        if (timerRunning && inAppTimerSeconds > 0) {
            delay(1000L)
            inAppTimerSeconds -= 1
            if (inAppTimerSeconds == 0) {
                timerRunning = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("video_task_screen"),
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
                        text = "Watch Video & Earn Coins",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Educational & sponsor videos with verified completion",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Prominent Transparency Notice (Mandatory Requirement #4)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Gold500,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Transparent Video Reward Policy",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Third-party YouTube videos are viewed voluntarily. Merely opening or watching third-party content does not guarantee rewards. Task reward coins are credited exclusively upon answering the verification check and passing anti-bot security.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        // Active Selected Video Player Card
        selectedVideoTask?.let { task ->
            val isDone = completedIds.contains(task.id)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth().testTag("active_video_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current Video Task",
                                style = MaterialTheme.typography.labelMedium,
                                color = Emerald500,
                                fontWeight = FontWeight.Bold
                            )
                            if (isDone) {
                                StatusBadge(status = "VERIFIED")
                            } else {
                                Text(
                                    text = "+${task.rewardCoins} Coins",
                                    color = Gold500,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Video Link Card
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Video Link:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = task.targetUrl,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(task.targetUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Red500),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("open_video_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Video", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    inAppTimerSeconds = 30
                                    timerRunning = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("start_demo_timer_button")
                            ) {
                                Text(
                                    text = if (timerRunning) "Timer: ${inAppTimerSeconds}s" else "Demo Timer (30s)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (!isDone) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Verification Question:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = task.verificationQuestion,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = verificationAnswer,
                                onValueChange = { verificationAnswer = it; errorMessage = null },
                                label = { Text("Enter Answer") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("video_verification_input")
                            )

                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = Red500,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (verificationAnswer.isBlank()) {
                                        errorMessage = "Please enter the verification answer."
                                        return@Button
                                    }
                                    showCaptcha = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("claim_video_reward_button")
                            ) {
                                Text("Verify & Claim +${task.rewardCoins} Coins", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Suggested Videos Section
        item {
            Text(
                text = "Suggested Videos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(videoTasks) { task ->
            val isDone = completedIds.contains(task.id)
            val isSelected = selectedVideoTask?.id == task.id
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                ),
                onClick = {
                    selectedVideoTask = task
                    verificationAnswer = ""
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth().testTag("suggested_video_${task.id}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = Red500,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "+${task.rewardCoins} Coins • ${task.estimatedTime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isDone) {
                        StatusBadge(status = "VERIFIED")
                    } else {
                        Button(
                            onClick = {
                                selectedVideoTask = task
                                verificationAnswer = ""
                                errorMessage = null
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Select", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (showCaptcha && selectedVideoTask != null) {
        val task = selectedVideoTask!!
        AntiBotCaptchaDialog(
            purpose = "Video Reward Verification",
            onDismiss = { showCaptcha = false },
            onVerified = {
                showCaptcha = false
                viewModel.completeTask(task.id, verificationAnswer, true) { success ->
                    if (!success) {
                        errorMessage = "Verification answer does not match."
                    } else {
                        verificationAnswer = ""
                    }
                }
            }
        )
    }
}
