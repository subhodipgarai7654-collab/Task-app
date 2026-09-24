package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.TaskEarnDatabase
import com.example.data.model.*
import com.example.data.repository.TaskEarnRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class UiMessage {
    data class Success(val message: String) : UiMessage()
    data class Error(val message: String) : UiMessage()
}

class TaskEarnViewModel(application: Application) : AndroidViewModel(application) {

    private val db = TaskEarnDatabase.getDatabase(application)
    val repository = TaskEarnRepository(db)

    // Current Session State
    private val _currentUserId = MutableStateFlow<Long?>(null)
    val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

    private val _currentAdmin = MutableStateFlow<AdminUserEntity?>(null)
    val currentAdmin: StateFlow<AdminUserEntity?> = _currentAdmin.asStateFlow()

    private val _uiMessage = MutableSharedFlow<UiMessage>()
    val uiMessage: SharedFlow<UiMessage> = _uiMessage.asSharedFlow()

    // Reactive current user
    val currentUser: StateFlow<UserEntity?> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getUserFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // App Settings
    val appSettings: StateFlow<AppSettingsEntity> = repository.appSettings
        .map { it ?: AppSettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettingsEntity())

    // Tasks
    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTasks: StateFlow<List<TaskEntity>> = repository.activeTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User Data
    val userCompletions: StateFlow<List<TaskCompletionEntity>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getCompletionsForUser(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTransactions: StateFlow<List<CoinTransactionEntity>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getTransactionsForUser(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userWithdrawals: StateFlow<List<WithdrawalEntity>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getWithdrawalsForUser(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentMethods: StateFlow<List<PaymentMethodEntity>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getPaymentMethodsForUser(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userReferrals: StateFlow<List<ReferralEntity>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getReferralsForUser(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userMembership: StateFlow<PremiumMembershipEntity?> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getMembershipForUser(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Admin Feeds
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawals: StateFlow<List<WithdrawalEntity>> = repository.allWithdrawals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminLogs: StateFlow<List<AdminLogEntity>> = repository.adminLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Dashboard Metrics
    val totalUsersCount = repository.totalUsersCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val activeUsersCount = repository.activeUsersCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val premiumUsersCount = repository.premiumUsersCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalTasksCount = repository.totalTasksCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val verifiedCompletionsCount = repository.verifiedCompletionsCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val pendingCompletionsCount = repository.pendingCompletionsCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalCoinsIssued = repository.totalCoinsIssued.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    val totalWithdrawalsCount = repository.totalWithdrawalsCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val pendingWithdrawalsCount = repository.pendingWithdrawalsCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val paidWithdrawalsCount = repository.paidWithdrawalsCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalPaidAmount = repository.totalPaidAmount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalPremiumRevenue = repository.totalPremiumRevenue.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    init {
        // Automatically check for demo user or initialize database
        viewModelScope.launch {
            val users = repository.allUsers.firstOrNull()
            if (users.isNullOrEmpty()) {
                TaskEarnDatabase.populateInitialData(db)
            }
            // Auto login default demo user for seamless test experience
            val first = repository.allUsers.firstOrNull()?.firstOrNull()
            if (first != null && _currentUserId.value == null) {
                _currentUserId.value = first.id
            }
        }
    }

    // --- Auth Actions ---
    fun login(identifier: String, pass: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.loginUser(identifier, pass)
            result.onSuccess { user ->
                _currentUserId.value = user.id
                _uiMessage.emit(UiMessage.Success("Welcome back, ${user.name}!"))
                onResult(true)
            }.onFailure { err ->
                _uiMessage.emit(UiMessage.Error(err.message ?: "Login failed."))
                onResult(false)
            }
        }
    }

    fun register(name: String, emailOrPhone: String, pass: String, refCode: String?, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.registerUser(name, emailOrPhone, pass, refCode)
            result.onSuccess { user ->
                _currentUserId.value = user.id
                _uiMessage.emit(UiMessage.Success("Welcome to TaskEarn Pro, ${user.name}! 100 Bonus Coins added."))
                onResult(true)
            }.onFailure { err ->
                _uiMessage.emit(UiMessage.Error(err.message ?: "Registration failed."))
                onResult(false)
            }
        }
    }

    fun resetPassword(identifier: String, newPass: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.resetPassword(identifier, newPass)
            result.onSuccess {
                _uiMessage.emit(UiMessage.Success("Password reset successfully. Please login."))
                onResult(true)
            }.onFailure { err ->
                _uiMessage.emit(UiMessage.Error(err.message ?: "Reset failed."))
                onResult(false)
            }
        }
    }

    fun logout() {
        _currentUserId.value = null
    }

    fun updateProfile(name: String, avatarId: Int) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            repository.updateUserProfile(uid, name, avatarId)
            _uiMessage.emit(UiMessage.Success("Profile updated successfully"))
        }
    }

    // --- Daily Checkin ---
    fun claimDailyCheckin() {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            val result = repository.claimDailyCheckin(uid)
            result.onSuccess { bonus ->
                _uiMessage.emit(UiMessage.Success("+$bonus Coins claimed for today's check-in!"))
            }.onFailure { err ->
                _uiMessage.emit(UiMessage.Error(err.message ?: "Check-in failed"))
            }
        }
    }

    // --- Task Verification ---
    fun completeTask(taskId: Long, answer: String, captchaPassed: Boolean, onDone: (Boolean) -> Unit) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            val result = repository.completeTask(uid, taskId, answer, captchaPassed)
            result.onSuccess { reward ->
                _uiMessage.emit(UiMessage.Success("Task verified! +$reward Coins credited to your wallet."))
                onDone(true)
            }.onFailure { err ->
                _uiMessage.emit(UiMessage.Error(err.message ?: "Verification failed."))
                onDone(false)
            }
        }
    }

    // --- Withdrawal ---
    fun submitWithdrawal(
        amountRupees: Double,
        method: String,
        identifier: String,
        holderName: String,
        captchaPassed: Boolean,
        onDone: (Boolean) -> Unit
    ) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            val result = repository.requestWithdrawal(uid, amountRupees, method, identifier, holderName, captchaPassed)
            result.onSuccess { w ->
                _uiMessage.emit(UiMessage.Success("Withdrawal request for ₹${w.amountInRupees} submitted. Status: PENDING review."))
                onDone(true)
            }.onFailure { err ->
                _uiMessage.emit(UiMessage.Error(err.message ?: "Withdrawal failed."))
                onDone(false)
            }
        }
    }

    // --- Payment Methods ---
    fun addPaymentMethod(
        type: String,
        identifier: String,
        holderName: String,
        ifsc: String,
        bankName: String,
        onDone: () -> Unit
    ) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            repository.addPaymentMethod(uid, type, identifier, holderName, ifsc, bankName)
            _uiMessage.emit(UiMessage.Success("Payment method saved securely."))
            onDone()
        }
    }

    // --- Premium Activation ---
    fun activatePremium(txRef: String, onDone: () -> Unit) {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            val result = repository.activatePremiumMembership(uid, txRef)
            result.onSuccess {
                _uiMessage.emit(UiMessage.Success("VIP Premium Membership activated! Enjoy exclusive perks & 1000 bonus coins."))
                onDone()
            }.onFailure { err ->
                _uiMessage.emit(UiMessage.Error(err.message ?: "Activation failed."))
            }
        }
    }

    // --- Admin Authentication & Controls ---
    fun loginAdmin(user: String, pass: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.verifyAdminLogin(user, pass)
            result.onSuccess { admin ->
                _currentAdmin.value = admin
                _uiMessage.emit(UiMessage.Success("Welcome Admin ${admin.username}!"))
                onResult(true)
            }.onFailure { err ->
                _uiMessage.emit(UiMessage.Error(err.message ?: "Admin verification failed."))
                onResult(false)
            }
        }
    }

    fun logoutAdmin() {
        _currentAdmin.value = null
    }

    fun adminToggleBlock(userId: Long, currentStatus: String) {
        val adminName = _currentAdmin.value?.username ?: "Admin"
        viewModelScope.launch {
            val result = repository.adminToggleUserBlock(userId, currentStatus, adminName)
            result.onSuccess { newStatus ->
                _uiMessage.emit(UiMessage.Success("User status changed to $newStatus"))
            }
        }
    }

    fun adminProcessWithdrawal(
        withdrawalId: Long,
        newStatus: String,
        utr: String,
        note: String,
        onDone: () -> Unit
    ) {
        val adminName = _currentAdmin.value?.username ?: "Admin"
        viewModelScope.launch {
            val result = repository.adminProcessWithdrawal(withdrawalId, newStatus, utr, note, adminName)
            result.onSuccess {
                _uiMessage.emit(UiMessage.Success("Withdrawal #$withdrawalId updated to $newStatus"))
                onDone()
            }.onFailure { err ->
                _uiMessage.emit(UiMessage.Error(err.message ?: "Action failed"))
            }
        }
    }

    fun adminSaveTask(task: TaskEntity, onDone: () -> Unit) {
        val adminName = _currentAdmin.value?.username ?: "Admin"
        viewModelScope.launch {
            repository.adminSaveTask(task, adminName)
            _uiMessage.emit(UiMessage.Success("Task saved successfully"))
            onDone()
        }
    }

    fun createAppInstallTask(task: TaskEntity, onDone: () -> Unit) {
        val author = _currentAdmin.value?.username ?: currentUser.value?.name ?: "CampaignManager"
        viewModelScope.launch {
            repository.adminSaveTask(task, author)
            _uiMessage.emit(UiMessage.Success("App Install task '${task.title}' published successfully!"))
            onDone()
        }
    }

    fun adminDeleteTask(task: TaskEntity) {
        val adminName = _currentAdmin.value?.username ?: "Admin"
        viewModelScope.launch {
            repository.adminDeleteTask(task, adminName)
            _uiMessage.emit(UiMessage.Success("Task deleted"))
        }
    }

    fun adminUpdateSettings(settings: AppSettingsEntity, onDone: () -> Unit) {
        val adminName = _currentAdmin.value?.username ?: "Admin"
        viewModelScope.launch {
            repository.adminUpdateSettings(settings, adminName)
            _uiMessage.emit(UiMessage.Success("App settings updated"))
            onDone()
        }
    }
}
