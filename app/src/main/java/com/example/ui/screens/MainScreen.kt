package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.TaskEntity
import com.example.ui.theme.Emerald500
import com.example.ui.viewmodel.TaskEarnViewModel
import com.example.ui.viewmodel.UiMessage
import kotlinx.coroutines.flow.collectLatest

enum class MainDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    TASKS("Tasks", Icons.Filled.TaskAlt, Icons.Outlined.TaskAlt, "nav_tasks"),
    EARN("Earn", Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp, "nav_earn"),
    WALLET("Wallet", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "nav_wallet"),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
}

enum class SubScreen {
    NONE,
    VIDEO_TASK,
    APP_DISCOVERY,
    APP_INSTALL_CREATE,
    PREMIUM,
    WITHDRAWAL_HISTORY,
    ADMIN
}

@Composable
fun MainScreen(
    viewModel: TaskEarnViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var currentDestination by remember { mutableStateOf(MainDestination.HOME) }
    var currentSubScreen by remember { mutableStateOf(SubScreen.NONE) }
    var taskScreenCategory by remember { mutableStateOf("ALL") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen to ViewModel UI events (success/error toasts)
    LaunchedEffect(Unit) {
        viewModel.uiMessage.collectLatest { msg ->
            when (msg) {
                is UiMessage.Success -> snackbarHostState.showSnackbar(msg.message)
                is UiMessage.Error -> snackbarHostState.showSnackbar(msg.message)
            }
        }
    }

    if (currentSubScreen == SubScreen.ADMIN) {
        AdminScreen(
            viewModel = viewModel,
            onExitAdmin = { currentSubScreen = SubScreen.NONE }
        )
        return
    }

    if (currentUser == null) {
        AuthScreen(
            viewModel = viewModel,
            onAdminClick = { currentSubScreen = SubScreen.ADMIN }
        )
        return
    }

    val user = currentUser!!

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentSubScreen == SubScreen.NONE) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    MainDestination.values().forEach { destination ->
                        val isSelected = currentDestination == destination
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.title
                                )
                            },
                            label = { Text(destination.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Emerald500,
                                selectedTextColor = Emerald500,
                                indicatorColor = Emerald500.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag(destination.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentSubScreen) {
                SubScreen.VIDEO_TASK -> {
                    VideoTaskScreen(
                        viewModel = viewModel,
                        onBackClick = { currentSubScreen = SubScreen.NONE }
                    )
                }
                SubScreen.APP_DISCOVERY -> {
                    AppDiscoveryScreen(
                        viewModel = viewModel,
                        onBackClick = { currentSubScreen = SubScreen.NONE },
                        onCreateAppInstallClick = { currentSubScreen = SubScreen.APP_INSTALL_CREATE }
                    )
                }
                SubScreen.APP_INSTALL_CREATE -> {
                    AppInstallCreateScreen(
                        viewModel = viewModel,
                        onBackClick = { currentSubScreen = SubScreen.APP_DISCOVERY },
                        onTaskCreated = { currentSubScreen = SubScreen.APP_DISCOVERY }
                    )
                }
                SubScreen.PREMIUM -> {
                    PremiumScreen(
                        viewModel = viewModel,
                        user = user,
                        onBackClick = { currentSubScreen = SubScreen.NONE }
                    )
                }
                SubScreen.WITHDRAWAL_HISTORY -> {
                    WithdrawalHistoryScreen(
                        viewModel = viewModel,
                        user = user,
                        onBackClick = { currentSubScreen = SubScreen.NONE },
                        onRequestPayoutClick = {
                            currentSubScreen = SubScreen.NONE
                            currentDestination = MainDestination.WALLET
                        }
                    )
                }
                SubScreen.ADMIN -> {
                    // Handled above
                }
                SubScreen.NONE -> {
                    when (currentDestination) {
                        MainDestination.HOME -> {
                            HomeScreen(
                                viewModel = viewModel,
                                user = user,
                                onNavigateToTasks = {
                                    taskScreenCategory = "ALL"
                                    currentDestination = MainDestination.TASKS
                                },
                                onNavigateToEarn = { currentDestination = MainDestination.EARN },
                                onNavigateToWallet = { currentDestination = MainDestination.WALLET },
                                onNavigateToPremium = { currentSubScreen = SubScreen.PREMIUM },
                                onStartTask = { task ->
                                    if (task.category == "WATCH_VIDEO" || task.category == "YOUTUBE") {
                                        currentSubScreen = SubScreen.VIDEO_TASK
                                    } else if (task.category == "APP_DISCOVERY" || task.category == "PLAY_STORE" || task.category == "APP_INSTALL") {
                                        currentSubScreen = SubScreen.APP_DISCOVERY
                                    } else {
                                        taskScreenCategory = task.category
                                        currentDestination = MainDestination.TASKS
                                    }
                                }
                            )
                        }
                        MainDestination.TASKS -> {
                            TasksScreen(
                                viewModel = viewModel,
                                user = user,
                                selectedCategoryInitial = taskScreenCategory,
                                onNavigateToPremium = { currentSubScreen = SubScreen.PREMIUM },
                                onCreateAppInstallClick = { currentSubScreen = SubScreen.APP_INSTALL_CREATE }
                            )
                        }
                        MainDestination.EARN -> {
                            EarnScreen(
                                viewModel = viewModel,
                                user = user,
                                onNavigateToTasks = { category ->
                                    taskScreenCategory = category
                                    currentDestination = MainDestination.TASKS
                                },
                                onNavigateToPremium = { currentSubScreen = SubScreen.PREMIUM },
                                onNavigateToWallet = { currentDestination = MainDestination.WALLET }
                            )
                        }
                        MainDestination.WALLET -> {
                            WalletScreen(
                                viewModel = viewModel,
                                user = user,
                                onNavigateToWithdrawalHistory = {
                                    currentSubScreen = SubScreen.WITHDRAWAL_HISTORY
                                }
                            )
                        }
                        MainDestination.PROFILE -> {
                            ProfileScreen(
                                viewModel = viewModel,
                                user = user,
                                onAdminClick = { currentSubScreen = SubScreen.ADMIN },
                                onNavigateToPremium = { currentSubScreen = SubScreen.PREMIUM },
                                onNavigateToEarn = { currentDestination = MainDestination.EARN }
                            )
                        }
                    }
                }
            }
        }
    }
}
