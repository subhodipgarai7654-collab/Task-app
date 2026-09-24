package com.example.data.repository

import com.example.data.local.TaskEarnDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TaskEarnRepository(private val db: TaskEarnDatabase) {

    private val userDao = db.userDao()
    private val taskDao = db.taskDao()
    private val completionDao = db.taskCompletionDao()
    private val coinTransactionDao = db.coinTransactionDao()
    private val withdrawalDao = db.withdrawalDao()
    private val paymentMethodDao = db.paymentMethodDao()
    private val premiumDao = db.premiumMembershipDao()
    private val referralDao = db.referralDao()
    private val adminDao = db.adminUserDao()
    private val adminLogDao = db.adminLogDao()
    private val settingsDao = db.appSettingsDao()

    // Observables
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val activeTasks: Flow<List<TaskEntity>> = taskDao.getActiveTasks()
    val allWithdrawals: Flow<List<WithdrawalEntity>> = withdrawalDao.getAllWithdrawals()
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val appSettings: Flow<AppSettingsEntity?> = settingsDao.getSettings()
    val adminLogs: Flow<List<AdminLogEntity>> = adminLogDao.getRecentLogs()

    // Metrics for Admin Dashboard
    val totalUsersCount = userDao.getUserCount()
    val activeUsersCount = userDao.getActiveUserCount()
    val premiumUsersCount = userDao.getPremiumUserCount()
    val totalTasksCount = taskDao.getTaskCount()
    val verifiedCompletionsCount = completionDao.getTotalVerifiedCompletions()
    val pendingCompletionsCount = completionDao.getTotalPendingCompletions()
    val totalCoinsIssued = coinTransactionDao.getTotalCoinsIssued()
    val totalWithdrawalsCount = withdrawalDao.getTotalWithdrawalCount()
    val pendingWithdrawalsCount = withdrawalDao.getPendingWithdrawalCount()
    val paidWithdrawalsCount = withdrawalDao.getPaidWithdrawalCount()
    val totalPaidAmount = withdrawalDao.getTotalPaidAmount()
    val totalPremiumRevenue = premiumDao.getTotalPremiumRevenue()

    fun getUserFlow(userId: Long): Flow<UserEntity?> = userDao.getUserById(userId)
    fun getTransactionsForUser(userId: Long): Flow<List<CoinTransactionEntity>> = coinTransactionDao.getTransactionsForUser(userId)
    fun getWithdrawalsForUser(userId: Long): Flow<List<WithdrawalEntity>> = withdrawalDao.getWithdrawalsForUser(userId)
    fun getPaymentMethodsForUser(userId: Long): Flow<List<PaymentMethodEntity>> = paymentMethodDao.getMethodsForUser(userId)
    fun getCompletionsForUser(userId: Long): Flow<List<TaskCompletionEntity>> = completionDao.getCompletionsForUser(userId)
    fun getReferralsForUser(userId: Long): Flow<List<ReferralEntity>> = referralDao.getReferralsForUser(userId)
    fun getMembershipForUser(userId: Long): Flow<PremiumMembershipEntity?> = premiumDao.getMembershipForUser(userId)

    suspend fun getSettingsOnce(): AppSettingsEntity {
        return withContext(Dispatchers.IO) {
            settingsDao.getSettingsOnce() ?: AppSettingsEntity()
        }
    }

    // --- Authentication ---
    suspend fun registerUser(
        name: String,
        emailOrPhone: String,
        password: String,
        referralCodeInput: String? = null
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByEmailOrPhone(emailOrPhone.trim().lowercase())
        if (existing != null) {
            return@withContext Result.failure(Exception("An account with this email or mobile number already exists."))
        }

        val cleanIdentifier = emailOrPhone.trim().lowercase()
        val isEmail = cleanIdentifier.contains("@")
        val email = if (isEmail) cleanIdentifier else "$cleanIdentifier@user.taskearn.local"
        val phone = if (!isEmail) cleanIdentifier else "9876500000"

        val uniqueId = "TEP-" + (100000..999999).random()
        val myRefCode = "TEP" + UUID.randomUUID().toString().substring(0, 4).uppercase()

        val newUser = UserEntity(
            userId = uniqueId,
            name = name.trim(),
            email = email,
            phone = phone,
            passwordHash = password,
            coins = 100, // Welcome signup bonus
            lifetimeCoins = 100,
            todayCoins = 100,
            referralCode = myRefCode,
            referredBy = referralCodeInput?.trim()?.uppercase()
        )

        val newId = userDao.insertUser(newUser)
        val createdUser = newUser.copy(id = newId)

        // Ledger entry for signup bonus
        coinTransactionDao.insertTransaction(
            CoinTransactionEntity(
                userId = newId,
                amount = 100,
                type = "TASK_REWARD",
                description = "Welcome Signup Bonus"
            )
        )

        // Check if referred by someone valid
        referralCodeInput?.trim()?.uppercase()?.let { refCode ->
            if (refCode.isNotBlank()) {
                val referrer = userDao.getUserByReferralCode(refCode)
                if (referrer != null && referrer.id != newId) {
                    val settings = getSettingsOnce()
                    val bonus = settings.referralBonusCoins
                    // Reward referrer
                    userDao.adjustUserCoins(referrer.id, bonus)
                    referralDao.insertReferral(
                        ReferralEntity(
                            referrerId = referrer.id,
                            referredUserId = newId,
                            referredUserName = createdUser.name,
                            rewardCoins = bonus
                        )
                    )
                    coinTransactionDao.insertTransaction(
                        CoinTransactionEntity(
                            userId = referrer.id,
                            amount = bonus,
                            type = "REFERRAL_BONUS",
                            description = "Referral Reward for inviting ${createdUser.name}"
                        )
                    )
                }
            }
        }

        Result.success(createdUser)
    }

    suspend fun loginUser(identifier: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmailOrPhone(identifier.trim().lowercase())
        if (user == null) {
            return@withContext Result.failure(Exception("Account not found. Please check credentials or register."))
        }
        if (user.passwordHash != password) {
            return@withContext Result.failure(Exception("Incorrect password. Please try again."))
        }
        if (user.status == "BLOCKED") {
            return@withContext Result.failure(Exception("Your account has been suspended due to security policy violations."))
        }
        Result.success(user)
    }

    suspend fun resetPassword(identifier: String, newPass: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmailOrPhone(identifier.trim().lowercase())
            ?: return@withContext Result.failure(Exception("No account found with this identifier."))
        userDao.updateUser(user.copy(passwordHash = newPass))
        Result.success(true)
    }

    suspend fun updateUserProfile(userId: Long, name: String, avatarId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByIdOnce(userId) ?: return@withContext Result.failure(Exception("User not found"))
        userDao.updateUser(user.copy(name = name, avatarId = avatarId))
        Result.success(true)
    }

    // --- Daily Check-in Streak ---
    suspend fun claimDailyCheckin(userId: Long): Result<Long> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByIdOnce(userId) ?: return@withContext Result.failure(Exception("User not found"))
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (user.lastCheckinDate == todayStr) {
            return@withContext Result.failure(Exception("Daily check-in already claimed today. Come back tomorrow!"))
        }

        val newStreak = user.checkinStreak + 1
        val bonus = 100L + (newStreak.coerceAtMost(7) * 25L) // Progressive streak bonus

        userDao.updateUser(
            user.copy(
                coins = user.coins + bonus,
                lifetimeCoins = user.lifetimeCoins + bonus,
                todayCoins = user.todayCoins + bonus,
                lastCheckinDate = todayStr,
                checkinStreak = newStreak
            )
        )

        coinTransactionDao.insertTransaction(
            CoinTransactionEntity(
                userId = userId,
                amount = bonus,
                type = "DAILY_CHECKIN",
                description = "Day $newStreak Check-in Bonus"
            )
        )

        Result.success(bonus)
    }

    // --- Task Verification & Rewards ---
    suspend fun completeTask(
        userId: Long,
        taskId: Long,
        proofAnswer: String,
        captchaPassed: Boolean
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (!captchaPassed) {
            return@withContext Result.failure(Exception("Anti-bot CAPTCHA verification failed. Please try again."))
        }

        val user = userDao.getUserByIdOnce(userId) ?: return@withContext Result.failure(Exception("User not found"))
        if (user.status == "BLOCKED") {
            return@withContext Result.failure(Exception("Account suspended. Cannot perform tasks."))
        }

        val task = taskDao.getTaskById(taskId) ?: return@withContext Result.failure(Exception("Task not found"))
        if (!task.isEnabled) {
            return@withContext Result.failure(Exception("This task is currently inactive."))
        }

        if (task.isPremiumOnly && !user.isPremium) {
            return@withContext Result.failure(Exception("This is a VIP Premium task. Upgrade to Premium to unlock."))
        }

        // Duplicate check
        val existing = completionDao.getCompletion(taskId, userId)
        if (existing != null && existing.status == "VERIFIED") {
            return@withContext Result.failure(Exception("You have already completed and received rewards for this task."))
        }

        // Verification validation
        if (task.verificationAnswer.isNotBlank()) {
            if (!proofAnswer.trim().equals(task.verificationAnswer.trim(), ignoreCase = true)) {
                return@withContext Result.failure(Exception("Verification answer does not match. Please verify carefully."))
            }
        }

        val finalReward = if (user.isPremium) {
            (task.rewardCoins * 1.5).toLong() // 1.5x VIP bonus
        } else {
            task.rewardCoins
        }

        // Save completion record
        completionDao.insertCompletion(
            TaskCompletionEntity(
                taskId = task.id,
                taskTitle = task.title,
                userId = userId,
                status = "VERIFIED",
                rewardCoins = finalReward,
                proofText = proofAnswer.trim(),
                verifiedAt = System.currentTimeMillis()
            )
        )

        // Credit coins
        userDao.adjustUserCoins(userId, finalReward)

        // Record in ledger
        coinTransactionDao.insertTransaction(
            CoinTransactionEntity(
                userId = userId,
                amount = finalReward,
                type = "TASK_REWARD",
                description = "Task Reward: ${task.title}"
            )
        )

        Result.success(finalReward)
    }

    // --- Withdrawal System ---
    suspend fun requestWithdrawal(
        userId: Long,
        amountRupees: Double,
        method: String,
        identifier: String,
        holderName: String,
        captchaPassed: Boolean
    ): Result<WithdrawalEntity> = withContext(Dispatchers.IO) {
        if (!captchaPassed) {
            return@withContext Result.failure(Exception("Anti-bot CAPTCHA verification required."))
        }

        val settings = getSettingsOnce()
        if (amountRupees < settings.minWithdrawalRupees) {
            return@withContext Result.failure(Exception("Minimum withdrawal is ₹${settings.minWithdrawalRupees}"))
        }

        val coinsRequired = (amountRupees * settings.coinRatePerRupee).toLong()

        val user = userDao.getUserByIdOnce(userId) ?: return@withContext Result.failure(Exception("User not found"))
        if (user.coins < coinsRequired) {
            return@withContext Result.failure(Exception("Insufficient coins. You have ${user.coins} coins (₹${user.coins / settings.coinRatePerRupee.toDouble()}) but need $coinsRequired coins."))
        }

        // Mask identifier for safety
        val masked = if (method == "UPI") {
            val parts = identifier.split("@")
            if (parts.size == 2 && parts[0].length > 3) {
                "${parts[0].take(3)}***@${parts[1]}"
            } else {
                "${identifier.take(2)}***"
            }
        } else {
            "Acct ending in ${identifier.takeLast(4)}"
        }

        // Deduct coins from user balance immediately
        userDao.adjustUserCoins(userId, -coinsRequired)

        // Record deduction transaction
        coinTransactionDao.insertTransaction(
            CoinTransactionEntity(
                userId = userId,
                amount = -coinsRequired,
                type = "WITHDRAWAL_DEDUCT",
                description = "Withdrawal request for ₹$amountRupees to $masked"
            )
        )

        val withdrawal = WithdrawalEntity(
            userId = userId,
            amountInRupees = amountRupees,
            coinsDeducted = coinsRequired,
            method = method,
            accountDetailsMasked = masked,
            accountHolderName = holderName.trim(),
            status = "PENDING"
        )

        val wid = withdrawalDao.insertWithdrawal(withdrawal)
        Result.success(withdrawal.copy(id = wid))
    }

    // --- Payment Methods ---
    suspend fun addPaymentMethod(
        userId: Long,
        type: String,
        identifier: String,
        holderName: String,
        ifsc: String = "",
        bankName: String = ""
    ): Result<Long> = withContext(Dispatchers.IO) {
        val masked = if (type == "UPI") {
            val parts = identifier.split("@")
            if (parts.size == 2 && parts[0].length > 3) {
                "${parts[0].take(3)}***@${parts[1]}"
            } else {
                "${identifier.take(2)}***"
            }
        } else {
            "Bank ****${identifier.takeLast(4)}"
        }

        val id = paymentMethodDao.insertPaymentMethod(
            PaymentMethodEntity(
                userId = userId,
                type = type,
                identifier = identifier.trim(),
                maskedIdentifier = masked,
                holderName = holderName.trim(),
                ifsc = ifsc.trim().uppercase(),
                bankName = bankName.trim(),
                isDefault = true
            )
        )
        Result.success(id)
    }

    // --- Premium Membership ---
    suspend fun activatePremiumMembership(userId: Long, txRef: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val settings = getSettingsOnce()
        val user = userDao.getUserByIdOnce(userId) ?: return@withContext Result.failure(Exception("User not found"))

        userDao.updatePremiumStatus(userId, true)
        premiumDao.insertMembership(
            PremiumMembershipEntity(
                userId = userId,
                planName = "TaskEarn Pro VIP",
                pricePaid = settings.premiumPriceRupees,
                status = "ACTIVE",
                transactionRef = txRef
            )
        )

        // Premium welcome bonus coins
        userDao.adjustUserCoins(userId, 1000)
        coinTransactionDao.insertTransaction(
            CoinTransactionEntity(
                userId = userId,
                amount = 1000,
                type = "PREMIUM_BONUS",
                description = "VIP Premium Activation Bonus (1000 Coins)"
            )
        )

        adminLogDao.insertLog(
            AdminLogEntity(
                adminUsername = "System",
                action = "PREMIUM_ACTIVATED",
                targetEntity = "USER_${user.userId}",
                details = "Activated VIP plan for ${user.name} (Ref: $txRef)"
            )
        )

        Result.success(true)
    }

    // --- Admin Operations ---
    suspend fun verifyAdminLogin(username: String, pass: String): Result<AdminUserEntity> = withContext(Dispatchers.IO) {
        // subhodip@2007 / subhodip@7
        if (username.trim() == "subhodip@2007" && pass.trim() == "subhodip@7") {
            val admin = adminDao.getAdminByUsername("subhodip@2007") ?: AdminUserEntity(
                username = "subhodip@2007",
                passwordHash = "subhodip@7",
                role = "SUPER_ADMIN"
            )
            adminLogDao.insertLog(
                AdminLogEntity(
                    adminUsername = "subhodip@2007",
                    action = "ADMIN_LOGIN",
                    targetEntity = "SYSTEM",
                    details = "Super Admin logged in successfully."
                )
            )
            return@withContext Result.success(admin)
        }

        val existing = adminDao.getAdminByUsername(username.trim())
        if (existing != null && existing.passwordHash == pass.trim()) {
            return@withContext Result.success(existing)
        }

        Result.failure(Exception("Invalid admin credentials."))
    }

    suspend fun adminToggleUserBlock(userId: Long, currentStatus: String, adminName: String): Result<String> = withContext(Dispatchers.IO) {
        val newStatus = if (currentStatus == "ACTIVE") "BLOCKED" else "ACTIVE"
        userDao.updateUserStatus(userId, newStatus)
        adminLogDao.insertLog(
            AdminLogEntity(
                adminUsername = adminName,
                action = if (newStatus == "BLOCKED") "BLOCK_USER" else "UNBLOCK_USER",
                targetEntity = "USER_$userId",
                details = "Changed user status to $newStatus"
            )
        )
        Result.success(newStatus)
    }

    suspend fun adminProcessWithdrawal(
        withdrawalId: Long,
        newStatus: String, // "APPROVED", "PAID", "REJECTED"
        transactionUtr: String,
        adminNote: String,
        adminName: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val withdrawal = withdrawalDao.getWithdrawalById(withdrawalId)
            ?: return@withContext Result.failure(Exception("Withdrawal record not found"))

        if (withdrawal.status == "PAID" || withdrawal.status == "REJECTED") {
            return@withContext Result.failure(Exception("Withdrawal is already finalized (${withdrawal.status})."))
        }

        // If rejected, refund coins back to user
        if (newStatus == "REJECTED") {
            userDao.adjustUserCoins(withdrawal.userId, withdrawal.coinsDeducted)
            coinTransactionDao.insertTransaction(
                CoinTransactionEntity(
                    userId = withdrawal.userId,
                    amount = withdrawal.coinsDeducted,
                    type = "WITHDRAWAL_REFUND",
                    description = "Refund for rejected withdrawal #$withdrawalId ($adminNote)"
                )
            )
        }

        withdrawalDao.updateWithdrawal(
            withdrawal.copy(
                status = newStatus,
                utrTransactionId = transactionUtr.trim(),
                adminNote = adminNote.trim(),
                processedAt = System.currentTimeMillis()
            )
        )

        adminLogDao.insertLog(
            AdminLogEntity(
                adminUsername = adminName,
                action = "WITHDRAWAL_$newStatus",
                targetEntity = "WITHDRAWAL_$withdrawalId",
                details = "Updated to $newStatus (UTR: $transactionUtr, Note: $adminNote)"
            )
        )

        Result.success(true)
    }

    suspend fun adminSaveTask(task: TaskEntity, adminName: String): Result<Long> = withContext(Dispatchers.IO) {
        val id = if (task.id == 0L) {
            taskDao.insertTask(task)
        } else {
            taskDao.updateTask(task)
            task.id
        }

        adminLogDao.insertLog(
            AdminLogEntity(
                adminUsername = adminName,
                action = if (task.id == 0L) "CREATE_TASK" else "UPDATE_TASK",
                targetEntity = "TASK_$id",
                details = "Saved task: ${task.title} (${task.rewardCoins} coins)"
            )
        )
        Result.success(id)
    }

    suspend fun adminDeleteTask(task: TaskEntity, adminName: String): Result<Boolean> = withContext(Dispatchers.IO) {
        taskDao.deleteTask(task)
        adminLogDao.insertLog(
            AdminLogEntity(
                adminUsername = adminName,
                action = "DELETE_TASK",
                targetEntity = "TASK_${task.id}",
                details = "Deleted task ${task.title}"
            )
        )
        Result.success(true)
    }

    suspend fun adminUpdateSettings(newSettings: AppSettingsEntity, adminName: String): Result<Boolean> = withContext(Dispatchers.IO) {
        settingsDao.updateSettings(newSettings)
        adminLogDao.insertLog(
            AdminLogEntity(
                adminUsername = adminName,
                action = "UPDATE_SETTINGS",
                targetEntity = "APP_SETTINGS",
                details = "Updated conversion: 1000 coins = ₹${1000 / newSettings.coinRatePerRupee}, Min Withdraw: ₹${newSettings.minWithdrawalRupees}"
            )
        )
        Result.success(true)
    }
}
