package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Red500
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import kotlin.random.Random

data class CaptchaChallenge(
    val num1: Int,
    val num2: Int,
    val isAddition: Boolean,
    val visualCode: String,
    val answer: Int
) {
    companion object {
        fun generate(): CaptchaChallenge {
            val isAdd = Random.nextBoolean()
            val n1 = Random.nextInt(12, 59)
            val n2 = Random.nextInt(5, 29)
            val ans = if (isAdd) n1 + n2 else n1 - n2
            val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
            val code = (1..4).map { chars.random() }.joinToString("")
            return CaptchaChallenge(n1, n2, isAdd, code, ans)
        }
    }
}

@Composable
fun AntiBotCaptchaDialog(
    purpose: String, // e.g., "Registration", "Task Completion", "Withdrawal Request"
    onDismiss: () -> Unit,
    onVerified: () -> Unit
) {
    var challenge by remember { mutableStateOf(CaptchaChallenge.generate()) }
    var userInputMath by remember { mutableStateOf("") }
    var userInputCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isVerifying by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("captcha_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Security Verification",
                    tint = Emerald500,
                    modifier = Modifier.size(44.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Anti-Bot Verification",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Confirm human action before $purpose",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Math Challenge Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Solve:  ${challenge.num1} ${if (challenge.isAddition) "+" else "-"} ${challenge.num2} = ?",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = {
                                challenge = CaptchaChallenge.generate()
                                userInputMath = ""
                                userInputCode = ""
                                errorMessage = null
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Challenge",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Visual Token Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate900)
                        .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Security Token:  ${challenge.visualCode}",
                        letterSpacing = 5.sp,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color(0xFFFBBF24),
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input field for Math
                OutlinedTextField(
                    value = userInputMath,
                    onValueChange = {
                        userInputMath = it
                        errorMessage = null
                    },
                    label = { Text("Math Answer") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("captcha_math_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Input field for Token
                OutlinedTextField(
                    value = userInputCode,
                    onValueChange = {
                        userInputCode = it.uppercase()
                        errorMessage = null
                    },
                    label = { Text("Enter 4-letter Token") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("captcha_token_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Red500,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("captcha_cancel_button")
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val mathVal = userInputMath.trim().toIntOrNull()
                            if (mathVal != challenge.answer) {
                                errorMessage = "Incorrect math solution. Try again."
                                challenge = CaptchaChallenge.generate()
                                userInputMath = ""
                                userInputCode = ""
                                return@Button
                            }
                            if (userInputCode.trim() != challenge.visualCode) {
                                errorMessage = "Token code does not match."
                                return@Button
                            }
                            isVerifying = true
                            onVerified()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("captcha_verify_button")
                    ) {
                        Text("Verify", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
