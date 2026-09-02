package com.example.ui.screens.welcome

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.example.R
import com.example.ui.components.SecondaryButton
import com.example.ui.theme.LocalCompactMode
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

@Composable
fun WelcomeScreen(
    onContinueWithGoogle: () -> Unit,
    onGoogleCredentialResult: ((idToken: String, email: String, displayName: String?) -> Unit)? = null,
    onContinueWithEmail: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onOpenAuthDialogWithError: ((String) -> Unit)? = null
) {
    val isCompact = LocalCompactMode.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val webClientId = "108520696426-0u2aqkeu69niur2vpirh7sm6d51lhcd3.apps.googleusercontent.com"

    var isGoogleLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(
                    horizontal = if (isCompact) 16.dp else 28.dp,
                    vertical = if (isCompact) 12.dp else 20.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = if (isCompact) 8.dp else 16.dp)
            ) {
                // Editorial Title
                Text(
                    text = "You & i",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isCompact) 32.sp else 42.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 8.dp))

                Text(
                    text = "\"Real people.\nReal connections.\"",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        lineHeight = if (isCompact) 22.sp else 28.sp,
                        fontSize = if (isCompact) 18.sp else 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 8.dp))

                Text(
                    text = "Find people who feel like your kind of people.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = if (isCompact) 13.sp else 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Visual 3D Connection Symbol Asset
            Box(
                modifier = Modifier
                    .padding(vertical = if (isCompact) 12.dp else 24.dp)
                    .size(if (isCompact) 160.dp else 240.dp)
                    .clip(RoundedCornerShape(if (isCompact) 24.dp else 36.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_welcome_hero_1788365226301),
                    contentDescription = "You & i Connection Graphic",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // Actions Block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Google Button
                Button(
                    onClick = {
                        if (isGoogleLoading) return@Button
                        isGoogleLoading = true
                        coroutineScope.launch {
                            try {
                                val credentialManager = CredentialManager.create(context)
                                val rawNonce = UUID.randomUUID().toString()
                                val bytes = rawNonce.toByteArray()
                                val md = MessageDigest.getInstance("SHA-256")
                                val digest = md.digest(bytes)
                                val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId(webClientId)
                                    .setAutoSelectEnabled(false)
                                    .setNonce(hashedNonce)
                                    .build()

                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()

                                val result = credentialManager.getCredential(request = request, context = context)
                                val credential = result.credential
                                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                    isGoogleLoading = false
                                    if (onGoogleCredentialResult != null) {
                                        onGoogleCredentialResult(
                                            googleIdTokenCredential.idToken,
                                            googleIdTokenCredential.id,
                                            googleIdTokenCredential.displayName
                                        )
                                    } else {
                                        onContinueWithGoogle()
                                    }
                                } else {
                                    isGoogleLoading = false
                                    onContinueWithEmail()
                                    onOpenAuthDialogWithError?.invoke("Please sign in or create an account with Email/Password.")
                                }
                            } catch (e: Exception) {
                                isGoogleLoading = false
                                onContinueWithEmail()
                                onOpenAuthDialogWithError?.invoke("Google Sign-In is unavailable on this device. Please sign in or register below.")
                            }
                        }
                    },
                    modifier = Modifier
                        .height(if (isCompact) 46.dp else 54.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(if (isCompact) 23.dp else 27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    enabled = !isGoogleLoading
                ) {
                    if (isGoogleLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Connecting Google...",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier.size(if (isCompact) 20.dp else 24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "G",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = if (isCompact) 12.sp else 14.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = if (isCompact) 14.sp else 16.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (isCompact) 8.dp else 12.dp))

                // Email Button
                SecondaryButton(
                    text = "Continue with Email",
                    onClick = onContinueWithEmail,
                    icon = Icons.Default.Email,
                    modifier = Modifier.heightIn(min = if (isCompact) 46.dp else 52.dp)
                )

                Spacer(modifier = Modifier.height(if (isCompact) 8.dp else 12.dp))

                // Guest Button
                TextButton(
                    onClick = onContinueAsGuest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Continue as Guest",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = if (isCompact) 13.sp else 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isCompact) 8.dp else 16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Private by design • Age-isolated safety architecture",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = if (isCompact) 10.sp else 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

