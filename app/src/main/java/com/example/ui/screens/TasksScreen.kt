package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.TaskEntity
import com.example.data.model.UserEntity
import com.example.ui.components.AntiBotCaptchaDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.TaskCardItem
import com.example.ui.theme.Blue500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Gold500
import com.example.ui.theme.Red500
import com.example.ui.viewmodel.TaskEarnViewModel

@Composable
fun TasksScreen(
    viewModel: TaskEarnViewModel,
    user: UserEntity,
    selectedCategoryInitial: String = "ALL",
    onNavigateToPremium: () -> Unit,
    onCreateAppInstallClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val allTasks by viewModel.activeTasks.collectAsState()
    val completions by viewModel.userCompletions.collectAsState()
    val settings by viewModel.appSettings.collectAsState()

    var selectedCategory by remember { mutableStateOf(selectedCategoryInitial) }
    LaunchedEffect(selectedCategoryInitial) {
        selectedCategory = selectedCategoryInitial
    }
    var searchQuery by remember { mutableStateOf("") }
    var activeTaskForModal by remember { mutableStateOf<TaskEntity?>(null) }
    var showCaptchaForTask by remember { mutableStateOf(false) }
    var proofInput by remember { mutableStateOf("") }
    var proofError by remember { mutableStateOf<String?>(null) }

    val completedTaskIds = remember(completions) {
        completions.filter { it.status == "VERIFIED" }.map { it.taskId }.toSet()
    }

    val categories = listOf(
        "ALL" to "All Tasks",
        "APP_INSTALL" to "App Installs",
        "APP_DISCOVERY" to "App Discovery",
        "WATCH_VIDEO" to "Watch Video",
        "YOUTUBE" to "YouTube Video",
        "PLAY_STORE" to "Play Store",
        "DAILY" to "Daily Tasks",
        "BONUS" to "Bonus Tasks",
        "PREMIUM" to "Premium Tasks"
    )

    val filteredTasks = remember(allTasks, selectedCategory, searchQuery) {
        allTasks.filter { task ->
            val matchCategory = when (selectedCategory) {
                "ALL" -> true
                "APP_INSTALL" -> task.category == "APP_INSTALL"
                "APP_DISCOVERY" -> task.category == "APP_DISCOVERY" || task.category == "APP_INSTALL" || task.category == "PLAY_STORE"
                else -> task.category.equals(selectedCategory, ignoreCase = true)
            }
            val matchSearch = searchQuery.isBlank() ||
                    task.title.contains(searchQuery, ignoreCase = true) ||
                    task.description.contains(searchQuery, ignoreCase = true)
            matchCategory && matchSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("tasks_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Task Center",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Complete verified tasks to earn instant reward coins",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onCreateAppInstallClick,
                colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("tasks_create_app_install_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("App Install", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search tasks...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("task_search_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { (catKey, catLabel) ->
                val isSelected = selectedCategory == catKey
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = catKey },
                    label = { Text(catLabel, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald500,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_chip_$catKey")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Task List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredTasks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Task,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No tasks found in this category",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Try selecting 'All Tasks' or clear your search term.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredTasks) { task ->
                    val isDone = completedTaskIds.contains(task.id)
                    TaskCardItem(
                        task = task,
                        isCompleted = isDone,
                        conversionRate = settings.coinRatePerRupee,
                        onStartClick = {
                            if (task.isPremiumOnly && !user.isPremium) {
                                onNavigateToPremium()
                            } else {
                                activeTaskForModal = task
                                proofInput = ""
                                proofError = null
                            }
                        }
                    )
                }
            }
        }
    }

    // Task Execution & Verification Modal
    activeTaskForModal?.let { task ->
        Dialog(onDismissRequest = { activeTaskForModal = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .testTag("task_detail_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Emerald500.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = task.category.replace("_", " "),
                                color = Emerald500,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(onClick = { activeTaskForModal = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Reward: +${task.rewardCoins} Coins (Est. ${task.estimatedTime})",
                        fontWeight = FontWeight.Bold,
                        color = Gold500,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Transparent destination notice
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "External Link Destination:",
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
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(task.targetUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // fallback
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_external_task_url")
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Link / App / Video", fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Verification Step:",
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
                        value = proofInput,
                        onValueChange = { proofInput = it; proofError = null },
                        label = { Text("Enter verification response") },
                        placeholder = { Text(if (task.verificationAnswer.isNotBlank()) "e.g. ${task.verificationAnswer}" else "e.g. Confirmed") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_verification_input")
                    )

                    if (proofError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = proofError ?: "",
                            color = Red500,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (proofInput.isBlank()) {
                                proofError = "Please provide the verification answer."
                                return@Button
                            }
                            // Require Anti-bot CAPTCHA before reward completion as required
                            showCaptchaForTask = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_task_completion_button")
                    ) {
                        Text("Verify & Claim ${task.rewardCoins} Coins", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showCaptchaForTask && activeTaskForModal != null) {
        val task = activeTaskForModal!!
        AntiBotCaptchaDialog(
            purpose = "Task Reward Claim",
            onDismiss = { showCaptchaForTask = false },
            onVerified = {
                showCaptchaForTask = false
                viewModel.completeTask(task.id, proofInput, true) { success ->
                    if (success) {
                        activeTaskForModal = null
                    } else {
                        proofError = "Verification failed. Please check your answer."
                    }
                }
            }
        )
    }
}
