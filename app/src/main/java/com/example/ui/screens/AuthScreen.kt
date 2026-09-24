package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AntiBotCaptchaDialog
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Gold500
import com.example.ui.theme.Red500
import com.example.ui.viewmodel.TaskEarnViewModel

@Composable
fun AuthScreen(
    viewModel: TaskEarnViewModel,
    onAdminClick: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var isForgotMode by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var identifier by remember { mutableStateOf("") } // Email or Mobile
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCaptcha by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
            .testTag("auth_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo / Emblem
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Emerald500),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "TaskEarn Pro",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "TaskEarn Pro",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Transparent Tasks & Daily Rewards",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = when {
                            isForgotMode -> "Reset Password"
                            isRegisterMode -> "Create Account"
                            else -> "Sign In"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; errorMessage = null },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_name_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = identifier,
                        onValueChange = { identifier = it; errorMessage = null },
                        label = { Text(if (isRegisterMode) "Mobile Number or Email" else "Email / Mobile") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_identifier_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text(if (isForgotMode) "New Password" else "Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )

                    if (isRegisterMode || isForgotMode) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; errorMessage = null },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_confirm_password_input")
                        )
                    }

                    if (isRegisterMode) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = referralCode,
                            onValueChange = { referralCode = it.uppercase() },
                            label = { Text("Referral Code (Optional)") },
                            leadingIcon = { Icon(Icons.Default.CardGiftcard, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_referral_input")
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Red500,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (identifier.isBlank()) {
                                errorMessage = "Please enter email or mobile number."
                                return@Button
                            }
                            if (password.length < 6) {
                                errorMessage = "Password must be at least 6 characters."
                                return@Button
                            }
                            if ((isRegisterMode || isForgotMode) && password != confirmPassword) {
                                errorMessage = "Passwords do not match."
                                return@Button
                            }

                            if (isRegisterMode) {
                                if (name.isBlank()) {
                                    errorMessage = "Please enter your name."
                                    return@Button
                                }
                                // Anti-bot challenge before registration as mandated
                                showCaptcha = true
                            } else if (isForgotMode) {
                                viewModel.resetPassword(identifier, password) { success ->
                                    if (success) {
                                        isForgotMode = false
                                    }
                                }
                            } else {
                                viewModel.login(identifier, password) { /* handle via state */ }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button")
                    ) {
                        Text(
                            text = when {
                                isForgotMode -> "Confirm Password Reset"
                                isRegisterMode -> "Verify & Register"
                                else -> "Sign In"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (!isForgotMode) {
                            TextButton(
                                onClick = {
                                    isRegisterMode = !isRegisterMode
                                    errorMessage = null
                                }
                            ) {
                                Text(
                                    text = if (isRegisterMode) "Already have an account? Sign In" else "New user? Create Account",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (!isRegisterMode) {
                            TextButton(
                                onClick = {
                                    isForgotMode = !isForgotMode
                                    errorMessage = null
                                }
                            ) {
                                Text(
                                    text = if (isForgotMode) "Back to Login" else "Forgot Password?",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Admin Access / Demo credentials notice
            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Admin Management Portal",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Login: subhodip@2007",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onAdminClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("admin_portal_button")
                    ) {
                        Text("Admin Login", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showCaptcha) {
            AntiBotCaptchaDialog(
                purpose = "Registration",
                onDismiss = { showCaptcha = false },
                onVerified = {
                    showCaptcha = false
                    viewModel.register(name, identifier, password, referralCode.ifBlank { null }) { success ->
                        if (success) {
                            isRegisterMode = false
                        }
                    }
                }
            )
        }
    }
}
