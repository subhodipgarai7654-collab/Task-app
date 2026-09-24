package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdOnce(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE email = :identifier OR phone = :identifier LIMIT 1")
    suspend fun getUserByEmailOrPhone(identifier: String): UserEntity?

    @Query("SELECT * FROM users WHERE referralCode = :code LIMIT 1")
    suspend fun getUserByReferralCode(code: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET coins = coins + :amount, lifetimeCoins = lifetimeCoins + CASE WHEN :amount > 0 THEN :amount ELSE 0 END, todayCoins = todayCoins + CASE WHEN :amount > 0 THEN :amount ELSE 0 END WHERE id = :userId")
    suspend fun adjustUserCoins(userId: Long, amount: Long)

    @Query("UPDATE users SET status = :status WHERE id = :userId")
    suspend fun updateUserStatus(userId: Long, status: String)

    @Query("UPDATE users SET isPremium = :isPremium WHERE id = :userId")
    suspend fun updatePremiumStatus(userId: Long, isPremium: Boolean)

    @Query("SELECT COUNT(*) FROM users")
    fun getUserCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE status = 'ACTIVE'")
    fun getActiveUserCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE isPremium = 1")
    fun getPremiumUserCount(): Flow<Int>
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isEnabled = 1 ORDER BY id ASC")
    fun getActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE category = :category AND isEnabled = 1")
    fun getTasksByCategory(category: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT COUNT(*) FROM tasks")
    fun getTaskCount(): Flow<Int>
}

@Dao
interface TaskCompletionDao {
    @Query("SELECT * FROM task_completions WHERE userId = :userId ORDER BY submittedAt DESC")
    fun getCompletionsForUser(userId: Long): Flow<List<TaskCompletionEntity>>

    @Query("SELECT * FROM task_completions WHERE taskId = :taskId AND userId = :userId LIMIT 1")
    suspend fun getCompletion(taskId: Long, userId: Long): TaskCompletionEntity?

    @Query("SELECT * FROM task_completions ORDER BY submittedAt DESC")
    fun getAllCompletions(): Flow<List<TaskCompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: TaskCompletionEntity): Long

    @Update
    suspend fun updateCompletion(completion: TaskCompletionEntity)

    @Query("SELECT COUNT(*) FROM task_completions WHERE userId = :userId AND status = 'VERIFIED'")
    fun getVerifiedCountForUser(userId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM task_completions WHERE userId = :userId AND status = 'PENDING'")
    fun getPendingCountForUser(userId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM task_completions WHERE status = 'VERIFIED'")
    fun getTotalVerifiedCompletions(): Flow<Int>

    @Query("SELECT COUNT(*) FROM task_completions WHERE status = 'PENDING'")
    fun getTotalPendingCompletions(): Flow<Int>
}

@Dao
interface CoinTransactionDao {
    @Query("SELECT * FROM coin_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUser(userId: Long): Flow<List<CoinTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CoinTransactionEntity): Long

    @Query("SELECT SUM(amount) FROM coin_transactions WHERE amount > 0")
    fun getTotalCoinsIssued(): Flow<Long?>
}

@Dao
interface WithdrawalDao {
    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY requestedAt DESC")
    fun getWithdrawalsForUser(userId: Long): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals ORDER BY requestedAt DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE id = :id LIMIT 1")
    suspend fun getWithdrawalById(id: Long): WithdrawalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity): Long

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity)

    @Query("SELECT COUNT(*) FROM withdrawals")
    fun getTotalWithdrawalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM withdrawals WHERE status = 'PENDING'")
    fun getPendingWithdrawalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM withdrawals WHERE status = 'PAID'")
    fun getPaidWithdrawalCount(): Flow<Int>

    @Query("SELECT SUM(amountInRupees) FROM withdrawals WHERE status = 'PAID'")
    fun getTotalPaidAmount(): Flow<Double?>
}

@Dao
interface PaymentMethodDao {
    @Query("SELECT * FROM payment_methods WHERE userId = :userId ORDER BY isDefault DESC, createdAt DESC")
    fun getMethodsForUser(userId: Long): Flow<List<PaymentMethodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(method: PaymentMethodEntity): Long

    @Delete
    suspend fun deletePaymentMethod(method: PaymentMethodEntity)

    @Query("UPDATE payment_methods SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultsForUser(userId: Long)

    @Query("UPDATE payment_methods SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultMethod(id: Long)
}

@Dao
interface PremiumMembershipDao {
    @Query("SELECT * FROM premium_memberships WHERE userId = :userId ORDER BY startDate DESC LIMIT 1")
    fun getMembershipForUser(userId: Long): Flow<PremiumMembershipEntity?>

    @Query("SELECT * FROM premium_memberships ORDER BY startDate DESC")
    fun getAllMemberships(): Flow<List<PremiumMembershipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembership(membership: PremiumMembershipEntity): Long

    @Query("SELECT SUM(pricePaid) FROM premium_memberships WHERE status = 'ACTIVE'")
    fun getTotalPremiumRevenue(): Flow<Double?>
}

@Dao
interface ReferralDao {
    @Query("SELECT * FROM referrals WHERE referrerId = :userId ORDER BY createdAt DESC")
    fun getReferralsForUser(userId: Long): Flow<List<ReferralEntity>>

    @Query("SELECT COUNT(*) FROM referrals WHERE referrerId = :userId")
    fun getReferralCountForUser(userId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferral(referral: ReferralEntity): Long
}

@Dao
interface AdminUserDao {
    @Query("SELECT * FROM admin_users WHERE username = :username LIMIT 1")
    suspend fun getAdminByUsername(username: String): AdminUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmin(admin: AdminUserEntity): Long
}

@Dao
interface AdminLogDao {
    @Query("SELECT * FROM admin_logs ORDER BY timestamp DESC LIMIT 150")
    fun getRecentLogs(): Flow<List<AdminLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AdminLogEntity): Long
}

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsOnce(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSettings(settings: AppSettingsEntity)
}
