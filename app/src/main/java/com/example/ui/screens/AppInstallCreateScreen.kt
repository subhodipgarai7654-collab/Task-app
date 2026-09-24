package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.TaskEarnViewModel

data class AppPreset(
    val name: String,
    val packageName: String,
    val genre: String,
    val defaultTitle: String,
    val defaultDescription: String,
    val defaultCoins: Long,
    val defaultEstTime: String,
    val question: String,
    val answer: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInstallCreateScreen(
    viewModel: TaskEarnViewModel,
    onBackClick: () -> Unit,
    onTaskCreated: () -> Unit = onBackClick
) {
    val context = LocalContext.current
    val settings by viewModel.appSettings.collectAsState()

    // Presets for rapid creation & testing
    val presets = remember {
        listOf(
            AppPreset(
                name = "PhonePe UPI",
                packageName = "com.phonepe.app",
                genre = "Fintech & UPI",
                defaultTitle = "Install & Explore PhonePe UPI",
                defaultDescription = "Download PhonePe from official Play Store, launch the app, explore homepage for 60 seconds, and answer the verification question.",
                defaultCoins = 500,
                defaultEstTime = "2 min",
                question = "What is the primary theme color of PhonePe?",
                answer = "Purple",
                icon = Icons.Default.AccountBalance,
                color = Purple500
            ),
            AppPreset(
                name = "Meesho Deals",
                packageName = "com.meesho.supply",
                genre = "Shopping",
                defaultTitle = "Discover Meesho Online Shopping",
                defaultDescription = "Install Meesho from Play Store. Browse trending wholesale fashion deals for at least 1 minute and submit verification.",
                defaultCoins = 450,
                defaultEstTime = "1 min",
                question = "Which top category did you see on Meesho home?",
                answer = "Fashion",
                icon = Icons.Default.ShoppingBag,
                color = Red500
            ),
            AppPreset(
                name = "Duolingo",
                packageName = "com.duolingo",
                genre = "Education",
                defaultTitle = "Install Duolingo Language App",
                defaultDescription = "Install Duolingo from Play Store, open to the language selector, and complete instant install verification.",
                defaultCoins = 600,
                defaultEstTime = "2 min",
                question = "What mascot animal is featured on Duolingo?",
                answer = "Green Owl",
                icon = Icons.Default.School,
                color = Emerald500
            ),
            AppPreset(
                name = "Ludo King",
                packageName = "com.ludo.king",
                genre = "Gaming",
                defaultTitle = "Install & Play Ludo King",
                defaultDescription = "Download Ludo King board game from Google Play Store, explore the main menu, and verify installation.",
                defaultCoins = 400,
                defaultEstTime = "3 min",
                question = "What dice game is featured?",
                answer = "Ludo",
                icon = Icons.Default.SportsEsports,
                color = Blue500
            ),
            AppPreset(
                name = "Spotify",
                packageName = "com.spotify.music",
                genre = "Music & Media",
                defaultTitle = "Discover Spotify Music & Podcasts",
                defaultDescription = "Install official Spotify app from Play Store, open and explore free trending playlists for 60 seconds.",
                defaultCoins = 550,
                defaultEstTime = "2 min",
                question = "What is Spotify's brand logo color?",
                answer = "Green",
                icon = Icons.Default.MusicNote,
                color = Emerald500
            )
        )
    }

    var appTitle by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var targetUrl by remember { mutableStateOf("") }
    var appDescription by remember { mutableStateOf("1. Click 'Open in Store' to open Google Play Store.\n2. Download and install the app.\n3. Open and explore features for at least 60 seconds.\n4. Return here and answer the verification question.") }
    var selectedGenre by remember { mutableStateOf("Fintech & UPI") }
    var requirementType by remember { mutableStateOf("Install & Open (60s)") }
    var rewardCoinsStr by remember { mutableStateOf("500") }
    var estimatedTime by remember { mutableStateOf("2 min") }
    var verificationQuestion by remember { mutableStateOf("What was the headline on the welcome screen?") }
    var verificationAnswer by remember { mutableStateOf("Welcome") }
    var isPremiumOnly by remember { mutableStateOf(false) }

    var titleError by remember { mutableStateOf<String?>(null) }
    var packageError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Auto-sync Target URL when package name changes
    fun updatePackage(pkg: String) {
        packageName = pkg.trim()
        if (packageName.isNotBlank()) {
            targetUrl = "https://play.google.com/store/apps/details?id=$packageName"
            packageError = null
        }
    }

    fun applyPreset(preset: AppPreset) {
        appTitle = preset.defaultTitle
        updatePackage(preset.packageName)
        appDescription = preset.defaultDescription
        selectedGenre = preset.genre
        rewardCoinsStr = preset.defaultCoins.toString()
        estimatedTime = preset.defaultEstTime
        verificationQuestion = preset.question
        verificationAnswer = preset.answer
        titleError = null
        packageError = null
    }

    // Initialize with first preset if blank
    LaunchedEffect(Unit) {
        if (appTitle.isBlank()) {
            applyPreset(presets[0])
        }
    }

    val rewardCoins = rewardCoinsStr.toLongOrNull() ?: 0L
    val coinRate = if (settings.coinRatePerRupee <= 0) 100 else settings.coinRatePerRupee
    val calculatedRupees = rewardCoins.toDouble() / coinRate

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Create App Install Task",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Play Store App Campaign Creator",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("create_app_install_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            if (presets.isNotEmpty()) {
                                val nextPreset = presets.random()
                                applyPreset(nextPreset)
                                Toast.makeText(context, "Filled preset: ${nextPreset.name}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp).testTag("random_preset_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Random App", fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.testTag("app_install_create_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Blue500.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddBusiness,
                                contentDescription = null,
                                tint = Blue500,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "App Install Campaign Builder",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Create and publish official Google Play Store app install tasks. Users install, explore, and get rewarded after anti-bot verification.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }

            // Quick Preset Bar
            item {
                Column {
                    Text(
                        text = "1-Tap Popular App Templates",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { preset ->
                            FilterChip(
                                selected = packageName == preset.packageName,
                                onClick = { applyPreset(preset) },
                                label = {
                                    Text(
                                        text = preset.name,
                                        fontWeight = if (packageName == preset.packageName) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = preset.icon,
                                        contentDescription = null,
                                        tint = if (packageName == preset.packageName) Color.White else preset.color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Blue500,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("preset_chip_${preset.name.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                }
            }

            // Section 1: Application Identity
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "1. App Information & Store Link",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // App Title
                        OutlinedTextField(
                            value = appTitle,
                            onValueChange = {
                                appTitle = it
                                titleError = if (it.isBlank()) "App title is required" else null
                            },
                            label = { Text("App Campaign Title *") },
                            placeholder = { Text("e.g. Install PhonePe UPI & Payments") },
                            isError = titleError != null,
                            supportingText = { titleError?.let { Text(it, color = Red500) } },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_app_title")
                        )

                        // Package Name
                        OutlinedTextField(
                            value = packageName,
                            onValueChange = { updatePackage(it) },
                            label = { Text("Android Package Name *") },
                            placeholder = { Text("e.g. com.phonepe.app") },
                            isError = packageError != null,
                            supportingText = {
                                Text(
                                    packageError ?: "Used to link to Google Play Store and verify package ID",
                                    fontSize = 11.sp
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_package_name")
                        )

                        // Target Play Store URL
                        OutlinedTextField(
                            value = targetUrl,
                            onValueChange = { targetUrl = it },
                            label = { Text("Google Play Store URL") },
                            placeholder = { Text("https://play.google.com/store/apps/details?id=...") },
                            singleLine = true,
                            trailingIcon = {
                                if (targetUrl.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Could not open URL: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.testTag("test_store_url_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = "Test URL in Browser",
                                            tint = Blue500
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_target_url")
                        )

                        // Genre Chips
                        Text(
                            text = "App Category Genre",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val genres = listOf("Fintech & UPI", "Shopping", "Gaming", "Education", "Music & Media", "Social", "Tools")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            genres.forEach { genre ->
                                FilterChip(
                                    selected = selectedGenre == genre,
                                    onClick = { selectedGenre = genre },
                                    label = { Text(genre, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Requirements & Instructions
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "2. Install Flow & User Instructions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Required Action Type",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val actions = listOf(
                            "Install & Open (60s)",
                            "Install & Free Sign-up",
                            "Install & Keep for 24h",
                            "Install & Rate App"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            actions.forEach { action ->
                                FilterChip(
                                    selected = requirementType == action,
                                    onClick = { requirementType = action },
                                    label = { Text(action, fontSize = 11.sp) }
                                )
                            }
                        }

                        // Detailed Task Steps
                        OutlinedTextField(
                            value = appDescription,
                            onValueChange = { appDescription = it },
                            label = { Text("Step-by-Step Instructions *") },
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_app_description")
                        )

                        // Estimated Time
                        Text(
                            text = "Estimated Time to Complete",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val timeOptions = listOf("1 min", "2 min", "3 min", "5 min")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            timeOptions.forEach { t ->
                                FilterChip(
                                    selected = estimatedTime == t,
                                    onClick = { estimatedTime = t },
                                    label = { Text(t, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Rewards & Target Settings
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "3. Reward & Audience Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Reward Coins
                        OutlinedTextField(
                            value = rewardCoinsStr,
                            onValueChange = { rewardCoinsStr = it.filter { char -> char.isDigit() } },
                            label = { Text("Reward Coins *") },
                            placeholder = { Text("500") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = {
                                Text(
                                    text = "Coins",
                                    fontWeight = FontWeight.Bold,
                                    color = Gold500,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_reward_coins")
                        )

                        // Quick Coin Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("250", "450", "500", "750", "1000").forEach { amount ->
                                SuggestionChip(
                                    onClick = { rewardCoinsStr = amount },
                                    label = { Text("+$amount", fontSize = 11.sp) }
                                )
                            }
                        }

                        // Live Rupee Value Display Banner
                        Surface(
                            color = Emerald500.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyRupee,
                                    contentDescription = null,
                                    tint = Emerald500,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "User Earns: ₹${String.format("%.2f", calculatedRupees)} INR ($rewardCoins Coins)",
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald500,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Calculated at official conversion rate ($coinRate Coins = ₹1.00)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // VIP Only Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isPremiumOnly) Purple500.copy(alpha = 0.1f) else Color.Transparent)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = if (isPremiumOnly) Purple500 else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "VIP Exclusive Task",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Only upgraded VIP members can see and earn from this task",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = isPremiumOnly,
                                onCheckedChange = { isPremiumOnly = it },
                                modifier = Modifier.testTag("switch_vip_only")
                            )
                        }
                    }
                }
            }

            // Section 4: Anti-Fraud & Verification Inquiry
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Blue500,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "4. Anti-Fraud Discovery Verification",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "To prevent botting and fake installs, users must answer this question after inspecting the installed app.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Verification Question
                        OutlinedTextField(
                            value = verificationQuestion,
                            onValueChange = { verificationQuestion = it },
                            label = { Text("Verification Question *") },
                            placeholder = { Text("e.g. What is the welcome headline or button text?") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_verification_question")
                        )

                        // Expected Verification Answer
                        OutlinedTextField(
                            value = verificationAnswer,
                            onValueChange = { verificationAnswer = it },
                            label = { Text("Expected Answer / Keyword *") },
                            placeholder = { Text("e.g. Welcome") },
                            supportingText = { Text("Comparison is case-insensitive. Bot protection will verify this.", fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_verification_answer")
                        )
                    }
                }
            }

            // Section 5: Live Marketplace Card Preview
            item {
                Column {
                    Text(
                        text = "Live Preview (As users will see in App Discovery)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppInstallPreviewCard(
                        title = if (appTitle.isBlank()) "Sample App Title" else appTitle,
                        description = appDescription,
                        coins = rewardCoins,
                        genre = selectedGenre,
                        isVipOnly = isPremiumOnly,
                        targetUrl = targetUrl
                    )
                }
            }

            // Action: Publish Campaign Button
            item {
                Button(
                    onClick = {
                        // Validation
                        if (appTitle.isBlank()) {
                            titleError = "Please specify an app title"
                            Toast.makeText(context, "App title is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (packageName.isBlank()) {
                            packageError = "Please enter an Android package name"
                            Toast.makeText(context, "Package name is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (rewardCoins <= 0) {
                            Toast.makeText(context, "Please set a valid reward coin amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (verificationQuestion.isBlank() || verificationAnswer.isBlank()) {
                            Toast.makeText(context, "Please enter both verification question and answer", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSubmitting = true
                        val newTask = TaskEntity(
                            title = appTitle.trim(),
                            description = appDescription.trim(),
                            category = "APP_INSTALL", // Primary category: App Install
                            rewardCoins = rewardCoins,
                            estimatedTime = estimatedTime,
                            targetUrl = targetUrl.ifBlank { "https://play.google.com/store/apps/details?id=${packageName.trim()}" },
                            requirements = "$requirementType • Verified Android installation on device.",
                            verificationQuestion = verificationQuestion.trim(),
                            verificationAnswer = verificationAnswer.trim(),
                            isDaily = false,
                            isPremiumOnly = isPremiumOnly,
                            isEnabled = true,
                            dailyLimit = 1
                        )

                        viewModel.createAppInstallTask(newTask) {
                            isSubmitting = false
                            Toast.makeText(context, "App Install Task Published!", Toast.LENGTH_LONG).show()
                            onTaskCreated()
                        }
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("publish_app_install_btn")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Publishing Campaign...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Publish App Install Task", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

/**
 * Real-time preview card demonstrating how the created App Install task looks to end users
 */
@Composable
private fun AppInstallPreviewCard(
    title: String,
    description: String,
    coins: Long,
    genre: String,
    isVipOnly: Boolean,
    targetUrl: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Blue500.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .testTag("app_install_preview_card")
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
                            .size(46.dp)
                            .background(Blue500.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.InstallMobile,
                            contentDescription = null,
                            tint = Blue500,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Play Store • $genre",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "+$coins Coins",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gold500,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (isVipOnly) {
                    Surface(
                        color = Purple500.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "VIP ONLY",
                            color = Purple500,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Icon(Icons.Default.Shop, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open in Store", fontSize = 12.sp)
                }

                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Text("Verify & Earn", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
