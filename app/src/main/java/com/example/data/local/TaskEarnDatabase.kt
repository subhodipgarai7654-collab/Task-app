package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        TaskEntity::class,
        TaskCompletionEntity::class,
        CoinTransactionEntity::class,
        WithdrawalEntity::class,
        PaymentMethodEntity::class,
        PremiumMembershipEntity::class,
        ReferralEntity::class,
        AdminUserEntity::class,
        AdminLogEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TaskEarnDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun taskCompletionDao(): TaskCompletionDao
    abstract fun coinTransactionDao(): CoinTransactionDao
    abstract fun withdrawalDao(): WithdrawalDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun premiumMembershipDao(): PremiumMembershipDao
    abstract fun referralDao(): ReferralDao
    abstract fun adminUserDao(): AdminUserDao
    abstract fun adminLogDao(): AdminLogDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: TaskEarnDatabase? = null

        fun getDatabase(context: Context): TaskEarnDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskEarnDatabase::class.java,
                    "taskearn_pro_database.db"
                ).addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: TaskEarnDatabase) {
            // 1. App Settings
            db.appSettingsDao().updateSettings(
                AppSettingsEntity(
                    id = 1,
                    coinRatePerRupee = 100, // 1000 coins = ₹10
                    minWithdrawalRupees = 50.0,
                    dailyTargetRupees = 166.0,
                    referralBonusCoins = 500,
                    premiumPriceRupees = 300.0,
                    supportEmail = "support@taskearnpro.app",
                    companyUpiId = "taskearnpro@icici"
                )
            )

            // 2. Admin User (Requested: subhodip@2007 / subhodip@7)
            db.adminUserDao().insertAdmin(
                AdminUserEntity(
                    username = "subhodip@2007",
                    passwordHash = "subhodip@7",
                    role = "SUPER_ADMIN",
                    permissions = "ALL",
                    lastLogin = System.currentTimeMillis()
                )
            )

            // 3. Demo User for immediate preview
            val demoUserId = db.userDao().insertUser(
                UserEntity(
                    userId = "TEP-782910",
                    name = "Rahul Sharma",
                    email = "demo@taskearnpro.app",
                    phone = "9876543210",
                    passwordHash = "demo1234",
                    role = "USER",
                    coins = 2450,
                    lifetimeCoins = 5600,
                    todayCoins = 650,
                    isPremium = false,
                    status = "ACTIVE",
                    referralCode = "TEP99X2",
                    avatarId = 1
                )
            )

            // Add demo payment method
            db.paymentMethodDao().insertPaymentMethod(
                PaymentMethodEntity(
                    userId = demoUserId,
                    type = "UPI",
                    identifier = "rahul@oksbi",
                    maskedIdentifier = "rahul***@oksbi",
                    holderName = "Rahul Sharma",
                    isDefault = true
                )
            )

            // Add some initial coin ledger history
            db.coinTransactionDao().insertTransaction(
                CoinTransactionEntity(
                    userId = demoUserId,
                    amount = 1000,
                    type = "TASK_REWARD",
                    description = "Completed Starter Video Guide"
                )
            )
            db.coinTransactionDao().insertTransaction(
                CoinTransactionEntity(
                    userId = demoUserId,
                    amount = 500,
                    type = "DAILY_CHECKIN",
                    description = "Day 1 Welcome Check-in Bonus"
                )
            )
            db.coinTransactionDao().insertTransaction(
                CoinTransactionEntity(
                    userId = demoUserId,
                    amount = 950,
                    type = "TASK_REWARD",
                    description = "Explored Financial Literacy App"
                )
            )

            // 4. Initial Tasks across all requested categories
            val initialTasks = listOf(
                TaskEntity(
                    title = "Quick Platform Guide",
                    description = "Watch the 45-second video introducing TaskEarn Pro's transparent coin & reward rules.",
                    category = "WATCH_VIDEO",
                    rewardCoins = 350,
                    estimatedTime = "45 sec",
                    targetUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    requirements = "Open the video link, watch the explanation, and complete the security verification challenge.",
                    verificationQuestion = "What is the conversion rate highlighted (Coins for ₹10)?",
                    verificationAnswer = "1000",
                    isDaily = true
                ),
                TaskEntity(
                    title = "YouTube: Smart Budgeting 101",
                    description = "Watch financial education video explaining how student budgets work.",
                    category = "YOUTUBE",
                    rewardCoins = 400,
                    estimatedTime = "1 min",
                    targetUrl = "https://www.youtube.com/watch?v=sVKQn2L406c",
                    requirements = "Open YouTube video. Note: third-party viewing is voluntary. Solve the anti-bot CAPTCHA to confirm participation.",
                    verificationQuestion = "Did you open the official YouTube video link?",
                    verificationAnswer = "Yes",
                    isDaily = true
                ),
                TaskEntity(
                    title = "Discover: Digilocker India",
                    description = "Explore government digital wallet capabilities on the official store.",
                    category = "APP_DISCOVERY",
                    rewardCoins = 600,
                    estimatedTime = "2 min",
                    targetUrl = "https://play.google.com/store/apps/details?id=com.digilocker.android",
                    requirements = "Open Play Store discovery page, inspect key features, and answer the verification question.",
                    verificationQuestion = "What government initiative powers Digilocker?",
                    verificationAnswer = "Digital India",
                    isDaily = false
                ),
                TaskEntity(
                    title = "Install & Explore: PhonePe UPI",
                    description = "Install the verified PhonePe application from Google Play Store, explore features for 60 seconds, and answer the security question.",
                    category = "APP_INSTALL",
                    rewardCoins = 500,
                    estimatedTime = "2 min",
                    targetUrl = "https://play.google.com/store/apps/details?id=com.phonepe.app",
                    requirements = "Install & Open (60s) • Verified Android installation on device.",
                    verificationQuestion = "What is the primary theme color of PhonePe?",
                    verificationAnswer = "Purple",
                    isDaily = false
                ),
                TaskEntity(
                    title = "Play Store: BHIM UPI",
                    description = "Discover the NPCI official payments app on the Google Play Store.",
                    category = "PLAY_STORE",
                    rewardCoins = 750,
                    estimatedTime = "2 min",
                    targetUrl = "https://play.google.com/store/apps/details?id=in.org.npci.upiapp",
                    requirements = "Tap 'Open in Play Store'. We do not track installations. Review the app description and answer the question.",
                    verificationQuestion = "Who developed the BHIM app?",
                    verificationAnswer = "NPCI",
                    isDaily = false
                ),
                TaskEntity(
                    title = "Daily Focus Quiz",
                    description = "Answer 3 general knowledge questions to earn daily engagement coins.",
                    category = "DAILY",
                    rewardCoins = 250,
                    estimatedTime = "30 sec",
                    targetUrl = "https://taskearnpro.app/quiz",
                    requirements = "Answer the daily community question and claim instant daily coins.",
                    verificationQuestion = "How many coins are in ₹1 at standard conversion?",
                    verificationAnswer = "100",
                    isDaily = true
                ),
                TaskEntity(
                    title = "Bonus: Profile Security Review",
                    description = "Confirm your UPI or bank payment details to earn bonus reward coins.",
                    category = "BONUS",
                    rewardCoins = 500,
                    estimatedTime = "1 min",
                    targetUrl = "https://taskearnpro.app/security",
                    requirements = "Ensure your profile and payment credentials are saved for smooth withdrawals.",
                    verificationQuestion = "Type CONFIRM to verify your security setup",
                    verificationAnswer = "CONFIRM",
                    isDaily = false
                ),
                TaskEntity(
                    title = "VIP Exclusive: Fintech Market Research",
                    description = "Detailed feedback review reserved strictly for Premium VIP members.",
                    category = "PREMIUM",
                    rewardCoins = 2500,
                    estimatedTime = "4 min",
                    targetUrl = "https://taskearnpro.app/premium-survey",
                    requirements = "Exclusive to active Premium Members. Submit research review for expedited reward.",
                    verificationQuestion = "Rate your VIP experience (EXCELLENT)",
                    verificationAnswer = "EXCELLENT",
                    isDaily = false,
                    isPremiumOnly = true
                )
            )

            db.taskDao().insertTasks(initialTasks)

            // Initial demo withdrawal requests for preview (Pending, Paid, Rejected)
            db.withdrawalDao().insertWithdrawal(
                WithdrawalEntity(
                    userId = demoUserId,
                    amountInRupees = 50.0,
                    coinsDeducted = 5000,
                    method = "UPI",
                    accountDetailsMasked = "rahul***@oksbi",
                    accountHolderName = "Rahul Sharma",
                    status = "PENDING",
                    requestedAt = System.currentTimeMillis() - 3600000L
                )
            )
            db.withdrawalDao().insertWithdrawal(
                WithdrawalEntity(
                    userId = demoUserId,
                    amountInRupees = 100.0,
                    coinsDeducted = 10000,
                    method = "UPI",
                    accountDetailsMasked = "rahul***@oksbi",
                    accountHolderName = "Rahul Sharma",
                    status = "PAID",
                    requestedAt = System.currentTimeMillis() - 86400000L * 2,
                    processedAt = System.currentTimeMillis() - 86400000L * 2 + 1800000L,
                    utrTransactionId = "UPI408928192301",
                    adminNote = "IMPS direct settlement completed successfully."
                )
            )
            db.withdrawalDao().insertWithdrawal(
                WithdrawalEntity(
                    userId = demoUserId,
                    amountInRupees = 75.0,
                    coinsDeducted = 7500,
                    method = "BANK",
                    accountDetailsMasked = "A/C ending in 4920 (HDFC)",
                    accountHolderName = "Rahul Sharma",
                    status = "REJECTED",
                    requestedAt = System.currentTimeMillis() - 86400000L * 5,
                    processedAt = System.currentTimeMillis() - 86400000L * 5 + 3600000L,
                    adminNote = "Invalid IFSC code entered. Coins refunded back to wallet balance."
                )
            )

            // Initial log
            db.adminLogDao().insertLog(
                AdminLogEntity(
                    adminUsername = "System",
                    action = "INITIALIZE",
                    targetEntity = "DATABASE",
                    details = "TaskEarn Pro initialized with standard seed tasks, admin user, and demo data."
                )
            )
        }
    }
}
