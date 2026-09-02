package com.example.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PrimaryButton
import com.example.ui.theme.LocalCompactMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthDialog(
    onDismiss: () -> Unit,
    onLoginSubmit: (email: String, pass: String) -> Unit,
    onRegisterSubmit: (name: String, email: String, pass: String, dob: String) -> Unit,
    onForgotPassword: (email: String) -> Unit,
    errorMessage: String? = null
) {
    val isCompact = LocalCompactMode.current
    var isRegisterMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("2002-06-15") } // YYYY-MM-DD
    var localError by remember { mutableStateOf<String?>(null) }
    var forgotPasswordSuccess by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = if (isCompact) 16.dp else 24.dp, vertical = if (isCompact) 12.dp else 20.dp)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isRegisterMode) "Create Account" else "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isCompact) 20.sp else 24.sp
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Text(
                text = if (isRegisterMode) "Join You & i for safe, authentic connections." else "Sign in with your registered account.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = if (isCompact) 12.sp else 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Mode Selector Tabs (Sign In vs Register)
            TabRow(
                selectedTabIndex = if (isRegisterMode) 1 else 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Tab(
                    selected = !isRegisterMode,
                    onClick = {
                        isRegisterMode = false
                        localError = null
                    },
                    text = { Text("Sign In", fontWeight = if (!isRegisterMode) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = isRegisterMode,
                    onClick = {
                        isRegisterMode = true
                        localError = null
                    },
                    text = { Text("Register", fontWeight = if (isRegisterMode) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            if (!errorMessage.isNullOrBlank() || localError != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = errorMessage ?: localError ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = if (isCompact) 12.sp else 14.sp
                        ),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (forgotPasswordSuccess) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "Password reset instructions sent to $email",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (isRegisterMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            if (isRegisterMode) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date of Birth (YYYY-MM-DD)") },
                    supportingText = {
                        Text("Teens (13-17) and Adults (18+) are strictly isolated for safety.")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            PrimaryButton(
                text = if (isRegisterMode) "Complete Registration & Enter" else "Sign In & Enter App",
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        localError = "Please fill in all required fields."
                        return@PrimaryButton
                    }
                    if (isRegisterMode) {
                        if (name.isBlank()) {
                            localError = "Display name is required."
                            return@PrimaryButton
                        }
                        onRegisterSubmit(name, email, password, dob)
                    } else {
                        onLoginSubmit(email, password)
                    }
                },
                modifier = Modifier.heightIn(min = if (isCompact) 46.dp else 52.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!isRegisterMode) {
                TextButton(
                    onClick = {
                        if (email.isBlank()) {
                            localError = "Enter your email address first to reset password."
                        } else {
                            onForgotPassword(email)
                            forgotPasswordSuccess = true
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Forgot password?",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

