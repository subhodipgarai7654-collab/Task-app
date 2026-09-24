package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String, // e.g. "TEP-10928"
    val name: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val role: String = "USER", // "USER", "ADMIN"
    val coins: Long = 0,
    val lifetimeCoins: Long = 0,
    val todayCoins: Long = 0,
    val isPremium: Boolean = false,
    val status: String = "ACTIVE", // "ACTIVE", "BLOCKED", "SUSPENDED"
    val referralCode: String,
    val referredBy: String? = null,
    val avatarId: Int = 1,
    val lastCheckinDate: String = "",
    val checkinStreak: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val phoneNumber: String get() = phone
    val accountStatus: String get() = status
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String, // "WATCH_VIDEO", "YOUTUBE", "APP_DISCOVERY", "PLAY_STORE", "DAILY", "BONUS", "PREMIUM"
    val rewardCoins: Long,
    val estimatedTime: String, // "30 sec", "2 min"
    val targetUrl: String, // external YouTube or Play Store or Demo URL
    val requirements: String,
    val verificationQuestion: String = "Confirm you explored this task completely",
    val verificationAnswer: String = "",
    val isDaily: Boolean = false,
    val isPremiumOnly: Boolean = false,
    val isEnabled: Boolean = true,
    val dailyLimit: Int = 1,
    val expiryDate: Long = 0L // 0 means no expiry
)

@Entity(tableName = "task_completions")
data class TaskCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val taskTitle: String,
    val userId: Long,
    val status: String = "VERIFIED", // "PENDING", "VERIFIED", "REJECTED"
    val rewardCoins: Long,
    val proofText: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val verifiedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "coin_transactions")
data class CoinTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val amount: Long, // positive for credits, negative for debits
    val type: String, // "TASK_REWARD", "DAILY_CHECKIN", "REFERRAL_BONUS", "WITHDRAWAL_DEDUCT", "WITHDRAWAL_REFUND", "PREMIUM_BONUS"
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val amountInRupees: Double,
    val coinsDeducted: Long,
    val method: String, // "UPI", "BANK"
    val accountDetailsMasked: String, // e.g. "user***@okaxis" or "Acct ending in 8921"
    val accountHolderName: String,
    val status: String = "PENDING", // "PENDING", "APPROVED", "PAID", "REJECTED"
    val utrTransactionId: String = "",
    val adminNote: String = "",
    val requestedAt: Long = System.currentTimeMillis(),
    val processedAt: Long = 0L
)

@Entity(tableName = "payment_methods")
data class PaymentMethodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val type: String, // "UPI", "BANK"
    val identifier: String, // raw UPI ID or Bank Account No (secure local store)
    val maskedIdentifier: String,
    val holderName: String,
    val ifsc: String = "",
    val bankName: String = "",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "premium_memberships")
data class PremiumMembershipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val planName: String = "TaskEarn Pro VIP",
    val pricePaid: Double = 300.0,
    val status: String = "ACTIVE", // "ACTIVE", "EXPIRED", "PENDING"
    val startDate: Long = System.currentTimeMillis(),
    val expiryDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000L),
    val transactionRef: String = ""
)

@Entity(tableName = "referrals")
data class ReferralEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referrerId: Long,
    val referredUserId: Long,
    val referredUserName: String,
    val rewardCoins: Long = 500,
    val status: String = "REWARDED", // "PENDING", "REWARDED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_users")
data class AdminUserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val role: String = "SUPER_ADMIN",
    val permissions: String = "ALL",
    val lastLogin: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_logs")
data class AdminLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val adminUsername: String,
    val action: String, // "APPROVE_WITHDRAWAL", "REJECT_WITHDRAWAL", "PAY_WITHDRAWAL", "BLOCK_USER", "UPDATE_TASK", "SETTINGS_CHANGE"
    val targetEntity: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val coinRatePerRupee: Int = 100, // 100 coins = ₹1 (i.e. 1000 coins = ₹10)
    val minWithdrawalRupees: Double = 50.0, // Minimum ₹50
    val dailyTargetRupees: Double = 166.0, // Configurable target ₹166/day
    val referralBonusCoins: Long = 500,
    val premiumPriceRupees: Double = 300.0,
    val supportEmail: String = "support@taskearnpro.app",
    val supportTelegram: String = "https://t.me/taskearnpro_official",
    val companyUpiId: String = "taskearnpro@icici",
    val termsText: String = "TaskEarn Pro Terms of Service: All tasks require verified completion. Fraudulent submissions or automated scripts result in permanent suspension. Rewards and withdrawals are subject to verification.",
    val privacyText: String = "Privacy Policy: User payment details are kept encrypted and confidential. We do not sell user data to third parties."
)
